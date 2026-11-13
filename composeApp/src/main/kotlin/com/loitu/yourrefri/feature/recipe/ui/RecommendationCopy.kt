package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason

internal data class EmptyRecommendationCopy(val message: String, val actionLabel: String)

internal val emptyRecommendationCopy = EmptyRecommendationCopy(
    message = "Mình chưa có công thức để gợi ý lúc này. Bạn vẫn xem được đồ trong tủ và hạn ước tính nhé.",
    actionLabel = "Xem tủ lạnh",
)

/** Nhãn rút gọn cho thẻ nhỏ (Trang chủ). Rỗng nghĩa là không hiện nhãn. Nhãn đầy đủ ở dưới vẫn dùng cho thẻ nổi bật. */
internal fun shortReasonLabel(reason: RecommendationReason): String = when (reason) {
    RecommendationReason.USE_SOON -> "Dùng đồ sắp dùng"
    RecommendationReason.READY -> "Nấu được ngay"
    RecommendationReason.QUICK -> "Nhanh"
    RecommendationReason.OTHER -> ""
}

internal fun recommendationLabel(reason: RecommendationReason): String = when (reason) {
    RecommendationReason.USE_SOON -> "Có món nên dùng sớm"
    RecommendationReason.READY -> "Đủ nguyên liệu trong tủ"
    RecommendationReason.QUICK -> "Món nhanh"
    RecommendationReason.OTHER -> "Gợi ý cho gia đình"
}
