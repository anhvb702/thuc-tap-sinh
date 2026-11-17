@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.loitu.yourrefri.feature.notice.logic

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class ReminderTriggerTest {
    @Test
    fun disabled_returnsNoTrigger() {
        assertNull(nextReminderTrigger(false, Instant.parse("2026-11-05T10:00:00Z"), TimeZone.UTC))
    }

    // Căn cứ quyết định Q5/Q13a (2026-11-16): Giờ nhắc ngày thường đổi sang mặc định 16:00 (14:00–20:00), thông báo tối đa 3 món.
    @Test
    fun enabledBefore1600_usesTodayLocalTime() {
        assertEquals(
            Instant.parse("2026-11-05T09:00:00Z"),
            nextReminderTrigger(true, Instant.parse("2026-11-05T08:59:59.999Z"), TimeZone.of("UTC+07:00")),
        )
    }

    // Căn cứ quyết định Q5/Q13a (2026-11-16): Giờ nhắc ngày thường đổi sang mặc định 16:00 (14:00–20:00).
    @Test
    fun enabledAtOrAfter1600_usesNextDay() {
        assertEquals(
            Instant.parse("2026-11-06T09:00:00Z"),
            nextReminderTrigger(true, Instant.parse("2026-11-05T09:00:00Z"), TimeZone.of("UTC+07:00")),
        )
    }

    // Căn cứ quyết định Q5/Q13a (2026-11-16): Giờ nhắc ngày thường đổi sang mặc định 16:00 (14:00–20:00).
    @Test
    fun timezone_keepsWallClockTime() {
        val now = Instant.parse("2026-11-05T23:00:00Z")
        assertEquals(Instant.parse("2026-11-06T09:00:00Z"), nextReminderTrigger(true, now, TimeZone.of("UTC+07:00")))
        assertEquals(Instant.parse("2026-11-06T21:00:00Z"), nextReminderTrigger(true, now, TimeZone.of("UTC-05:00")))
    }

    // Căn cứ quyết định Q5/Q13a (2026-11-16): Giờ nhắc ngày thường đổi sang mặc định 16:00 (14:00–20:00).
    @Test
    fun dstTransition_keepsLocal1600() {
        assertEquals(
            Instant.parse("2026-03-08T20:00:00Z"),
            nextReminderTrigger(true, Instant.parse("2026-03-07T23:00:00Z"), TimeZone.of("America/New_York")),
        )
    }

    @Test
    fun frozenDinnerDay_shiftsSingleTriggerTo0800() {
        val frozenDay = LocalDate(2026, 11, 5)
        val tz = TimeZone.of("UTC+07:00")
        assertEquals(
            Instant.parse("2026-11-05T01:00:00Z"),
            nextDailyTrigger(
                enabled = true,
                now = Instant.parse("2026-11-05T00:30:00Z"),
                tz = tz,
                sundayTime = LocalTime(8, 0),
                weekdayTime = LocalTime(16, 0),
                hasFrozenDinnerForDay = { it == frozenDay },
            ),
        )
        // Qodo PR #9 Finding 3: khi 08:00 ngày có món đông đã qua, rơi về weekdayTime (16:00) cùng ngày.
        assertEquals(
            Instant.parse("2026-11-05T09:00:00Z"),
            nextDailyTrigger(
                enabled = true,
                now = Instant.parse("2026-11-05T01:00:00Z"),
                tz = tz,
                sundayTime = LocalTime(8, 0),
                weekdayTime = LocalTime(16, 0),
                hasFrozenDinnerForDay = { it == frozenDay },
            ),
        )
    }
}
