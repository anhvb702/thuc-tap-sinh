package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
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

// Hoàn tác "Nấu xong": đưa tủ về đúng trạng thái trước khi nấu.
// Món số lượng > 1 trước đó -> tăng lại 1; món số lượng 1 -> Tap để khôi phục dòng cũ (kể cả ngày đánh dấu).
// Món không có trong tủ trước khi nấu thì không bị đụng tới nên bỏ qua.
internal fun cookUndoActions(before: PantryGridState, ids: List<String>): List<PantryGridAction> =
    ids.distinct().mapNotNull { id ->
        if (before.pantry[id]?.have != true) return@mapNotNull null
        if ((before.quantities[id] ?: 1) > 1) PantryGridAction.IncreaseQuantity(id) else PantryGridAction.Tap(id)
    }

internal data class RecipeIngredientChip(val name: String, val have: Boolean)

// Mỗi nhóm nguyên liệu bắt buộc thành một chip: ưu tiên món đang có trong tủ, không thì món đầu tiên của nhóm.
internal fun recipeIngredientChips(recipe: Recipe, state: PantryGridState): List<RecipeIngredientChip> {
    val haveIds = state.pantry.values.filter { it.have }.mapTo(HashSet()) { it.ingredientId }
    val namesById = state.ingredients.associate { it.id to it.nameVi }
    return recipe.required.map { slot ->
        val owned = slot.anyOf.firstOrNull { it in haveIds }
        val id = owned ?: slot.anyOf.first()
        RecipeIngredientChip(namesById[id] ?: id, owned != null)
    }
}

internal fun missingChipSuffix(have: Boolean): String? = if (have) null else "chưa có"

internal fun stepTitle(index: Int): String = "Bước ${index + 1}"

internal fun cookDoneMessage(hasUpdates: Boolean): String =
    if (hasUpdates) "Đã cập nhật tủ. Chúc cả nhà ngon miệng" else "Chúc cả nhà ngon miệng"

internal fun recipeSegmentLabels(): List<String> = listOf("Hôm nay", "Cả tuần")
