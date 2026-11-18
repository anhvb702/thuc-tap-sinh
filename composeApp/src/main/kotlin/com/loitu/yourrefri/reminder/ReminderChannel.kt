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
}
