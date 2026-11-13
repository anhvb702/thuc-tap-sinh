@file:OptIn(kotlin.time.ExperimentalTime::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.loitu.yourrefri.feature.cooklog.ui

import com.loitu.yourrefri.feature.cooklog.data.CookLog
import com.loitu.yourrefri.feature.cooklog.data.CookLogRepository
import com.loitu.yourrefri.feature.cooklog.logic.CookHistoryWeekLabel
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class CookHistoryViewModelTest {
    private class FakeRepository(initial: List<CookLog> = emptyList()) : CookLogRepository {
        val logs = MutableStateFlow(initial)
        val added = mutableListOf<Triple<String, LocalDate, List<String>>>()
        val addedMeals = mutableListOf<MealSlot?>()
        var failAdd = false
        var failObserve = false
        private var nextId = 100L
        override fun observeAll(): Flow<List<CookLog>> = flow {
            if (failObserve) throw IllegalStateException("read failed")
            logs.collect { emit(it) }
        }
        override suspend fun add(recipeId: String, cookedOn: LocalDate, savedIngredientIds: List<String>, meal: MealSlot?): Long {
            if (failAdd) throw IllegalStateException("disk full")
            added += Triple(recipeId, cookedOn, savedIngredientIds)
            addedMeals += meal
            val id = nextId++
            logs.value = listOf(CookLog(id, recipeId, cookedOn, null, savedIngredientIds, meal)) + logs.value
            return id
        }
        override suspend fun delete(id: Long) {
            logs.value = logs.value.filterNot { it.id == id }
        }
    }

    private fun clockAt(iso: String) = object : Clock {
        override fun now(): Instant = Instant.parse(iso)
    }

    private fun TestScope.vm(repo: FakeRepository, iso: String = "2026-11-13T05:00:00Z", tz: TimeZone = TimeZone.UTC) =
        CookHistoryViewModel(repo, clockAt(iso), tz, backgroundScope)

    @Test
    fun startsUnloadedThenGroupsLogsByWeek() = runTest {
        val repo = FakeRepository(
            listOf(
                CookLog(1, "a", LocalDate(2026, 11, 12), null, emptyList()),
                CookLog(2, "b", LocalDate(2026, 11, 3), null, emptyList()),
            ),
        )
        val viewModel = vm(repo)
        assertFalse(viewModel.state.value.loaded)
        runCurrent()
        assertTrue(viewModel.state.value.loaded)
        assertEquals(
            listOf(CookHistoryWeekLabel.THIS_WEEK, CookHistoryWeekLabel.LAST_WEEK),
            viewModel.state.value.weeks.map { it.label },
        )
    }

    @Test
    fun record_writesTodayAndIngredientIds_andReturnsNewId() = runTest {
        val repo = FakeRepository()
        val viewModel = vm(repo)
        runCurrent()
        var result: Long? = -1
        viewModel.record("canh-chua", listOf("ca", "ca-chua")) { result = it }
        runCurrent()
        assertEquals(listOf(Triple("canh-chua", LocalDate(2026, 11, 13), listOf("ca", "ca-chua"))), repo.added)
        assertEquals(100L, result)
        assertEquals(1, viewModel.state.value.weeks.single().entries.size)
    }

    @Test
    fun record_passesTheMealToTheRepository() = runTest {
        val repo = FakeRepository()
        val viewModel = vm(repo)
        runCurrent()
        viewModel.record("a", emptyList(), MealSlot.LUNCH) {}
        viewModel.record("b", emptyList(), null) {}
        runCurrent()
        assertEquals(listOf<MealSlot?>(MealSlot.LUNCH, null), repo.addedMeals)
        assertEquals(MealSlot.LUNCH, viewModel.state.value.weeks.single().entries.first { it.recipeId == "a" }.meal)
    }

    @Test
    fun record_withNoIngredients_stillRecords() = runTest {
        val repo = FakeRepository()
        val viewModel = vm(repo)
        runCurrent()
        viewModel.record("rau-luoc", emptyList()) {}
        runCurrent()
        assertEquals(1, repo.added.size)
    }

    @Test
    fun record_blankRecipeId_isIgnored_andCallbackGetsNull() = runTest {
        val repo = FakeRepository()
        val viewModel = vm(repo)
        runCurrent()
        var result: Long? = -1
        viewModel.record(" ", listOf("ca")) { result = it }
        runCurrent()
        assertTrue(repo.added.isEmpty())
        assertNull(result)
    }

    @Test
    fun record_failure_emitsEffect_andCallbackGetsNull() = runTest {
        val repo = FakeRepository().apply { failAdd = true }
        val viewModel = vm(repo)
        val effects = mutableListOf<CookHistoryEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect(effects::add) }
        runCurrent()
        var result: Long? = -1
        viewModel.record("canh", emptyList()) { result = it }
        runCurrent()
        assertNull(result)
        assertEquals(listOf<CookHistoryEffect>(CookHistoryEffect.SaveFailed), effects)
    }

    @Test
    fun delete_removesTheEntry() = runTest {
        val repo = FakeRepository(listOf(CookLog(5, "a", LocalDate(2026, 11, 12), null, emptyList())))
        val viewModel = vm(repo)
        runCurrent()
        viewModel.dispatch(CookHistoryAction.Delete(5))
        runCurrent()
        assertTrue(viewModel.state.value.weeks.isEmpty())
    }

    @Test
    fun todayComesFromInjectedClockAndZone() = runTest {
        // 20:00Z ngày 13/11 là 03:00 ngày 14/11 ở UTC+7.
        val repo = FakeRepository()
        val viewModel = vm(repo, "2026-11-13T20:00:00Z", TimeZone.of("UTC+07:00"))
        runCurrent()
        viewModel.record("canh", emptyList()) {}
        runCurrent()
        assertEquals(LocalDate(2026, 11, 14), repo.added.single().second)
    }

    @Test
    fun loadFailure_marksLoaded_andEmitsEffect() = runTest {
        val repo = FakeRepository().apply { failObserve = true }
        val viewModel = vm(repo)
        val effects = mutableListOf<CookHistoryEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect(effects::add) }
        runCurrent()
        assertTrue(viewModel.state.value.loaded)
        assertEquals(listOf<CookHistoryEffect>(CookHistoryEffect.LoadFailed), effects)
    }
}
