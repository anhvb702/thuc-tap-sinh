package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import kotlin.random.Random

data class RandomRecipePick(val recipe: Recipe, val canCookNow: Boolean)

typealias RandomPick = RandomRecipePick

/**
 * Rút một món ngẫu nhiên đồng đều. Không lọc theo chế độ ăn: người gọi truyền danh sách công thức chưa lọc.
 * `canCookNow` chỉ mô tả trạng thái của món đã rút, không ảnh hưởng xác suất.
 */
fun pickRandomRecipe(
    recipes: List<Recipe>,
    state: PantryGridState,
    excludeId: String?,
    random: Random,
): RandomRecipePick? {
    val candidates = if (recipes.size <= 1) recipes else recipes.filter { it.id != excludeId }
    if (candidates.isEmpty()) return null
    val pantry = state.pantry.values.toList()
    val picked = candidates.random(random)
    return RandomRecipePick(picked, canCookNow = canCook(picked, pantry))
}
