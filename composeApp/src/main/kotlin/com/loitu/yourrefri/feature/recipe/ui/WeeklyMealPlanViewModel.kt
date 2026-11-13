package com.loitu.yourrefri.feature.recipe.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanRepository
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanSettings
import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class WeeklyMealPlanState(
    val isLoaded: Boolean,
    val weekStart: LocalDate,
    val goal: DietGoal = DietGoal.BALANCED,
    val preferences: Set<FamilyPreferenceTag> = emptySet(),
    val swappedOffsets: Map<Int, Int> = emptyMap(),
    val uncheckedIds: Set<String> = emptySet(),
    val includeLunchMealPrep: Boolean = false,
)

sealed interface WeeklyMealPlanAction {
    data class SetGoal(val value: DietGoal) : WeeklyMealPlanAction
    data class TogglePreference(val value: FamilyPreferenceTag) : WeeklyMealPlanAction
    data class SwapDay(val dayIndex: Int) : WeeklyMealPlanAction
    data class ToggleIngredient(val id: String, val checked: Boolean) : WeeklyMealPlanAction
    data class SetLunchMealPrep(val enabled: Boolean) : WeeklyMealPlanAction
}

sealed interface WeeklyMealPlanEffect {
    data object SettingsSaveFailed : WeeklyMealPlanEffect
    data object SettingsLoadFailed : WeeklyMealPlanEffect
}

