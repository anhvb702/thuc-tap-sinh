package com.loitu.yourrefri.feature.recipe.ui

internal fun resolveInitialRecipeId(
    requestedId: String?,
    recommendationIds: List<String>,
): String? = requestedId
    ?.takeIf { it in recommendationIds }
    ?: recommendationIds.firstOrNull()

/** Id món mở ở màn chi tiết khi bấm thông báo nhắc, cùng quy tắc với thẻ Trang chủ. */
internal fun notificationDetailRequest(recipeId: String): String = recipeId

internal fun resolveRequestedRecipeId(
    requestedId: String?,
    recommendationIds: List<String>,
    knownRecipeIds: Collection<String>,
): String? = requestedId
    ?.takeIf { it in knownRecipeIds }
    ?: resolveInitialRecipeId(requestedId, recommendationIds)

internal fun recipeRequestForHomeCard(recipeId: String): String = recipeId

internal fun recipeRequestForAllRecipes(): String? = null

internal fun shouldClearDurationForRequest(requestedId: String?): Boolean = requestedId != null

internal fun shouldBringRecipeIntoView(
    requestedId: String?,
    resolvedId: String?,
    cardId: String,
): Boolean = requestedId != null && requestedId == resolvedId && cardId == resolvedId
