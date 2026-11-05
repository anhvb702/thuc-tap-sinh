@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.loitu.yourrefri.feature.notice.logic

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun nextTriggerTime(now: Instant, tz: TimeZone, hour: LocalTime): Instant {
    val date = now.toLocalDateTime(tz).date
    val today = LocalDateTime(date, hour).toInstant(tz)
    return if (today > now) today else {
        // Cộng ngày lịch để giữ giờ địa phương khi chuyển DST.
        LocalDateTime(date.plus(1, DateTimeUnit.DAY), hour).toInstant(tz)
    }
}
