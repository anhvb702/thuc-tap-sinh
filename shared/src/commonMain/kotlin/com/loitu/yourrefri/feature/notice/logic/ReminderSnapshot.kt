package com.loitu.yourrefri.feature.notice.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.LeftoverItem
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.recipe.data.MealSlotKey
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanEntry
import kotlinx.datetime.LocalDate

/** Ảnh chụp dữ liệu cần thiết để tính món cần rã đông. */
class ReminderSnapshot(
    private val weeklyPlan: List<WeeklyMealPlanEntry>,
    private val recipes: List<Recipe>,
    private val ingredients: List<Ingredient>,
    private val pantry: List<PantryItem>,
    private val leftovers: List<LeftoverItem> = emptyList(),
    private val disabledMeals: List<MealSlotKey> = emptyList(),
) {
    /** Trả về tên món cần rã đông trong ngày. */
    fun defrostNamesFor(date: LocalDate): List<String> = defrostNamesForDay(
        day = date,
        weeklyPlan = weeklyPlan,
        recipes = recipes,
        ingredients = ingredients,
        pantry = pantry,
        leftovers = leftovers,
        disabledMeals = disabledMeals,
    )

    /** Cho biết ngày đó có món tối cần chuẩn bị rã đông không. */
    fun hasFrozenDinnerFor(date: LocalDate): Boolean = defrostNamesFor(date).isNotEmpty()
}
