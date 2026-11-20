package com.loitu.yourrefri.feature.home.ui

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
