package com.loitu.yourrefri.feature.settings.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ReminderTimeLabelTest {
    @Test
    fun formatsHourAndMinuteWithTwoDigits() {
        assertEquals("16:00", reminderTimeLabel(hour = 16, minute = 0))
        assertEquals("08:05", reminderTimeLabel(hour = 8, minute = 5))
    }

    @Test
    fun sundayLabelUsesTheSameFormat() {
        assertEquals("Chủ Nhật nhắc đi chợ lúc 08:05", ReminderCopy.sundayTimeLabel(kotlinx.datetime.LocalTime(8, 5)))
    }
}
