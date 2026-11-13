package com.loitu.yourrefri.feature.cooklog.ui

import com.loitu.yourrefri.feature.cooklog.logic.CookHistoryWeekLabel
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CookHistoryCopyTest {
    private val banned = listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên", "điểm", "streak")

    @Test
    fun titlesAreExact() {
        assertEquals("Đã nấu", CookHistoryCopy.title)
        assertEquals("Xem lại các món đã nấu", CookHistoryCopy.homeSubtitle)
        assertEquals("Nấu lại", CookHistoryCopy.cookAgain)
        assertEquals("Món đã lưu", CookHistoryCopy.unknownRecipe)
        assertEquals("Chưa có món nào ở đây. Nấu xong, món sẽ nằm gọn trong này nhé", CookHistoryCopy.empty)
    }

    @Test
    fun weekLabelsAreExact() {
        assertEquals("Tuần này", CookHistoryCopy.weekLabel(CookHistoryWeekLabel.THIS_WEEK))
        assertEquals("Tuần trước", CookHistoryCopy.weekLabel(CookHistoryWeekLabel.LAST_WEEK))
        assertEquals("Trước đó", CookHistoryCopy.weekLabel(CookHistoryWeekLabel.EARLIER))
    }

    @Test
    fun dateLabelShowsShortWeekdayAndDayMonth() {
        assertEquals("T5 12/11", cookDateLabel(LocalDate(2026, 11, 12)))
        assertEquals("T2 09/11", cookDateLabel(LocalDate(2026, 11, 9)))
        assertEquals("CN 08/11", cookDateLabel(LocalDate(2026, 11, 8)))
        assertEquals("T7 03/01", cookDateLabel(LocalDate(2026, 1, 3)))
    }

    @Test
    fun moreIngredientsLabelCountsOnlyTheHiddenOnes() {
        assertEquals("+2", CookHistoryCopy.moreIngredients(hidden = 2))
    }

    @Test
    fun copyAvoidsBannedWords() {
        val all = listOf(
            CookHistoryCopy.title, CookHistoryCopy.cookAgain, CookHistoryCopy.unknownRecipe, CookHistoryCopy.empty,
        ) + CookHistoryWeekLabel.entries.map { CookHistoryCopy.weekLabel(it) }
        all.forEach { text -> banned.forEach { w -> assertFalse(text.contains(w, ignoreCase = true), "$text chứa $w") } }
    }
}
