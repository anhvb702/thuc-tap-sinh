package com.loitu.yourrefri.feature.settings.ui

import kotlinx.datetime.LocalTime

internal object ReminderCopy {
    // Căn cứ quyết định Q5/Q13a (2026-11-16): Giờ nhắc ngày thường đổi sang mặc định 16:00.
    const val title = "Bật nhắc lúc 16:00"
    const val subtitle = "Tối đa 1 lời nhắc mỗi ngày, hạn là ước tính. Tắt cũng không sao, mở app vẫn thấy đủ."

    fun sundayTimeLabel(time: LocalTime): String =
        "Chủ Nhật nhắc đi chợ lúc ${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"

    fun weekdayTimeLabel(time: LocalTime): String =
        "Ngày thường nhắc lúc ${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
}

private const val DEFAULT_SUNDAY_MINUTES = 8 * 60

/** Đọc giờ Chủ Nhật đã lưu (số phút trong ngày); giá trị hỏng thì quay về 8:00. */
internal fun sundayTimeOf(storedMinutes: Int): LocalTime {
    val minutes = if (storedMinutes in 0..1439) storedMinutes else DEFAULT_SUNDAY_MINUTES
    return LocalTime(minutes / 60, minutes % 60)
}

internal fun minutesOf(time: LocalTime): Int = time.hour * 60 + time.minute

// Căn cứ quyết định Q5/Q13a (2026-11-16): Giờ nhắc ngày thường đổi sang mặc định 16:00 (14:00–20:00).
private const val DEFAULT_WEEKDAY_MINUTES = 16 * 60

/** Đọc giờ ngày thường đã lưu; chỉ chấp nhận dải 14:00–20:00 (840..1200 phút), còn lại quay về mặc định 16:00. */
internal fun weekdayTimeOf(storedMinutes: Int): LocalTime {
    val minutes = if (storedMinutes in (14 * 60)..(20 * 60)) storedMinutes else DEFAULT_WEEKDAY_MINUTES
    return LocalTime(minutes / 60, minutes % 60)
}

/** Kẹp giờ ngày thường vào khung 14:00–20:00 theo quyết định Q5. */
internal fun coerceWeekdayTime(time: LocalTime): LocalTime {
    val clamped = minutesOf(time).coerceIn(14 * 60, 20 * 60)
    return LocalTime(clamped / 60, clamped % 60)
}
