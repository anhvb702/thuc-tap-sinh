@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.loitu.yourrefri.feature.notice.logic

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class DefrostTriggerTest {
    @Test
    fun nextMorningDefrostTriggerUsesLocalEightAm() {
        assertEquals(Instant.parse("2026-11-13T01:00:00Z"), nextDefrostTrigger(true,
            Instant.parse("2026-11-12T23:00:00Z"), TimeZone.of("UTC+07:00")))
        assertEquals(Instant.parse("2026-11-14T01:00:00Z"), nextDefrostTrigger(true,
            Instant.parse("2026-11-13T01:00:00Z"), TimeZone.of("UTC+07:00")))
    }

    @Test
    fun disabledDefrostTriggerIsAbsent() {
        assertNull(nextDefrostTrigger(false, Instant.parse("2026-11-12T23:00:00Z"), TimeZone.of("UTC+07:00")))
    }

    @Test
    fun unifiedTriggerUses0800OnlyOnFrozenDinnerDays() {
        val friday = LocalDate(2026, 11, 13)
        val tz = TimeZone.of("UTC+07:00")
        assertEquals(
            Instant.parse("2026-11-13T01:00:00Z"),
            nextDailyTrigger(true, Instant.parse("2026-11-12T23:00:00Z"), tz, LocalTime(8, 0), LocalTime(16, 0)) { it == friday },
        )
        assertEquals(
            Instant.parse("2026-11-13T09:00:00Z"),
            nextDailyTrigger(true, Instant.parse("2026-11-12T23:00:00Z"), tz, LocalTime(8, 0), LocalTime(16, 0)) { false },
        )
    }
}
