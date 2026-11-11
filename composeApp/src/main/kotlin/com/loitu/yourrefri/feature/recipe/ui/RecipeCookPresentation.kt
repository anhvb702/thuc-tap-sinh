package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.ingredientIdsToConsume

internal data class RecipeCookPresentation(
    val ingredientIds: List<String>,
    val ingredientNames: List<String>,
    val confirmationMessage: String,
    val resultMessage: String?,
)

internal fun confirmedCookIngredientIds(
    presentation: RecipeCookPresentation,
    confirmed: Boolean,
    callbackAvailable: Boolean,
): List<String> = if (confirmed && callbackAvailable) presentation.ingredientIds else emptyList()

internal fun recipeCookPresentation(
    recipe: Recipe,
    state: PantryGridState,
    callbackAvailable: Boolean,
): RecipeCookPresentation {
    val cells = cellsOf(state)
    val availableIds = cells.filter { it.have }.mapTo(HashSet()) { it.ingredientId }
    val preferredIds = cells.filter { it.have && it.daysLeft in 0..2 }.mapTo(HashSet()) { it.ingredientId }
    val ingredientIds = ingredientIdsToConsume(recipe, availableIds, preferredIds)
    val namesById = state.ingredients.associate { it.id to it.nameVi }
    val ingredientNames = ingredientIds.mapNotNull(namesById::get)

    val confirmationMessage = when {
        !callbackAvailable -> "Chưa thể cập nhật số lượng từ màn hình này."
        ingredientNames.isEmpty() -> "Trong tủ chưa có nguyên liệu nào phù hợp để cập nhật."
        else -> "Mình sẽ giảm số lượng: ${ingredientNames.joinToString(", ")}."
    }
    val resultMessage = when {
        !callbackAvailable -> null
        ingredientNames.isEmpty() -> "Món đã nấu xong. Chưa có nguyên liệu nào cần cập nhật trong tủ."
        else -> "Đã cập nhật trong tủ: ${ingredientNames.joinToString(", ")}."
    }

    return RecipeCookPresentation(ingredientIds, ingredientNames, confirmationMessage, resultMessage)
}
