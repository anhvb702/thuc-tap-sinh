package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.ExtraShoppingItem
import com.loitu.yourrefri.feature.recipe.data.ExtraShoppingRepository
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.MealSlotKey
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
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
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
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

    @Test fun mealPresetSetsExpectedSlotsAndIndividualToggleRestoresSlot() = runTest {
        val repository = FakeWeeklyMealPlanRepository()
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.dispatch(WeeklyMealPlanAction.ApplyMealPreset(WeeklyMealPreset.ONE_MEAL))
        runCurrent()
        assertEquals(
            (0..6).map { day -> MealSlotKey(monday.plus(DatePeriod(days = day)), MealSlot.LUNCH) }.toSet(),
            repository.disabledMeals.value.toSet(),
        )

        val lunch = MealSlotKey(monday, MealSlot.LUNCH)
        viewModel.dispatch(WeeklyMealPlanAction.ToggleMealSlot(0, MealSlot.LUNCH))
        runCurrent()
        assertEquals(false, lunch in repository.disabledMeals.value)
        viewModel.dispatch(WeeklyMealPlanAction.ToggleMealSlot(0, MealSlot.LUNCH))
        runCurrent()
        assertEquals(true, lunch in repository.disabledMeals.value)

        viewModel.dispatch(WeeklyMealPlanAction.ApplyMealPreset(WeeklyMealPreset.TWO_MEALS))
        runCurrent()
        assertEquals(emptyList(), repository.disabledMeals.value)

        viewModel.dispatch(WeeklyMealPlanAction.ApplyMealPreset(WeeklyMealPreset.WORKWEEK))
        runCurrent()
        assertEquals(true, MealSlot.LUNCH in repository.disabledMeals.value.filter { it.day == monday }.map { it.slot })
        assertEquals(false, MealSlot.DINNER in repository.disabledMeals.value.filter { it.day == monday }.map { it.slot })
        assertEquals(false, MealSlot.LUNCH in repository.disabledMeals.value.filter { it.day == monday.plus(DatePeriod(days = 5)) }.map { it.slot })
    }

    @Test
    fun lunchPrepToggleReplacesPersistedWeekAndRemovesLunchEntries() = runTest {
        val repository = FakeWeeklyMealPlanRepository()
        val viewModel = createViewModel(repository)
        runCurrent()
        val recipes = listOf(
            Recipe("dinner", "Bữa tối", listOf("Nấu"), listOf(Slot(listOf("pork")))),
            Recipe("lunch", "Bữa trưa", listOf("Nấu"), listOf(Slot(listOf("rice")))),
        )
        val pantry = PantryGridState(
            today = monday,
            ingredients = listOf(Ingredient("pork", "Thịt", "thit", 3), Ingredient("rice", "Gạo", "khac", 30)),
            pantry = emptyMap(),
        )
        viewModel.syncSchedule(recipes, pantry)
        runCurrent()
        assertEquals(7, repository.entries.value.count { it.meal == MealSlot.LUNCH })
        viewModel.dispatch(WeeklyMealPlanAction.ToggleLunchMealPrep(true))
        runCurrent()
        assertEquals(0, repository.entries.value.count { it.meal == MealSlot.LUNCH })
        assertEquals(7, repository.entries.value.count { it.meal == MealSlot.DINNER })
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

    @Test
    fun syncSchedule_persistsPlanAndReloadWithSameWeekKeepsSavedPlanEvenIfPantryChanges() = runTest {
        val catalog = listOf(
            Ingredient("pork", "Thịt heo", "thit", 7),
            Ingredient("rau-muong", "Rau muống", "rau", 3),
            Ingredient("rice", "Gạo", "khac", 30),
        )
        val recipes = listOf(
            Recipe("pork", "Thịt kho", listOf("Nấu"), listOf(Slot(listOf("pork")))),
            Recipe("green", "Xào rau muống", listOf("Xào"), listOf(Slot(listOf("rau-muong")))),
            Recipe("rice", "Cơm chiên", listOf("Chiên"), listOf(Slot(listOf("rice")))),
        )
        val initialPantry = PantryGridState(
            today = monday,
            ingredients = catalog,
            pantry = mapOf("rau-muong" to PantryItem("rau-muong", true, monday)),
        )
        val changedPantry = PantryGridState(
            today = monday,
            ingredients = catalog,
            pantry = mapOf("pork" to PantryItem("pork", true, LocalDate(2026, 11, 3))),
        )
        val repository = FakeWeeklyMealPlanRepository()
        val clock = MutableClock(Instant.parse("2026-11-11T12:00:00Z"))
        val firstVm = WeeklyMealPlanViewModel(repository, clock, TimeZone.UTC, backgroundScope)
        runCurrent()

        firstVm.syncSchedule(recipes, initialPantry)
        runCurrent()

        val initialSaved = repository.entries.value
        assertEquals(14, initialSaved.size)
        assertEquals("green", initialSaved.first { it.day == monday && it.meal == MealSlot.DINNER }.recipeId)

        // Tải lại cùng tuần với tủ lạnh thay đổi: phải giữ nguyên thực đơn đã lưu.
        val reloadedVm = WeeklyMealPlanViewModel(repository, clock, TimeZone.UTC, backgroundScope)
        runCurrent()
        reloadedVm.syncSchedule(recipes, changedPantry)
        runCurrent()

        assertEquals(initialSaved, repository.entries.value)
        assertEquals(initialSaved, reloadedVm.state.value.savedEntries)
    }

    @Test
    fun swapDayAndNewWeek_recomputeAndPersistUpdatedPlan() = runTest {
        val catalog = listOf(
            Ingredient("pork", "Thịt heo", "thit", 7),
            Ingredient("rau-muong", "Rau muống", "rau", 3),
            Ingredient("rice", "Gạo", "khac", 30),
        )
        val recipes = listOf(
            Recipe("pork", "Thịt kho", listOf("Nấu"), listOf(Slot(listOf("pork")))),
            Recipe("green", "Xào rau muống", listOf("Xào"), listOf(Slot(listOf("rau-muong")))),
            Recipe("rice", "Cơm chiên", listOf("Chiên"), listOf(Slot(listOf("rice")))),
        )
        val initialPantry = PantryGridState(
            today = monday,
            ingredients = catalog,
            pantry = mapOf("rau-muong" to PantryItem("rau-muong", true, monday)),
        )
        val changedPantry = PantryGridState(
            today = monday,
            ingredients = catalog,
            pantry = mapOf("pork" to PantryItem("pork", true, LocalDate(2026, 11, 3))),
        )
        val repository = FakeWeeklyMealPlanRepository()
        val clock = MutableClock(Instant.parse("2026-11-11T12:00:00Z"))
        val viewModel = WeeklyMealPlanViewModel(repository, clock, TimeZone.UTC, backgroundScope)
        runCurrent()

        viewModel.syncSchedule(recipes, initialPantry)
        runCurrent()
        val beforeSwapDinner = repository.entries.value.first { it.day == monday && it.meal == MealSlot.DINNER }.recipeId

        // Bấm "Đổi món" -> tính lại và lưu món mới cho ngày đó.
        viewModel.dispatch(WeeklyMealPlanAction.SwapDay(0))
        runCurrent()
        val afterSwapDinner = repository.entries.value.first { it.day == monday && it.meal == MealSlot.DINNER }.recipeId
        assertNotEquals(beforeSwapDinner, afterSwapDinner)

        // Sang tuần mới -> tính lại thực đơn cho tuần mới với tủ lạnh hiện tại.
        viewModel.syncSchedule(recipes, changedPantry)
        runCurrent()
        val nextMonday = monday.plus(DatePeriod(days = 7))
        clock.instant = Instant.parse("2026-11-11T12:00:00Z") + 7.days
        viewModel.refresh(TimeZone.UTC)
        runCurrent()

        assertEquals(nextMonday, viewModel.state.value.weekStart)
        val nextWeekDinner = viewModel.state.value.savedEntries.first { it.day == nextMonday && it.meal == MealSlot.DINNER }
        assertEquals("pork", nextWeekDinner.recipeId)
    }

    @Test
    fun extraShoppingRepository_observesRemovesAndClearsExtraShoppingItems() = runTest {
        val repository = FakeWeeklyMealPlanRepository()
        val extraRepository = FakeExtraShoppingRepository(
            listOf(
                ExtraShoppingItem("garlic", monday),
                ExtraShoppingItem("onion", monday),
            ),
        )
        val viewModel = WeeklyMealPlanViewModel(
            repository = repository,
            clock = MutableClock(Instant.parse("2026-11-11T12:00:00Z")),
            timeZone = TimeZone.UTC,
            scope = backgroundScope,
            extraShoppingRepository = extraRepository,
        )
        runCurrent()

        assertEquals(2, viewModel.state.value.extraShoppingItems.size)
        assertEquals(listOf("garlic", "onion"), viewModel.state.value.extraShoppingItems.map { it.ingredientId })

        viewModel.removeExtraShoppingItem("   ")
        runCurrent()
        assertEquals(2, viewModel.state.value.extraShoppingItems.size)

        viewModel.removeExtraShoppingItem("garlic")
        runCurrent()
        assertEquals(listOf("onion"), viewModel.state.value.extraShoppingItems.map { it.ingredientId })

        viewModel.clearExtraShoppingItems()
        runCurrent()
        assertEquals(emptyList(), viewModel.state.value.extraShoppingItems)
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
        val disabledMeals = MutableStateFlow<List<MealSlotKey>>(emptyList())
        val entries = MutableStateFlow<List<WeeklyMealPlanEntry>>(emptyList())
        var failSaves = false

        override fun observeDisabledMeals(): Flow<List<MealSlotKey>> = disabledMeals

        override suspend fun setMealEnabled(day: LocalDate, slot: MealSlot, enabled: Boolean) {
            val key = MealSlotKey(day, slot)
            disabledMeals.value = if (enabled) disabledMeals.value.filterNot { it == key } else (disabledMeals.value + key).distinct()
        }

        override fun observeAll(): Flow<List<WeeklyMealPlanEntry>> = entries

        override suspend fun save(entry: WeeklyMealPlanEntry) {
            entries.value = entries.value.filterNot { it.day == entry.day && it.meal == entry.meal } + entry
        }

        override suspend fun replaceWeek(weekStart: LocalDate, entries: List<WeeklyMealPlanEntry>) {
            val weekEnd = weekStart.plus(DatePeriod(days = 6))
            this.entries.value = this.entries.value.filterNot { it.day in weekStart..weekEnd } + entries
        }

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

    private class FakeExtraShoppingRepository(
        initial: List<ExtraShoppingItem> = emptyList(),
    ) : ExtraShoppingRepository {
        private val items = MutableStateFlow(initial)

        override fun observeAll(): Flow<List<ExtraShoppingItem>> = items

        override suspend fun add(ingredientId: String, addedAt: LocalDate) {
            items.value = items.value.filterNot { it.ingredientId == ingredientId } + ExtraShoppingItem(ingredientId, addedAt)
        }

        override suspend fun remove(ingredientId: String) {
            items.value = items.value.filterNot { it.ingredientId == ingredientId }
        }

        override suspend fun clear() {
            items.value = emptyList()
        }
    }
}
