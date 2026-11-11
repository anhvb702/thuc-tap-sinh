package com.loitu.yourrefri.feature.pantry.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.logic.PantryCell

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
    val locationCells = when (storageTab) {
        StorageTab.ALL -> catalogCells
        StorageTab.FRIDGE -> catalogCells.filterNot { isFreezerItem(it.ingredientId) }
        StorageTab.FREEZER -> catalogCells.filter { isFreezerItem(it.ingredientId) }
    }
    val visibleCells = if (presentOnly) locationCells.filter { it.have } else locationCells

    return PantryGridPresentation(
        presentCount = catalogCells.count { it.have },
        visibleCells = visibleCells,
    )
}
