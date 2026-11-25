package com.loitu.yourrefri.feature.recipe.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loitu.yourrefri.feature.recipe.data.UnlockedRecipeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class UnlockedRecipesViewModel(
    private val repository: UnlockedRecipeRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
    scope: CoroutineScope? = null,
) : ViewModel() {
    private val workerScope = scope ?: viewModelScope
    private val mutableState = MutableStateFlow(UnlockedRecipesState())
    private val mutableEffects = Channel<UnlockedRecipesEffect>(Channel.BUFFERED)
    private var collectJob: Job? = null
    private val mutableUnlockedIds = MutableStateFlow<Set<String>>(emptySet())
    private val mutableLoaded = MutableStateFlow(false)
    private val mutableFailedId = MutableStateFlow<String?>(null)
    private val mutableLoadFailed = MutableStateFlow(false)

    val unlockedIds: StateFlow<Set<String>> = mutableUnlockedIds.asStateFlow()
    val loaded = mutableLoaded.asStateFlow()
    val failedId = mutableFailedId.asStateFlow()
    val loadFailed = mutableLoadFailed.asStateFlow()
    val uiState: StateFlow<UnlockedRecipesState> = mutableState.asStateFlow()
    val effects = mutableEffects.receiveAsFlow()

    init {
        startCollecting()
    }

    fun onIntent(intent: UnlockedRecipesIntent) = when (intent) {
        is UnlockedRecipesIntent.Unlock -> persistUnlock(intent.recipeId)
        UnlockedRecipesIntent.Reload -> startCollecting()
    }

    fun reload() = onIntent(UnlockedRecipesIntent.Reload)
    fun unlock(recipeId: String) = onIntent(UnlockedRecipesIntent.Unlock(recipeId))

    private fun startCollecting() {
        collectJob?.cancel()
        collectJob = workerScope.launch {
            try {
                repository.observeUnlockedIds().collect { ids ->
                    publish { it.copy(unlockedIds = ids, loaded = true, loadFailed = false) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                publish { it.copy(loaded = false, loadFailed = true) }
                mutableEffects.send(UnlockedRecipesEffect.LoadFailed)
            }
        }
    }

    private fun persistUnlock(recipeId: String) {
        if (recipeId.isBlank()) return
        publish { it.copy(failedId = null) }
        workerScope.launch {
            try {
                repository.unlock(recipeId, clock.now().toLocalDateTime(timeZone).date)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                publish { it.copy(failedId = recipeId) }
                mutableEffects.send(UnlockedRecipesEffect.UnlockFailed(recipeId))
            }
        }
    }

    private fun publish(transform: (UnlockedRecipesState) -> UnlockedRecipesState) {
        val next = mutableState.updateAndGet(transform)
        mutableUnlockedIds.value = next.unlockedIds
        mutableLoaded.value = next.loaded
        mutableFailedId.value = next.failedId
        mutableLoadFailed.value = next.loadFailed
    }
}
