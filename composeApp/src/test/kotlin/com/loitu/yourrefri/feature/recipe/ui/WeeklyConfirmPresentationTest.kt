package com.loitu.yourrefri.feature.recipe.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyConfirmPresentationTest {
    @Test
    fun confirmTargetIsShoppingForAnyPlan() {
        assertEquals(RecipeScreenMode.SHOPPING, confirmTargetMode(listOf("a")))
        assertEquals(RecipeScreenMode.SHOPPING, confirmTargetMode(emptyList<String>()))
    }

    @Test
    fun confirmWithPlanConfirmsThenNavigatesToShopping() {
        val calls = mutableListOf<String>()
        handleWeeklyConfirm(listOf("a"), { calls += "confirm" }, { calls += "nav:$it" })
        assertEquals(listOf("confirm", "nav:SHOPPING"), calls)
    }

    @Test
    fun confirmWithEmptyPlanConfirmsThenNavigatesToShopping() {
        val calls = mutableListOf<String>()
        handleWeeklyConfirm(emptyList<String>(), { calls += "confirm" }, { calls += "nav:$it" })
        assertEquals(listOf("confirm", "nav:SHOPPING"), calls)
    }
}
