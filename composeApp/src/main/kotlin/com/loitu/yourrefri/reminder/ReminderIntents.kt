package com.loitu.yourrefri.reminder

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Hằng số dùng chung giữa popup nhắc, nút "thử thông báo ngay" và MainActivity. */
object ReminderIntents {
    const val ACTION_TEST = "com.loitu.yourrefri.reminder.TEST"
    const val EXTRA_RECIPE_ID = "reminder_recipe_id"

    /** Id công thức từ intent của popup; rỗng hoặc thiếu thì không mở màn nấu nào. */
    fun recipeIdFromExtra(value: String?): String? = value?.trim()?.takeIf { it.isNotEmpty() }
}

/**
 * Yêu cầu mở nấu rảnh tay từ popup. MainActivity ghi vào đây khi bấm popup; MainScaffold đọc, mở màn, rồi xóa.
 * Chỉ là bộ nhớ trong tiến trình, không lưu đĩa: nếu app bị tắt hẳn thì bấm popup vẫn mở app nhưng không tự nấu.
 */
object ReminderOpenRequest {
    private val state = MutableStateFlow<String?>(null)
    val recipeId: StateFlow<String?> = state.asStateFlow()

    fun request(raw: String?) { state.value = ReminderIntents.recipeIdFromExtra(raw) }
    fun consume() { state.value = null }
}
