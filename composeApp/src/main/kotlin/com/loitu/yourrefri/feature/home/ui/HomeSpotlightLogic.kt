package com.loitu.yourrefri.feature.home.ui

import com.loitu.yourrefri.feature.home.domain.model.HomeWeather
import com.loitu.yourrefri.feature.home.domain.model.WeatherCity
import com.loitu.yourrefri.feature.home.domain.model.formatHomeWeatherLine
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.RecipeRecommendation
import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import com.loitu.yourrefri.feature.recipe.ui.shortReasonLabel

internal fun activeSpotlightSuggestion(
    recommendations: List<RecipeRecommendation>,
    dismissedIds: Set<String>,
): RecipeRecommendation? = recommendations.firstOrNull { it.recipe.id !in dismissedIds }

internal fun spotlightSubtitle(suggestion: RecipeRecommendation): String = buildList {
    suggestion.recipe.minutes?.let { add("$it phút") }
    add(
        if (suggestion.reason == RecommendationReason.READY || suggestion.reason == RecommendationReason.USE_SOON) {
            "Đủ nguyên liệu"
        } else {
            shortReasonLabel(suggestion.reason)
        },
    )
}.joinToString(" · ")

internal fun resetDismissedSuggestions(): Set<String> = emptySet()

internal const val DEFAULT_USER_NAME = "Andy"

internal const val SPOTLIGHT_PRIMARY_CTA_LABEL = "Xem & nấu ngay"

internal const val BUCKET_SHEET_TAP_HINT = "Chạm món để xem gợi ý nấu"

internal fun userGreetingText(userName: String = DEFAULT_USER_NAME): String = "Chào ${userName.trim().ifEmpty { DEFAULT_USER_NAME }}"

/** Ẩn dòng placeholder "Đang xem thời tiết..." khi mới mở app; chỉ hiện khi có dữ liệu hoặc đã tải xong. */
internal fun homeHeaderWeatherLine(city: WeatherCity, weather: HomeWeather?, loading: Boolean): String? =
    if (loading && weather == null) null else formatHomeWeatherLine(city, weather, loading)

/** Tìm món ăn phù hợp nhất dùng nguyên liệu vừa chạm (ưu tiên gợi ý hiện có trước, rồi đến toàn bộ danh sách). */
internal fun findRecipeIdForIngredient(
    ingredientId: String,
    recommendations: List<RecipeRecommendation>,
    recipes: List<Recipe>,
): String? {
    fun Recipe.usesIngredient(): Boolean =
        required.any { ingredientId in it.anyOf } || optional.any { ingredientId in it.anyOf }

    return recommendations.firstOrNull { it.recipe.usesIngredient() }?.recipe?.id
        ?: recipes.firstOrNull { it.usesIngredient() }?.id
}
