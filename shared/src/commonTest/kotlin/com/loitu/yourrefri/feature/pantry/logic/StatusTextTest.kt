package com.loitu.yourrefri.feature.pantry.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class StatusTextTest {
    @Test
    fun negativeDays_invitesCheckingWithoutSafetyClaim() {
        for (days in listOf(-1, -2, Int.MIN_VALUE)) {
            assertEquals(
                "Ước tính đã qua ngày nên dùng trước; kiểm tra lại xem còn dùng được không",
                statusText(days),
            )
        }
    }

    @Test
    fun zeroDays_saysToday() {
        assertEquals("Ước tính nên dùng trước hôm nay", statusText(0))
    }

    @Test
    fun positiveDays_keepsEstimateAndActualDayCount() {
        for (days in listOf(1, 2, 3, 30, Int.MAX_VALUE)) {
            assertEquals("Ước tính nên dùng trước khoảng $days ngày nữa", statusText(days))
        }
    }

    @Test
    fun allBranches_avoidForbiddenWordsAndSafetyClaims() {
        val forbidden = listOf(
            "bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", "điểm số", "streak",
            "an toàn", "chắc chắn", "phải dùng", "tốt hơn",
        )
        for (days in listOf(Int.MIN_VALUE, -1, 0, 1, 2, 3, Int.MAX_VALUE)) {
            val text = statusText(days).lowercase()
            for (phrase in forbidden) assertFalse(text.contains(phrase), "$days: $phrase")
        }
    }
}
