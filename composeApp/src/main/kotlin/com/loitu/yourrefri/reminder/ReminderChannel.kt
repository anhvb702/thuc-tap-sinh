package com.loitu.yourrefri.reminder

import android.app.NotificationManager

/**
 * Kênh nhắc ăn uống. Mức quan trọng của kênh không đổi được sau khi tạo, nên kênh mới dùng id khác;
 * kênh cũ (IMPORTANCE_DEFAULT, không popup) bị xóa khi app tạo kênh mới.
 */
object ReminderChannel {
    const val ID = "daily_reminder_hi"
    const val LEGACY_ID = "daily_reminder"
    const val NAME = "Nhắc nhở YourRefri"
    const val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH

    /**
     * Người dùng đã chặn kênh cũ trong Cài đặt Android thì không gửi lại qua kênh mới.
     * legacyImportance = null nghĩa là chưa có kênh cũ (cài mới hoặc đã xóa).
     */
    fun shouldNotify(legacyImportance: Int?): Boolean = importanceFor(legacyImportance) != NotificationManager.IMPORTANCE_NONE

    /**
     * Giữ lựa chọn của người dùng ở kênh cũ: đã chặn thì không gửi; đã chọn im lặng thì kênh mới cũng im lặng
     * (không popup, không âm). Chưa có kênh cũ hoặc kênh cũ là mặc định trở lên thì dùng IMPORTANCE.
     */
    fun importanceFor(legacyImportance: Int?): Int = when (legacyImportance) {
        null -> IMPORTANCE
        NotificationManager.IMPORTANCE_NONE -> NotificationManager.IMPORTANCE_NONE
        NotificationManager.IMPORTANCE_MIN, NotificationManager.IMPORTANCE_LOW -> NotificationManager.IMPORTANCE_LOW
        else -> IMPORTANCE
    }

    fun shouldVibrate(legacyImportance: Int?, legacyVibrate: Boolean): Boolean = when {
        importanceFor(legacyImportance) != NotificationManager.IMPORTANCE_HIGH -> false
        legacyImportance == null || legacyImportance == NotificationManager.IMPORTANCE_DEFAULT -> true
        else -> legacyVibrate
    }
}
