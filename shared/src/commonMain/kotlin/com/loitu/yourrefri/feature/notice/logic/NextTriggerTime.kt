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

val DEFAULT_WEEKDAY_REMINDER_TIME = LocalTime(16, 0)
val MIN_WEEKDAY_REMINDER_TIME = LocalTime(14, 0)
val MAX_WEEKDAY_REMINDER_TIME = LocalTime(20, 0)

/** Kẹp giờ nhắc ngày thường vào khoảng 14:00-20:00 (so cả giờ và phút). */
fun clampWeekdayReminderTime(time: LocalTime): LocalTime = when {
    time < MIN_WEEKDAY_REMINDER_TIME -> MIN_WEEKDAY_REMINDER_TIME
    time > MAX_WEEKDAY_REMINDER_TIME -> MAX_WEEKDAY_REMINDER_TIME
    else -> time
}

/** Ngày nhắc đi chợ cuối tuần: Thứ 7 hoặc Chủ Nhật. */
enum class WeekendReminderDay(val dayOfWeek: DayOfWeek) {
    SATURDAY(DayOfWeek.SATURDAY),
    SUNDAY(DayOfWeek.SUNDAY),
}

/** Chủ Nhật dùng giờ riêng (nhắc đi chợ), Thứ 2-Thứ 7 dùng giờ ngày thường đã kẹp (mặc định 16:00). */
fun reminderTimeFor(
    date: LocalDate,
    sundayTime: LocalTime,
    weekdayTime: LocalTime = DEFAULT_WEEKDAY_REMINDER_TIME,
): LocalTime =
    if (date.dayOfWeek == DayOfWeek.SUNDAY) sundayTime else clampWeekdayReminderTime(weekdayTime)

/**
 * Giờ nhắc của một ngày. Ngày cuối tuần đã chọn dùng [weekendTime]; ngày khác dùng giờ ngày thường đã kẹp
 * nếu [weekdayEnabled], không thì `null` (không nhắc).
 */
fun reminderTimeFor(
    date: LocalDate,
    weekendTime: LocalTime,
    weekendDay: WeekendReminderDay,
    weekdayEnabled: Boolean,
    weekdayTime: LocalTime = DEFAULT_WEEKDAY_REMINDER_TIME,
): LocalTime? = when {
    date.dayOfWeek == weekendDay.dayOfWeek -> weekendTime
    weekdayEnabled -> clampWeekdayReminderTime(weekdayTime)
    else -> null
}

/**
 * Mốc nhắc kế tiếp. `sundayTime == null` giữ nguyên hành vi cũ (16:00 mọi ngày); có giá trị thì ngày cuối tuần
 * đã chọn ([weekendDay]) dùng giờ đó. `weekdayEnabled = false` chỉ nhắc vào cuối tuần.
 * Mỗi ngày đúng một mốc nên tối đa một thông báo mỗi ngày.
 */
fun nextReminderTrigger(
    enabled: Boolean,
    now: Instant,
    tz: TimeZone,
    sundayTime: LocalTime? = null,
    weekdayTime: LocalTime = DEFAULT_WEEKDAY_REMINDER_TIME,
    weekendDay: WeekendReminderDay = WeekendReminderDay.SUNDAY,
    weekdayEnabled: Boolean = true,
): Instant? {
    if (!enabled) return null
    if (sundayTime == null) {
        if (!weekdayEnabled) return null
        return nextTriggerTime(now, tz, clampWeekdayReminderTime(weekdayTime))
    }
    val today = now.toLocalDateTime(tz).date
    // Cộng ngày lịch rồi mới ghép giờ địa phương để giữ đúng giờ treo tường khi chuyển DST.
    return (0..7).asSequence()
        .map { offset -> today.plus(offset, DateTimeUnit.DAY) }
        .mapNotNull { date ->
            reminderTimeFor(date, sundayTime, weekendDay, weekdayEnabled, weekdayTime)
                ?.let { LocalDateTime(date, it).toInstant(tz) }
        }
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
