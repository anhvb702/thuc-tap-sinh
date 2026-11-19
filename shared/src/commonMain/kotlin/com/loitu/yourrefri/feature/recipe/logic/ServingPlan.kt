package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.feature.recipe.data.Recipe

const val SERVINGS_PER_UNIT = 2
const val DEFAULT_DESIRED_SERVINGS = 2

data class ServingPlan(
    val desiredServings: Int,
    val maxServings: Int,
    val recommendedServings: Int,
    val isScaledDown: Boolean,
    val canCookAtAll: Boolean,
)

fun planServings(
    recipe: Recipe,
    state: PantryGridState,
    desiredServings: Int = DEFAULT_DESIRED_SERVINGS,
): ServingPlan {
    val cells = cellsOf(state)
    val maxServings = if (recipe.required.isEmpty()) {
        desiredServings
    } else {
        recipe.required.minOf { slot ->
            slot.anyOf.sumOf { id ->
                cells.firstOrNull { it.ingredientId == id && it.have }?.quantity?.coerceAtLeast(1) ?: 0
            } * SERVINGS_PER_UNIT
        }
    }
    val canCookAtAll = maxServings >= 1
    val recommendedServings = if (!canCookAtAll) 0 else desiredServings.coerceIn(1, maxServings)
    return ServingPlan(
        desiredServings = desiredServings,
        maxServings = maxServings,
        recommendedServings = recommendedServings,
        isScaledDown = canCookAtAll && recommendedServings < desiredServings,
        canCookAtAll = canCookAtAll,
    )
}

fun unitsToConsumeForServings(servings: Int): Int =
    ((servings.coerceAtLeast(1) + SERVINGS_PER_UNIT - 1) / SERVINGS_PER_UNIT).coerceAtLeast(1)

fun servingNoteVi(plan: ServingPlan): String = when {
    !plan.canCookAtAll -> "Cần thêm nguyên liệu chính để nấu món này"
    plan.isScaledDown -> "Nguyên liệu hiện đủ cho khoảng ${plan.recommendedServings} suất nhỏ (ước tính) — vẫn nấu gọn được nhé"
    else -> "Đủ cho khoảng ${plan.recommendedServings} người (ước tính)"
}
