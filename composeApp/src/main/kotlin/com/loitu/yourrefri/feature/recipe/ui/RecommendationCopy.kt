package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason

internal data class EmptyRecommendationCopy(val message: String, val actionLabel: String)

internal val emptyRecommendationCopy = EmptyRecommendationCopy(
    message = "Mình chưa có công thức để gợi ý lúc này. Bạn vẫn xem được đồ trong tủ và hạn ước tính nhé.",
    actionLabel = "Xem tủ lạnh",
)

internal fun recommendationLabel(reason: RecommendationReason): String = when (reason) {
    RecommendationReason.USE_SOON -> "Có món nên dùng sớm"
    RecommendationReason.READY -> "Đủ nguyên liệu trong tủ"
    RecommendationReason.QUICK -> "Món nhanh"
    RecommendationReason.OTHER -> "Gợi ý cho gia đình"
}
