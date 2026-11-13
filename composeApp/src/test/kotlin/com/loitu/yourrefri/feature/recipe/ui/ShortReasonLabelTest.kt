package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShortReasonLabelTest {
    @Test
    fun labelsAreShortAndExact() {
        assertEquals("Dùng đồ sắp dùng", shortReasonLabel(RecommendationReason.USE_SOON))
        assertEquals("Nấu được ngay", shortReasonLabel(RecommendationReason.READY))
        assertEquals("Nhanh", shortReasonLabel(RecommendationReason.QUICK))
        assertEquals("", shortReasonLabel(RecommendationReason.OTHER))
    }

    @Test
    fun everyShortLabelIsShorterThanTheLongOneOrEmpty() {
        RecommendationReason.entries.forEach { reason ->
            val short = shortReasonLabel(reason)
            assertTrue(short.length < recommendationLabel(reason).length || short.isEmpty(), "$reason: $short")
        }
    }

    @Test
    fun labelsAvoidBannedWords() {
        RecommendationReason.entries.forEach { reason ->
            listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên").forEach {
                assertFalse(shortReasonLabel(reason).contains(it, ignoreCase = true))
            }
        }
    }

    @Test
    fun otherHasNoTag() {
        assertTrue(shortReasonLabel(RecommendationReason.OTHER).isEmpty())
    }
}
