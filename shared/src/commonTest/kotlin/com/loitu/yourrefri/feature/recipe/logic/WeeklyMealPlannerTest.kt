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

    @Test fun pairsSavoryMainWithSoupOrVeggieCoursePerDay() {
        val fullCatalog = listOf(
            Recipe("thit-kho", "Thịt heo kho trứng", listOf("Nấu"), listOf(Slot(listOf("pork")))),
            Recipe("bo-xao", "Thịt bò xào hành tây", listOf("Xào"), listOf(Slot(listOf("pork")))),
            Recipe("canh-rau", "Canh rau ngót nấu thịt", listOf("Nấu canh"), listOf(Slot(listOf("rau-muong")))),
            Recipe("rau-xao", "Rau muống xào hành tây", listOf("Xào rau"), listOf(Slot(listOf("rau-muong")))),
        )
        val plan = buildWeeklyMealPlan(fullCatalog, state)
        assertEquals(7, plan.size)
        assertEquals(true, plan.all { it.sideRecipe != null && it.dishes.size == 2 })
        assertEquals(true, plan.zipWithNext().all { (a, b) -> a.recipe.id != b.recipe.id && a.sideRecipe?.id != b.sideRecipe?.id })
    }

    @Test fun kidPreferenceRecognizesKhoaiTayAndShoppingPrefersSelectedTagAndTracksMissingQty() {
        val extCatalog = catalog + listOf(
            Ingredient("khoai-tay", "Khoai tây", "cu", 7),
            Ingredient("tom", "Tôm", "ca", 2),
            Ingredient("thit-heo-xay", "Thịt heo xay", "thit", 2),
        )
        val extState = state.copy(
            ingredients = extCatalog,
            pantry = mapOf("pork" to PantryItem("pork", true, LocalDate(2026, 11, 9))),
            quantities = emptyMap(),
        )
        val kidPlan = buildWeeklyMealPlan(
            listOf(recipe("normal", "rice"), recipe("kid-potato", "khoai-tay")),
            extState,
            preferences = setOf(FamilyPreferenceTag.KID_FAVORITE),
        )
        assertEquals("kid-potato", kidPlan.first().recipe.id)

        val flexSoup = Recipe("canh-bi", "Canh bí", listOf("Nấu"), listOf(Slot(listOf("tom", "thit-heo-xay"))))
        val shoppingForMeat = buildWeeklyShoppingList(
            listOf(
                PlannedDayMeal(0, "Thứ 2", flexSoup, ""),
                PlannedDayMeal(1, "Thứ 3", flexSoup, ""),
                PlannedDayMeal(2, "Thứ 4", recipe("pork-dish", "thit-heo-xay"), ""),
            ),
            extState.copy(pantry = emptyMap()),
            preferences = setOf(FamilyPreferenceTag.MEAT),
        )
        assertEquals("thit-heo-xay", shoppingForMeat.single().ingredient.id)
        assertEquals(mapOf("thit-heo-xay" to 3), shoppingQuantitiesToStock(shoppingForMeat))

        val partialShopping = buildWeeklyShoppingList(
            listOf(
                PlannedDayMeal(0, "Thứ 2", recipe("d1", "pork"), ""),
                PlannedDayMeal(1, "Thứ 3", recipe("d2", "pork"), ""),
                PlannedDayMeal(2, "Thứ 4", recipe("d3", "pork"), ""),
            ),
            extState,
        )
        assertEquals(2, partialShopping.single().missingQuantity)
        assertEquals(mapOf("pork" to 2), shoppingQuantitiesToStock(partialShopping))
    }

    @Test fun includeLunchMealPrepDoublesPortionsForNextDayLunch() {
        val plan = listOf(
            PlannedDayMeal(0, "Thứ 2", recipe("d1", "pork"), ""),
            PlannedDayMeal(1, "Thứ 3", recipe("d2", "pork"), ""),
        )
        val normal = buildWeeklyShoppingList(plan, state.copy(pantry = emptyMap()), includeLunchMealPrep = false)
        assertEquals(2, normal.single().mealCount)

        val withLunch = buildWeeklyShoppingList(plan, state.copy(pantry = emptyMap()), includeLunchMealPrep = true)
        assertEquals(4, withLunch.single().mealCount)
        assertEquals(mapOf("pork" to 4), shoppingQuantitiesToStock(withLunch))
    }
}
