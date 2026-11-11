package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.RecipeBundle
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RecipeRecommendationTest {
    private val today = LocalDate(2026, 11, 6)
    private val ingredients = listOf(
        Ingredient("egg", "Trứng gà", "khac", 7),
        Ingredient("pork", "Thịt heo", "thit", 30),
        Ingredient("rice", "Gạo", "khac", 30),
        Ingredient("garlic", "Tỏi", "khac", 30),
        Ingredient("tomato", "Cà chua", "rau-cu", 7),
    )
    private val recipes = listOf(
        Recipe("quick", "Rau xào", listOf("Xào"), listOf(Slot(listOf("garlic"))), minutes = 12),
        Recipe("ready", "Thịt kho", listOf("Kho"), listOf(Slot(listOf("pork")), Slot(listOf("rice"))), minutes = 40),
        Recipe("urgent", "Trứng hấp", listOf("Hấp"), listOf(Slot(listOf("egg"))), minutes = 25),
        Recipe("more-urgent", "Trứng cà chua", listOf("Nấu"), listOf(Slot(listOf("egg")), Slot(listOf("tomato"))), minutes = 30),
        Recipe("other", "Món khác", listOf("Nấu"), listOf(Slot(listOf("unknown"))), minutes = 50),
    )

    private fun state() = PantryGridState(
        today = today,
        ingredients = ingredients,
        pantry = mapOf(
            "egg" to PantryItem("egg", true, LocalDate(2026, 11, 0 + 1)),
            "pork" to PantryItem("pork", true, today),
            "rice" to PantryItem("rice", true, today),
            "tomato" to PantryItem("tomato", true, LocalDate(2026, 11, 1)),
        ),
    )

    @Test
    fun recommendationsOrderUrgentReadyQuick() {
        val recommendations = recommendRecipes(recipes, state())
        assertEquals(listOf("more-urgent", "ready", "quick"), recommendations.map { it.recipe.id })
        assertEquals(
            listOf(RecommendationReason.USE_SOON, RecommendationReason.READY, RecommendationReason.QUICK),
            recommendations.map { it.reason },
        )
    }

    @Test
    fun recommendationsAreDistinctAndLimitedToThree() {
        val recommendations = recommendRecipes(recipes.take(2), state())
        assertEquals(2, recommendations.size)
        assertEquals(recommendations.size, recommendations.map { it.recipe.id }.distinct().size)
    }

    @Test
    fun overdueItemsDoNotBecomeUseSoonSuggestions() {
        val initial = state()
        val overdue = initial.copy(
            pantry = initial.pantry.filterKeys { it != "egg" && it != "tomato" } +
                ("egg" to PantryItem("egg", true, LocalDate(2026, 10, 1))),
        )
        val recommendations = recommendRecipes(recipes, overdue)
        assertEquals("ready", recommendations.first().recipe.id)
        assertEquals(RecommendationReason.READY, recommendations.first().reason)
    }

    @Test
    fun missingAndFutureItemsDoNotCreateUrgentReason() {
        val initial = state()
        val withoutTomato = initial.pantry - "tomato"
        val missingEgg = initial.copy(
            pantry = withoutTomato + ("egg" to PantryItem("egg", false, LocalDate(2026, 11, 1))),
        )
        val futureEgg = initial.copy(
            pantry = withoutTomato + ("egg" to PantryItem("egg", true, today)),
        )

        listOf(missingEgg, futureEgg).forEach { candidateState ->
            val first = recommendRecipes(recipes, candidateState).first()
            assertEquals("ready", first.recipe.id)
            assertEquals(RecommendationReason.READY, first.reason)
        }
    }

    @Test
    fun partlyAvailableRecipeIsNotMarkedReady() {
        val candidateState = state().copy(pantry = state().pantry - "rice")
        val recommendations = recommendRecipes(recipes, candidateState)
        assertEquals(
            false,
            recommendations.any { it.recipe.id == "ready" && it.reason == RecommendationReason.READY },
        )
    }

    @Test
    fun twentyMinutesIsNotQuick() {
        val atBoundary = recipes.map { if (it.id == "quick") it.copy(minutes = 20) else it }
        val recommendations = recommendRecipes(atBoundary, state())
        assertEquals(
            false,
            recommendations.any { it.recipe.id == "quick" && it.reason == RecommendationReason.QUICK },
        )
    }

    @Test
    fun duplicateRecipeIdsAreReturnedOnce() {
        val recommendations = recommendRecipes(recipes + recipes.first(), state())
        assertEquals(recommendations.size, recommendations.map { it.recipe.id }.distinct().size)
    }

    @Test
    fun alternativeIngredientsInOneSlotCountAsOneUrgentUse() {
        val candidateRecipes = listOf(
            Recipe("alternatives", "Chọn một loại thịt", listOf("Nấu"), listOf(Slot(listOf("egg", "tomato")))),
            Recipe("two-slots", "Dùng hai món sắp tới", listOf("Nấu"), listOf(Slot(listOf("egg")), Slot(listOf("tomato")))),
            Recipe("quick", "Món nhanh", listOf("Nấu"), listOf(Slot(listOf("garlic"))), minutes = 10),
        )

        assertEquals("two-slots", recommendRecipes(candidateRecipes, state()).first().recipe.id)
    }

    @Test
    fun recipeBundleParsesAndValidatesOptionalMinutes() {
        val row = """{"id":"dish","title":"Món","steps":["Nấu"],"required":[{"anyOf":["egg"]}],"minutes":12}"""
        assertEquals(12, RecipeBundle.parse("[$row]").single().minutes)
        assertEquals(null, RecipeBundle.parse("[${row.replace(",\"minutes\":12", "")}]").single().minutes)
        assertFailsWith<IllegalArgumentException> {
            RecipeBundle.parse("[${row.replace("12", "0")}]")
        }
    }
}
