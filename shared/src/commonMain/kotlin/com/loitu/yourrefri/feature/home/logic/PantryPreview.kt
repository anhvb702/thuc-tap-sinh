package com.loitu.yourrefri.feature.home.logic

import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.cellsOf

fun pantryPreviewNames(state: PantryGridState, limit: Int = 3): List<String> {
    require(limit >= 0)
    return cellsOf(state)
        .asSequence()
        .filter { it.have }
        .take(limit)
        .map { it.nameVi }
        .toList()
}
