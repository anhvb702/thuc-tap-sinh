package com.loitu.yourrefri.feature.home.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class PantryPreviewTest {
    private val today = LocalDate(2026, 11, 7)
    private val ingredients = listOf(
        Ingredient("egg", "Trứng gà", "khac", 7),
        Ingredient("pork", "Thịt heo", "thit", 30),
        Ingredient("rice", "Gạo", "khac", 30),
        Ingredient("tomato", "Cà chua", "rau-cu", 7),
        Ingredient("garlic", "Tỏi", "rau-cu", 14),
    )

    @Test
    fun pantryPreviewNames_returnsPresentNamesInCatalogOrderAndCapsAtThree() {
        val state = PantryGridState(
            today = today,
            ingredients = ingredients,
            pantry = mapOf(
                "garlic" to PantryItem("garlic", true, today),
                "rice" to PantryItem("rice", true, today),
                "pork" to PantryItem("pork", true, today),
                "egg" to PantryItem("egg", true, today),
                "tomato" to PantryItem("tomato", false, today),
            ),
        )

        assertEquals(listOf("Trứng gà", "Thịt heo", "Gạo"), pantryPreviewNames(state))
    }

    @Test
    fun pantryPreviewNames_returnsEmptyWhenNoCatalogItemsArePresent() {
        val state = PantryGridState(
            today = today,
            ingredients = ingredients,
            pantry = mapOf("egg" to PantryItem("egg", false, today)),
        )

        assertEquals(emptyList(), pantryPreviewNames(state))
        assertEquals(emptyList(), pantryPreviewNames(state.copy(ingredients = emptyList(), pantry = emptyMap())))
    }

    @Test
    fun pantryPreviewNames_returnsEmptyWhenCatalogExistsButPantryHasNoRows() {
        val state = PantryGridState(today = today, ingredients = ingredients, pantry = emptyMap())

        assertEquals(emptyList(), pantryPreviewNames(state))
    }

    @Test
    fun pantryPreviewNames_ignoresPantryRowsWithoutCatalogItems() {
        val state = PantryGridState(
            today = today,
            ingredients = ingredients,
            pantry = mapOf("unknown" to PantryItem("unknown", true, today)),
        )

        assertEquals(emptyList(), pantryPreviewNames(state))
    }
}
