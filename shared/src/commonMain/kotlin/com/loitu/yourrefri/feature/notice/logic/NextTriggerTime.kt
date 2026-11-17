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

// Căn cứ quyết định Q5/Q13a (2026-11-16): Giờ nhắc ngày thường đổi sang mặc định 16:00 (14:00–20:00).
val WEEKDAY_REMINDER_TIME = LocalTime(16, 0)

/**
 * Chủ Nhật dùng giờ riêng (nhắc đi chợ); ngày thường có đồ ngăn đông cho bữa tối dời sang 08:00,
 * các ngày khác dùng [weekdayTime].
 */
fun reminderTimeFor(
    date: LocalDate,
    sundayTime: LocalTime,
    weekdayTime: LocalTime = WEEKDAY_REMINDER_TIME,
    hasFrozenDinner: Boolean = false,
): LocalTime = when {
    date.dayOfWeek == DayOfWeek.SUNDAY -> sundayTime
    hasFrozenDinner -> DEFROST_REMINDER_TIME
    else -> weekdayTime
}

/**
 * Mốc nhắc kế tiếp. Chủ Nhật dùng `sundayTime` nếu có; ngày thường có đồ ngăn đông cho bữa tối
 * dời sang 08:00 (`DEFROST_REMINDER_TIME`); các ngày khác dùng `weekdayTime`.
 * Mỗi ngày đúng một mốc nên tối đa một thông báo mỗi ngày (Q4, Q23).
 */
fun nextReminderTrigger(
    enabled: Boolean,
    now: Instant,
    tz: TimeZone,
    sundayTime: LocalTime? = null,
    weekdayTime: LocalTime = WEEKDAY_REMINDER_TIME,
    hasFrozenDinnerForDay: (LocalDate) -> Boolean = { false },
): Instant? {
    if (!enabled) return null
    val effectiveSundayTime = sundayTime ?: weekdayTime
    val today = now.toLocalDateTime(tz).date
    // Cộng ngày lịch rồi mới ghép giờ địa phương để giữ đúng giờ treo tường khi chuyển DST.
    return (0..7).asSequence()
        .map { offset -> today.plus(offset, DateTimeUnit.DAY) }
        .map { date ->
            val time = reminderTimeFor(date, effectiveSundayTime, weekdayTime, hasFrozenDinnerForDay(date))
            LocalDateTime(date, time).toInstant(tz)
        }
        .first { it > now }
}

/** Alias cho [nextReminderTrigger] nhấn mạnh cơ chế 1 thông báo/ngày hợp nhất (R6-01). */
fun nextDailyTrigger(
    enabled: Boolean,
    now: Instant,
    tz: TimeZone,
    sundayTime: LocalTime? = null,
    weekdayTime: LocalTime = WEEKDAY_REMINDER_TIME,
    hasFrozenDinnerForDay: (LocalDate) -> Boolean = { false },
): Instant? = nextReminderTrigger(enabled, now, tz, sundayTime, weekdayTime, hasFrozenDinnerForDay)

fun nextTriggerTime(now: Instant, tz: TimeZone, hour: LocalTime): Instant {
    val date = now.toLocalDateTime(tz).date
    val today = LocalDateTime(date, hour).toInstant(tz)
    return if (today > now) today else {
        // Cộng ngày lịch để giữ giờ địa phương khi chuyển DST.
        LocalDateTime(date.plus(1, DateTimeUnit.DAY), hour).toInstant(tz)
    }
}

val DEFROST_REMINDER_TIME = LocalTime(8, 0)

/** Next local 08:00 trigger for the freezer preparation reminder. */
fun nextDefrostTrigger(enabled: Boolean, now: Instant, tz: TimeZone): Instant? =
    if (enabled) nextTriggerTime(now, tz, DEFROST_REMINDER_TIME) else null
