package com.loitu.yourrefri.feature.settings.ui

import com.loitu.yourrefri.feature.notice.logic.DEFAULT_WEEKDAY_REMINDER_TIME
import com.loitu.yourrefri.feature.notice.logic.clampWeekdayReminderTime
import kotlinx.datetime.LocalTime

internal object ReminderCopy {
    const val title = "Bật nhắc lúc 16:00"
    const val subtitle = "Tối đa 1 lời nhắc mỗi ngày, hạn là ước tính. Tắt cũng không sao, mở app vẫn thấy đủ."

    fun sundayTimeLabel(time: LocalTime): String =
        "Chủ Nhật nhắc đi chợ lúc ${reminderTimeLabel(time.hour, time.minute)}"

    fun weekdayTimeLabel(time: LocalTime): String =
        "Ngày thường lúc ${reminderTimeLabel(time.hour, time.minute)}"
}

private const val DEFAULT_SUNDAY_MINUTES = 8 * 60

/** Đọc giờ Chủ Nhật đã lưu (số phút trong ngày); giá trị hỏng thì quay về 8:00. */
internal fun sundayTimeOf(storedMinutes: Int): LocalTime {
    val minutes = if (storedMinutes in 0..1439) storedMinutes else DEFAULT_SUNDAY_MINUTES
    return LocalTime(minutes / 60, minutes % 60)
}

internal fun weekdayTimeOf(storedMinutes: Int): LocalTime =
    if (storedMinutes in 0..1439) {
        clampWeekdayReminderTime(LocalTime(storedMinutes / 60, storedMinutes % 60))
    } else {
        DEFAULT_WEEKDAY_REMINDER_TIME
    }

internal fun minutesOf(time: LocalTime): Int = time.hour * 60 + time.minute
