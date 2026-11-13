package com.loitu.yourrefri.feature.home.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ShortEstimateTextTest {
    @Test
    fun usesGentleWordingForEachRange() {
        assertEquals("Kiểm tra lại giúp mình", shortEstimateText(-1))
        assertEquals("Nên dùng hôm nay", shortEstimateText(0))
        assertEquals("Khoảng còn 1 ngày", shortEstimateText(1))
        assertEquals("Khoảng còn 2 ngày", shortEstimateText(2))
    }

    @Test
    fun avoidsBannedWords() {
        (-2..3).map(::shortEstimateText).forEach { text ->
            listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên").forEach { banned ->
                assertFalse(text.contains(banned, ignoreCase = true), text)
            }
        }
    }
}
