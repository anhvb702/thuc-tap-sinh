package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe

internal data class DirectCookPlan(
    val ingredientIds: List<String>,
    val resultMessage: String,
)

internal fun deduplicateCookIngredientIds(ingredientIds: List<String>): List<String> = ingredientIds.distinct()

internal fun buildDirectCookPlan(
    recipe: Recipe,
    state: PantryGridState,
    callbackAvailable: Boolean,
): DirectCookPlan? {
    if (!callbackAvailable) return null

    val presentation = recipeCookPresentation(recipe, state, callbackAvailable = true)
    val uniqueIds = deduplicateCookIngredientIds(presentation.ingredientIds)
    val namesById = state.ingredients.associate { it.id to it.nameVi }
    val names = uniqueIds.mapNotNull { namesById[it] }
    return DirectCookPlan(ingredientIds = uniqueIds, resultMessage = cookResultMessage(names))
}
