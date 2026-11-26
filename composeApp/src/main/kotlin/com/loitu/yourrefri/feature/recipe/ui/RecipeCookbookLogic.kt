package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.ui.matchesFoodQuery
import com.loitu.yourrefri.feature.pantry.ui.normalizeFoodQuery
import com.loitu.yourrefri.feature.recipe.data.Recipe

internal const val MYSTERY_RECIPE_LABEL = "Món bí mật"
internal const val COOKBOOK_SCREEN_TITLE = "Món ăn"
internal const val COOKBOOK_WEEKLY_SHORTCUT_LABEL = "Thực đơn"
internal const val COOKBOOK_WEEKLY_SHORTCUT_DESC = "Mở thực đơn tuần"
internal const val RANDOM_WHEEL_READY_ONLY_LABEL = "Chỉ quay từ đồ đang có"

internal fun randomWheelPool(
    cookableRecipes: List<Recipe>,
    allRecipes: List<Recipe>,
    onlyReadyIngredients: Boolean,
): List<Recipe> = if (onlyReadyIngredients) cookableRecipes else allRecipes

internal enum class CookbookDurationGroup {
    QUICK,
    MEDIUM,
    LONG,
}

internal val ALL_COOKBOOK_DURATIONS: Set<CookbookDurationGroup> = CookbookDurationGroup.entries.toSet()

internal data class CookbookFilters(
    val canCookOnly: Boolean = false,
    val favoriteOnly: Boolean = false,
    val selectedDurations: Set<CookbookDurationGroup> = ALL_COOKBOOK_DURATIONS,
) {
    fun resetDurations() = copy(selectedDurations = ALL_COOKBOOK_DURATIONS)
    fun resetAll() = CookbookFilters()
}

internal fun toggleCookbookFavoriteOnly(filters: CookbookFilters): CookbookFilters =
    filters.copy(favoriteOnly = !filters.favoriteOnly)

internal fun selectCookbookDurationPreset(
    filters: CookbookFilters,
    group: CookbookDurationGroup?,
): CookbookFilters =
    if (group == null) filters.resetDurations() else filters.copy(selectedDurations = setOf(group))

internal fun activeCookbookDurationPreset(filters: CookbookFilters): CookbookDurationGroup? =
    filters.selectedDurations.singleOrNull()

internal fun cookbookDurationPresetLabel(group: CookbookDurationGroup?): String = when (group) {
    null -> "Tất cả thời gian"
    CookbookDurationGroup.QUICK -> "Nấu nhanh · ≤15 phút"
    CookbookDurationGroup.MEDIUM -> "Nấu vừa · 16–30 phút"
    CookbookDurationGroup.LONG -> "Nấu cầu kỳ · >30 phút"
}

internal fun cookbookDurationBadgeLabel(group: CookbookDurationGroup): String = when (group) {
    CookbookDurationGroup.QUICK -> "≤15p"
    CookbookDurationGroup.MEDIUM -> "16–30p"
    CookbookDurationGroup.LONG -> ">30p"
}

internal fun cookbookDurationOf(minutes: Int?): CookbookDurationGroup = when {
    minutes != null && minutes <= 15 -> CookbookDurationGroup.QUICK
    minutes != null && minutes <= 30 -> CookbookDurationGroup.MEDIUM
    else -> CookbookDurationGroup.LONG
}

internal fun toggleCookbookDuration(
    selected: Set<CookbookDurationGroup>,
    group: CookbookDurationGroup,
): Set<CookbookDurationGroup> = if (group in selected) selected - group else selected + group

internal fun Set<CookbookDurationGroup>.toDurationMask(): Int = fold(0) { mask, group ->
    mask or (1 shl group.ordinal)
}

internal fun Int.toCookbookDurations(): Set<CookbookDurationGroup> =
    CookbookDurationGroup.entries.filterTo(mutableSetOf()) { this and (1 shl it.ordinal) != 0 }

internal fun cookbookPresenceSwitchLabel(canCookOnly: Boolean): String =
    if (canCookOnly) "Đủ nguyên liệu nấu ngay" else "Tất cả món ăn"

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
    filters: CookbookFilters,
    query: String,
): List<CookbookEntry> {
    val unlocked = entries.filter { it.unlocked }
    if (filters.selectedDurations.isEmpty()) return emptyList()
    val filterByDuration = filters.selectedDurations.size < CookbookDurationGroup.entries.size
    val normalized = normalizeFoodQuery(query)
    return unlocked.filter { entry ->
        (!filters.canCookOnly || entry.canCookNow) &&
            (!filters.favoriteOnly || entry.recipe.id in favoriteIds) &&
            (!filterByDuration || cookbookDurationOf(entry.recipe.minutes) in filters.selectedDurations) &&
            (normalized.isEmpty() || matchesFoodQuery(entry.recipe.title, normalized))
    }.sortedBy { it.missingCount }
}
