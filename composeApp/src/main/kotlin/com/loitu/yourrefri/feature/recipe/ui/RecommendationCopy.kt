package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason

internal data class EmptyRecommendationCopy(val message: String, val actionLabel: String)

internal val emptyRecommendationCopy = EmptyRecommendationCopy(
    message = "Mình chưa có công thức để gợi ý lúc này. Bạn vẫn xem được đồ trong tủ và hạn ước tính nhé.",
    actionLabel = "Xem tủ lạnh",
)

/** Căn cứ quyết định Q2 (2026-11-16): Gợi ý đổi chế độ ăn khi bộ lọc loại hết mọi món. */
internal val emptyDietRecommendationCopy = EmptyRecommendationCopy(
    message = "Chưa có món phù hợp với chế độ ăn hiện tại. Bạn thử điều chỉnh chế độ ăn nhé.",
    actionLabel = "Điều chỉnh chế độ ăn",
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
