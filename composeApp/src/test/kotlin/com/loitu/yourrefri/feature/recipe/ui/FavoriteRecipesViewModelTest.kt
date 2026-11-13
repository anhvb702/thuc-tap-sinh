package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.data.FavoriteRecipeRepository
import com.loitu.yourrefri.feature.recipe.data.toggleFavorite
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteRecipesViewModelTest {
    private class FakeRepository(initial: Set<String> = emptySet()) : FavoriteRecipeRepository {
        val ids = MutableStateFlow(initial)
        var failToggle = false
        var failObserve = false
        var observeCount = 0
        override fun observeIds(): Flow<Set<String>> = flow {
            observeCount++
            if (failObserve) throw IllegalStateException("read failed")
            ids.collect { emit(it) }
        }
        override suspend fun setFavorite(recipeId: String, favorite: Boolean) {
            ids.value = if (favorite) ids.value + recipeId else ids.value - recipeId
        }
        override suspend fun toggle(recipeId: String): Boolean {
            if (failToggle) throw IllegalStateException("disk full")
            ids.value = toggleFavorite(ids.value, recipeId)
            return recipeId in ids.value
        }
    }

    private fun TestScope.vm(repo: FakeRepository) = FavoriteRecipesViewModel(repo, backgroundScope)

    @Test fun startsUnloadedThenLoadsSavedIds() = runTest {
        val viewModel = vm(FakeRepository(setOf("soup")))
        runCurrent()
        assertEquals(FavoriteRecipesState(setOf("soup"), loaded = true), viewModel.state.value)
    }

    @Test fun toggleAddsThenRemoves() = runTest {
        val repo = FakeRepository()
        val viewModel = vm(repo)
        runCurrent()
        viewModel.dispatch(FavoriteRecipesAction.Toggle("soup"))
        runCurrent()
        assertEquals(setOf("soup"), viewModel.state.value.ids)
        viewModel.dispatch(FavoriteRecipesAction.Toggle("soup"))
        runCurrent()
        assertEquals(emptySet(), viewModel.state.value.ids)
    }

    @Test fun blankIdIsIgnored() = runTest {
        val repo = FakeRepository()
        val viewModel = vm(repo)
        runCurrent()
        viewModel.dispatch(FavoriteRecipesAction.Toggle(" "))
        runCurrent()
        assertEquals(emptySet(), repo.ids.value)
    }

    @Test fun saveFailureEmitsEffectKeepsStateAndAppAlive() = runTest {
        val repo = FakeRepository(setOf("egg"))
        val viewModel = vm(repo)
        val effects = mutableListOf<FavoriteRecipesEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect(effects::add) }
        runCurrent()
        repo.failToggle = true
        viewModel.dispatch(FavoriteRecipesAction.Toggle("soup"))
        runCurrent()
        assertEquals(listOf<FavoriteRecipesEffect>(FavoriteRecipesEffect.SaveFailed), effects)
        assertEquals(setOf("egg"), viewModel.state.value.ids)
        repo.failToggle = false
        viewModel.dispatch(FavoriteRecipesAction.Toggle("soup"))
        runCurrent()
        assertEquals(setOf("egg", "soup"), viewModel.state.value.ids)
    }

    @Test fun loadFailureStillMarksLoadedAndEmitsEffect() = runTest {
        val repo = FakeRepository().apply { failObserve = true }
        val viewModel = vm(repo)
        val effects = mutableListOf<FavoriteRecipesEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect(effects::add) }
        runCurrent()
        assertTrue(viewModel.state.value.loaded)
        assertEquals(listOf<FavoriteRecipesEffect>(FavoriteRecipesEffect.LoadFailed), effects)
    }

    @Test fun reloadRestartsObservationAndPicksUpRestoredData() = runTest {
        val repo = FakeRepository(setOf("a"))
        val viewModel = vm(repo)
        runCurrent()
        repo.failObserve = false
        repo.ids.value = setOf("restored")
        viewModel.reload()
        runCurrent()
        assertEquals(2, repo.observeCount)
        assertEquals(setOf("restored"), viewModel.state.value.ids)
        repo.ids.value = setOf("restored", "more")
        runCurrent()
        assertEquals(setOf("restored", "more"), viewModel.state.value.ids)
        assertFalse(viewModel.state.value.ids.isEmpty())
    }
}