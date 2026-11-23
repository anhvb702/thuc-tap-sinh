package com.loitu.yourrefri.feature.recipe.domain.model

import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.RecipeRecommendation

data class RecipeCatalogData(
    val cookableRandomPool: List<Recipe> = emptyList(),
    val recommendations: List<RecipeRecommendation> = emptyList(),
    val visibleRecipes: List<RecipeRecommendation> = emptyList(),
)
