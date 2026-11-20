package com.loitu.yourrefri.feature.home.domain.model

import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.RecipeRecommendation

data class HomeDashboardData(
    val greeting: String = "",
    val haveItems: List<PantryCell> = emptyList(),
    val urgentItems: List<PantryCell> = emptyList(),
    val recommendations: List<RecipeRecommendation> = emptyList(),
    val cookableRecipes: List<Recipe> = emptyList(),
)
