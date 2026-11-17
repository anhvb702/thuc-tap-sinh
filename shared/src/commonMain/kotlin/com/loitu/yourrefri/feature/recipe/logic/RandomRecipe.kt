package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import kotlin.random.Random

data class RandomPick(val recipe: Recipe, val canCookNow: Boolean)

/**
 * Rút một món ngẫu nhiên từ danh sách người gọi truyền vào.
 * Căn cứ quyết định Q2 (2026-11-16): Chế độ ăn là bộ lọc cứng ở mọi nơi gợi ý món. Danh sách truyền vào
 * bắt buộc phải là danh sách đã lọc theo chế độ ăn (không lách bộ lọc). Khi không còn món thỏa mãn thì trả về null.
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
        val available = pantry.filter { it.have }.mapTo(HashSet()) { it.ingredientId }
        val fewestMissing = candidates.groupBy { recipe ->
            recipe.required.count { slot -> slot.anyOf.none { it in available } }
        }.minBy { it.key }.value
        RandomPick(fewestMissing.random(random), canCookNow = false)
    }
}
