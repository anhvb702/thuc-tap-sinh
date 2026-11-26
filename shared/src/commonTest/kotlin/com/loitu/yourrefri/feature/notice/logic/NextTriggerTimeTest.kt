@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.loitu.yourrefri.feature.notice.logic

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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

    private val weekendTime = LocalTime(9, 0)
    private val dateSat = LocalDate(2026, 11, 14)
    private val dateFri = LocalDate(2026, 11, 13)
    private val dateSun = LocalDate(2026, 11, 15)

    @Test
    fun reminderTimeFor_weekendOverload_pickedDayUsesWeekendTime() {
        assertEquals(weekendTime, reminderTimeFor(dateSat, weekendTime, WeekendReminderDay.SATURDAY, weekdayEnabled = true))
        assertEquals(LocalTime(16, 0), reminderTimeFor(dateFri, weekendTime, WeekendReminderDay.SATURDAY, weekdayEnabled = true))
        assertEquals(LocalTime(16, 0), reminderTimeFor(dateSun, weekendTime, WeekendReminderDay.SATURDAY, weekdayEnabled = true))
        assertEquals(LocalTime(20, 0), reminderTimeFor(dateFri, weekendTime, WeekendReminderDay.SATURDAY, true, LocalTime(21, 0)))
    }

    @Test
    fun reminderTimeFor_weekendOverload_weekdayDisabledReturnsNullOnOtherDays() {
        assertNull(reminderTimeFor(dateFri, weekendTime, WeekendReminderDay.SATURDAY, weekdayEnabled = false))
        assertNull(reminderTimeFor(dateSun, weekendTime, WeekendReminderDay.SATURDAY, weekdayEnabled = false))
        assertEquals(weekendTime, reminderTimeFor(dateSat, weekendTime, WeekendReminderDay.SATURDAY, weekdayEnabled = false))
    }

    @Test
    fun weekendOnly_saturday_skipsOtherDaysAndFiresNextSaturday() {
        fun next(now: String) = nextReminderTrigger(
            true, instant(now), TimeZone.UTC, weekendTime,
            weekendDay = WeekendReminderDay.SATURDAY, weekdayEnabled = false,
        )
        assertEquals(instant("2026-11-14T09:00:00Z"), next("2026-11-13T12:00:00Z")) // Thứ 6 -> Thứ 7 mai
        assertEquals(instant("2026-11-21T09:00:00Z"), next("2026-11-15T00:00:00Z")) // Chủ Nhật -> Thứ 7 tuần sau
        assertEquals(instant("2026-11-21T09:00:00Z"), next("2026-11-16T12:00:00Z")) // Thứ 2
        assertEquals(instant("2026-11-14T09:00:00Z"), next("2026-11-14T08:59:59Z")) // Thứ 7 trước giờ
        assertEquals(instant("2026-11-21T09:00:00Z"), next("2026-11-14T09:00:00Z")) // đúng giờ đã qua
        assertEquals(instant("2026-11-21T09:00:00Z"), next("2026-11-14T10:00:00Z")) // Thứ 7 sau giờ
    }

    @Test
    fun weekendOnly_sunday_skipsOtherDaysAndFiresNextSunday() {
        val sundayTime = LocalTime(10, 0)
        fun next(now: String) = nextReminderTrigger(
            true, instant(now), TimeZone.UTC, sundayTime,
            weekendDay = WeekendReminderDay.SUNDAY, weekdayEnabled = false,
        )
        assertEquals(instant("2026-11-15T10:00:00Z"), next("2026-11-14T12:00:00Z")) // Thứ 7 -> Chủ Nhật mai
        assertEquals(instant("2026-11-15T10:00:00Z"), next("2026-11-15T09:00:00Z")) // Chủ Nhật trước giờ
        assertEquals(instant("2026-11-22T10:00:00Z"), next("2026-11-15T10:30:00Z")) // Chủ Nhật sau giờ
        assertEquals(instant("2026-11-22T10:00:00Z"), next("2026-11-16T12:00:00Z")) // Thứ 2
    }

    @Test
    fun weekendOnly_sunday_dstSpringForward_keepsLocalNineAm() {
        // 01/03/2026 là Chủ Nhật; 14:00Z = 09:00 EST. Chuyển sang EDT ngày 08/03 nên 09:00 EDT = 13:00Z (không phải +168h = 14:00Z).
        val newYork = TimeZone.of("America/New_York")
        assertEquals(
            instant("2026-03-08T13:00:00Z"),
            nextReminderTrigger(
                true, instant("2026-03-01T14:00:00Z"), newYork, weekendTime,
                weekendDay = WeekendReminderDay.SUNDAY, weekdayEnabled = false,
            ),
        )
    }

    @Test
    fun weekendPlusWeekday_saturday_usesWeekendTimeOnSaturday_weekdayTimeOnOtherDays() {
        val weekdayTime = LocalTime(18, 30)
        fun next(now: String) = nextReminderTrigger(
            true, instant(now), TimeZone.UTC, weekendTime, weekdayTime,
            weekendDay = WeekendReminderDay.SATURDAY, weekdayEnabled = true,
        )
        assertEquals(instant("2026-11-13T18:30:00Z"), next("2026-11-13T10:00:00Z")) // Thứ 6 dùng giờ ngày thường
        assertEquals(instant("2026-11-14T09:00:00Z"), next("2026-11-14T08:00:00Z")) // Thứ 7 dùng giờ cuối tuần
        assertEquals(instant("2026-11-15T18:30:00Z"), next("2026-11-14T10:00:00Z")) // Chủ Nhật dùng giờ ngày thường
        assertEquals(
            instant("2026-11-16T20:00:00Z"),
            nextReminderTrigger(
                true, instant("2026-11-16T10:00:00Z"), TimeZone.UTC, weekendTime, LocalTime(21, 0),
                weekendDay = WeekendReminderDay.SATURDAY,
            ),
        ) // giờ ngày thường 21:00 bị kẹp về 20:00
    }

    @Test
    fun disabled_alwaysNull_inEveryWeekendMode() {
        for (day in WeekendReminderDay.entries) {
            for (weekdayEnabled in listOf(true, false)) {
                assertNull(nextReminderTrigger(
                    false, instant("2026-11-14T08:00:00Z"), TimeZone.UTC, weekendTime,
                    weekendDay = day, weekdayEnabled = weekdayEnabled,
                ))
            }
        }
    }

    @Test
    fun noWeekendTime_andWeekdayDisabled_returnsNull() {
        assertNull(nextReminderTrigger(true, instant("2026-11-14T08:00:00Z"), TimeZone.UTC, null, weekdayEnabled = false))
    }
}
