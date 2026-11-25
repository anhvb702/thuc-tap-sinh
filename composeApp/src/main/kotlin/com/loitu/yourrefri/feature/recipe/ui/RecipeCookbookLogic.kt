package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.ui.matchesFoodQuery
import com.loitu.yourrefri.feature.pantry.ui.normalizeFoodQuery
import com.loitu.yourrefri.feature.recipe.data.Recipe

internal const val MYSTERY_RECIPE_LABEL = "Món bí mật"

internal enum class RecipeCookbookFilter(val labelVi: String) {
    ALL("Tất cả"),
    CAN_COOK("Đủ nguyên liệu"),
    FAVORITE("Yêu thích"),
    QUICK("Nhanh ≤ 15p"),
    MYSTERY(MYSTERY_RECIPE_LABEL),
}

internal data class CookbookEntry(
    val recipe: Recipe,
    val unlocked: Boolean,
    val canCookNow: Boolean,
    val missingCount: Int,
)

internal fun buildCookbookEntries(
    recipes: List<Recipe>,
    state: PantryGridState,
    unlockedIds: Set<String>,
): List<CookbookEntry> {
    val availableIds = state.pantry.values.filter { it.have }.mapTo(HashSet()) { it.ingredientId }
    return recipes.map { recipe ->
        val missingCount = recipe.required.count { slot -> slot.anyOf.none { it in availableIds } }
        CookbookEntry(recipe, !recipe.isMystery || recipe.id in unlockedIds, missingCount == 0, missingCount)
    }
}

internal fun filterCookbookEntries(
    entries: List<CookbookEntry>,
    favoriteIds: Set<String>,
    filter: RecipeCookbookFilter,
    query: String,
): List<CookbookEntry> {
    val filtered = when (filter) {
        RecipeCookbookFilter.ALL -> entries.sortedByDescending { it.unlocked }
        RecipeCookbookFilter.CAN_COOK -> entries.filter { it.unlocked && it.canCookNow }
        RecipeCookbookFilter.FAVORITE -> entries.filter { it.unlocked && it.recipe.id in favoriteIds }
        RecipeCookbookFilter.QUICK -> entries.filter { it.unlocked && (it.recipe.minutes ?: Int.MAX_VALUE) <= 15 }
        RecipeCookbookFilter.MYSTERY -> entries.filter { it.recipe.isMystery }.sortedBy { it.unlocked }
    }
    val normalized = normalizeFoodQuery(query)
    if (normalized.isEmpty()) return filtered
    return filtered.filter { entry ->
        val searchableTitle = if (entry.unlocked) entry.recipe.title else MYSTERY_RECIPE_LABEL
        matchesFoodQuery(searchableTitle, normalized)
    }
}
