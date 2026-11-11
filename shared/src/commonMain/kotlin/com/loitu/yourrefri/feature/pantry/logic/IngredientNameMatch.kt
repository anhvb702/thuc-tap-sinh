package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient

fun findIngredientByName(ingredients: List<Ingredient>, input: String): Ingredient? {
    val name = input.trim()
    if (name.isEmpty()) return null
    ingredients.firstOrNull { it.nameVi.equals(name, ignoreCase = true) }?.let { return it }
    val partialMatches = ingredients.filter {
        it.nameVi.contains(name, ignoreCase = true) || name.contains(it.nameVi, ignoreCase = true)
    }
    return partialMatches.singleOrNull()
}
