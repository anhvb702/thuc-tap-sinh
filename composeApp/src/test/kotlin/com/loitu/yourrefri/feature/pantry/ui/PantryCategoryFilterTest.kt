package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import kotlin.test.Test
import kotlin.test.assertEquals

class PantryCategoryFilterTest {
    private val cells = listOf(
        cell("rau", "Rau muống", group = "rau-la"),
        cell("com", "Cơm nguội", kind = "leftover"),
        cell("sua", "Sữa tươi", kind = "ready", group = "sua-trung"),
        cell("muoi", "Muối", group = "gia-vi"),
    )

    @Test
    fun pantryCategoryOf_mapsAllKindsAndGroups() {
        assertEquals(PantryCategory.FRESH, pantryCategoryOf(cells[0]))
        assertEquals(PantryCategory.LEFTOVER, pantryCategoryOf(cells[1]))
        assertEquals(PantryCategory.INSTANT_DRINK, pantryCategoryOf(cells[2]))
        assertEquals(PantryCategory.SPICE, pantryCategoryOf(cells[3]))
        assertEquals(PantryCategory.FRESH, pantryCategoryOf(cell("fresh", "Đậu phụ")))
    }

    @Test
    fun filterByCategory_allKeepsOrderAndEverySpecificCategoryKeepsMatchingCells() {
        assertEquals(cells, filterByCategory(cells, PantryCategory.ALL))
        assertEquals(listOf("rau"), filterByCategory(cells, PantryCategory.FRESH).ids())
        assertEquals(listOf("com"), filterByCategory(cells, PantryCategory.LEFTOVER).ids())
        assertEquals(listOf("sua"), filterByCategory(cells, PantryCategory.INSTANT_DRINK).ids())
        assertEquals(listOf("muoi"), filterByCategory(cells, PantryCategory.SPICE).ids())
    }

    private fun cell(
        id: String,
        name: String,
        kind: String = "fresh",
        group: String? = null,
    ) = PantryCell(id, name, have = true, quantity = 1, unit = "phần", daysLeft = null, urgency = null, kind = kind, group = group)

    private fun List<PantryCell>.ids() = map(PantryCell::ingredientId)
}
