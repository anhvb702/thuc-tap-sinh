package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.Urgency
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.feature.pantry.logic.expiringList
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class PantryGridSoonTabTest {
    private val today = LocalDate.parse("2026-11-13")

    private val cells = listOf(
        cell("ok", have = true, daysLeft = 5, urgency = Urgency.OK),
        cell("soon-b", have = true, daysLeft = 1, urgency = Urgency.SOON),
        cell("expired", have = true, daysLeft = -1, urgency = Urgency.EXPIRED),
        cell("soon-a", have = true, daysLeft = 1, urgency = Urgency.SOON),
        cell("absent", have = false, daysLeft = null, urgency = null),
        cell("today", have = true, daysLeft = 0, urgency = Urgency.SOON),
    )

    @Test
    fun soonTab_listsOnlyHaveWithNonOkUrgency_sortedByDaysLeftThenId() {
        val result = pantryGridPresentation(cells, StorageTab.SOON, presentOnly = false)

        assertEquals(listOf("expired", "today", "soon-a", "soon-b"), result.visibleCells.map { it.ingredientId })
    }

    @Test
    fun soonTab_neverListsItemsNotInPantry_evenIfUrgencyIsSet() {
        val stale = listOf(cell("gone", have = false, daysLeft = 1, urgency = Urgency.SOON))

        assertEquals(emptyList(), pantryGridPresentation(stale, StorageTab.SOON, presentOnly = false).visibleCells)
        assertEquals(0, soonCount(stale))
    }

    @Test
    fun soonTab_putsOverdueFirst() {
        val result = pantryGridPresentation(cells, StorageTab.SOON, presentOnly = false)

        assertEquals("expired", result.visibleCells.first().ingredientId)
    }

    @Test
    fun soonTab_ignoresPresentOnlyFlag() {
        val withFlag = pantryGridPresentation(cells, StorageTab.SOON, presentOnly = true)
        val without = pantryGridPresentation(cells, StorageTab.SOON, presentOnly = false)

        assertEquals(without.visibleCells, withFlag.visibleCells)
    }

    @Test
    fun soonTab_emptyWhenNothingUrgent() {
        val calm = listOf(cell("ok", have = true, daysLeft = 9, urgency = Urgency.OK))

        assertEquals(emptyList(), pantryGridPresentation(calm, StorageTab.SOON, presentOnly = false).visibleCells)
        assertEquals(0, soonCount(calm))
    }

    @Test
    fun emptyState_soonTabWithNothingVisible_isNoSoonItems() {
        assertEquals(
            PantryGridEmptyState.NO_SOON_ITEMS,
            pantryGridEmptyState(25, 0, PantryGridFilters(StorageTab.SOON, presentOnly = false)),
        )
        assertEquals(
            PantryGridEmptyState.NO_SOON_ITEMS,
            pantryGridEmptyState(25, 0, PantryGridFilters(StorageTab.SOON, presentOnly = true)),
        )
    }

    @Test
    fun soonCount_matchesExpiringListRule() {
        val ingredients = listOf(
            Ingredient("rau-muong", "Rau muống", "rau-la", 2),
            Ingredient("thit-heo", "Thịt heo", "thit", 3),
            Ingredient("gao", "Gạo", "khac", 90),
            Ingredient("sua-chua", "Sữa chua", "sua-trung", 5),
        )
        val pantry = listOf(
            PantryItem("rau-muong", have = true, markedAt = LocalDate.parse("2026-11-12")),
            PantryItem("thit-heo", have = true, markedAt = LocalDate.parse("2026-11-08")),
            PantryItem("gao", have = true, markedAt = LocalDate.parse("2026-11-01")),
            PantryItem("sua-chua", have = false, markedAt = LocalDate.parse("2026-11-01")),
        )
        val state = PantryGridState(today = today, ingredients = ingredients, pantry = pantry.associateBy { it.ingredientId })

        assertEquals(expiringList(pantry, ingredients, today).size, soonCount(cellsOf(state)))
        assertEquals(2, soonCount(cellsOf(state)))
    }

    private fun cell(id: String, have: Boolean, daysLeft: Int?, urgency: Urgency?) = PantryCell(
        ingredientId = id,
        nameVi = id,
        have = have,
        quantity = 1,
        unit = "phần",
        daysLeft = daysLeft,
        urgency = urgency,
    )
}
