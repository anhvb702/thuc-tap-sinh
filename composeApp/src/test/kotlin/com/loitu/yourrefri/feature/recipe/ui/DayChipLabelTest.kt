package com.loitu.yourrefri.feature.recipe.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class DayChipLabelTest {
    @Test
    fun showsWeekdayAndDayMonth() {
        assertEquals("Thứ 2 · 16.11", dayChipLabel(dayName = "Thứ 2", day = 16, month = 11))
    }

    @Test
    fun padsSingleDigitDayAndMonth() {
        assertEquals("Chủ nhật · 05.03", dayChipLabel(dayName = "Chủ nhật", day = 5, month = 3))
    }
}
