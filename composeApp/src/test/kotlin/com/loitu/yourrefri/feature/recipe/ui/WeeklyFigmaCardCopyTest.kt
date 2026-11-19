package com.loitu.yourrefri.feature.recipe.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class WeeklyFigmaCardCopyTest {
    @Test
    fun weeklyCardCopy_isExact() {
        assertEquals("Mua đến đâu, cất đến đó.", WEEKLY_SHOPPING_HELPER_BANNER)
        assertEquals("Xác nhận thực đơn tuần", WEEKLY_CONFIRM_LABEL)
    }

    @Test
    fun weeklyCardCopy_avoidsBannedWords() {
        val copy = listOf(WEEKLY_SHOPPING_HELPER_BANNER, WEEKLY_CONFIRM_LABEL)
        val bannedWords = listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên") // lint-copy: rule
        bannedWords.forEach { banned ->
            copy.forEach { text -> assertFalse(text.contains(banned, ignoreCase = true)) }
        }
    }
}
