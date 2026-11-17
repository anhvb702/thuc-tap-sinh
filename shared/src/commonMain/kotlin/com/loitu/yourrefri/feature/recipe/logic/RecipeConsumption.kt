package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.IngredientKind
import com.loitu.yourrefri.feature.recipe.data.Recipe

fun ingredientIdsToConsume(
    recipe: Recipe,
    availableIngredientIds: Set<String>,
    preferredIngredientIds: Set<String> = emptySet(),
    ingredients: List<Ingredient> = emptyList(),
): List<String> {
    val seasoningIds = ingredients
        .filter { it.kind == IngredientKind.SEASONING }
        .mapTo(HashSet()) { it.id }
    return recipe.required.mapNotNull { slot ->
        val candidates = slot.anyOf.filter { it !in seasoningIds }
        candidates.firstOrNull { it in availableIngredientIds && it in preferredIngredientIds }
            ?: candidates.firstOrNull { it in availableIngredientIds }
    }.distinct()
}
