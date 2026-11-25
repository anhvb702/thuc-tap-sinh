package com.loitu.yourrefri.feature.recipe.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loitu.yourrefri.feature.recipe.data.FavoriteRecipeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class FavoriteRecipesViewModel(
    private val repository: FavoriteRecipeRepository,
    scope: CoroutineScope? = null,
) : ViewModel() {
    private val workerScope = scope ?: viewModelScope
    private val mutableState = MutableStateFlow(FavoriteRecipesState())
    private val mutableEffects = Channel<FavoriteRecipesEffect>(Channel.BUFFERED)
    private var collectJob: Job? = null

    val uiState: StateFlow<FavoriteRecipesState> = mutableState.asStateFlow()
    val state: StateFlow<FavoriteRecipesState> get() = uiState
    val effects = mutableEffects.receiveAsFlow()

    init {
        startCollecting()
    }

    fun onIntent(intent: FavoriteRecipesIntent) = when (intent) {
        is FavoriteRecipesIntent.Toggle -> toggle(intent.recipeId)
        FavoriteRecipesIntent.Reload -> startCollecting()
    }

    fun dispatch(action: FavoriteRecipesAction) = onIntent(action)

    /** Đọc lại từ DB, dùng sau khi khôi phục sao lưu. */
    fun reload() = onIntent(FavoriteRecipesIntent.Reload)

    private fun startCollecting() {
        collectJob?.cancel()
        collectJob = workerScope.launch {
            try {
                repository.observeIds().collect { ids ->
                    mutableState.value = FavoriteRecipesState(ids = ids, loaded = true)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.value = mutableState.value.copy(loaded = true)
                mutableEffects.send(FavoriteRecipesEffect.LoadFailed)
            }
        }
    }

    private fun toggle(recipeId: String) {
        if (recipeId.isBlank()) return
        workerScope.launch {
            try {
                repository.toggle(recipeId)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableEffects.send(FavoriteRecipesEffect.SaveFailed)
            }
        }
    }
}
