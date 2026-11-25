package com.loitu.yourrefri.feature.cooklog.ui

import com.loitu.yourrefri.feature.cooklog.logic.CookHistoryWeek
import com.loitu.yourrefri.feature.cooklog.ui.CookHistoryIntent.Delete as DeleteIntent
import com.loitu.yourrefri.feature.recipe.data.MealSlot

/** Lịch sử nấu đã gom theo tuần; [loaded] false cho tới khi đọc xong lần đầu. */
data class CookHistoryState(
    val weeks: List<CookHistoryWeek> = emptyList(),
    val loaded: Boolean = false,
)

sealed interface CookHistoryIntent {
    data class Delete(val id: Long) : CookHistoryIntent
    data object Reload : CookHistoryIntent
    data class Record(
        val recipeId: String,
        val savedIngredientIds: List<String>,
        val meal: MealSlot? = null,
        val onResult: (Long?) -> Unit,
    ) : CookHistoryIntent

    companion object {
        // Kotlin cannot resolve nested classifiers through a typealias.
        @Suppress("FunctionName")
        fun Delete(id: Long): DeleteIntent = DeleteIntent(id)
    }
}

typealias CookHistoryAction = CookHistoryIntent

sealed interface CookHistoryEffect {
    data object SaveFailed : CookHistoryEffect
    data object LoadFailed : CookHistoryEffect
}
