package com.loitu.yourrefri.feature.home.ui

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Contract W4-03: thẻ "Tủ lạnh hiện có" đặt nút "Xem tủ" xuống dưới chữ khi cỡ chữ hệ thống từ 1.2 trở lên,
// để chữ không chạm nút ở 360dp.
class SummaryLayoutTest {
    @Test
    fun normalFontScale_keepsButtonBesideText() {
        assertFalse(summaryButtonBelowText(1.0f))
    }

    @Test
    fun largeFontScale_movesButtonBelowText() {
        assertTrue(summaryButtonBelowText(1.2f))
        assertTrue(summaryButtonBelowText(1.3f))
        assertTrue(summaryButtonBelowText(2.0f))
    }
}
