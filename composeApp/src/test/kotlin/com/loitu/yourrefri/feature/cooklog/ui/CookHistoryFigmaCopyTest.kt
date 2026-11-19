package com.loitu.yourrefri.feature.cooklog.ui

import com.loitu.yourrefri.feature.recipe.data.MealSlot
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CookHistoryFigmaCopyTest {
    @Test
    fun dateMealLabel_fullWeekdayDayMonthAndMeal() {
        assertEquals("Thứ 2, 16.11 · Bữa tối", cookDateMealLabel(LocalDate(2026, 11, 16), MealSlot.DINNER))
        assertEquals("Chủ nhật, 15.11 · Bữa trưa", cookDateMealLabel(LocalDate(2026, 11, 15), MealSlot.LUNCH))
    }

    @Test
    fun dateMealLabel_withoutMealOmitsIt() {
        assertEquals("Thứ 6, 13.11", cookDateMealLabel(LocalDate(2026, 11, 13), null))
    }

    @Test
    fun screenCopy_isWarm() {
        assertEquals("NHỮNG BỮA CƠM NHÀ", CookHistoryCopy.eyebrow)
        assertEquals("Món quen vẫn ngon, nấu lại lúc nào cũng được.", CookHistoryCopy.subtitle)
        listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên", "điểm", "streak").forEach { w -> // lint-copy: rule
            assertFalse(CookHistoryCopy.subtitle.contains(w, true))
        }
    }
}
