package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState

internal fun quickAddConfirmActions(
    state: PantryGridState,
    selectedIds: Set<String>,
    query: String,
    days: Int?,
): List<PantryGridAction> {
    val normalized = normalizeFoodQuery(query)
    if (selectedIds.isEmpty() && normalized.isNotEmpty()) {
        val match = state.ingredients.filter { matchesFoodQuery(it.nameVi, normalized) }.singleOrNull()
        if (match != null && state.pantry[match.id]?.have == true) {
            return listOf(PantryGridAction.IncreaseQuantity(match.id))
        }
    }
    return quickAddIdsToSave(state, selectedIds, query).map { PantryGridAction.AddIngredient(it, days) }
}

internal fun quickAddConfirmLabelFor(selectedCount: Int, increasesExisting: Boolean): String =
    if (increasesExisting) "Thêm 1 nữa vào tủ" else quickAddConfirmLabel(selectedCount)
