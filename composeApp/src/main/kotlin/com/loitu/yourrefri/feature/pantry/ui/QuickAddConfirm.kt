package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.customFoodId

internal fun quickAddConfirmActions(
    state: PantryGridState,
    selectedIds: Set<String>,
    query: String,
    days: Int?,
): List<PantryGridAction> {
    val normalized = normalizeFoodQuery(query)
    if (selectedIds.isEmpty() && normalized.isNotEmpty()) {
        val matches = state.ingredients.filter { matchesFoodQuery(it.nameVi, normalized) }
        val match = matches.singleOrNull()
        if (match != null && state.pantry[match.id]?.have == true) {
            return listOf(PantryGridAction.IncreaseQuantity(match.id))
        }
        // Không khớp danh mục: thêm như món ngoài danh mục, nếu tên hợp lệ.
        if (matches.isEmpty()) {
            val customId = customFoodId(query) ?: return emptyList()
            if (state.pantry[customId]?.have == true) return listOf(PantryGridAction.IncreaseQuantity(customId))
            return listOf(PantryGridAction.AddIngredient(customId, days))
        }
    }
    return quickAddIdsToSave(state, selectedIds, query).map { PantryGridAction.AddIngredient(it, days) }
}

internal fun quickAddConfirmLabelFor(selectedCount: Int, increasesExisting: Boolean): String =
    if (increasesExisting) "Thêm 1 nữa vào tủ" else quickAddConfirmLabel(selectedCount)
