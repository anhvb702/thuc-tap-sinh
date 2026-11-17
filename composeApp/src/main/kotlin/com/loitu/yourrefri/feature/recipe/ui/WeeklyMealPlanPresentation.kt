package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.MealSlotKey
import com.loitu.yourrefri.feature.recipe.logic.PlannedDayMeal
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

internal const val EMPTY_MEAL_DAY_LABEL = "Nghỉ bếp / Ăn ngoài"

internal fun enabledMealSlots(preset: WeeklyMealPreset, dayIndex: Int): Set<MealSlot> = when (preset) {
    WeeklyMealPreset.ONE_MEAL -> setOf(MealSlot.DINNER)
    WeeklyMealPreset.TWO_MEALS -> setOf(MealSlot.LUNCH, MealSlot.DINNER)
    WeeklyMealPreset.THREE_MEALS -> MealSlot.entries.toSet()
    WeeklyMealPreset.WORKWEEK -> if (dayIndex < 5) setOf(MealSlot.DINNER) else setOf(MealSlot.LUNCH, MealSlot.DINNER)
}

internal fun enabledMealsForDay(
    schedule: List<PlannedDayMeal>, dayIndex: Int, weekStart: LocalDate, disabledMeals: List<MealSlotKey>,
): List<PlannedDayMeal> = schedule.filter { meal ->
    meal.dayIndex == dayIndex && MealSlotKey(weekStart + DatePeriod(days = dayIndex), meal.mealSlot) !in disabledMeals
}

internal data class WeeklyChecklistItem(val name: String, val compartment: PantryCompartment, val selected: Boolean)

internal fun weeklyShoppingAisles(items: List<com.loitu.yourrefri.feature.recipe.logic.ShoppingListItem>): List<Pair<String, List<com.loitu.yourrefri.feature.recipe.logic.ShoppingListItem>>> =
    listOf(
        "Rau củ & Quả" to setOf("rau-la", "rau-cu", "trai-cay"),
        "Thịt & Hải sản" to setOf("thit", "ca"),
        "Trứng, Đậu & Đồ khô" to setOf("sua-trung", "khac"),
    ).mapNotNull { (title, groups) -> title to items.filter { it.ingredient.group in groups }.takeIf { it.isNotEmpty() } }
        .mapNotNull { (title, groupItems) -> groupItems?.let { title to it } }

internal fun suggestedWeeklyCompartment(item: com.loitu.yourrefri.feature.recipe.logic.ShoppingListItem): PantryCompartment =
    if (item.ingredient.group in setOf("thit", "ca") && item.usedDayIndexes.any { it >= 3 }) PantryCompartment.FREEZE
    else PantryCompartment.CHILL

internal fun wasCookedOnDay(cooked: Map<Int, Set<String>>, dayIndex: Int, recipeId: String): Boolean =
    recipeId in cooked[dayIndex].orEmpty()

internal fun weeklyStockCompartments(
    ids: List<String>, compartments: Map<String, PantryCompartment>,
): List<Pair<String, PantryCompartment>> = ids.map { it to (compartments[it] ?: PantryCompartment.CHILL) }

internal fun weeklyChecklistText(items: List<WeeklyChecklistItem>): String =
    listOf(PantryCompartment.CHILL to "Ngăn mát", PantryCompartment.FREEZE to "Ngăn đông")
        .mapNotNull { (compartment, title) ->
            val names = items.filter { it.selected && it.compartment == compartment }.map { "☐ ${it.name}" }
            if (names.isEmpty()) null else (listOf(title) + names).joinToString("\n")
        }.joinToString("\n\n")

// SHOPPING là tab "Đi chợ" (chỉ phần danh sách đi chợ của thực đơn tuần).
enum class RecipeScreenMode { DAILY, WEEKLY, SHOPPING }

// Nút vào thực đơn tuần ở Trang chủ.
internal const val WEEKLY_BUTTON_LABEL = "Lên thực đơn cả tuần"

internal const val WEEKLY_DIET_GOAL_LABEL = "Định hướng"
internal const val WEEKLY_FAMILY_PREFERENCE_LABEL = "Khẩu vị"

// Người dùng bỏ tính năng nấu thêm phần cơm trưa hôm sau ngày 2026-11-12.
internal const val LUNCH_MEAL_PREP_ENABLED = false

// Giữ các chuỗi và nhánh cũ vì contract SW-02 đã khóa chúng.
internal fun lunchPrepSummaryText(affectedIngredientCount: Int): String =
    "Danh sách đi chợ đã tính thêm phần cơm trưa cho $affectedIngredientCount nguyên liệu."

internal fun lunchPrepDayLabel(): String = "🍱 + cơm trưa"

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
