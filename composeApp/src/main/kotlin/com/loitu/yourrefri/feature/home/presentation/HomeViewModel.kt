package com.loitu.yourrefri.feature.home.presentation

import com.loitu.yourrefri.core.mvi.MviViewModel
import com.loitu.yourrefri.feature.home.domain.usecase.BuildHomeDashboardUseCase
import com.loitu.yourrefri.feature.home.domain.usecase.PickHomeRandomRecipeUseCase

class HomeViewModel(
    private val buildHomeDashboard: BuildHomeDashboardUseCase = BuildHomeDashboardUseCase(),
    private val pickRandomRecipe: PickHomeRandomRecipeUseCase = PickHomeRandomRecipeUseCase(),
) : MviViewModel<HomeUiState, HomeIntent, HomeEffect>(HomeUiState()) {
    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.SyncInputs -> {
                latestPantryState = intent.pantryState
                updateState {
                copy(
                    dashboard = buildHomeDashboard(
                        state = intent.pantryState,
                        recipes = intent.recipes,
                        meal = intent.meal,
                        tasteProfile = intent.tasteProfile,
                    ),
                    meal = intent.meal,
                )
                }
            }
            HomeIntent.OpenRandomWheel, HomeIntent.SpinRandomAgain -> openRandomWheel()
            HomeIntent.DismissRandomWheel -> updateState { copy(showRandomWheel = false) }
            HomeIntent.DismissNoCookableDialog -> updateState { copy(showNoCookableDialog = false) }
            is HomeIntent.SelectRandomRecipe -> {
                updateState { copy(showRandomWheel = false) }
                sendEffect(HomeEffect.OpenRecipeDetail(intent.recipeId))
            }
        }
    }

    private fun openRandomWheel() {
        val cookable = currentState.dashboard.cookableRecipes
        if (cookable.isEmpty()) {
            updateState { copy(showRandomWheel = false, showNoCookableDialog = true) }
            return
        }
        val pick = pickRandomRecipe(
            cookable = cookable,
            state = latestPantryState ?: return,
            excludeId = currentState.randomPick?.recipe?.id,
        ) ?: return
        updateState { copy(randomPick = pick, showRandomWheel = true, showNoCookableDialog = false) }
    }

    private var latestPantryState: com.loitu.yourrefri.feature.pantry.logic.PantryGridState? = null
}
