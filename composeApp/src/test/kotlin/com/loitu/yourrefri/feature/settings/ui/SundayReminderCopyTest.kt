package com.loitu.yourrefri.feature.settings.ui

import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SundayReminderCopyTest {
    @Test
    fun label_formatsHoursAndMinutesWithPadding() {
        assertEquals("Chủ Nhật nhắc đi chợ lúc 08:00", ReminderCopy.sundayTimeLabel(LocalTime(8, 0)))
        assertEquals("Chủ Nhật nhắc đi chợ lúc 07:05", ReminderCopy.sundayTimeLabel(LocalTime(7, 5)))
        assertEquals("Chủ Nhật nhắc đi chợ lúc 18:30", ReminderCopy.sundayTimeLabel(LocalTime(18, 30)))
    }

    @Test
    fun label_avoidsBannedWords() {
        val text = ReminderCopy.sundayTimeLabel(LocalTime(8, 0))
        listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên").forEach { assertFalse(text.contains(it, ignoreCase = true)) }
    }

    @Test
    fun weekdayReminderTimeLabel_saysWeekdaysOnly() {
        assertEquals("Ngày thường lúc 17:30", SettingsPresentation.reminderTime)
    }

    @Test
    fun alreadyPresentHint_isShortAndKeepsTheMeaning() {
        assertEquals(
            "Món này đã có trong tủ. Bấm bên dưới nếu vừa mua thêm 1.",
            com.loitu.yourrefri.feature.pantry.ui.AddFoodSheetCopy.alreadyPresentWithIncrease,
        )
    }

    @Test
    fun sundayTimeOf_convertsStoredMinutes() {
        assertEquals(LocalTime(8, 0), sundayTimeOf(480))
        assertEquals(LocalTime(0, 0), sundayTimeOf(0))
        assertEquals(LocalTime(23, 59), sundayTimeOf(1439))
        assertEquals(LocalTime(7, 5), sundayTimeOf(425))
    }

    @Test
    fun sundayTimeOf_fallsBackTo0800ForInvalidStoredValue() {
        assertEquals(LocalTime(8, 0), sundayTimeOf(-1))
        assertEquals(LocalTime(8, 0), sundayTimeOf(1440))
        assertEquals(LocalTime(8, 0), sundayTimeOf(Int.MAX_VALUE))
    }

    @Test
    fun minutesOf_isInverseOfSundayTimeOf() {
        assertEquals(480, minutesOf(LocalTime(8, 0)))
        assertEquals(425, minutesOf(LocalTime(7, 5)))
        assertEquals(1439, minutesOf(LocalTime(23, 59)))
    }
}
