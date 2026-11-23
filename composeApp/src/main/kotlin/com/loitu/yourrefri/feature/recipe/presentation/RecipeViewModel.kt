package com.loitu.yourrefri.feature.recipe.presentation

import com.loitu.yourrefri.core.mvi.MviViewModel
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.domain.usecase.BuildRecipeCatalogUseCase
import com.loitu.yourrefri.feature.recipe.domain.usecase.PickRecipeRandomUseCase
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile

class RecipeViewModel(
    private val buildRecipeCatalog: BuildRecipeCatalogUseCase = BuildRecipeCatalogUseCase(),
    private val pickRecipeRandom: PickRecipeRandomUseCase = PickRecipeRandomUseCase(),
) : MviViewModel<RecipeUiState, RecipeIntent, RecipeEffect>(RecipeUiState()) {
    override fun onIntent(intent: RecipeIntent) {
        when (intent) {
            is RecipeIntent.SyncInputs -> syncInputs(intent)
            is RecipeIntent.SelectDuration -> {
                updateState { copy(selectedDuration = intent.minutes) }
                latestInputs?.let { inputs ->
                    updateState { copy(catalog = buildCatalog(inputs, intent.minutes)) }
                }
            }
            RecipeIntent.OpenRandomWheel, RecipeIntent.SpinRandomAgain -> openRandomWheel()
            RecipeIntent.DismissRandomWheel -> updateState { copy(showRandomWheel = false) }
            RecipeIntent.DismissNoCookableDialog -> updateState { copy(showNoCookableDialog = false) }
            is RecipeIntent.SelectRandomRecipe -> {
                updateState { copy(showRandomWheel = false) }
                sendEffect(RecipeEffect.OpenRecipeDetail(intent.recipeId))
            }
        }
    }

    private fun syncInputs(intent: RecipeIntent.SyncInputs) {
        latestInputs = Inputs(intent.state, intent.recipes, intent.randomPool, intent.tasteProfile)
        updateState { copy(catalog = buildCatalog(latestInputs!!, selectedDuration), showNoCookableDialog = false) }
    }

    private fun buildCatalog(inputs: Inputs, duration: Int?) = buildRecipeCatalog(
        recipes = inputs.recipes,
        randomPool = inputs.randomPool,
        state = inputs.state,
        tasteProfile = inputs.tasteProfile,
        maxDurationMinutes = duration,
    )

    private fun openRandomWheel() {
        val inputs = latestInputs ?: return
        val cookable = currentState.catalog.cookableRandomPool
        if (cookable.isEmpty()) {
            updateState { copy(showRandomWheel = false, showNoCookableDialog = true) }
            return
        }
        val pick = pickRecipeRandom(cookable, inputs.state, excludeId = currentState.randomPick?.recipe?.id)
        updateState { copy(randomPick = pick, showRandomWheel = pick != null, showNoCookableDialog = false) }
    }

    private data class Inputs(
        val state: PantryGridState,
        val recipes: List<Recipe>,
        val randomPool: List<Recipe>?,
        val tasteProfile: FamilyTasteProfile,
    )

    private var latestInputs: Inputs? = null
}
