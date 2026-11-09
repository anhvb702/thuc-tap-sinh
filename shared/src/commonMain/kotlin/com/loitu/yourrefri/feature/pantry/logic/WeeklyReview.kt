package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.PantryItem

fun weeklyReview(
    pantry: List<PantryItem>,
    changes: Map<String, Boolean> = emptyMap(),
): List<PantryItem> = pantry.map { item ->
    val have = changes[item.ingredientId]
    if (have == null) item else item.copy(have = have)
}
