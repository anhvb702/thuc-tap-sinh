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
}
