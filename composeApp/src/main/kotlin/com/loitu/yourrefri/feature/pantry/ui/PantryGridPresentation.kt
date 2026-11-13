package com.loitu.yourrefri.feature.pantry.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import com.loitu.yourrefri.feature.pantry.logic.Urgency

internal fun pantryGridContentPadding(): PaddingValues = PaddingValues(bottom = 88.dp)

internal data class PantryGridPresentation(
    val presentCount: Int,
    val visibleCells: List<PantryCell>,
)

internal fun pantryGridPresentation(
    catalogCells: List<PantryCell>,
    storageTab: StorageTab,
    presentOnly: Boolean,
): PantryGridPresentation {
    val presentCount = catalogCells.count { it.have }
    // Tab "Nên dùng sớm" tự chỉ gồm món đang có nên không cần cờ presentOnly.
    if (storageTab == StorageTab.SOON) {
        return PantryGridPresentation(presentCount, soonCells(catalogCells))
    }
    val locationCells = when (storageTab) {
        StorageTab.ALL, StorageTab.SOON -> catalogCells
        StorageTab.FRIDGE -> catalogCells.filterNot { isFreezerItem(it.ingredientId) }
        StorageTab.FREEZER -> catalogCells.filter { isFreezerItem(it.ingredientId) }
    }
    val visibleCells = if (presentOnly) locationCells.filter { it.have } else locationCells

    return PantryGridPresentation(
        presentCount = presentCount,
        visibleCells = visibleCells,
    )
}

// Cùng quy tắc với expiringList(): món đang có và urgency khác OK; quá hạn ước tính đứng đầu.
private fun soonCells(cells: List<PantryCell>): List<PantryCell> =
    cells.filter { it.have && it.urgency != null && it.urgency != Urgency.OK && it.daysLeft != null }
        .sortedWith(compareBy<PantryCell> { it.daysLeft }.thenBy { it.ingredientId })

internal fun soonCount(cells: List<PantryCell>): Int = soonCells(cells).size
