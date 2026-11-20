package com.loitu.yourrefri.feature.home.domain.usecase

import com.loitu.yourrefri.feature.home.domain.model.HomeDashboardData
import com.loitu.yourrefri.feature.home.logic.homeGreeting
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.recipe.logic.cookableRecipes
import com.loitu.yourrefri.feature.recipe.logic.recommendRecipes
import com.loitu.yourrefri.feature.recipe.logic.recipesForMeal

class BuildHomeDashboardUseCase {
    operator fun invoke(
        state: PantryGridState,
        recipes: List<Recipe>,
        meal: MealSlot,
        tasteProfile: FamilyTasteProfile = FamilyTasteProfile(),
        recipeLimit: Int = 3,
    ): HomeDashboardData {
        val haveItems = cellsOf(state).filter { it.have }
        return HomeDashboardData(
            greeting = homeGreeting(state.hourOfDay),
            haveItems = haveItems,
            urgentItems = haveItems.filter { it.daysLeft in 0..2 }.sortedBy { it.daysLeft },
            recommendations = recommendRecipes(
                recipesForMeal(recipes, meal),
                state,
                tasteProfile = tasteProfile,
            ).take(recipeLimit),
            cookableRecipes = cookableRecipes(recipes, state),
        )
    }
}
