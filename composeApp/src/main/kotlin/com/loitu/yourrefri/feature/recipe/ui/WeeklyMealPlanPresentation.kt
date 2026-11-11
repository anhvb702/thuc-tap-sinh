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

internal fun dayMealCardHeader(dayLabel: String, mealType: String = "Bữa cơm chính"): String =
    "$dayLabel · $mealType"

internal fun shoppingItemSubtitle(
    mealCount: Int,
    availableQuantity: Int,
    missingQuantity: Int,
    isLunchPrep: Boolean = false,
): String = when {
    availableQuantity > 0 && isLunchPrep ->
        "Cần cho $mealCount phần (gồm cơm trưa) · trong tủ có $availableQuantity, mua thêm $missingQuantity"
    availableQuantity > 0 ->
        "Cần cho $mealCount bữa (trong tủ có $availableQuantity, mua thêm $missingQuantity)"
    isLunchPrep ->
        "Dùng cho $mealCount phần (có phần cơm trưa mang đi)"
    else ->
        "Dùng cho $mealCount bữa"
}
