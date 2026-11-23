package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class WeeklyPerDishSwapTest {
    private val today = LocalDate(2026, 11, 20)
    private val ingredients = listOf(
        Ingredient("egg", "Trứng gà", "dam", 7),
        Ingredient("pork", "Thịt heo", "dam", 4),
        Ingredient("beef", "Thịt bò", "dam", 4),
        Ingredient("fish", "Cá hồi", "dam", 3),
        Ingredient("chicken", "Thịt gà", "dam", 4),
        Ingredient("cabbage", "Cải ngọt", "rau", 4),
        Ingredient("spinach", "Rau muống", "rau", 3),
        Ingredient("gourd", "Bí xanh", "rau", 5),
        Ingredient("lettuce", "Xà lách", "rau", 3),
    )
    private val emptyState = PantryGridState(today = today, ingredients = ingredients)

    private fun main(id: String, ing: String) = Recipe(id, id, listOf("nấu"), listOf(Slot(listOf(ing))), minutes = 15)
    private fun side(id: String, ing: String) = Recipe("canh-$id", "Canh $id", listOf("nấu"), listOf(Slot(listOf(ing))), minutes = 10)

    private val catalog = listOf(
        main("m1", "egg"),
        main("m2", "pork"),
        main("m3", "beef"),
        main("m4", "fish"),
        main("m5", "chicken"),
        side("s1", "cabbage"),
        side("s2", "spinach"),
        side("s3", "gourd"),
        side("s4", "lettuce"),
    )

    @Test
    fun weeklyDishSwapKey_isUniqueAndDisjointFromLegacyDayKeys() {
        val keys = (0..6).flatMap { day -> WeeklyDishSlot.entries.map { slot -> weeklyDishSwapKey(day, slot) } }
        assertEquals(28, keys.toSet().size)
        assertTrue(keys.all { it >= 100 && isValidWeeklySwapKey(it) })
        assertTrue((0..6).all { isValidWeeklySwapKey(it) })
        assertTrue(!isValidWeeklySwapKey(-1) && !isValidWeeklySwapKey(7) && !isValidWeeklySwapKey(109))
    }

    @Test
    fun swapDinnerSideOnly_changesOnlyDinnerSideAndPreservesOtherDishes() {
        val base = buildWeeklyMealPlan(catalog, emptyState, separateLunchAndDinner = true).first()
        val swapped = buildWeeklyMealPlan(
            catalog,
            emptyState,
            swappedOffsets = mapOf(weeklyDishSwapKey(0, WeeklyDishSlot.DINNER_SIDE) to 1),
            separateLunchAndDinner = true,
        ).first()

        assertEquals(base.recipe.id, swapped.recipe.id)
        assertNotEquals(base.sideRecipe?.id, swapped.sideRecipe?.id)
        assertEquals(base.lunchRecipe?.id, swapped.lunchRecipe?.id)
    }

    @Test
    fun swapLunchMainAndLunchSideOnly_preservesDinnerAndChangesTargetLunchDish() {
        val base = buildWeeklyMealPlan(catalog, emptyState, separateLunchAndDinner = true).first()
        val swappedMain = buildWeeklyMealPlan(
            catalog,
            emptyState,
            swappedOffsets = mapOf(weeklyDishSwapKey(0, WeeklyDishSlot.LUNCH_MAIN) to 1),
            separateLunchAndDinner = true,
        ).first()
        assertEquals(base.recipe.id, swappedMain.recipe.id)
        assertEquals(base.sideRecipe?.id, swappedMain.sideRecipe?.id)
        assertNotEquals(base.lunchRecipe?.id, swappedMain.lunchRecipe?.id)

        val swappedSide = buildWeeklyMealPlan(
            catalog,
            emptyState,
            swappedOffsets = mapOf(weeklyDishSwapKey(0, WeeklyDishSlot.LUNCH_SIDE) to 1),
            separateLunchAndDinner = true,
        ).first()
        assertEquals(base.recipe.id, swappedSide.recipe.id)
        assertEquals(base.sideRecipe?.id, swappedSide.sideRecipe?.id)
        assertEquals(base.lunchRecipe?.id, swappedSide.lunchRecipe?.id)
        assertNotEquals(base.lunchSideRecipe?.id, swappedSide.lunchSideRecipe?.id)
    }
}
