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
}
