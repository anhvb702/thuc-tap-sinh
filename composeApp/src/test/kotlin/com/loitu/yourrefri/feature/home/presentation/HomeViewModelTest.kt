package com.loitu.yourrefri.feature.home.presentation

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HomeViewModelTest {
    private val today = LocalDate(2026, 1, 10)

    private fun recipe(id: String) = Recipe(
        id = id,
        title = id,
        steps = listOf("cook"),
        required = listOf(Slot(listOf("rice"))),
    )

    private fun state(withRice: Boolean = false) = PantryGridState(
        today = today,
        ingredients = listOf(Ingredient("rice", "Rice", "fresh", 5)),
        pantry = if (withRice) mapOf("rice" to PantryItem("rice", true, today)) else emptyMap(),
    )

    @Test
    fun syncInputs_computesDashboardState() {
        val viewModel = HomeViewModel()
        viewModel.onIntent(HomeIntent.SyncInputs(state(withRice = true), listOf(recipe("rice-recipe")), MealSlot.DINNER, com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile()))

        assertEquals(listOf("rice"), viewModel.state.value.dashboard.haveItems.map { it.ingredientId })
        assertEquals(listOf("rice-recipe"), viewModel.state.value.dashboard.cookableRecipes.map { it.id })
        assertEquals(MealSlot.DINNER, viewModel.state.value.meal)
    }

    @Test
    fun openRandomWheel_withoutCookableRecipes_showsNoCookableDialog() {
        val viewModel = HomeViewModel()
        viewModel.onIntent(HomeIntent.SyncInputs(state(), listOf(recipe("rice-recipe")), MealSlot.DINNER, com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile()))
        viewModel.onIntent(HomeIntent.OpenRandomWheel)

        assertTrue(viewModel.state.value.showNoCookableDialog)
        assertFalse(viewModel.state.value.showRandomWheel)
    }

    @Test
    fun openRandomWheel_withCookableRecipe_showsWheelAndPick() {
        val viewModel = HomeViewModel()
        viewModel.onIntent(HomeIntent.SyncInputs(state(withRice = true), listOf(recipe("rice-recipe")), MealSlot.DINNER, com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile()))
        viewModel.onIntent(HomeIntent.OpenRandomWheel)

        assertTrue(viewModel.state.value.showRandomWheel)
        assertNotNull(viewModel.state.value.randomPick)
        assertFalse(viewModel.state.value.showNoCookableDialog)
    }

    @Test
    fun selectRandomRecipe_closesWheelAndEmitsNavigationEffect() = runTest {
        val viewModel = HomeViewModel()
        viewModel.onIntent(HomeIntent.SyncInputs(state(withRice = true), listOf(recipe("rice-recipe")), MealSlot.DINNER, com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile()))
        viewModel.onIntent(HomeIntent.OpenRandomWheel)
        viewModel.onIntent(HomeIntent.SelectRandomRecipe("rice-recipe"))

        assertFalse(viewModel.state.value.showRandomWheel)
        assertEquals(HomeEffect.OpenRecipeDetail("rice-recipe"), viewModel.effects.first())
    }
}
