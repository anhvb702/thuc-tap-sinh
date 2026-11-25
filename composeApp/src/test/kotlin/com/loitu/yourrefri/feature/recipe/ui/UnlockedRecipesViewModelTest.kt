package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.data.UnlockedRecipeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class UnlockedRecipesViewModelTest {
    private class FakeRepository : UnlockedRecipeRepository {
        var ids = MutableStateFlow(setOf("egg"))
        val writes = mutableListOf<Pair<String, LocalDate>>()
        var failUnlock = false
        var failObserve = false

        override fun observeUnlockedIds() = ids.also { check(!failObserve) }

        override suspend fun unlock(recipeId: String, unlockedOn: LocalDate) {
            check(!failUnlock)
            writes += recipeId to unlockedOn
            ids.value += recipeId
        }
    }

    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-25T18:00:00Z")
    }
    private fun TestScope.vm(repo: FakeRepository) =
        UnlockedRecipesViewModel(repo, clock, TimeZone.of("Asia/Bangkok"), backgroundScope)

    @Test fun init_observesUnlockedIdsAndSetsLoaded() = runTest {
        val viewModel = vm(FakeRepository())
        runCurrent()
        assertEquals(UnlockedRecipesState(setOf("egg"), loaded = true), viewModel.uiState.value)
        assertEquals(setOf("egg"), viewModel.unlockedIds.value)
    }

    @Test fun onIntent_unlock_persistsRecipeAndClearsPreviousFailure() = runTest {
        val repo = FakeRepository().apply { failUnlock = true }
        val viewModel = vm(repo)
        viewModel.unlock("soup")
        runCurrent()
        assertEquals("soup", viewModel.failedId.value)
        repo.failUnlock = false
        viewModel.onIntent(UnlockedRecipesIntent.Unlock("soup"))
        runCurrent()
        assertEquals(listOf("soup" to LocalDate(2026, 11, 26)), repo.writes)
        assertEquals(UnlockedRecipesState(setOf("egg", "soup"), loaded = true), viewModel.uiState.value)
    }

    @Test fun onIntent_unlock_blankId_isIgnored() = runTest {
        val repo = FakeRepository()
        vm(repo).onIntent(UnlockedRecipesIntent.Unlock(" "))
        runCurrent()
        assertTrue(repo.writes.isEmpty())
    }

    @Test fun onIntent_unlock_failureSetsFailedIdAndEmitsUnlockFailedEffect() = runTest {
        val viewModel = vm(FakeRepository().apply { failUnlock = true })
        viewModel.onIntent(UnlockedRecipesIntent.Unlock("soup"))
        runCurrent()
        assertEquals("soup", viewModel.uiState.value.failedId)
        assertEquals(UnlockedRecipesEffect.UnlockFailed("soup"), withTimeout(1_000) { viewModel.effects.first() })
    }

    @Test fun onIntent_reload_cancelsOldCollectorAndReobserves() = runTest {
        val repo = FakeRepository()
        val oldIds = repo.ids
        val viewModel = vm(repo)
        runCurrent()
        repo.ids = MutableStateFlow(setOf("restored"))
        viewModel.onIntent(UnlockedRecipesIntent.Reload)
        runCurrent()
        assertEquals(0, oldIds.subscriptionCount.value)
        assertEquals(1, repo.ids.subscriptionCount.value)
        assertEquals(setOf("restored"), viewModel.uiState.value.unlockedIds)
    }

    @Test fun observeFailure_setsLoadFailedAndEmitsLoadFailedEffect() = runTest {
        val repo = FakeRepository().apply { failObserve = true }
        val viewModel = vm(repo)
        runCurrent()
        assertEquals(UnlockedRecipesState(loadFailed = true), viewModel.uiState.value)
        assertTrue(viewModel.loadFailed.value)
        assertEquals(UnlockedRecipesEffect.LoadFailed, withTimeout(1_000) { viewModel.effects.first() })
        repo.failObserve = false
        viewModel.reload()
        runCurrent()
        assertEquals(UnlockedRecipesState(setOf("egg"), loaded = true), viewModel.uiState.value)
    }
}
