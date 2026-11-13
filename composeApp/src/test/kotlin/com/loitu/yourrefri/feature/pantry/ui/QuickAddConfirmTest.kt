package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuickAddConfirmTest {
    private val today = LocalDate(2026, 11, 12)
    private val state = PantryGridState(
        today = today,
        ingredients = listOf(
            Ingredient("egg", "Trứng gà", "sua-trung", 7),
            Ingredient("pork", "Thịt heo", "thit", 3),
            Ingredient("tomato", "Cà chua", "rau-cu", 7),
            Ingredient("milk", "Sữa tươi", "sua-trung", 5),
            Ingredient("milk_alt", "Sữa hạt", "sua-trung", 5),
        ),
        pantry = mapOf("egg" to PantryItem("egg", true, today)),
    )

    @Test fun uniquePresentMatch_increasesByOne() {
        assertEquals(listOf(PantryGridAction.IncreaseQuantity("egg")), quickAddConfirmActions(state, emptySet(), "Trứng gà", 3))
        assertEquals(listOf(PantryGridAction.IncreaseQuantity("egg")), quickAddConfirmActions(state, emptySet(), "trung ga", null))
    }

    @Test fun absentUniqueMatch_stillAdds() {
        assertEquals(listOf(PantryGridAction.AddIngredient("tomato", 3)), quickAddConfirmActions(state, emptySet(), "Cà chua", 3))
    }

    @Test fun selectedTiles_winOverTypedPresentName() {
        assertEquals(listOf(PantryGridAction.AddIngredient("pork", null)), quickAddConfirmActions(state, setOf("pork"), "Trứng gà", null))
    }

    @Test fun ambiguousOrBlankQuery_doesNothing() {
        assertEquals(emptyList(), quickAddConfirmActions(state, emptySet(), "Sữa", null))
        assertEquals(emptyList(), quickAddConfirmActions(state, emptySet(), "  ", null))
        assertEquals(emptyList(), quickAddConfirmActions(state, emptySet(), "Không có", null))
        val mixed = state.copy(pantry = state.pantry + ("milk" to PantryItem("milk", true, today)))
        assertEquals(emptyList(), quickAddConfirmActions(mixed, emptySet(), "Sữa", null))
    }

    @Test fun confirmLabel_saysOneMoreOnlyForExistingMatch() {
        assertEquals("Thêm 1 nữa vào tủ", quickAddConfirmLabelFor(0, true))
        assertEquals("Thêm vào tủ", quickAddConfirmLabelFor(0, false))
        assertEquals("Thêm 2 món vào tủ", quickAddConfirmLabelFor(2, false))
    }

    @Test fun hintKeepsAlreadyPresentPhrase() {
        assertTrue(AddFoodSheetCopy.alreadyPresentWithIncrease.contains("đã có trong tủ"))
    }

    @Test fun presentItemsStillNotOfferedAsTiles() {
        assertTrue(quickAddSuggestions(state).none { it.id == "egg" })
        assertTrue(quickAddSuggestions(state, "Trứng gà").isEmpty())
    }
}
