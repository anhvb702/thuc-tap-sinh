package com.loitu.yourrefri.feature.notice.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
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

fun planDefrostNotice(
    today: LocalDate,
    weeklyPlan: List<WeeklyMealPlanEntry>,
    recipes: List<Recipe>,
    ingredients: List<Ingredient>,
    pantry: List<PantryItem>,
): DefrostNotice? {
    val dinnerId = weeklyPlan.firstOrNull { it.day == today && it.meal == MealSlot.DINNER }?.recipeId ?: return null
    val recipe = recipes.firstOrNull { it.id == dinnerId } ?: return null
    val groupById = ingredients.associate { it.id to it.group }
    val requiredIds = recipe.required.flatMap { it.anyOf }.toSet()
    val frozenIds = pantry.asSequence()
        .filter { it.have && it.compartment == PantryCompartment.FREEZE && it.ingredientId in requiredIds }
        .map { it.ingredientId }
        .filter { DietIngredients.isBanned(it, DietRestriction.VEGETARIAN, groupById) }
        .distinct()
        .toList()
    if (frozenIds.isEmpty()) return null
    val namesById = ingredients.associate { it.id to it.nameVi }
    val names = frozenIds.mapNotNull { namesById[it] }
    if (names.isEmpty()) return null
    val summary = names.joinToString(", ")
    return DefrostNotice(
        day = today,
        ingredientIds = frozenIds,
        title = "Chuẩn bị rã đông",
        body = "$summary đang ở ngăn đông; mình có thể chuyển xuống ngăn mát để chuẩn bị cho bữa tối nay nhé.",
    )
}
