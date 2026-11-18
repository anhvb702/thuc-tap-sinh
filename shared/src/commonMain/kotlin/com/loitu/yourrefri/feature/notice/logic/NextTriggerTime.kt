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

/** Mặc định ngày thường 16:00 (R0-02, Q5). Màn Cài đặt chỉ được chọn trong khoảng [isWeekdayTimeAllowed]. */
val WEEKDAY_REMINDER_TIME: LocalTime = LocalTime(16, 0)

private val WEEKDAY_TIME_MIN = LocalTime(14, 0)
private val WEEKDAY_TIME_MAX = LocalTime(20, 0)

/** Giờ nhắc ngày thường người dùng được chọn: từ 14:00 đến 20:00, gồm cả hai đầu. */
fun isWeekdayTimeAllowed(time: LocalTime): Boolean = time >= WEEKDAY_TIME_MIN && time <= WEEKDAY_TIME_MAX

/** Chủ Nhật dùng [sundayTime] (nhắc đi chợ), các ngày khác dùng [weekdayTime]. */
fun reminderTimeFor(
    date: LocalDate,
    sundayTime: LocalTime,
    weekdayTime: LocalTime = WEEKDAY_REMINDER_TIME,
): LocalTime =
    if (date.dayOfWeek == DayOfWeek.SUNDAY) sundayTime else weekdayTime

/**
 * Mốc nhắc kế tiếp. `sundayTime == null` dùng [weekdayTime] mọi ngày (hành vi không có giờ Chủ Nhật riêng);
 * có giá trị thì Chủ Nhật dùng giờ đó. Mỗi ngày đúng một mốc nên tối đa một thông báo mỗi ngày.
 */
fun nextReminderTrigger(
    enabled: Boolean,
    now: Instant,
    tz: TimeZone,
    sundayTime: LocalTime? = null,
    weekdayTime: LocalTime = WEEKDAY_REMINDER_TIME,
): Instant? {
    if (!enabled) return null
    if (sundayTime == null) return nextTriggerTime(now, tz, weekdayTime)
    val today = now.toLocalDateTime(tz).date
    // Cộng ngày lịch rồi mới ghép giờ địa phương để giữ đúng giờ treo tường khi chuyển DST.
    return (0..7).asSequence()
        .map { offset -> today.plus(offset, DateTimeUnit.DAY) }
        .map { date -> LocalDateTime(date, reminderTimeFor(date, sundayTime, weekdayTime)).toInstant(tz) }
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
