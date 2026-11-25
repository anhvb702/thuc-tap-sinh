package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.ui.FavoriteRecipesIntent.Toggle as ToggleIntent

/** Trạng thái các món yêu thích; [loaded] false cho tới khi đọc xong lần đầu. */
data class FavoriteRecipesState(
    val ids: Set<String> = emptySet(),
    val loaded: Boolean = false,
)

sealed interface FavoriteRecipesIntent {
    data class Toggle(val recipeId: String) : FavoriteRecipesIntent
    data object Reload : FavoriteRecipesIntent

    companion object {
        // Kotlin cannot resolve nested classifiers through a typealias.
        @Suppress("FunctionName")
        fun Toggle(recipeId: String): ToggleIntent = ToggleIntent(recipeId)
    }
}

typealias FavoriteRecipesAction = FavoriteRecipesIntent

sealed interface FavoriteRecipesEffect {
    /** Báo nhẹ: "Chưa lưu được món yêu thích." */
    data object SaveFailed : FavoriteRecipesEffect
    data object LoadFailed : FavoriteRecipesEffect
}

data class UnlockedRecipesState(
    val unlockedIds: Set<String> = emptySet(),
    val loaded: Boolean = false,
    val failedId: String? = null,
    val loadFailed: Boolean = false,
)

sealed interface UnlockedRecipesIntent {
    data class Unlock(val recipeId: String) : UnlockedRecipesIntent
    data object Reload : UnlockedRecipesIntent
}

sealed interface UnlockedRecipesEffect {
    data class UnlockFailed(val recipeId: String) : UnlockedRecipesEffect
    data object LoadFailed : UnlockedRecipesEffect
}
