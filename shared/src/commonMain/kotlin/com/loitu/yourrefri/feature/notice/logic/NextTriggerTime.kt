@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.loitu.yourrefri.feature.notice.logic

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private val WEEKDAY_REMINDER_TIME = LocalTime(17, 30)

/** Chủ Nhật dùng giờ riêng (nhắc đi chợ), các ngày khác 17:30. */
fun reminderTimeFor(date: LocalDate, sundayTime: LocalTime): LocalTime =
    if (date.dayOfWeek == DayOfWeek.SUNDAY) sundayTime else WEEKDAY_REMINDER_TIME

/**
 * Mốc nhắc kế tiếp. `sundayTime == null` giữ nguyên hành vi cũ (17:30 mọi ngày); có giá trị thì Chủ Nhật dùng giờ đó.
 * Mỗi ngày đúng một mốc nên tối đa một thông báo mỗi ngày.
 */
fun nextReminderTrigger(enabled: Boolean, now: Instant, tz: TimeZone, sundayTime: LocalTime? = null): Instant? {
    if (!enabled) return null
    if (sundayTime == null) return nextTriggerTime(now, tz, WEEKDAY_REMINDER_TIME)
    val today = now.toLocalDateTime(tz).date
    // Cộng ngày lịch rồi mới ghép giờ địa phương để giữ đúng giờ treo tường khi chuyển DST.
    return (0..7).asSequence()
        .map { offset -> today.plus(offset, DateTimeUnit.DAY) }
        .map { date -> LocalDateTime(date, reminderTimeFor(date, sundayTime)).toInstant(tz) }
        .first { it > now }
}

fun nextTriggerTime(now: Instant, tz: TimeZone, hour: LocalTime): Instant {
    val date = now.toLocalDateTime(tz).date
    val today = LocalDateTime(date, hour).toInstant(tz)
    return if (today > now) today else {
        // Cộng ngày lịch để giữ giờ địa phương khi chuyển DST.
        LocalDateTime(date.plus(1, DateTimeUnit.DAY), hour).toInstant(tz)
    }
}
