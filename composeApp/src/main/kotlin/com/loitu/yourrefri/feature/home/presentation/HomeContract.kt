package com.loitu.yourrefri.feature.home.presentation

import com.loitu.yourrefri.feature.home.domain.model.HomeDashboardData
import com.loitu.yourrefri.feature.home.domain.model.HomeWeather
import com.loitu.yourrefri.feature.home.domain.model.WeatherCity
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.recipe.logic.RandomRecipePick

data class HomeUiState(
    val dashboard: HomeDashboardData = HomeDashboardData(),
    val meal: MealSlot = MealSlot.DINNER,
    val randomPick: RandomRecipePick? = null,
    val randomOnlyReady: Boolean = true,
    val showRandomWheel: Boolean = false,
    val showNoCookableDialog: Boolean = false,
    val weatherCity: WeatherCity = WeatherCity.HANOI,
    val weather: HomeWeather? = null,
    val weatherLoading: Boolean = false,
)

sealed interface HomeIntent {
    data class SyncInputs(
        val pantryState: PantryGridState,
        val recipes: List<Recipe>,
        val meal: MealSlot,
        val tasteProfile: FamilyTasteProfile,
    ) : HomeIntent
    data object OpenRandomWheel : HomeIntent
    data object SpinRandomAgain : HomeIntent
    data class SetRandomOnlyReady(val onlyReady: Boolean) : HomeIntent
    data object DismissRandomWheel : HomeIntent
    data object DismissNoCookableDialog : HomeIntent
    data class SelectRandomRecipe(val recipeId: String) : HomeIntent
    data object LoadWeather : HomeIntent
    data object ToggleWeatherCity : HomeIntent
}

sealed interface HomeEffect {
    data class OpenRecipeDetail(val recipeId: String) : HomeEffect
}
