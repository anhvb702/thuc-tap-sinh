package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.recipe.data.Recipe

fun canCook(recipe: Recipe, pantry: List<PantryItem>): Boolean {
    val available = pantry.filter { it.have }.map { it.ingredientId }.toSet()
    return recipe.required.all { slot -> slot.anyOf.any { it in available } }
}
