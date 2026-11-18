package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.logic.RecipeRecommendation
import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason

/** Nhãn mục theo Figma 03: "01 · DÙNG ĐỒ NÊN DÙNG TRƯỚC", "02 · NẤU ĐƯỢC NGAY", "03 · MỘT MÓN NHANH". */
internal fun recipeSectionLabel(reason: RecommendationReason): String = when (reason) {
    RecommendationReason.USE_SOON -> "01 · DÙNG ĐỒ NÊN DÙNG TRƯỚC"
    RecommendationReason.READY -> "02 · NẤU ĐƯỢC NGAY"
    RecommendationReason.QUICK -> "03 · MỘT MÓN NHANH"
    RecommendationReason.OTHER -> "04 · KHÁC"
}

/** Gắn nhãn mục cho từng gợi ý, giữ thứ tự gợi ý gốc trong mỗi mục. */
internal fun recipeCardSections(list: List<RecipeRecommendation>): List<Pair<String, RecipeRecommendation>> =
    list.map { recipeSectionLabel(it.reason) to it }
