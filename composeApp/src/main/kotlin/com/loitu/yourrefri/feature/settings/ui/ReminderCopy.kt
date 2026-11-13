package com.loitu.yourrefri.feature.settings.ui

import kotlinx.datetime.LocalTime

internal object ReminderCopy {
    const val title = "Bật nhắc lúc 17:30"
    const val subtitle = "Tối đa 1 lời nhắc mỗi ngày, hạn là ước tính. Tắt cũng không sao, mở app vẫn thấy đủ."

    fun sundayTimeLabel(time: LocalTime): String =
        "Chủ Nhật nhắc đi chợ lúc ${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
}

private const val DEFAULT_SUNDAY_MINUTES = 8 * 60

/** Đọc giờ Chủ Nhật đã lưu (số phút trong ngày); giá trị hỏng thì quay về 8:00. */
internal fun sundayTimeOf(storedMinutes: Int): LocalTime {
    val minutes = if (storedMinutes in 0..1439) storedMinutes else DEFAULT_SUNDAY_MINUTES
    return LocalTime(minutes / 60, minutes % 60)
}

internal fun minutesOf(time: LocalTime): Int = time.hour * 60 + time.minute
