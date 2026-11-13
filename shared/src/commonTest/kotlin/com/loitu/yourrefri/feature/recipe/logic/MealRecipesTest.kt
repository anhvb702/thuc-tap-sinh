package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlin.test.Test
import kotlin.test.assertEquals

class MealRecipesTest {
    private fun recipe(id: String, minutes: Int?) =
        Recipe(id, id, listOf("nấu"), listOf(Slot(listOf("trung"))), minutes = minutes)

    @Test
    fun dinner_keepsEveryRecipeInOrder() {
        val all = listOf(recipe("a", 40), recipe("b", 10), recipe("c", null))
        assertEquals(all, recipesForMeal(all, MealSlot.DINNER))
    }

    @Test
    fun lunch_withEnoughQuickRecipes_showsOnlyQuickOnes_inOriginalOrder() {
        val all = listOf(recipe("slow", 45), recipe("q1", 10), recipe("q2", 20), recipe("q3", 15), recipe("q4", 5))
        assertEquals(listOf("q1", "q2", "q3", "q4"), recipesForMeal(all, MealSlot.LUNCH).map { it.id })
    }

    @Test
    fun lunch_boundary_20IsQuick_21IsNot() {
        val all = listOf(recipe("x21", 21), recipe("a", 20), recipe("b", 20), recipe("c", 20))
        assertEquals(listOf("a", "b", "c"), recipesForMeal(all, MealSlot.LUNCH).map { it.id })
    }

    @Test
    fun lunch_withTooFewQuickRecipes_putsQuickFirstAndKeepsTheRest() {
        val all = listOf(recipe("slow1", 60), recipe("q1", 10), recipe("slow2", 40), recipe("q2", 20))
        assertEquals(listOf("q1", "q2", "slow1", "slow2"), recipesForMeal(all, MealSlot.LUNCH).map { it.id })
    }

    @Test
    fun lunch_unknownDurationIsNotQuick() {
        val all = listOf(recipe("unknown", null), recipe("q1", 10), recipe("q2", 10), recipe("q3", 10))
        assertEquals(listOf("q1", "q2", "q3"), recipesForMeal(all, MealSlot.LUNCH).map { it.id })
    }

    @Test
    fun lunch_noQuickRecipes_returnsEverythingUnchanged() {
        val all = listOf(recipe("a", 40), recipe("b", null))
        assertEquals(all, recipesForMeal(all, MealSlot.LUNCH))
    }

    @Test
    fun emptyListStaysEmpty() {
        assertEquals(emptyList(), recipesForMeal(emptyList(), MealSlot.LUNCH))
        assertEquals(emptyList(), recipesForMeal(emptyList(), MealSlot.DINNER))
    }
}
