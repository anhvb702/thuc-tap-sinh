package com.loitu.yourrefri.feature.home.domain

import com.loitu.yourrefri.feature.home.domain.usecase.BuildHomeDashboardUseCase
import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildHomeDashboardUseCaseTest {
    private val today = LocalDate(2026, 1, 10)
    private val build = BuildHomeDashboardUseCase()

    private fun recipe(id: String, minutes: Int? = null, ingredient: String = "rice") = Recipe(
        id = id,
        title = id,
        steps = listOf("cook"),
        required = listOf(Slot(listOf(ingredient))),
        minutes = minutes,
    )

    private fun state(
        ingredients: List<Ingredient> = emptyList(),
        pantry: List<PantryItem> = emptyList(),
        hour: Int = 12,
    ) = PantryGridState(today = today, hourOfDay = hour, ingredients = ingredients, pantry = pantry.associateBy { it.ingredientId })

    @Test
    fun emptyFridge_hasNoItemsOrUrgentItems() {
        val result = build(state(), emptyList(), MealSlot.DINNER)

        assertTrue(result.haveItems.isEmpty())
        assertTrue(result.urgentItems.isEmpty())
        assertEquals("Chào buổi chiều 🌿", result.greeting)
    }

    @Test
    fun urgentItems_areLimitedToZeroThroughTwoDaysAndSorted() {
        val ingredients = listOf(
            Ingredient("two", "Two", "fresh", 2),
            Ingredient("zero", "Zero", "fresh", 0),
            Ingredient("three", "Three", "fresh", 3),
            Ingredient("expired", "Expired", "fresh", 1),
        )
        val pantry = listOf(
            PantryItem("two", true, today),
            PantryItem("zero", true, today),
            PantryItem("three", true, today),
            PantryItem("expired", true, LocalDate(2026, 1, 8)),
        )

        val result = build(state(ingredients, pantry), emptyList(), MealSlot.DINNER)

        assertEquals(listOf("zero", "two"), result.urgentItems.map { it.ingredientId })
        assertEquals(listOf(0, 2), result.urgentItems.map { it.daysLeft })
    }

    @Test
    fun mealFiltering_andRecipeLimit_areApplied() {
        val recipes = listOf(
            recipe("quick-a", 10),
            recipe("quick-b", 15),
            recipe("quick-c", 20),
            recipe("slow", 40),
        )

        val result = build(state(), recipes, MealSlot.LUNCH, recipeLimit = 2)

        assertEquals(2, result.recommendations.size)
        assertTrue(result.recommendations.all { it.recipe.minutes!! <= 20 })
    }

    @Test
    fun cookableRecipes_containsOnlyRecipesWithAvailableIngredients() {
        val ingredients = listOf(
            Ingredient("rice", "Rice", "fresh", 5),
            Ingredient("egg", "Egg", "fresh", 5),
        )
        val recipes = listOf(recipe("rice-recipe"), recipe("egg-recipe", ingredient = "egg"), recipe("missing", ingredient = "fish"))

        val result = build(
            state(ingredients, listOf(PantryItem("rice", true, today))),
            recipes,
            MealSlot.DINNER,
        )

        assertEquals(listOf("rice-recipe"), result.cookableRecipes.map { it.id })
    }
}
