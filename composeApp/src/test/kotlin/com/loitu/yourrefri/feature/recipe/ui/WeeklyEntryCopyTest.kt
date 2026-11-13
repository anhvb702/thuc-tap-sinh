package com.loitu.yourrefri.feature.recipe.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WeeklyEntryCopyTest {
    @Test
    fun weeklyButtonLabel_isExact() {
        assertEquals("Lên thực đơn cả tuần", WEEKLY_BUTTON_LABEL)
    }

    @Test
    fun weeklyButtonLabel_avoidsBannedWords() {
        listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên").forEach {
            assertFalse(WEEKLY_BUTTON_LABEL.contains(it, ignoreCase = true))
        }
    }

    @Test
    fun weeklyModeStillReachableFromRecipeTabs() {
        assertTrue(recipeTabSpecs().any { it.mode == RecipeScreenMode.WEEKLY })
    }
}
