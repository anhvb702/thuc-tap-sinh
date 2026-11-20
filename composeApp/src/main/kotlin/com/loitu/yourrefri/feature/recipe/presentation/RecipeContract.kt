package com.loitu.yourrefri.feature.recipe.presentation

import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.domain.model.RecipeCatalogData
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.recipe.logic.RandomPick

data class RecipeUiState(
    val catalog: RecipeCatalogData = RecipeCatalogData(),
    val selectedDuration: Int? = null,
    val randomPick: RandomPick? = null,
    val showRandomWheel: Boolean = false,
    val showNoCookableDialog: Boolean = false,
)

sealed interface RecipeIntent {
    data class SyncInputs(
        val state: PantryGridState,
        val recipes: List<Recipe>,
        val randomPool: List<Recipe>?,
        val tasteProfile: FamilyTasteProfile,
    ) : RecipeIntent
    data class SelectDuration(val minutes: Int?) : RecipeIntent
    data object OpenRandomWheel : RecipeIntent
    data object SpinRandomAgain : RecipeIntent
    data object DismissRandomWheel : RecipeIntent
    data object DismissNoCookableDialog : RecipeIntent
    data class SelectRandomRecipe(val recipeId: String) : RecipeIntent
}

sealed interface RecipeEffect {
    data class OpenRecipeDetail(val recipeId: String) : RecipeEffect
}
