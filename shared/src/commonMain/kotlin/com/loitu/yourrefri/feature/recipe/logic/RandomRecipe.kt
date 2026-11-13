package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import kotlin.random.Random

data class RandomPick(val recipe: Recipe, val canCookNow: Boolean)

/**
 * Rút một món ngẫu nhiên. Không lọc theo chế độ ăn: người gọi truyền danh sách công thức chưa lọc.
 * Ưu tiên món nấu được từ đồ đang có; hết món như vậy thì rút từ phần còn lại.
 */
fun pickRandomRecipe(
    recipes: List<Recipe>,
    state: PantryGridState,
    excludeId: String?,
    random: Random,
): RandomPick? {
    val candidates = recipes.filter { it.id != excludeId }
    if (candidates.isEmpty()) return null
    val pantry = state.pantry.values.toList()
    val cookable = candidates.filter { canCook(it, pantry) }
    return if (cookable.isNotEmpty()) {
        RandomPick(cookable.random(random), canCookNow = true)
    } else {
        RandomPick(candidates.random(random), canCookNow = false)
    }
}
