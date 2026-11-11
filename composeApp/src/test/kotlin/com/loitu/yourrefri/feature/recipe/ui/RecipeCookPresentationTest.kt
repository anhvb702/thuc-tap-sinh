package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RecipeCookPresentationTest {
    private val today = LocalDate(2026, 11, 8)
    private val recipe = Recipe(
        id = "meal",
        title = "Món nhà mình",
        steps = listOf("Nấu"),
        required = listOf(Slot(listOf("pork", "tofu")), Slot(listOf("egg"))),
    )

    private val ingredients = listOf(
        Ingredient("pork", "Thịt heo", "thit", defaultShelfDays = 14),
        Ingredient("tofu", "Đậu phụ", "khac", defaultShelfDays = 1),
        Ingredient("egg", "Trứng gà", "sua-trung", defaultShelfDays = 21),
    )

    @Test
    fun planUsesTheSamePreferredUniqueIdsAndNamesForConfirmation() {
        val state = PantryGridState(
            today = today,
            ingredients = ingredients,
            pantry = ingredients.associate { it.id to PantryItem(it.id, true, today) },
        )

        val presentation = recipeCookPresentation(recipe, state, callbackAvailable = true)

        assertEquals(listOf("tofu", "egg"), presentation.ingredientIds)
        assertEquals(listOf("Đậu phụ", "Trứng gà"), presentation.ingredientNames)
        assertEquals("Mình sẽ giảm số lượng: Đậu phụ, Trứng gà.", presentation.confirmationMessage)
        assertEquals("Đã cập nhật trong tủ: Đậu phụ, Trứng gà.", presentation.resultMessage)
    }

    @Test
    fun emptyPantryDoesNotPromiseQuantityUpdates() {
        val presentation = recipeCookPresentation(
            recipe,
            PantryGridState(today = today, ingredients = ingredients),
            callbackAvailable = true,
        )

        assertEquals(emptyList<String>(), presentation.ingredientIds)
        assertEquals(emptyList<String>(), presentation.ingredientNames)
        assertEquals("Trong tủ chưa có nguyên liệu nào phù hợp để cập nhật.", presentation.confirmationMessage)
        assertEquals("Món đã nấu xong. Chưa có nguyên liệu nào cần cập nhật trong tủ.", presentation.resultMessage)
    }

    @Test
    fun unavailableCallbackNeverShowsAnUpdateSuccessMessage() {
        val state = PantryGridState(
            today = today,
            ingredients = ingredients,
            pantry = ingredients.associate { it.id to PantryItem(it.id, true, today) },
        )

        val presentation = recipeCookPresentation(recipe, state, callbackAvailable = false)

        assertNull(presentation.resultMessage)
    }

    @Test
    fun absentIngredientsAreNotIncludedEvenIfAStalePantryRowExists() {
        val state = PantryGridState(
            today = today,
            ingredients = ingredients,
            pantry = mapOf("pork" to PantryItem("pork", have = false, markedAt = today)),
        )

        val presentation = recipeCookPresentation(recipe, state, callbackAvailable = true)

        assertEquals(emptyList<String>(), presentation.ingredientIds)
        assertEquals(emptyList<String>(), presentation.ingredientNames)
    }

    @Test
    fun repeatedRequiredSlotDoesNotDuplicateAnIngredient() {
        val duplicateRecipe = recipe.copy(
            required = listOf(Slot(listOf("tofu")), Slot(listOf("tofu"))),
        )
        val state = PantryGridState(
            today = today,
            ingredients = ingredients,
            pantry = mapOf("tofu" to PantryItem("tofu", true, today)),
        )

        val presentation = recipeCookPresentation(duplicateRecipe, state, callbackAvailable = true)

        assertEquals(listOf("tofu"), presentation.ingredientIds)
        assertEquals(listOf("Đậu phụ"), presentation.ingredientNames)
    }

    @Test
    fun cookUpdateOnlyDispatchesAfterConfirmationWithAvailableCallbackAndItems() {
        val presentation = RecipeCookPresentation(
            ingredientIds = listOf("tofu"),
            ingredientNames = listOf("Đậu phụ"),
            confirmationMessage = "Mình sẽ giảm số lượng: Đậu phụ.",
            resultMessage = "Đã gửi yêu cầu cập nhật: Đậu phụ.",
        )

        assertEquals(emptyList<String>(), confirmedCookIngredientIds(presentation, confirmed = false, callbackAvailable = true))
        assertEquals(emptyList<String>(), confirmedCookIngredientIds(presentation, confirmed = true, callbackAvailable = false))
        assertEquals(listOf("tofu"), confirmedCookIngredientIds(presentation, confirmed = true, callbackAvailable = true))
        assertEquals(
            emptyList<String>(),
            confirmedCookIngredientIds(presentation.copy(ingredientIds = emptyList()), confirmed = true, callbackAvailable = true),
        )
    }
}
