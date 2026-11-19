package com.loitu.yourrefri.feature.recipe.ui

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DayDateLabelTest {
    @Test
    fun wednesdayOfWeekStartingMondayNov16() {
        // dayIndex 0 = thứ 2 của tuần bắt đầu 16.11.2026; index 2 = thứ 4 ngày 18.11.
        assertEquals("Thứ 4 · 18.11", dayDateLabel(LocalDate(2026, 11, 16), dayIndex = 2, dayLabel = "Thứ 4"))
    }

    @Test
    fun crossesMonthBoundary() {
        // 27.11 + 5 ngày = 02.12: qua tháng.
        assertEquals("Thứ 7 · 02.12", dayDateLabel(LocalDate(2026, 11, 27), dayIndex = 5, dayLabel = "Thứ 7"))
    }
}
