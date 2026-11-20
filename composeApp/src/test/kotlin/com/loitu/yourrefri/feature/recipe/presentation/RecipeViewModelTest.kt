package com.loitu.yourrefri.feature.recipe.presentation

import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
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

class RecipeViewModelTest {
    private val today = LocalDate(2026, 1, 10)
    private val state = PantryGridState(today = today, pantry = mapOf("rice" to PantryItem("rice", true, today)))
    private fun recipe(id: String, ingredient: String = "rice") = Recipe(id, id, listOf("cook"), listOf(Slot(listOf(ingredient))))

    @Test
    fun syncInputs_buildsCatalogAndDurationSelectionRebuildsVisibleRecipes() {
        val viewModel = RecipeViewModel()
        viewModel.onIntent(RecipeIntent.SyncInputs(state, listOf(recipe("quick"), recipe("missing", "egg")), listOf(recipe("ready")), com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile()))
        viewModel.onIntent(RecipeIntent.SelectDuration(15))

        assertEquals(listOf("ready"), viewModel.state.value.catalog.cookableRandomPool.map { it.id })
        assertEquals(15, viewModel.state.value.selectedDuration)
    }

    @Test
    fun openRandomWheel_withoutCookable_showsDialog() {
        val viewModel = RecipeViewModel()
        viewModel.onIntent(RecipeIntent.SyncInputs(state, emptyList(), listOf(recipe("missing", "egg")), com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile()))
        viewModel.onIntent(RecipeIntent.OpenRandomWheel)

        assertTrue(viewModel.state.value.showNoCookableDialog)
        assertFalse(viewModel.state.value.showRandomWheel)
    }

    @Test
    fun selectRandomRecipe_closesWheelAndEmitsEffect() = runTest {
        val viewModel = RecipeViewModel()
        viewModel.onIntent(RecipeIntent.SyncInputs(state, emptyList(), listOf(recipe("ready")), com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile()))
        viewModel.onIntent(RecipeIntent.OpenRandomWheel)
        assertNotNull(viewModel.state.value.randomPick)
        viewModel.onIntent(RecipeIntent.SelectRandomRecipe("ready"))

        assertFalse(viewModel.state.value.showRandomWheel)
        assertEquals(RecipeEffect.OpenRecipeDetail("ready"), viewModel.effects.first())
    }
}
