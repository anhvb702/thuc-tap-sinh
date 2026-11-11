package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.logic.canCook
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class FoodIconsAndRecipeTest {
    @Test
    fun foodEmojiFor_returnsExpectedEmojiForKnownIngredientsAndFallback() {
        assertEquals("🥩", foodEmojiFor("thit-heo"))
        assertEquals("🥩", foodEmojiFor("thit-bo"))
        assertEquals("🍗", foodEmojiFor("thit-ga"))
        assertEquals("🐟", foodEmojiFor("ca-basa"))
        assertEquals("🦐", foodEmojiFor("tom"))
        assertEquals("🥬", foodEmojiFor("rau-muong"))
        assertEquals("🥚", foodEmojiFor("trung-ga"))
        assertEquals("🍄", foodEmojiFor("nam"))
        assertEquals("🥦", foodEmojiFor("unknown"))
    }

    @Test
    fun isFreezerItem_classifiesMeatFishAndShrimpAsFreezerItems() {
        listOf("thit-heo", "thit-bo", "thit-ga", "ca-basa", "tom").forEach { id ->
            assertTrue(isFreezerItem(id), "$id should be a freezer item")
        }
    }

    @Test
    fun isFreezerItem_classifiesVegetablesAsNonFreezerItems() {
        listOf("rau-muong", "ca-rot", "chuoi").forEach { id ->
            assertFalse(isFreezerItem(id), "$id should not be a freezer item")
        }
    }

    @Test
    fun canCook_meatKhoTrung_returnsTrueWhenBothRequiredIngredientsArePresent() {
        assertTrue(canCook(meatKhoTrung, listOf(pantryItem("thit-heo"), pantryItem("trung-ga"))))
    }

    @Test
    fun canCook_meatKhoTrung_returnsFalseWhenEitherRequiredIngredientIsMissing() {
        assertFalse(canCook(meatKhoTrung, listOf(pantryItem("thit-heo"))))
        assertFalse(canCook(meatKhoTrung, listOf(pantryItem("trung-ga"))))
    }

    @Test
    fun canCook_meatKhoTrung_returnsFalseWhenRequiredIngredientHasHaveFalse() {
        assertFalse(
            canCook(
                meatKhoTrung,
                listOf(pantryItem("thit-heo", have = false), pantryItem("trung-ga")),
            ),
        )
    }

    @Test
    fun cookingRecipe_identifiesUsedIngredientsToDeduct() {
        val pantry = mapOf(
            "thit-heo" to pantryItem("thit-heo", have = true),
            "trung-ga" to pantryItem("trung-ga", have = true),
            "ca-chua" to pantryItem("ca-chua", have = false),
        )
        val usedIngredients = meatKhoTrung.required.mapNotNull { slot ->
            slot.anyOf.firstOrNull { id -> pantry[id]?.have == true }
        }
        assertEquals(listOf("thit-heo", "trung-ga"), usedIngredients)
    }

    private fun pantryItem(id: String, have: Boolean = true) =
        PantryItem(ingredientId = id, have = have, markedAt = LocalDate(2026, 11, 6))

    private companion object {
        val meatKhoTrung = Recipe(
            id = "thit-kho-trung",
            title = "Thịt heo kho trứng",
            steps = listOf("Kho thịt và trứng"),
            required = listOf(Slot(listOf("thit-heo")), Slot(listOf("trung-ga"))),
        )
    }
}
