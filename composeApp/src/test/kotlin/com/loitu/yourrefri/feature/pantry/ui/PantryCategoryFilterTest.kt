package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class PantryCategoryFilterTest {
    private val cells = listOf(
        cell("rau", "Rau muống", group = "rau-la"),
        cell("com", "Cơm nguội", kind = "leftover"),
        cell("sua", "Sữa tươi", kind = "ready", group = "sua-trung"),
        cell("muoi", "Muối", group = "gia-vi"),
    )

    @Test
    fun pantryCategoryOf_mapsAllKindsAndGroups() {
        assertEquals(
            listOf(PantryCategory.UNPROCESSED, PantryCategory.PROCESSED, PantryCategory.PROCESSED, PantryCategory.SPICE),
            cells.map(::pantryCategoryOf),
        )
        assertEquals(PantryCategory.UNPROCESSED, pantryCategoryOf(cell("fresh", "Đậu phụ")))
        assertEquals(PantryCategory.UNPROCESSED, pantryCategoryOf(cell("egg", "Trứng gà", group = "sua-trung")))
    }

    @Test
    fun pantryCategoryOf_recognizesLeftoversReadyFoodAndDrinks() {
        val processed = listOf(
            cell("1", "Món nhà nấu", group = "LEFTOVER"),
            cell("2", "Cá kho", group = "do-an-thua"),
            cell("3", "Mì ăn liền", kind = "ready"),
            cell("4", "Đồ ăn thừa"),
            cell("5", "Leftover"),
            cell("6", "Canh dư"),
            cell("7", "Thịt thừa"),
            cell("8", "Cơm nguội"),
            cell("9", "Nước ép", group = "sua-trung"),
            cell("10", "Sữa tươi", group = "sua-trung"),
            cell("11", "Trà", group = "sua-trung"),
            cell("12", "Cà phê", group = "sua-trung"),
            cell("13", "Nước", group = "sua-trung"),
            cell("14", "Canh", kind = "leftover"),
        )
        processed.forEach { assertEquals(PantryCategory.PROCESSED, pantryCategoryOf(it), it.nameVi) }
    }

    @Test
    fun pantryCategoryOf_recognizesSeasoningAndGivesProcessedItemsPriority() {
        val seasonings = listOf("muối", "đường", "tiêu", "nước mắm", "nước tương", "dầu ăn", "dầu hào", "bột ngọt")
        seasonings.forEach { assertEquals(PantryCategory.SPICE, pantryCategoryOf(cell(it, it))) }
        assertEquals(PantryCategory.SPICE, pantryCategoryOf(cell("mix", "Hỗn hợp", kind = "SEASONING")))
        assertEquals(PantryCategory.SPICE, pantryCategoryOf(cell("liquid", "Giấm", group = "gia-vi")))
        assertEquals(PantryCategory.PROCESSED, pantryCategoryOf(cell("ready", "Gà tiêu", kind = "ready")))
    }

    @Test
    fun togglePantryCategory_removesAnActiveChipAndAddsItBack() {
        val withoutSpice = setOf(PantryCategory.UNPROCESSED, PantryCategory.PROCESSED)
        assertEquals(withoutSpice, togglePantryCategory(ALL_PANTRY_CATEGORIES, PantryCategory.SPICE))
        assertEquals(ALL_PANTRY_CATEGORIES, togglePantryCategory(withoutSpice, PantryCategory.SPICE))
        assertEquals(emptySet(), togglePantryCategory(setOf(PantryCategory.SPICE), PantryCategory.SPICE))
        assertEquals(setOf(PantryCategory.SPICE), togglePantryCategory(emptySet(), PantryCategory.SPICE))
    }

    @Test
    fun filterByCategories_returnsTheUnionInCatalogOrder() {
        assertSame(cells, filterByCategories(cells, ALL_PANTRY_CATEGORIES))
        assertEquals(emptyList(), filterByCategories(cells, emptySet()))
        assertEquals(listOf("rau"), filterByCategories(cells, setOf(PantryCategory.UNPROCESSED)).ids())
        val selected = setOf(PantryCategory.SPICE, PantryCategory.PROCESSED)
        assertEquals(listOf("com", "sua", "muoi"), filterByCategories(cells, selected).ids())
    }

    private fun cell(
        id: String,
        name: String,
        kind: String = "fresh",
        group: String? = null,
    ) = PantryCell(id, name, have = true, quantity = 1, unit = "phần", daysLeft = null, urgency = null, kind = kind, group = group)

    private fun List<PantryCell>.ids() = map(PantryCell::ingredientId)
}
