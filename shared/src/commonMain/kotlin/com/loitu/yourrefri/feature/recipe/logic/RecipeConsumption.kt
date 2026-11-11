package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.recipe.data.Recipe

fun ingredientIdsToConsume(
    recipe: Recipe,
    availableIngredientIds: Set<String>,
    preferredIngredientIds: Set<String> = emptySet(),
): List<String> = recipe.required.mapNotNull { slot ->
    slot.anyOf.firstOrNull { it in availableIngredientIds && it in preferredIngredientIds }
        ?: slot.anyOf.firstOrNull { it in availableIngredientIds }
}.distinct()
