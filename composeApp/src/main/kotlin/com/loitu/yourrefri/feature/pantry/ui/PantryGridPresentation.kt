package com.loitu.yourrefri.feature.pantry.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
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
        StorageTab.FRIDGE -> catalogCells.filter { it.compartment == PantryCompartment.CHILL }
        StorageTab.FREEZER -> catalogCells.filter { it.compartment == PantryCompartment.FREEZE }
    }
    val visibleCells = if (presentOnly) locationCells.filter { it.have } else locationCells

    return PantryGridPresentation(
        presentCount = presentCount,
        visibleCells = visibleCells,
    )
}

// Cùng quy tắc với expiringList(): món đang có và urgency khác OK; quá hạn ước tính đứng đầu.
private fun soonCells(cells: List<PantryCell>): List<PantryCell> =
    cells.filter {
        it.have && it.compartment != PantryCompartment.FREEZE &&
            it.urgency != null && it.urgency != Urgency.OK && it.daysLeft != null
    }
        .sortedWith(compareBy<PantryCell> { it.daysLeft }.thenBy { it.ingredientId })

internal fun soonCount(cells: List<PantryCell>): Int = soonCells(cells).size

internal fun compartmentChangeMessage(target: String, oldDays: Int, newDays: Int): String =
    "Đã chuyển sang $target (hạn ước tính: $oldDays ngày → $newDays ngày)"
