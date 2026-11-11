package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason

internal fun recommendationLabel(reason: RecommendationReason): String = when (reason) {
    RecommendationReason.USE_SOON -> "Có món nên dùng sớm"
    RecommendationReason.READY -> "Đủ nguyên liệu trong tủ"
    RecommendationReason.QUICK -> "Món nhanh"
    RecommendationReason.OTHER -> "Gợi ý cho gia đình"
}
