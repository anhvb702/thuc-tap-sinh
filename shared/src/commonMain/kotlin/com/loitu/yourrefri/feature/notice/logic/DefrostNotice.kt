package com.loitu.yourrefri.feature.notice.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.LeftoverItem
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanEntry
import com.loitu.yourrefri.feature.recipe.logic.DietIngredients
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import kotlinx.datetime.LocalDate

data class DefrostNotice(
    val day: LocalDate,
    val ingredientIds: List<String>,
    val title: String,
    val body: String,
)

/** Trả về danh sách tên nguyên liệu hoặc đồ ăn thừa ngăn đông cần rã đông cho bữa tối ngày [day]. */
fun defrostNamesForDay(
    day: LocalDate,
    weeklyPlan: List<WeeklyMealPlanEntry>,
    recipes: List<Recipe>,
    ingredients: List<Ingredient>,
    pantry: List<PantryItem>,
    leftovers: List<LeftoverItem> = emptyList(),
): List<String> {
    val dinnerId = weeklyPlan.firstOrNull { it.day == day && it.meal == MealSlot.DINNER }?.recipeId
        ?: return emptyList()
    val recipe = recipes.firstOrNull { it.id == dinnerId }
    val ingredientNames = if (recipe != null) {
        val groupById = ingredients.associate { it.id to it.group }
        val requiredIds = recipe.required.flatMap { it.anyOf }.toSet()
        val namesById = ingredients.associate { it.id to it.nameVi }
        pantry.asSequence()
            .filter { it.have && it.compartment == PantryCompartment.FREEZE && it.ingredientId in requiredIds }
            .map { it.ingredientId }
            .filter { DietIngredients.isBanned(it, DietRestriction.VEGETARIAN, groupById) }
            .distinct()
            .mapNotNull { namesById[it] }
            .toList()
    } else {
        emptyList()
    }
    val leftoverNames = leftovers.asSequence()
        .filter { it.compartment == PantryCompartment.FREEZE }
        .filter { it.id == dinnerId || (recipe != null && it.title.trim().equals(recipe.title.trim(), ignoreCase = true)) }
        .map { it.title.trim() }
        .distinct()
        .toList()
    return (ingredientNames + leftoverNames).distinct()
}

fun planDefrostNotice(
    today: LocalDate,
    weeklyPlan: List<WeeklyMealPlanEntry>,
    recipes: List<Recipe>,
    ingredients: List<Ingredient>,
    pantry: List<PantryItem>,
    leftovers: List<LeftoverItem> = emptyList(),
): DefrostNotice? {
    val dinnerId = weeklyPlan.firstOrNull { it.day == today && it.meal == MealSlot.DINNER }?.recipeId ?: return null
    val recipe = recipes.firstOrNull { it.id == dinnerId }
    val frozenIds = if (recipe != null) {
        val groupById = ingredients.associate { it.id to it.group }
        val requiredIds = recipe.required.flatMap { it.anyOf }.toSet()
        pantry.asSequence()
            .filter { it.have && it.compartment == PantryCompartment.FREEZE && it.ingredientId in requiredIds }
            .map { it.ingredientId }
            .filter { DietIngredients.isBanned(it, DietRestriction.VEGETARIAN, groupById) }
            .distinct()
            .toList()
    } else {
        emptyList()
    }
    val names = defrostNamesForDay(today, weeklyPlan, recipes, ingredients, pantry, leftovers)
    if (names.isEmpty()) return null
    val summary = names.joinToString(", ")
    return DefrostNotice(
        day = today,
        ingredientIds = frozenIds,
        title = "Chuẩn bị rã đông",
        body = "$summary đang ở ngăn đông; mình có thể chuyển xuống ngăn mát để chuẩn bị cho bữa tối nay nhé.",
    )
}
