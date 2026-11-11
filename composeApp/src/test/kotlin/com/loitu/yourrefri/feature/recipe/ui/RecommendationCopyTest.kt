package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class RecommendationCopyTest {
    @Test
    fun recommendationLabelsKeepEveryReasonAndUseGentleUrgentCopy() {
        assertEquals("Có món nên dùng sớm", recommendationLabel(RecommendationReason.USE_SOON))
        assertEquals("Đủ nguyên liệu trong tủ", recommendationLabel(RecommendationReason.READY))
        assertEquals("Món nhanh", recommendationLabel(RecommendationReason.QUICK))
        assertEquals("Gợi ý cho gia đình", recommendationLabel(RecommendationReason.OTHER))
        assertFalse(RecommendationReason.entries
            .map(::recommendationLabel)
            .contains("Dùng món cần ưu tiên"))
    }
}
