package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanEntry
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanRepository
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanSettings
import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class WeeklyMealPlanViewModelTest {
    private val monday = LocalDate(2026, 11, 9)

    @Test
    fun loadCurrentWeek_restoresEverySavedField() = runTest {
        val saved = WeeklyMealPlanSettings(
            weekStart = monday,
            goalId = DietGoal.QUICK.name,
            preferenceIds = setOf(FamilyPreferenceTag.SEAFOOD.name),
            swappedOffsets = mapOf(2 to 1),
            uncheckedIds = setOf("egg"),
            includeLunchMealPrep = true,
        )
        val repository = FakeWeeklyMealPlanRepository(saved)
        val viewModel = createViewModel(repository)

        runCurrent()

        assertEquals(
            WeeklyMealPlanState(
                isLoaded = true,
                weekStart = monday,
                goal = DietGoal.QUICK,
                preferences = setOf(FamilyPreferenceTag.SEAFOOD),
                swappedOffsets = mapOf(2 to 1),
                uncheckedIds = setOf("egg"),
                includeLunchMealPrep = true,
            ),
            viewModel.state.value,
        )
    }

    @Test
    fun loadPriorWeek_persistsRolloverWithStablePreferences() = runTest {
        val repository = FakeWeeklyMealPlanRepository(
            WeeklyMealPlanSettings(
                weekStart = LocalDate(2026, 11, 2),
                goalId = DietGoal.LIGHT.name,
                preferenceIds = setOf(FamilyPreferenceTag.MEAT.name),
                swappedOffsets = mapOf(1 to 2),
                uncheckedIds = setOf("rice"),
                includeLunchMealPrep = true,
            ),
        )
        val viewModel = createViewModel(repository)

        runCurrent()

        assertEquals(monday, repository.saved.value?.weekStart)
        assertEquals(DietGoal.LIGHT.name, repository.saved.value?.goalId)
        assertEquals(setOf(FamilyPreferenceTag.MEAT.name), repository.saved.value?.preferenceIds)
        assertEquals(emptyMap(), repository.saved.value?.swappedOffsets)
        assertEquals(emptySet(), repository.saved.value?.uncheckedIds)
        assertEquals(true, repository.saved.value?.includeLunchMealPrep)
        assertEquals(monday, viewModel.state.value.weekStart)
    }

    @Test
    fun actions_updateStateAndPersistSettings() = runTest {
        val repository = FakeWeeklyMealPlanRepository()
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.dispatch(WeeklyMealPlanAction.SetGoal(DietGoal.QUICK))
        viewModel.dispatch(WeeklyMealPlanAction.TogglePreference(FamilyPreferenceTag.SEAFOOD))
        viewModel.dispatch(WeeklyMealPlanAction.SwapDay(2))
        viewModel.dispatch(WeeklyMealPlanAction.ToggleIngredient("egg", checked = false))
        viewModel.dispatch(WeeklyMealPlanAction.SetLunchMealPrep(true))
        runCurrent()

        assertEquals(
            WeeklyMealPlanSettings(
                weekStart = monday,
                goalId = DietGoal.QUICK.name,
                preferenceIds = setOf(FamilyPreferenceTag.SEAFOOD.name),
                swappedOffsets = mapOf(2 to 1),
                uncheckedIds = setOf("egg"),
                includeLunchMealPrep = true,
            ),
            repository.saved.value,
        )
    }

    @Test
    fun saveFailure_emitsEffect() = runTest {
        val repository = FakeWeeklyMealPlanRepository().apply { failSaves = true }
        val viewModel = createViewModel(repository)
        val effects = mutableListOf<WeeklyMealPlanEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect(effects::add) }
        runCurrent()

        viewModel.dispatch(WeeklyMealPlanAction.SetGoal(DietGoal.QUICK))
        runCurrent()

        assertEquals(listOf<WeeklyMealPlanEffect>(WeeklyMealPlanEffect.SettingsSaveFailed), effects)
    }

    @Test
    fun actionBeforeInitialSettingsEmission_isAppliedAfterHydration() = runTest {
        val repository = DelayedWeeklyMealPlanRepository()
        val viewModel = createViewModel(repository)

        viewModel.dispatch(WeeklyMealPlanAction.TogglePreference(FamilyPreferenceTag.SEAFOOD))
        runCurrent()
        assertEquals(false, viewModel.state.value.isLoaded)

        repository.emitInitial(
            WeeklyMealPlanSettings(
                weekStart = monday,
                goalId = DietGoal.LIGHT.name,
                preferenceIds = emptySet(),
                swappedOffsets = emptyMap(),
                uncheckedIds = emptySet(),
                includeLunchMealPrep = false,
            ),
        )
        runCurrent()

        assertEquals(setOf(FamilyPreferenceTag.SEAFOOD.name), repository.saved.value?.preferenceIds)
        assertEquals(DietGoal.LIGHT, viewModel.state.value.goal)
    }

    @Test
    fun settingsReadFailure_finishesLoadingAndEmitsEffect() = runTest {
        val repository = object : WeeklyMealPlanRepository {
            override fun observeAll(): Flow<List<WeeklyMealPlanEntry>> = emptyFlow()
            override suspend fun save(entry: WeeklyMealPlanEntry) = Unit
            override fun observeSettings(): Flow<WeeklyMealPlanSettings?> = kotlinx.coroutines.flow.flow {
                error("read failed")
            }
            override suspend fun saveSettings(settings: WeeklyMealPlanSettings) = Unit
        }
        val viewModel = createViewModel(repository)
        val effects = mutableListOf<WeeklyMealPlanEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect(effects::add) }

        runCurrent()

        assertEquals(true, viewModel.state.value.isLoaded)
        assertEquals(listOf<WeeklyMealPlanEffect>(WeeklyMealPlanEffect.SettingsLoadFailed), effects)
    }

    @Test
    fun reloadSettings_reflectsBackupRestoreWhileViewModelIsAlive() = runTest {
        val repository = FakeWeeklyMealPlanRepository()
        val viewModel = createViewModel(repository)
        runCurrent()
        repository.saved.value = WeeklyMealPlanSettings(
            weekStart = monday,
            goalId = DietGoal.LIGHT.name,
            preferenceIds = setOf(FamilyPreferenceTag.VEGGIE_TOFU.name),
            swappedOffsets = emptyMap(),
            uncheckedIds = emptySet(),
            includeLunchMealPrep = true,
        )

        viewModel.reloadSettings()
        runCurrent()

        assertEquals(DietGoal.LIGHT, viewModel.state.value.goal)
        assertEquals(setOf(FamilyPreferenceTag.VEGGIE_TOFU), viewModel.state.value.preferences)
        assertEquals(true, viewModel.state.value.includeLunchMealPrep)
    }

    private fun TestScope.createViewModel(repository: WeeklyMealPlanRepository) = WeeklyMealPlanViewModel(
        repository = repository,
        clock = MutableClock(Instant.parse("2026-11-11T12:00:00Z")),
        timeZone = TimeZone.UTC,
        scope = backgroundScope,
    )

    private class MutableClock(var instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private class FakeWeeklyMealPlanRepository(
        initial: WeeklyMealPlanSettings? = null,
    ) : WeeklyMealPlanRepository {
        val saved = MutableStateFlow(initial)
        var failSaves = false

        override fun observeAll(): Flow<List<WeeklyMealPlanEntry>> = emptyFlow()

        override suspend fun save(entry: WeeklyMealPlanEntry) = Unit

        override fun observeSettings(): Flow<WeeklyMealPlanSettings?> = saved

        override suspend fun saveSettings(settings: WeeklyMealPlanSettings) {
            if (failSaves) error("save failed")
            saved.value = settings
        }
    }

    private class DelayedWeeklyMealPlanRepository : WeeklyMealPlanRepository {
        private val settings = MutableSharedFlow<WeeklyMealPlanSettings?>(extraBufferCapacity = 8)
        val saved = MutableStateFlow<WeeklyMealPlanSettings?>(null)

        suspend fun emitInitial(value: WeeklyMealPlanSettings) {
            saved.value = value
            settings.emit(value)
        }

        override fun observeAll(): Flow<List<WeeklyMealPlanEntry>> = emptyFlow()
        override suspend fun save(entry: WeeklyMealPlanEntry) = Unit
        override fun observeSettings(): Flow<WeeklyMealPlanSettings?> = settings
        override suspend fun saveSettings(settings: WeeklyMealPlanSettings) {
            saved.value = settings
            this.settings.emit(settings)
        }
    }
}
