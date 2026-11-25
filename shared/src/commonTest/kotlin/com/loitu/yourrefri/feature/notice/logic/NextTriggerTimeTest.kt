@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.loitu.yourrefri.feature.notice.logic

import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class NextTriggerTimeTest {
    private val target = LocalTime(17, 30)
    private fun instant(value: String) = Instant.parse(value)
    private fun check(now: String, zone: String, time: LocalTime, expected: String) {
        assertEquals(instant(expected), nextTriggerTime(instant(now), TimeZone.of(zone), time))
    }

    @Test
    fun beforeTarget_usesToday() {
        check("2026-11-05T17:29:59.999Z", "UTC", target, "2026-11-05T17:30:00Z")
    }

    @Test
    fun exactTarget_usesTomorrow() {
        check("2026-11-05T17:30:00Z", "UTC", target, "2026-11-06T17:30:00Z")
    }

    @Test
    fun afterTarget_usesTomorrow() {
        check("2026-11-05T17:30:00.001Z", "UTC", target, "2026-11-06T17:30:00Z")
    }

    @Test
    fun timeZone_changesLocalDateAndInstant() {
        check("2026-11-05T23:00:00Z", "UTC+07:00", target, "2026-11-06T10:30:00Z")
        check("2026-11-05T23:00:00Z", "UTC-05:00", target, "2026-11-06T22:30:00Z")
        check("2026-11-05T01:00:00Z", "UTC-05:00", target, "2026-11-05T22:30:00Z")
    }

    @Test
    fun midnightAndYearBoundary_useCalendarDay() {
        check("2026-12-31T23:59:59Z", "UTC", LocalTime(0, 0), "2027-01-01T00:00:00Z")
        check("2026-11-05T23:58:59Z", "UTC", LocalTime(23, 59), "2026-11-05T23:59:00Z")
        check("2026-11-05T23:59:01Z", "UTC", LocalTime(23, 59), "2026-11-06T23:59:00Z")
    }

    @Test
    fun springForward_usesCalendarDayNot24Hours() {
        check("2026-03-07T23:00:00Z", "America/New_York", target, "2026-03-08T21:30:00Z")
    }

    @Test
    fun fallBack_usesCalendarDayNot24Hours() {
        check("2026-10-31T22:00:00Z", "America/New_York", target, "2026-11-01T22:30:00Z")
    }

    @Test
    fun gapAndOverlap_followDateTimeResolution() {
        check("2026-03-08T05:00:00Z", "America/New_York", LocalTime(2, 30), "2026-03-08T07:30:00Z")
        check("2026-11-01T04:00:00Z", "America/New_York", LocalTime(1, 30), "2026-11-01T05:30:00Z")
        check("2026-11-01T05:45:00Z", "America/New_York", LocalTime(1, 30), "2026-11-02T06:30:00Z")
    }

    @Test
    fun defaultWeekdayTime_is1600() {
        assertEquals(LocalTime(16, 0), DEFAULT_WEEKDAY_REMINDER_TIME)
        assertEquals(LocalTime(14, 0), MIN_WEEKDAY_REMINDER_TIME)
        assertEquals(LocalTime(20, 0), MAX_WEEKDAY_REMINDER_TIME)
    }

    @Test
    fun clamp_lowerBound() {
        assertEquals(LocalTime(14, 0), clampWeekdayReminderTime(LocalTime(13, 59)))
        assertEquals(LocalTime(14, 0), clampWeekdayReminderTime(LocalTime(14, 0)))
        assertEquals(LocalTime(14, 1), clampWeekdayReminderTime(LocalTime(14, 1)))
    }

    @Test
    fun clamp_upperBound_checksMinutes() {
        assertEquals(LocalTime(20, 0), clampWeekdayReminderTime(LocalTime(20, 0)))
        assertEquals(LocalTime(20, 0), clampWeekdayReminderTime(LocalTime(20, 1)))
        assertEquals(LocalTime(20, 0), clampWeekdayReminderTime(LocalTime(20, 30)))
        assertEquals(LocalTime(19, 59), clampWeekdayReminderTime(LocalTime(19, 59)))
    }

    @Test
    fun reminderTimeFor_weekdayUsesWeekdayTime_sundayKeepsOwn() {
        val sunday = LocalTime(9, 0)
        val monday = kotlinx.datetime.LocalDate(2026, 11, 23)
        val sundayDate = kotlinx.datetime.LocalDate(2026, 11, 29)
        assertEquals(LocalTime(16, 0), reminderTimeFor(monday, sunday))
        assertEquals(LocalTime(18, 30), reminderTimeFor(monday, sunday, LocalTime(18, 30)))
        assertEquals(LocalTime(14, 0), reminderTimeFor(monday, sunday, LocalTime(8, 0)))
        assertEquals(sunday, reminderTimeFor(sundayDate, sunday, LocalTime(18, 30)))
    }

    @Test
    fun nextReminderTrigger_weekdayTimeApplied_sundayNotOverridden() {
        val utc = TimeZone.UTC
        // Không có sundayTime: giờ ngày thường áp mọi ngày và được kẹp.
        assertEquals(instant("2026-11-25T18:30:00Z"), nextReminderTrigger(true, instant("2026-11-25T10:00:00Z"), utc, null, LocalTime(18, 30)))
        assertEquals(instant("2026-11-25T20:00:00Z"), nextReminderTrigger(true, instant("2026-11-25T10:00:00Z"), utc, null, LocalTime(20, 30)))
        assertEquals(instant("2026-11-25T16:00:00Z"), nextReminderTrigger(true, instant("2026-11-25T10:00:00Z"), utc))
        // Thứ 7 -> dùng weekdayTime; sau đó Chủ Nhật giữ sundayTime.
        val sunday = LocalTime(9, 0)
        assertEquals(instant("2026-11-28T15:00:00Z"), nextReminderTrigger(true, instant("2026-11-28T00:00:00Z"), utc, sunday, LocalTime(15, 0)))
        assertEquals(instant("2026-11-29T09:00:00Z"), nextReminderTrigger(true, instant("2026-11-28T16:00:00Z"), utc, sunday, LocalTime(15, 0)))
        assertEquals(instant("2026-11-23T14:00:00Z"), nextReminderTrigger(true, instant("2026-11-23T00:00:00Z"), utc, sunday, LocalTime(13, 0)))
        assertEquals(null, nextReminderTrigger(false, instant("2026-11-23T00:00:00Z"), utc, sunday, LocalTime(15, 0)))
    }
}
