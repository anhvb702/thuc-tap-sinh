package com.loitu.yourrefri.feature.pantry.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShelfEditorStatusTextTest {
    @Test
    fun zeroDays_saysEstimatedSameDay() {
        assertEquals("Đang áp dụng: ước tính dùng trong ngày", shelfEditorStatusText(0))
    }

    @Test
    fun nullOrPositiveDays_showNothing() {
        assertNull(shelfEditorStatusText(null))
        assertNull(shelfEditorStatusText(1))
        assertNull(shelfEditorStatusText(30))
    }

    @Test
    fun negativeDays_showNothing() {
        assertNull(shelfEditorStatusText(-1))
    }

    @Test
    fun statusText_keepsEstimateWordAndNoBannedWords() {
        val text = assertNotNull(shelfEditorStatusText(0))
        assertTrue(text.contains("ước tính"))
        listOf("0 ngày", "bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", "an toàn").forEach {
            assertFalse(text.contains(it))
        }
    }
}
