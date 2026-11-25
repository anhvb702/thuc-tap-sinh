package com.loitu.yourrefri.feature.recipe.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanRepository
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanSettings
import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import com.loitu.yourrefri.feature.recipe.logic.isValidWeeklySwapKey
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class WeeklyMealPlanViewModel(
    private val repository: WeeklyMealPlanRepository,
    private val clock: Clock,
    timeZone: TimeZone,
    scope: CoroutineScope? = null,
    initialTasteProfile: FamilyTasteProfile = FamilyTasteProfile(),
) : ViewModel() {
    private sealed interface Event {
        data class Action(val value: WeeklyMealPlanStateAction) : Event
        data class Refresh(val timeZone: TimeZone) : Event
        data class Settings(val value: WeeklyMealPlanSettings?, val replace: Boolean) : Event
        data object SettingsLoadFailed : Event
    }

    private val workerScope = scope ?: viewModelScope
    private val events = Channel<Event>(Channel.UNLIMITED)
    private val mutableEffects = Channel<WeeklyMealPlanEffect>(Channel.BUFFERED)
    private val pendingActions = mutableListOf<WeeklyMealPlanStateAction>()
    private var currentTimeZone = timeZone
    private var currentTasteProfile = initialTasteProfile
    private val mutableState = MutableStateFlow(
        WeeklyMealPlanState(isLoaded = false, weekStart = currentWeekStart()),
    )
    val uiState: StateFlow<WeeklyMealPlanState> = mutableState.asStateFlow()
    val state: StateFlow<WeeklyMealPlanState> get() = uiState
    val effects = mutableEffects.receiveAsFlow()

    fun updateTasteProfile(profile: FamilyTasteProfile) {
        onIntent(WeeklyMealPlanIntent.UpdateTasteProfile(profile))
    }

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

    fun onIntent(intent: WeeklyMealPlanIntent) {
        when (intent) {
            is WeeklyMealPlanIntent.Refresh -> events.trySend(Event.Refresh(intent.timeZone)).getOrThrow()
            WeeklyMealPlanIntent.ReloadSettings -> requestSettings(replace = true)
            is WeeklyMealPlanIntent.UpdateTasteProfile -> currentTasteProfile = intent.profile
            is WeeklyMealPlanStateAction -> events.trySend(Event.Action(intent)).getOrThrow()
        }
    }

    fun dispatch(action: WeeklyMealPlanAction) {
        onIntent(action)
    }

    fun refresh(timeZone: TimeZone) {
        onIntent(WeeklyMealPlanIntent.Refresh(timeZone))
    }

    fun reloadSettings() {
        onIntent(WeeklyMealPlanIntent.ReloadSettings)
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

    private suspend fun apply(action: WeeklyMealPlanStateAction) {
        val current = mutableState.value
        val next = when (action) {
            is WeeklyMealPlanIntent.SetGoal -> current.copy(goal = action.value)
            is WeeklyMealPlanIntent.TogglePreference -> current.copy(
                preferences = togglePreferenceTag(current.preferences, action.value),
            )
            is WeeklyMealPlanIntent.SwapDay -> {
                if (!isValidWeeklySwapKey(action.dayIndex)) return
                current.copy(swappedOffsets = bumpDaySwapOffset(current.swappedOffsets, action.dayIndex))
            }
            is WeeklyMealPlanIntent.ToggleIngredient -> {
                if (action.id.isBlank()) return
                val unchecked = if (action.checked) current.uncheckedIds - action.id
                else current.uncheckedIds + action.id
                current.copy(uncheckedIds = unchecked)
            }
            is WeeklyMealPlanIntent.SetLunchMealPrep -> current.copy(includeLunchMealPrep = action.enabled)
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
