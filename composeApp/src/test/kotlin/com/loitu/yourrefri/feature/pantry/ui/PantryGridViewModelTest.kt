package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.data.PantryRepository
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

@OptIn(kotlin.time.ExperimentalTime::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PantryGridViewModelTest {
    private val oldDate = LocalDate(2026, 3, 1)
    private val egg = Ingredient("egg", "Trứng", "sua-trung", 21)
    private val tomato = Ingredient("tomato", "Cà chua", "rau-cu", 5)

    private class TestClock(var instant: Instant) : Clock {
        override fun now() = instant
    }

    private fun newClock() = TestClock(Instant.parse("2026-03-10T23:30:00Z"))

    private class FakeRepository : PantryRepository {
        val pantry = MutableStateFlow<List<PantryItem>>(emptyList())
        val overrides = MutableStateFlow<Map<String, Int>>(emptyMap())
        val quantities = MutableStateFlow<Map<String, Int>>(emptyMap())
        val units = MutableStateFlow<Map<String, String>>(emptyMap())
        private val shelfSnapshots = MutableSharedFlow<Map<String, Int>>(extraBufferCapacity = 16)
        val writes = mutableListOf<Triple<String, Boolean, LocalDate?>>()
        val deleteWrites = mutableListOf<String>()
        val shelfWrites = mutableListOf<Pair<String, Int?>>()
        val quantityWrites = mutableListOf<Pair<String, Int>>()
        val delayedShelfSnapshots = mutableListOf<Map<String, Int>>()
        var delayShelfSnapshots = false
        var gate: CompletableDeferred<Unit>? = null
        var writeHook: (suspend (String, Boolean, LocalDate?) -> Unit)? = null
        override fun observe() = pantry
        override fun observeQuantities(): Flow<Map<String, Int>> = quantities
        override fun observeShelfOverrides(): Flow<Map<String, Int>> = flow {
            emit(overrides.value)
            shelfSnapshots.collect { emit(it) }
        }
        override fun observeUnits(): Flow<Map<String, String>> = units

        suspend fun setExternalShelfOverrides(value: Map<String, Int>) {
            overrides.value = value
            shelfSnapshots.emit(value)
        }

        suspend fun emitOldestShelfSnapshot() {
            shelfSnapshots.emit(delayedShelfSnapshots.removeAt(0))
        }
        override suspend fun setHave(ingredientId: String, have: Boolean, markedAt: LocalDate?) {
            writes += Triple(ingredientId, have, markedAt)
            val existing = pantry.value.firstOrNull { it.ingredientId == ingredientId }
            pantry.value = when {
                have -> pantry.value.filterNot { it.ingredientId == ingredientId } +
                    PantryItem(ingredientId, true, markedAt ?: LocalDate(2026, 3, 10))
                markedAt != null && existing != null -> pantry.value.filterNot { it.ingredientId == ingredientId } +
                    PantryItem(ingredientId, false, markedAt)
                existing != null -> pantry.value.map {
                    if (it.ingredientId == ingredientId) it.copy(have = false) else it
                }
                else -> pantry.value
            }
            writeHook?.invoke(ingredientId, have, markedAt)
            gate?.await()
        }
        override suspend fun deleteHave(ingredientId: String) {
            deleteWrites += ingredientId
            pantry.value = pantry.value.filterNot { it.ingredientId == ingredientId }
        }
        override suspend fun setShelfDays(ingredientId: String, days: Int?) {
            shelfWrites += ingredientId to days
            val updated = overrides.value.toMutableMap().apply {
                if (days == null) remove(ingredientId) else put(ingredientId, days)
            }
            overrides.value = updated
            if (delayShelfSnapshots) delayedShelfSnapshots += updated else shelfSnapshots.emit(updated)
        }

        override suspend fun setQuantity(ingredientId: String, quantity: Int) {
            quantityWrites += ingredientId to quantity
            quantities.value = quantities.value.toMutableMap().apply {
                if (quantity == 1) remove(ingredientId) else put(ingredientId, quantity)
            }
        }
        override suspend fun setUnit(ingredientId: String, unit: String?) {
            units.value = units.value.toMutableMap().apply {
                if (unit == null) remove(ingredientId) else put(ingredientId, unit)
            }
        }
    }

    @Test fun loadedQuantities_areAvailableInState() = runTest {
        val repository = FakeRepository()
        repository.quantities.value = mapOf("egg" to 3)
        val vm = PantryGridViewModel(repository, listOf(egg, tomato), newClock(), TimeZone.UTC, backgroundScope)

        runCurrent()

        assertEquals(mapOf("egg" to 3), vm.state.value.quantities)
        assertEquals(mapOf("egg" to 3, "tomato" to 1), cellsOf(vm.state.value).associate { it.ingredientId to it.quantity })
    }

    @Test fun undoingFirstTap_deletesPersistedPantryRow() = runTest {
        val repository = FakeRepository()
        val vm = PantryGridViewModel(repository, listOf(egg, tomato), newClock(), TimeZone.UTC, backgroundScope)
        runCurrent()

        vm.dispatch(com.loitu.yourrefri.feature.pantry.logic.PantryGridAction.Tap("egg"))
        vm.dispatch(com.loitu.yourrefri.feature.pantry.logic.PantryGridAction.Tap("egg"))
        runCurrent()

        assertEquals(listOf("egg"), repository.deleteWrites)
        assertTrue(repository.pantry.value.isEmpty())
    }

    @Test fun increaseQuantity_updatesStateAndPersists() = runTest {
        val repository = FakeRepository()
        repository.pantry.value = listOf(PantryItem("egg", true, oldDate))
        val vm = PantryGridViewModel(repository, listOf(egg), newClock(), TimeZone.UTC, backgroundScope)
        runCurrent()

        vm.dispatch(PantryGridAction.IncreaseQuantity("egg"))
        runCurrent()

        assertEquals(2, cellsOf(vm.state.value).single().quantity)
        assertEquals(listOf("egg" to 2), repository.quantityWrites)
        assertEquals(PantryItem("egg", true, oldDate), repository.pantry.value.single())
    }

    @Test fun decreaseQuantity_aboveOne_updatesAndPersistsDefault() = runTest {
        val repository = FakeRepository()
        repository.pantry.value = listOf(PantryItem("egg", true, oldDate))
        repository.quantities.value = mapOf("egg" to 2)
        val vm = PantryGridViewModel(repository, listOf(egg), newClock(), TimeZone.UTC, backgroundScope)
        runCurrent()

        vm.dispatch(PantryGridAction.DecreaseQuantity("egg"))
        runCurrent()

        assertEquals(1, cellsOf(vm.state.value).single().quantity)
        assertEquals(listOf("egg" to 1), repository.quantityWrites)
        assertEquals(emptyMap(), vm.state.value.quantities)
        assertEquals(PantryItem("egg", true, oldDate), repository.pantry.value.single())
    }

    @Test fun decreaseQuantity_atOne_keepsExistingHaveToggle() = runTest {
        val repository = FakeRepository()
        repository.pantry.value = listOf(PantryItem("egg", true, oldDate))
        val vm = PantryGridViewModel(repository, listOf(egg), newClock(), TimeZone.UTC, backgroundScope)
        runCurrent()

        vm.dispatch(PantryGridAction.DecreaseQuantity("egg"))
        runCurrent()

        assertEquals(1, cellsOf(vm.state.value).single().quantity)
        assertEquals(emptyList<Pair<String, Int>>(), repository.quantityWrites)
        assertEquals(listOf<Triple<String, Boolean, LocalDate?>>(Triple("egg", false, oldDate)), repository.writes)
        assertEquals(PantryItem("egg", false, oldDate), repository.pantry.value.single())
    }

    @Test fun increaseQuantity_atIntMax_keepsValueWithoutWrite() = runTest {
        val repository = FakeRepository()
        repository.pantry.value = listOf(PantryItem("egg", true, oldDate))
        repository.quantities.value = mapOf("egg" to Int.MAX_VALUE)
        val vm = PantryGridViewModel(repository, listOf(egg), newClock(), TimeZone.UTC, backgroundScope)
        runCurrent()

        vm.dispatch(PantryGridAction.IncreaseQuantity("egg"))
        runCurrent()

        assertEquals(Int.MAX_VALUE, cellsOf(vm.state.value).single().quantity)
        assertEquals(emptyList<Pair<String, Int>>(), repository.quantityWrites)
    }

    @Test fun quantityChange_doesNotChangeHaveOrMarkedAt() = runTest {
        val repository = FakeRepository()
        repository.pantry.value = listOf(PantryItem("egg", true, oldDate))
        val vm = PantryGridViewModel(repository, listOf(egg), newClock(), TimeZone.UTC, backgroundScope)
        runCurrent()

        vm.dispatch(PantryGridAction.IncreaseQuantity("egg"))
        runCurrent()

        assertEquals(PantryItem("egg", true, oldDate), vm.state.value.pantry["egg"])
        assertEquals(PantryItem("egg", true, oldDate), repository.pantry.value.single())
    }

    @Test fun loadsInjectedDateAndOverrides() = runTest {
        val repository = FakeRepository()
        repository.overrides.value = mapOf("egg" to 0)
        val vm = PantryGridViewModel(repository, listOf(egg), newClock(), TimeZone.of("UTC+07:00"), backgroundScope)
        runCurrent()
        assertEquals(LocalDate(2026, 3, 11), vm.state.value.today)
        assertEquals(0, vm.state.value.ingredients.single().userShelfDays)
        repository.setExternalShelfOverrides(emptyMap())
        runCurrent()
        assertEquals(null, vm.state.value.ingredients.single().userShelfDays)
    }

    @Test fun resumeRefreshUsesCurrentTimeZone() = runTest {
        val repository = FakeRepository()
        val vm = PantryGridViewModel(repository, listOf(egg), newClock(), TimeZone.UTC, backgroundScope)
        runCurrent()
        assertEquals(LocalDate(2026, 3, 10), vm.state.value.today)

        vm.refresh(TimeZone.of("UTC+07:00"))
        runCurrent()

        assertEquals(LocalDate(2026, 3, 11), vm.state.value.today)
    }

    @Test fun tapPersistsAndDoubleTapRestoresDate() = runTest {
        val repository = FakeRepository()
        repository.pantry.value = listOf(PantryItem("egg", true, oldDate))
        val vm = PantryGridViewModel(repository, listOf(egg), newClock(), TimeZone.UTC, backgroundScope)
        runCurrent()
        vm.dispatch(PantryGridAction.Tap("egg"))
        vm.dispatch(PantryGridAction.Tap("egg"))
        runCurrent()
        assertEquals(
            listOf<Triple<String, Boolean, LocalDate?>>(Triple("egg", false, oldDate), Triple("egg", true, oldDate)),
            repository.writes,
        )
        assertEquals(PantryItem("egg", true, oldDate), repository.pantry.value.single())
    }

    @Test fun writesStayInActionOrder() = runTest {
        val repository = FakeRepository()
        repository.gate = CompletableDeferred()
        val vm = PantryGridViewModel(repository, listOf(egg), newClock(), TimeZone.UTC, backgroundScope)
        runCurrent()
        vm.dispatch(PantryGridAction.Tap("egg"))
        vm.dispatch(PantryGridAction.Tap("egg"))
        runCurrent()
        assertEquals(1, repository.writes.size)
        repository.gate!!.complete(Unit)
        runCurrent()
        assertEquals(listOf(true), repository.writes.map { it.second })
        assertEquals(listOf("egg"), repository.deleteWrites)
        assertTrue(repository.pantry.value.isEmpty())
    }

    @Test fun staleWriteEchoKeepsLatestTapAndOtherLoadedItems() = runTest {
        val repository = FakeRepository()
        val vm = PantryGridViewModel(repository, listOf(egg, tomato), newClock(), TimeZone.UTC, backgroundScope)
        runCurrent()

        repository.gate = CompletableDeferred()
        vm.dispatch(PantryGridAction.Tap("egg"))
        vm.dispatch(PantryGridAction.Tap("egg"))
        runCurrent()
        assertEquals(1, repository.writes.size)
        repository.pantry.value = repository.pantry.value + PantryItem("tomato", true, oldDate)
        vm.dispatch(PantryGridAction.Tap("egg"))

        repository.gate!!.complete(Unit)
        runCurrent()

        assertEquals(true, vm.state.value.pantry["egg"]?.have)
        assertEquals(true, vm.state.value.pantry["tomato"]?.have)
        assertEquals(true, repository.pantry.value.single { it.ingredientId == "egg" }.have)
        assertEquals(true, repository.pantry.value.single { it.ingredientId == "tomato" }.have)
        assertEquals(listOf(true, true), repository.writes.map { it.second })
        assertEquals(listOf("egg"), repository.deleteWrites)
        assertEquals(
            listOf<Triple<Boolean, LocalDate?, String>>(
                Triple(true, LocalDate(2026, 3, 10), "egg"),
                Triple(true, LocalDate(2026, 3, 10), "egg"),
            ),
            repository.writes.map { Triple(it.second, it.third, it.first) },
        )
        assertEquals(
            PantryItem("egg", true, LocalDate(2026, 3, 10)),
            repository.pantry.value.single { it.ingredientId == "egg" },
        )
    }

    @Test fun externalPantryUpdateForTouchedItemIsObserved() = runTest {
        val repository = FakeRepository()
        repository.pantry.value = listOf(PantryItem("egg", true, oldDate))
        val vm = PantryGridViewModel(repository, listOf(egg), newClock(), TimeZone.UTC, backgroundScope)
        runCurrent()
        vm.dispatch(PantryGridAction.Tap("egg"))
        runCurrent()
        assertEquals(false, vm.state.value.pantry["egg"]?.have)

        repository.pantry.value = listOf(PantryItem("egg", true, LocalDate(2026, 3, 9)))
        runCurrent()

        assertEquals(PantryItem("egg", true, LocalDate(2026, 3, 9)), vm.state.value.pantry["egg"])
        assertEquals(PantryItem("egg", true, LocalDate(2026, 3, 9)), repository.pantry.value.single())
    }

    @Test fun shelfWriteAndExternalUpdateAreObserved() = runTest {
        val repository = FakeRepository()
        val vm = PantryGridViewModel(repository, listOf(egg), newClock(), TimeZone.UTC, backgroundScope)
        runCurrent()
        vm.dispatch(PantryGridAction.OpenShelfEditor("egg"))
        vm.dispatch(PantryGridAction.PickShelfDays(7))
        vm.dispatch(PantryGridAction.SaveShelfEditor)
        runCurrent()
        assertEquals(7, vm.state.value.ingredients.single().userShelfDays)
        assertEquals(7, repository.overrides.value["egg"])

        repository.setExternalShelfOverrides(mapOf("egg" to 0))
        runCurrent()

        assertEquals(0, vm.state.value.ingredients.single().userShelfDays)
        assertEquals(0, repository.overrides.value["egg"])
    }

    @Test fun delayedShelfEchoDoesNotRollBackNewerSave() = runTest {
        val repository = FakeRepository()
        val vm = PantryGridViewModel(repository, listOf(egg), newClock(), TimeZone.UTC, backgroundScope)
        runCurrent()
        repository.delayShelfSnapshots = true

        vm.dispatch(PantryGridAction.OpenShelfEditor("egg"))
        vm.dispatch(PantryGridAction.PickShelfDays(7))
        vm.dispatch(PantryGridAction.SaveShelfEditor)
        runCurrent()
        vm.dispatch(PantryGridAction.OpenShelfEditor("egg"))
        vm.dispatch(PantryGridAction.PickShelfDays(14))
        vm.dispatch(PantryGridAction.SaveShelfEditor)
        runCurrent()

        assertEquals(listOf(mapOf("egg" to 7), mapOf("egg" to 14)), repository.delayedShelfSnapshots)
        assertEquals(14, repository.overrides.value["egg"])
        repository.emitOldestShelfSnapshot()
        runCurrent()

        vm.dispatch(PantryGridAction.OpenShelfEditor("egg"))
        runCurrent()
        assertEquals(14, vm.state.value.ingredients.single().userShelfDays)
        assertEquals(14, vm.state.value.editor?.selectedDays)
        assertEquals(14, repository.overrides.value["egg"])
        assertEquals(listOf<Pair<String, Int?>>("egg" to 7, "egg" to 14), repository.shelfWrites)
    }

    @Test fun resumeRefreshResetsSessionBaseAndKeepsEditor() = runTest {
        val newDate = LocalDate(2026, 3, 9)
        val repository = FakeRepository()
        repository.pantry.value = listOf(PantryItem("egg", true, oldDate))
        val vm = PantryGridViewModel(repository, listOf(egg), newClock(), TimeZone.UTC, backgroundScope)
        runCurrent()
        vm.dispatch(PantryGridAction.Tap("egg"))
        runCurrent()
        assertEquals(false, vm.state.value.pantry["egg"]?.have)

        repository.pantry.value = listOf(PantryItem("egg", true, newDate))
        runCurrent()
        vm.dispatch(PantryGridAction.OpenShelfEditor("egg"))
        runCurrent()
        vm.refresh()
        runCurrent()
        assertEquals(PantryItem("egg", true, newDate), vm.state.value.pantry["egg"])
        assertEquals("egg", vm.state.value.editor?.ingredientId)

        vm.dispatch(PantryGridAction.Tap("egg"))
        vm.dispatch(PantryGridAction.Tap("egg"))
        runCurrent()

        assertEquals(listOf(false, false, true), repository.writes.map { it.second })
        assertEquals(
            listOf<Pair<Boolean, LocalDate?>>(false to newDate, true to newDate),
            repository.writes.takeLast(2).map { it.second to it.third },
        )
        assertEquals(PantryItem("egg", true, newDate), vm.state.value.pantry["egg"])
        assertEquals(PantryItem("egg", true, newDate), repository.pantry.value.single())
        assertEquals("egg", vm.state.value.editor?.ingredientId)
    }

    @Test fun refreshAfterMidnightUsesInjectedZone() = runTest {
        val clock = newClock().apply { instant = Instant.parse("2026-03-10T16:59:59Z") }
        val repository = FakeRepository()
        val vm = PantryGridViewModel(repository, listOf(egg), clock, TimeZone.of("UTC+07:00"), backgroundScope)
        runCurrent()
        assertEquals(LocalDate(2026, 3, 10), vm.state.value.today)
        assertEquals(23, vm.state.value.hourOfDay)

        clock.instant = Instant.parse("2026-03-10T17:00:00Z")
        vm.refresh()
        runCurrent()

        assertEquals(LocalDate(2026, 3, 11), vm.state.value.today)
        assertEquals(0, vm.state.value.hourOfDay)
    }

    @Test fun tapAcrossMidnightWritesCurrentLocalDate() = runTest {
        val clock = newClock().apply { instant = Instant.parse("2026-03-10T16:59:59Z") }
        val repository = FakeRepository()
        val vm = PantryGridViewModel(repository, listOf(egg), clock, TimeZone.of("UTC+07:00"), backgroundScope)
        runCurrent()
        clock.instant = Instant.parse("2026-03-10T17:00:00Z")

        vm.dispatch(PantryGridAction.Tap("egg"))
        runCurrent()

        assertEquals(LocalDate(2026, 3, 11), vm.state.value.today)
        assertEquals(LocalDate(2026, 3, 11), repository.writes.single().third)
        assertEquals(PantryItem("egg", true, LocalDate(2026, 3, 11)), repository.pantry.value.single())
    }

    @Test fun addIngredientAcrossMidnightWritesCurrentLocalDate() = runTest {
        val clock = newClock().apply { instant = Instant.parse("2026-03-10T16:59:59Z") }
        val repository = FakeRepository()
        val vm = PantryGridViewModel(repository, listOf(egg), clock, TimeZone.of("UTC+07:00"), backgroundScope)
        runCurrent()
        clock.instant = Instant.parse("2026-03-10T17:00:00Z")

        vm.dispatch(PantryGridAction.AddIngredient("egg", 7))
        runCurrent()

        assertEquals(LocalDate(2026, 3, 11), vm.state.value.today)
        assertEquals(LocalDate(2026, 3, 11), repository.writes.single().third)
        assertEquals(PantryItem("egg", true, LocalDate(2026, 3, 11)), repository.pantry.value.single())
        assertEquals(listOf<Pair<String, Int?>>("egg" to 7), repository.shelfWrites)
    }

    @Test fun savesShelfAndCancelDoesNotWrite() = runTest {
        val repository = FakeRepository()
        val vm = PantryGridViewModel(repository, listOf(egg), newClock(), TimeZone.UTC, backgroundScope)
        runCurrent()
        vm.dispatch(PantryGridAction.OpenShelfEditor("egg"))
        vm.dispatch(PantryGridAction.PickShelfDays(3))
        vm.dispatch(PantryGridAction.CancelShelfEditor)
        runCurrent()
        assertEquals(emptyList(), repository.shelfWrites)
        vm.dispatch(PantryGridAction.OpenShelfEditor("egg"))
        vm.dispatch(PantryGridAction.PickShelfDays(7))
        vm.dispatch(PantryGridAction.SaveShelfEditor)
        runCurrent()
        assertEquals(listOf<Pair<String, Int?>>("egg" to 7), repository.shelfWrites)
        assertEquals(7, repository.overrides.value["egg"])
    }
}
