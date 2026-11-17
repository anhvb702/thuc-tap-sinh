package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlin.test.Test
import kotlin.test.assertEquals

class DailyShoppingTest {
    @Test fun mergePreservesChoicesAndAvoidsDuplicateIds() {
        val existing = DailyShoppingItem("a", "Cá", false, PantryCompartment.FREEZE)
        assertEquals(listOf(existing, DailyShoppingItem("b", "Rau")), mergeShoppingItems(listOf(existing), listOf(existing.copy(checked = true), DailyShoppingItem("b", "Rau"))))
    }

    @Test fun copyGroupsOnlySelectedItemsByCompartment() {
        val items = listOf(DailyShoppingItem("a", "Cá", true, PantryCompartment.FREEZE), DailyShoppingItem("b", "Rau"), DailyShoppingItem("c", "Trứng", false))
        assertEquals("Ngăn mát\n☐ Rau\n\nNgăn đông\n☐ Cá", shoppingChecklist(items))
    }

    @Test fun editPreservesIdentityAndOtherRecipeFields() {
        val recipe = Recipe("custom-1", "Cũ", listOf("Nấu"), listOf(Slot(listOf("a"))), minutes = 15)
        val edited = editLiteRecipe(recipe, " Mới ", listOf("b", "b", "c"))
        assertEquals(recipe.copy(title = "Mới", required = listOf(Slot(listOf("b")), Slot(listOf("c")))), edited)
    }
}
