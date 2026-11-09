package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag

enum class RecipeScreenMode { DAILY, WEEKLY }

internal fun togglePreferenceTag(
    selected: Set<FamilyPreferenceTag>, tag: FamilyPreferenceTag,
): Set<FamilyPreferenceTag> = if (tag in selected) selected - tag else selected + tag

internal fun bumpDaySwapOffset(offsets: Map<Int, Int>, dayIndex: Int): Map<Int, Int> =
    offsets + (dayIndex to ((offsets[dayIndex] ?: 0) + 1))

internal fun shoppingSummaryText(toBuy: Int, inPantry: Int): String =
    "$toBuy món cần mua · $inPantry món đã có trong tủ"

internal fun stockActionButtonLabel(count: Int): String =
    "Đã mua xong ($count món) · Cất vào tủ lạnh 🧊"

internal fun stockResultMessage(count: Int): String =
    if (count == 0) "Danh sách đã được cập nhật." else "Đã cất $count món vào tủ lạnh."
