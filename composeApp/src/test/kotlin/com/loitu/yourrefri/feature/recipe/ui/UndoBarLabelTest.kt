package com.loitu.yourrefri.feature.recipe.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class UndoBarLabelTest {
    @Test
    fun showsSavedAndCountdown() {
        assertEquals("Đã lưu · Hoàn tác (5s)", undoBarLabel(secondsLeft = 5))
    }

    @Test
    fun countdownNeverShowsNegative() {
        assertEquals("Đã lưu · Hoàn tác (0s)", undoBarLabel(secondsLeft = -2))
    }
}
