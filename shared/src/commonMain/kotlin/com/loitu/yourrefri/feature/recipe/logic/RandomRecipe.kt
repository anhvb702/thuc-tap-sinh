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

/** Rút đều một món; khi [onlyCookable] bật chỉ rút trong các món đủ nguyên liệu, tắt thì rút trên toàn bộ danh sách. */
fun pickRandomRecipe(
    recipes: List<Recipe>,
    state: PantryGridState,
    excludeId: String?,
    random: Random,
    onlyCookable: Boolean = true,
): RandomRecipePick? {
    val pantry = state.pantry.values.toList()
    val pool = if (onlyCookable) recipes.filter { canCook(it, pantry) } else recipes
    if (pool.isEmpty()) return null
    val candidates = if (pool.size >= 2) pool.filter { it.id != excludeId } else pool
    if (candidates.isEmpty()) return null
    val picked = candidates.random(random)
    return RandomRecipePick(picked, canCookNow = onlyCookable || canCook(picked, pantry))
}
