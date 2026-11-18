package com.loitu.yourrefri.feature.home.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.IngredientKind
import com.loitu.yourrefri.feature.pantry.data.LeftoverItem
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.ui.CookDoneCopy
import com.loitu.yourrefri.feature.recipe.ui.leftoverShelfDaysFor
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeDefrostAndLeftoverTest {
    private val today = LocalDate(2026, 11, 17)

    @Test
    fun defrostCard_showsOnlyWhenItemsNonEmpty_andFormatsSummary() {
        assertFalse(shouldShowDefrostCard(emptyList()))
        assertTrue(shouldShowDefrostCard(listOf("Cá hồi", "Thịt kho")))
        assertEquals(
            "Cho bữa tối hôm nay: Cá hồi, Thịt kho",
            defrostSummaryText(listOf("Cá hồi", "Thịt kho")),
        )
    }

    @Test
    fun urgentStrip_placesUrgentLeftoversFirst_andExcludesNonUrgentLeftovers() {
        val ingredients = listOf(
            Ingredient(
                id = "ca-chua",
                nameVi = "Cà chua",
                group = "Rau",
                defaultShelfDays = 2,
                kind = IngredientKind.FRESH,
            ),
        )
        val pantry = mapOf(
            "ca-chua" to PantryItem(
                ingredientId = "ca-chua",
                have = true,
                markedAt = LocalDate(2026, 11, 16), // daysLeft = 1
                compartment = PantryCompartment.CHILL,
            ),
        )
        val leftovers = listOf(
            LeftoverItem(
                id = "l-fresh-chill",
                title = "Canh chua",
                compartment = PantryCompartment.CHILL,
                markedAt = LocalDate(2026, 11, 17),
                shelfDays = 5, // daysLeft = 5 -> hidden
            ),
            LeftoverItem(
                id = "l-urgent-chill",
                title = "Thịt kho",
                compartment = PantryCompartment.CHILL,
                markedAt = LocalDate(2026, 11, 16),
                shelfDays = 2, // daysLeft = 1 -> urgent
            ),
            LeftoverItem(
                id = "l-frozen-ok",
                title = "Chả giò đông",
                compartment = PantryCompartment.FREEZE,
                markedAt = LocalDate(2026, 11, 10),
                shelfDays = 14, // daysLeft = 7 -> hidden
            ),
            LeftoverItem(
                id = "l-frozen-expired",
                title = "Cá kho cũ",
                compartment = PantryCompartment.FREEZE,
                markedAt = LocalDate(2026, 11, 1),
                shelfDays = 14, // daysLeft = -2 -> expired frozen, shown
            ),
        )
        val state = PantryGridState(today = today, ingredients = ingredients, pantry = pantry)

        val strip = buildUrgentStrip(state, leftovers)
        assertEquals(listOf("Cá kho cũ", "Thịt kho", "Cà chua"), strip.map { it.name })
        assertTrue(strip[0].isLeftover)
        assertEquals("Đã qua 2 ngày (ước tính)", strip[0].caption)
        assertTrue(strip[1].isLeftover)
        assertEquals("Còn 1 ngày (ước tính)", strip[1].caption)
        assertFalse(strip[2].isLeftover)
    }

    @Test
    fun urgentStrip_showsExpiredFrozenIngredientWithGentleMessage_hidesNonExpiredFrozen() {
        val ingredients = listOf(
            Ingredient(
                id = "thit-bo",
                nameVi = "Thịt bò",
                group = "Thịt",
                defaultShelfDays = 3,
                freezerShelfDays = 14,
            ),
            Ingredient(
                id = "ca-hoi",
                nameVi = "Cá hồi",
                group = "Cá",
                defaultShelfDays = 2,
                freezerShelfDays = 14,
            ),
        )
        val pantry = mapOf(
            "thit-bo" to PantryItem(
                ingredientId = "thit-bo",
                have = true,
                markedAt = LocalDate(2026, 11, 1), // 14 days -> -2 days left (expired frozen)
                compartment = PantryCompartment.FREEZE,
            ),
            "ca-hoi" to PantryItem(
                ingredientId = "ca-hoi",
                have = true,
                markedAt = LocalDate(2026, 11, 10), // 14 days -> 7 days left (hidden)
                compartment = PantryCompartment.FREEZE,
            ),
        )
        val state = PantryGridState(today = today, ingredients = ingredients, pantry = pantry)

        val strip = buildUrgentStrip(state, emptyList())
        assertEquals(1, strip.size)
        assertEquals("Thịt bò", strip[0].name)
        assertEquals("Đã qua 2 ngày (ước tính)", strip[0].caption)
    }

    @Test
    fun cookDoneLeftoverCopy_andShelfDaysMatchSpec() {
        assertEquals("Còn thừa, cất tủ", CookDoneCopy.saveLeftover)
        assertEquals("Đã cất vào Đồ ăn thừa", CookDoneCopy.savedLeftover)
        assertEquals(2, leftoverShelfDaysFor(PantryCompartment.CHILL))
        assertEquals(14, leftoverShelfDaysFor(PantryCompartment.FREEZE))
    }
}
