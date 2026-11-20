package com.loitu.yourrefri.feature.recipe.ui

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WeeklyCompactPresentationTest {
    @Test
    fun compactMealOptionLabel_usesShortLabelsForAllOptions() {
        assertEquals("Tối", compactMealOptionLabel(WeeklyMealOption.DINNER))
        assertEquals("Trưa", compactMealOptionLabel(WeeklyMealOption.LUNCH))
        assertEquals("Trưa & Tối", compactMealOptionLabel(WeeklyMealOption.BOTH))
    }

    @Test
    fun compactDurationLabel_formatsPositiveAndHidesZeroOrNull() {
        assertEquals("15p", compactDurationLabel(15))
        assertEquals("1p", compactDurationLabel(1))
        assertNull(compactDurationLabel(0))
        assertNull(compactDurationLabel(-5))
        assertNull(compactDurationLabel(null))
    }

    @Test
    fun compactDayTitle_appendsDateWhenWeekStartProvided() {
        val monday = LocalDate(2026, 11, 16)
        assertEquals("Thứ 2 · 16.11", compactDayTitle("Thứ 2", monday, 0))
        assertEquals("Thứ 4 · 18.11", compactDayTitle("Thứ 4", monday, 2))
        assertEquals("Thứ 2", compactDayTitle("Thứ 2", null, 0))
    }

    @Test
    fun compactDishTitles_returnsTrimmedNonBlankDishesInOrder() {
        assertEquals(
            listOf("Cá hồi áp chảo", "Canh cải đậu phụ"),
            compactDishTitles("  Cá hồi áp chảo ", " Canh cải đậu phụ "),
        )
        assertEquals(
            listOf("Trứng chiên"),
            compactDishTitles("Trứng chiên", "   "),
        )
        assertEquals(emptyList(), compactDishTitles(" ", null))
    }
}
