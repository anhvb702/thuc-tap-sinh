package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddChoicePresentationTest {
    @Test fun labels() {
        assertEquals("Thêm nguyên liệu", addChoiceLabel(AddChoice.ADD_INGREDIENT))
        assertEquals("Khác / Thêm món", addChoiceLabel(AddChoice.ADD_OTHER))
    }

    @Test fun blankNameIsBlocked() {
        assertFalse(canAddOther("  ", emptySet()))
    }

    @Test fun duplicateIgnoresCaseAndSpaces() {
        assertFalse(canAddOther("Canh Chua", setOf("canh chua")))
        assertFalse(canAddOther("  canh chua ", setOf("Canh Chua")))
    }

    @Test fun newNameIsAllowed() {
        assertTrue(canAddOther("Bún chả", setOf("canh chua")))
    }

    @Test fun duplicateIgnoresInternalSpaces() {
        assertFalse(canAddOther("Canh  chua", setOf("Canh chua")))
        assertFalse(canAddOther("Canh chua", setOf("Canh   chua")))
    }

    private val today = LocalDate(2026, 11, 12)
    private val state = PantryGridState(
        today = today,
        ingredients = listOf(Ingredient("egg", "Trứng gà", "sua-trung", 7)),
        pantry = mapOf("egg" to PantryItem("egg", true, today)),
    )

    @Test fun ingredientPathRejectsUnmatchedName() {
        assertEquals(emptyList(), ingredientOnlyConfirmActions(state, emptySet(), "Bún chả", null))
    }

    @Test fun ingredientPathStillAddsCatalogItem() {
        assertEquals(
            listOf(PantryGridAction.IncreaseQuantity("egg")),
            ingredientOnlyConfirmActions(state, emptySet(), "Trứng gà", null),
        )
    }
}
