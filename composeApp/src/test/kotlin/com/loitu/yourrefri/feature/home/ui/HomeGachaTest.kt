package com.loitu.yourrefri.feature.home.ui

import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlinx.datetime.LocalDate
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HomeGachaTest {
    private val today = LocalDate(2026, 11, 13)
    private val recipe = Recipe("a", "Món A", listOf("Nấu"), listOf(Slot(listOf("a", "b")), Slot(listOf("c"))))

    @Test
    fun ingredientsChooseAvailableAlternativeAndOnlyMissingRequiredSlots() {
        val state = PantryGridState(today, pantry = mapOf("b" to PantryItem("b", true, today)))
        assertEquals(listOf(GachaIngredient("b", true), GachaIngredient("c", false)), gachaIngredients(recipe, state))
    }

    @Test
    fun emptyPantryChoosesFirstAlternativeForShopping() {
        assertEquals(listOf(GachaIngredient("a", false), GachaIngredient("c", false)), gachaIngredients(recipe, PantryGridState(today)))
    }

    @Test
    fun pickUsesOnlyProvidedRecipesAndSingleRecipeCanBeRerolled() {
        val state = PantryGridState(today)
        assertEquals("a", pickHomeGacha(listOf(recipe), state, "a", Random(1))?.recipe?.id)
        assertNull(pickHomeGacha(emptyList(), state, null, Random(1)))
    }

    @Test
    fun pickHomeGachaReturnsNullWhenFilteredByDietToEmpty_decisionQ2() {
        // Căn cứ quyết định Q2 (2026-11-16): Chế độ ăn là bộ lọc cứng ở mọi nơi gợi ý món.
        // Khi không còn món nào thỏa mãn bộ lọc, pickHomeGacha trả về null, tuyệt đối không lách bộ lọc.
        val state = PantryGridState(today)
        val filteredRecipes = emptyList<Recipe>()
        val pick = pickHomeGacha(filteredRecipes, state, null, Random(42))
        assertNull(pick)
    }

    @Test
    fun pickHomeGachaRespectsDietFilteredList_neverPicksOutsideDiet_decisionQ2() {
        // Căn cứ quyết định Q2 (2026-11-16): Chỉ rút món trong danh sách đã thỏa mãn chế độ ăn.
        val state = PantryGridState(today)
        val vegetarianRecipe = Recipe("veg", "Món Chay", listOf("Nấu"), listOf(Slot(listOf("dau-phu"))))
        val pick = pickHomeGacha(listOf(vegetarianRecipe), state, null, Random(42))
        assertEquals("veg", pick?.recipe?.id)
    }
}
