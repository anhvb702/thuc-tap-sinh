package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.IngredientKind
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.recipe.data.Recipe

fun canCook(
    recipe: Recipe,
    pantry: List<PantryItem>,
    ingredients: List<Ingredient> = emptyList(),
): Boolean {
    val available = pantry.filter { it.have }.map { it.ingredientId }.toSet()
    val seasoningIds = ingredients
        .filter { it.kind == IngredientKind.SEASONING }
        .mapTo(HashSet()) { it.id }
    return recipe.required.all { slot ->
        slot.anyOf.any { it in available || it in seasoningIds }
    }
}
