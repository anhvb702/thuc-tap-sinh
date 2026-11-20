package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WeeklySeparateLunchDinnerTest {
    private val today = LocalDate(2026, 11, 20)
    private val ingredients = listOf(
        Ingredient("egg", "Trứng gà", "dam", 7),
        Ingredient("pork", "Thịt heo", "dam", 4),
        Ingredient("beef", "Thịt bò", "dam", 4),
        Ingredient("fish", "Cá hồi", "dam", 3),
        Ingredient("cabbage", "Cải ngọt", "rau", 4),
        Ingredient("spinach", "Rau muống", "rau", 3),
        Ingredient("gourd", "Bí xanh", "rau", 5),
    )
    private val emptyState = PantryGridState(today = today, ingredients = ingredients)

    private fun main(id: String, ing: String) = Recipe(id, id, listOf("nấu"), listOf(Slot(listOf(ing))), minutes = 15)
    private fun side(id: String, ing: String) = Recipe("canh-$id", "Canh $id", listOf("nấu"), listOf(Slot(listOf(ing))), minutes = 10)

    private val catalog = listOf(
        main("m1", "egg"),
        main("m2", "pork"),
        main("m3", "beef"),
        main("m4", "fish"),
        side("s1", "cabbage"),
        side("s2", "spinach"),
        side("s3", "gourd"),
    )

    @Test
    fun singleMealMode_keepsLunchRecipesNull() {
        val plan = buildWeeklyMealPlan(catalog, emptyState, separateLunchAndDinner = false)
        assertEquals(7, plan.size)
        assertTrue(plan.all { it.lunchRecipe == null && it.lunchSideRecipe == null })
    }

    @Test
    fun separateLunchAndDinner_picksDifferentLunchAndDinnerDishesEachDay() {
        val plan = buildWeeklyMealPlan(catalog, emptyState, separateLunchAndDinner = true)
        assertEquals(7, plan.size)
        plan.forEach { day ->
            assertNotNull(day.lunchRecipe, "Day ${day.dayIndex} should have lunchRecipe")
            assertNotEquals(day.recipe.id, day.lunchRecipe?.id, "Lunch and dinner main must differ on day ${day.dayIndex}")
            if (day.sideRecipe != null && day.lunchSideRecipe != null) {
                assertNotEquals(day.sideRecipe?.id, day.lunchSideRecipe?.id, "Lunch and dinner side must differ")
            }
            assertTrue(day.dishes.size >= 3)
        }
    }

    @Test
    fun shoppingList_forSeparateLunchAndDinner_sumsActualDishesWithoutDoublingAgain() {
        val singleDay = PlannedDayMeal(
            dayIndex = 0,
            dayLabelVi = "Thứ 2",
            recipe = main("m1", "egg"),
            matchReasonVi = "test",
            sideRecipe = side("s1", "cabbage"),
            lunchRecipe = main("m2", "pork"),
            lunchSideRecipe = side("s2", "spinach"),
        )
        val shopping = buildWeeklyShoppingList(listOf(singleDay), emptyState, includeLunchMealPrep = true)
        val byId = shopping.associate { it.ingredient.id to it.mealCount }
        assertEquals(1, byId["egg"])
        assertEquals(1, byId["pork"])
        assertEquals(1, byId["cabbage"])
        assertEquals(1, byId["spinach"])
    }
}
