package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class WeeklyMealPlannerTest {
    private val catalog = listOf(
        Ingredient("pork", "Thịt heo", "thit", 7),
        Ingredient("rau-muong", "Rau muống", "rau", 3),
        Ingredient("rice", "Gạo", "khac", 30),
    )
    private val state = PantryGridState(
        today = LocalDate(2026, 11, 9), ingredients = catalog,
        pantry = mapOf("rau-muong" to PantryItem("rau-muong", true, LocalDate(2026, 11, 9))),
    )
    private fun recipe(id: String, ingredient: String, minutes: Int = 40) = Recipe(
        id, id, listOf("Nấu"), listOf(Slot(listOf(ingredient))), minutes = minutes,
    )
    private val recipes = listOf(recipe("pork", "pork"), recipe("green", "rau-muong"), recipe("rice", "rice"))

    @Test fun buildsSevenDaysWithoutAdjacentRepeats() {
        val plan = buildWeeklyMealPlan(recipes, state)
        assertEquals(listOf("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"), plan.map { it.dayLabelVi })
        assertEquals(7, plan.size)
        assertEquals(true, plan.zipWithNext().all { (a, b) -> a.recipe.id != b.recipe.id })
    }

    @Test fun swapOffsetSelectsNextEligibleRecipe() {
        val plan = buildWeeklyMealPlan(recipes, state, swappedOffsets = mapOf(0 to 1))
        assertEquals("pork", plan.first().recipe.id)
    }

    @Test fun quickGoalPrefersAtMostTwentyMinutes() {
        val plan = buildWeeklyMealPlan(listOf(recipe("slow", "pork"), recipe("quick", "rice", 20)), state, DietGoal.QUICK)
        assertEquals("quick", plan.first().recipe.id)
    }

    @Test fun swappingOneDayKeepsLaterDaysStable() {
        val many = listOf("a", "b", "c", "d", "e").map { recipe(it, "rice") }
        val base = buildWeeklyMealPlan(many, state)
        val swapped = buildWeeklyMealPlan(many, state, swappedOffsets = mapOf(0 to 1))
        assertEquals(true, swapped[0].recipe.id != base[0].recipe.id)
        assertEquals(base.drop(2).map { it.recipe.id }, swapped.drop(2).map { it.recipe.id })
        assertEquals(true, swapped.zipWithNext().all { (a, b) -> a.recipe.id != b.recipe.id })
    }

    @Test fun swappingMidWeekDayLeavesOtherDaysUntouched() {
        val many = listOf("a", "b", "c", "d", "e", "f").map { recipe(it, "rice") }
        val base = buildWeeklyMealPlan(many, state).map { it.recipe.id }
        val swapped = buildWeeklyMealPlan(many, state, swappedOffsets = mapOf(3 to 2)).map { it.recipe.id }
        assertEquals(base.take(3), swapped.take(3))
        assertEquals(true, swapped[3] != base[3])
        assertEquals(base.drop(5), swapped.drop(5))
    }

    @Test fun earlyDaysPreferUrgentPantryIngredients() {
        val urgentState = state.copy(pantry = state.pantry + ("pork" to PantryItem("pork", true, LocalDate(2026, 11, 8))))
        assertEquals("pork", buildWeeklyMealPlan(recipes, urgentState).first().recipe.id)
    }

    @Test fun shoppingUsesAvailableAlternativeAndCountsMeals() {
        val dish = Recipe("dish", "Dish", listOf("Nấu"), listOf(Slot(listOf("pork", "rau-muong"))))
        val plan = listOf(PlannedDayMeal(0, "Thứ 2", dish, ""), PlannedDayMeal(1, "Thứ 3", dish, ""))
        val item = buildWeeklyShoppingList(plan, state).single()
        assertEquals("rau-muong", item.ingredient.id)
        assertEquals(2, item.mealCount)
        assertEquals(true, item.alreadyInPantry)
    }

    @Test fun shoppingSortsMissingBeforePantryAndStockFiltersUnchecked() {
        val plan = listOf(PlannedDayMeal(0, "Thứ 2", recipe("dish", "rice"), ""), PlannedDayMeal(1, "Thứ 3", recipe("dish2", "rau-muong"), ""))
        val items = buildWeeklyShoppingList(plan, state)
        assertEquals(listOf("rice", "rau-muong"), items.map { it.ingredient.id })
        assertEquals(listOf("rice"), shoppingIdsToStock(items, uncheckedIds = setOf("pork")))
    }
}
