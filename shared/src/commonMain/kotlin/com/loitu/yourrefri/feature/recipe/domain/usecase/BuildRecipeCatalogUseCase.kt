package com.loitu.yourrefri.feature.recipe.domain.usecase

import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.domain.model.RecipeCatalogData
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.recipe.logic.cookableRecipes
import com.loitu.yourrefri.feature.recipe.logic.filterRecipesByDurationInclusive
import com.loitu.yourrefri.feature.recipe.logic.recommendRecipes

class BuildRecipeCatalogUseCase {
    operator fun invoke(
        recipes: List<Recipe>,
        randomPool: List<Recipe>?,
        state: PantryGridState,
        tasteProfile: FamilyTasteProfile = FamilyTasteProfile(),
        maxDurationMinutes: Int? = null,
    ): RecipeCatalogData {
        val cookable = cookableRecipes(randomPool.orEmpty(), state)
        val recs = recommendRecipes(recipes, state, tasteProfile = tasteProfile)
        val visible = filterRecipesByDurationInclusive(recs, maxDurationMinutes) { it.recipe.minutes }
        return RecipeCatalogData(cookable, recs, visible)
    }
}
