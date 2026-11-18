@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.loitu.yourrefri.feature.notice.logic

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

// R0-02 (Q5, 2026-11-16): mốc ngày thường đổi từ 17:30 sang 16:00 (UTC+7 = 09:00Z, UTC-5 = 21:00Z).
class ReminderTriggerTest {
    @Test
    fun disabled_returnsNoTrigger() {
        assertNull(nextReminderTrigger(false, Instant.parse("2026-11-05T10:00:00Z"), TimeZone.UTC))
    }

    @Test
    fun enabledBefore1600_usesTodayLocalTime() {
        assertEquals(
            Instant.parse("2026-11-05T09:00:00Z"),
            nextReminderTrigger(true, Instant.parse("2026-11-05T08:59:59.999Z"), TimeZone.of("UTC+07:00")),
        )
    }

    @Test
    fun enabledAtOrAfter1600_usesNextDay() {
        assertEquals(
            Instant.parse("2026-11-06T09:00:00Z"),
            nextReminderTrigger(true, Instant.parse("2026-11-05T09:00:00Z"), TimeZone.of("UTC+07:00")),
        )
    }

    @Test
    fun timezone_keepsWallClockTime() {
        val now = Instant.parse("2026-11-05T23:00:00Z")
        assertEquals(Instant.parse("2026-11-06T09:00:00Z"), nextReminderTrigger(true, now, TimeZone.of("UTC+07:00")))
        assertEquals(Instant.parse("2026-11-06T21:00:00Z"), nextReminderTrigger(true, now, TimeZone.of("UTC-05:00")))
    }

    @Test
    fun dstTransition_keepsLocal1600() {
        assertEquals(
            Instant.parse("2026-03-08T20:00:00Z"),
            nextReminderTrigger(true, Instant.parse("2026-03-07T23:00:00Z"), TimeZone.of("America/New_York")),
        )
    }
}
