@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.loitu.yourrefri.feature.notice.logic

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

    @Test
    fun enabledBefore1730_usesTodayLocalTime() {
        assertEquals(
            Instant.parse("2026-11-05T10:30:00Z"),
            nextReminderTrigger(true, Instant.parse("2026-11-05T10:29:59.999Z"), TimeZone.of("UTC+07:00")),
        )
    }

    @Test
    fun enabledAtOrAfter1730_usesNextDay() {
        assertEquals(
            Instant.parse("2026-11-06T10:30:00Z"),
            nextReminderTrigger(true, Instant.parse("2026-11-05T10:30:00Z"), TimeZone.of("UTC+07:00")),
        )
    }

    @Test
    fun timezone_keepsWallClockTime() {
        val now = Instant.parse("2026-11-05T23:00:00Z")
        assertEquals(Instant.parse("2026-11-06T10:30:00Z"), nextReminderTrigger(true, now, TimeZone.of("UTC+07:00")))
        assertEquals(Instant.parse("2026-11-06T22:30:00Z"), nextReminderTrigger(true, now, TimeZone.of("UTC-05:00")))
    }

    @Test
    fun dstTransition_keepsLocal1730() {
        assertEquals(
            Instant.parse("2026-03-08T21:30:00Z"),
            nextReminderTrigger(true, Instant.parse("2026-03-07T23:00:00Z"), TimeZone.of("America/New_York")),
        )
    }
}
