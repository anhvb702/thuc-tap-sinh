package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.*
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class MealToggleLogicTest {
    @Test fun disabledSlotExcludedFromShopping() {
        val day = LocalDate(2026, 11, 9)
        val state = PantryGridState(today = day, ingredients = listOf(Ingredient("egg", "Egg", "food", 3), Ingredient("rice", "Rice", "food", 30)))
        fun meal(id: String, slot: MealSlot) = PlannedDayMeal(0, "Monday", Recipe(id, id, listOf("Cook"), listOf(Slot(listOf(id)))), "", mealSlot = slot)
        val plan = listOf(meal("egg", MealSlot.LUNCH), meal("rice", MealSlot.DINNER))
        assertEquals(listOf("rice"), buildWeeklyShoppingList(plan, state, disabledMeals = listOf(MealSlotKey(day, MealSlot.LUNCH)), weekStart = day).map { it.ingredient.id })
    }
}
