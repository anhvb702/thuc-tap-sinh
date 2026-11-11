package com.loitu.yourrefri.feature.pantry.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryRepository
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridEffect
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.reduce
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(kotlin.time.ExperimentalTime::class)
class PantryGridViewModel(
    private val repository: PantryRepository,
    private val ingredients: List<Ingredient>,
    private val clock: Clock,
    timeZone: TimeZone,
    scope: CoroutineScope? = null,
) : ViewModel() {
    private sealed interface Event {
        data object RepositoryChanged : Event
        data class ResumeRefresh(val timeZone: TimeZone?) : Event
        data class Action(val value: PantryGridAction) : Event
    }

    private val workerScope = scope ?: viewModelScope
    private val events = Channel<Event>(Channel.UNLIMITED)
    private var currentTimeZone = timeZone
    private val initialLocalTime = clock.now().toLocalDateTime(currentTimeZone)
    private val mutableState = MutableStateFlow(
        PantryGridState(today = initialLocalTime.date, hourOfDay = initialLocalTime.hour),
    )
    val state = mutableState.asStateFlow()

    init {
        workerScope.launch {
            for (event in events) {
                when (event) {
                    Event.RepositoryChanged -> refreshSnapshot(clearSessionBase = false)
                    is Event.ResumeRefresh -> {
                        currentTimeZone = event.timeZone ?: currentTimeZone
                        refreshSnapshot(clearSessionBase = true)
                    }
                    is Event.Action -> {
                        val action = event.value
                        val now = clock.now().toLocalDateTime(currentTimeZone)
                        val current = mutableState.value.let {
                            it.copy(today = now.date, hourOfDay = now.hour)
                        }
                        val update = reduce(current, action)
                        mutableState.value = update.state
                        for (effect in update.effects) {
                            when (effect) {
                                is PantryGridEffect.SaveHave -> repository.setHave(
                                    effect.ingredientId, effect.have, effect.markedAt,
                                )
                                is PantryGridEffect.DeleteHave -> repository.deleteHave(effect.ingredientId)
                                is PantryGridEffect.SaveQuantity -> repository.setQuantity(
                                    effect.ingredientId, effect.quantity,
                                )
                                is PantryGridEffect.SaveUnit -> repository.setUnit(
                                    effect.ingredientId, effect.unit,
                                )
                                is PantryGridEffect.SaveShelfDays -> repository.setShelfDays(
                                    effect.ingredientId, effect.days,
                                )
                            }
                        }
                    }
                }
            }
        }
        workerScope.launch {
            combine(
                repository.observe(),
                repository.observeShelfOverrides(),
                repository.observeQuantities(),
                repository.observeUnits(),
            ) { _, _, _, _ -> Event.RepositoryChanged }
                .collect { events.send(it) }
        }
    }

    fun dispatch(action: PantryGridAction) {
        events.trySend(Event.Action(action)).getOrThrow()
    }

    fun refresh(timeZone: TimeZone? = null) {
        events.trySend(Event.ResumeRefresh(timeZone)).getOrThrow()
    }

    private suspend fun refreshSnapshot(clearSessionBase: Boolean) {
        val pantry = repository.observe().first()
        val overrides = repository.observeShelfOverrides().first()
        val quantities = repository.observeQuantities().first()
        val units = repository.observeUnits().first()
        val current = mutableState.value.let {
            if (clearSessionBase) it.copy(sessionBase = emptyMap()) else it
        }
        val now = clock.now().toLocalDateTime(currentTimeZone)
        val refreshed = reduce(
            current,
            PantryGridAction.Loaded(
                ingredients.map { it.copy(userShelfDays = overrides[it.id]) },
                pantry,
                now.date,
                quantities,
                units,
                now.hour,
            ),
        ).state
        mutableState.value = refreshed
    }

    override fun onCleared() {
        events.close()
        super.onCleared()
    }

}
