package com.loitu.yourrefri.feature.recipe.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeeklyConfirmPresentationTest {
    @Test
    fun hasPlanOpensShopping() {
        assertEquals(RecipeScreenMode.SHOPPING, weeklyConfirmTarget(true))
    }

    @Test
    fun noPlanStaysPut() {
        assertNull(weeklyConfirmTarget(false))
    }

    @Test
    fun confirmWithPlanConfirmsThenNavigatesToShopping() {
        val calls = mutableListOf<String>()
        handleWeeklyConfirm(listOf("a"), { calls += "confirm" }, { calls += "nav:$it" })
        assertEquals(listOf("confirm", "nav:SHOPPING"), calls)
    }

    @Test
    fun confirmWithEmptyPlanDoesNotNavigate() {
        val calls = mutableListOf<String>()
        handleWeeklyConfirm(emptyList<String>(), { calls += "confirm" }, { calls += "nav:$it" })
        assertEquals(listOf("confirm"), calls)
    }
}