@OptIn(ExperimentalTime::class)
class WeeklyMealPlanViewModel(
    private val repository: WeeklyMealPlanRepository,
    private val clock: Clock,
    timeZone: TimeZone,
    scope: CoroutineScope? = null,
) : ViewModel() {
    private sealed interface Event {
        data class Action(val value: WeeklyMealPlanAction) : Event
        data class Refresh(val timeZone: TimeZone) : Event
        data class Settings(val value: WeeklyMealPlanSettings?, val replace: Boolean) : Event
        data object SettingsLoadFailed : Event
    }

    private val workerScope = scope ?: viewModelScope
    private val events = Channel<Event>(Channel.UNLIMITED)
    private val mutableEffects = Channel<WeeklyMealPlanEffect>(Channel.BUFFERED)
    private val pendingActions = mutableListOf<WeeklyMealPlanAction>()
    private var currentTimeZone = timeZone
    private val mutableState = MutableStateFlow(
        WeeklyMealPlanState(isLoaded = false, weekStart = currentWeekStart()),
    )
    val state = mutableState.asStateFlow()
    val effects = mutableEffects.receiveAsFlow()

    init {
        requestSettings(replace = false)
        workerScope.launch {
            for (event in events) {
                when (event) {
                    is Event.Action -> {
                        if (!mutableState.value.isLoaded) pendingActions += event.value
                        else apply(event.value)
                    }
                    is Event.Refresh -> {
                        if (mutableState.value.isLoaded) refreshWeek(event.timeZone)
                    }
                    is Event.Settings -> acceptSettings(event.value, event.replace)
                    Event.SettingsLoadFailed -> {
                        if (!mutableState.value.isLoaded) {
                            mutableState.value = mutableState.value.copy(isLoaded = true)
                            // Thao tác đã xếp hàng khi đang tải phải được áp dụng, không được bỏ rơi.
                            val queued = pendingActions.toList()
                            pendingActions.clear()
                            queued.forEach { apply(it) }
                        }
                        mutableEffects.send(WeeklyMealPlanEffect.SettingsLoadFailed)
                    }
                }
            }
        }
    }

    fun dispatch(action: WeeklyMealPlanAction) {
        events.trySend(Event.Action(action)).getOrThrow()
    }

    fun refresh(timeZone: TimeZone) {
        events.trySend(Event.Refresh(timeZone)).getOrThrow()
    }

    fun reloadSettings() {
        requestSettings(replace = true)
    }

    private fun requestSettings(replace: Boolean) {
        workerScope.launch {
            try {
                events.send(Event.Settings(repository.observeSettings().first(), replace))
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                events.send(Event.SettingsLoadFailed)
            }
        }
    }

    private suspend fun apply(action: WeeklyMealPlanAction) {
        val current = mutableState.value
        val next = when (action) {
            is WeeklyMealPlanAction.SetGoal -> current.copy(goal = action.value)
            is WeeklyMealPlanAction.TogglePreference -> current.copy(
                preferences = togglePreferenceTag(current.preferences, action.value),
            )
            is WeeklyMealPlanAction.SwapDay -> {
                if (action.dayIndex !in 0..6) return
                current.copy(swappedOffsets = bumpDaySwapOffset(current.swappedOffsets, action.dayIndex))
            }
            is WeeklyMealPlanAction.ToggleIngredient -> {
                if (action.id.isBlank()) return
                val unchecked = if (action.checked) current.uncheckedIds - action.id
                else current.uncheckedIds + action.id
                current.copy(uncheckedIds = unchecked)
            }
            is WeeklyMealPlanAction.SetLunchMealPrep -> current.copy(includeLunchMealPrep = action.enabled)
        }
        mutableState.value = next
        save(next)
    }

    private suspend fun acceptSettings(settings: WeeklyMealPlanSettings?, replace: Boolean) {
        if (!mutableState.value.isLoaded) {
            acceptInitialSettings(settings)
        } else if (replace && settings != mutableState.value.toSettings()) {
            val normalized = normalize(settings)
            mutableState.value = normalized
            if (settings != null && normalized.toSettings() != settings) save(normalized)
        }
    }

    private suspend fun acceptInitialSettings(settings: WeeklyMealPlanSettings?) {
        if (mutableState.value.isLoaded) return
        val normalized = normalize(settings)
        mutableState.value = normalized
        if (settings != null && normalized.toSettings() != settings) save(normalized)
        val queued = pendingActions.toList()
        pendingActions.clear()
        queued.forEach { apply(it) }
    }

    private suspend fun refreshWeek(timeZone: TimeZone) {
        currentTimeZone = timeZone
        val newWeekStart = currentWeekStart()
        val current = mutableState.value
        if (newWeekStart == current.weekStart) return
        val rolled = rollWeeklyPlanSettings(current.toSettings(), newWeekStart)
        val next = current.copy(
            weekStart = rolled.weekStart,
            swappedOffsets = rolled.swappedOffsets,
            uncheckedIds = rolled.uncheckedIds,
        )
        mutableState.value = next
        save(next)
    }

    private fun normalize(settings: WeeklyMealPlanSettings?): WeeklyMealPlanState {
        val weekStart = currentWeekStart()
        if (settings == null) return WeeklyMealPlanState(isLoaded = true, weekStart = weekStart)
        val normalized = rollWeeklyPlanSettings(settings, weekStart)
        val state = WeeklyMealPlanState(
            isLoaded = true,
            weekStart = normalized.weekStart,
            goal = DietGoal.entries.firstOrNull { it.name == settings.goalId } ?: DietGoal.BALANCED,
            preferences = settings.preferenceIds.mapNotNull { id ->
                FamilyPreferenceTag.entries.firstOrNull { it.name == id }
            }.toSet(),
            swappedOffsets = normalized.swappedOffsets,
            uncheckedIds = normalized.uncheckedIds,
            includeLunchMealPrep = settings.includeLunchMealPrep,
        )
        return state
    }

    private suspend fun save(state: WeeklyMealPlanState) {
        try {
            val settings = state.toSettings()
            repository.saveSettings(settings)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            mutableEffects.send(WeeklyMealPlanEffect.SettingsSaveFailed)
        }
    }

    private fun currentWeekStart(): LocalDate {
        return weeklyPlanWeekStart(clock.now(), currentTimeZone)
    }

    private fun WeeklyMealPlanState.toSettings() = WeeklyMealPlanSettings(
        weekStart = weekStart,
        goalId = goal.name,
        preferenceIds = preferences.mapTo(LinkedHashSet()) { it.name },
        swappedOffsets = swappedOffsets,
        uncheckedIds = uncheckedIds,
        includeLunchMealPrep = includeLunchMealPrep,
    )

    override fun onCleared() {
        events.close()
        mutableEffects.close()
        super.onCleared()
    }
}
