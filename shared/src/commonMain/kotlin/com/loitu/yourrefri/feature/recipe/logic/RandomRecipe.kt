package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import kotlin.random.Random

data class RandomRecipePick(val recipe: Recipe, val canCookNow: Boolean)

typealias RandomPick = RandomRecipePick

fun cookableRecipes(recipes: List<Recipe>, state: PantryGridState): List<Recipe> {
    val pantry = state.pantry.values.toList()
    return recipes.filter { canCook(it, pantry) }
}

/** Rút đều một món có thể nấu; món vừa chọn chỉ bị loại khi còn lựa chọn khác. */
fun pickRandomRecipe(
    recipes: List<Recipe>,
    state: PantryGridState,
    excludeId: String?,
    random: Random,
): RandomRecipePick? {
    val cookable = cookableRecipes(recipes, state)
    if (cookable.isEmpty()) return null
    val candidates = if (cookable.size >= 2) cookable.filter { it.id != excludeId } else cookable
    if (candidates.isEmpty()) return null
    val picked = candidates.random(random)
    return RandomRecipePick(picked, canCookNow = true)
}
