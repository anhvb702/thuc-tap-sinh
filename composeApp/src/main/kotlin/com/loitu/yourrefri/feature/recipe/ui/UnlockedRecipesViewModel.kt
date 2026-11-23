package com.loitu.yourrefri.feature.recipe.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loitu.yourrefri.feature.recipe.data.UnlockedRecipeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
) : ViewModel() {
    private val mutableUnlockedIds = MutableStateFlow<Set<String>>(emptySet())
    private val mutableLoaded = MutableStateFlow(false)
    private val mutableFailedId = MutableStateFlow<String?>(null)
    private val mutableLoadFailed = MutableStateFlow(false)

    val unlockedIds: StateFlow<Set<String>> = mutableUnlockedIds.asStateFlow()
    val loaded = mutableLoaded.asStateFlow()
    val failedId = mutableFailedId.asStateFlow()
    val loadFailed = mutableLoadFailed.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.observeUnlockedIds().collect { ids ->
                    mutableUnlockedIds.value = ids
                    mutableLoaded.value = true
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableLoaded.value = false
                mutableLoadFailed.value = true
            }
        }
    }

    fun unlock(recipeId: String) {
        if (recipeId.isBlank()) return
        mutableFailedId.value = null
        viewModelScope.launch {
            try {
                repository.unlock(recipeId, clock.now().toLocalDateTime(timeZone).date)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableFailedId.value = recipeId
            }
        }
    }
}
