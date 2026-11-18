package com.loitu.yourrefri.feature.home.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ShortEstimateTextTest {
    @Test
    fun usesGentleWordingForEachRange() {
        assertEquals("Đã qua 1 ngày (ước tính)", shortEstimateText(-1))
        assertEquals("Nên dùng hôm nay (ước tính)", shortEstimateText(0))
        assertEquals("Còn 1 ngày (ước tính)", shortEstimateText(1))
        assertEquals("Còn 2 ngày (ước tính)", shortEstimateText(2))
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
