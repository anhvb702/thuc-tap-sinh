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

    // Căn cứ quyết định Q5/Q13a (2026-11-16): Giờ nhắc ngày thường đổi sang mặc định 16:00 (14:00–20:00).
    @Test
    fun weekdayReminderTimeLabel_saysWeekdaysOnly() {
        assertEquals("Ngày thường lúc 16:00", SettingsPresentation.reminderTime)
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

    // Căn cứ quyết định Q5/Q13a (2026-11-16): Giờ nhắc ngày thường đổi sang mặc định 16:00, hợp lệ trong khung 14:00–20:00 (840..1200 phút).
    @Test
    fun weekdayTimeOf_uses1600DefaultAndValidStoredValues() {
        assertEquals(LocalTime(16, 0), weekdayTimeOf(-1))
        assertEquals(LocalTime(16, 0), weekdayTimeOf(1440))
        assertEquals(LocalTime(16, 0), weekdayTimeOf(0))
        assertEquals(LocalTime(16, 0), weekdayTimeOf(1439))
        assertEquals(LocalTime(16, 0), weekdayTimeOf(425))
        assertEquals(LocalTime(14, 0), weekdayTimeOf(14 * 60))
        assertEquals(LocalTime(20, 0), weekdayTimeOf(20 * 60))
        assertEquals(LocalTime(16, 30), weekdayTimeOf(16 * 60 + 30))
    }

    // Căn cứ quyết định Q5 (2026-11-16): Kẹp giờ trong khung 14:00–20:00.
    @Test
    fun coerceWeekdayTime_clampsBetween1400And2000() {
        assertEquals(LocalTime(14, 0), coerceWeekdayTime(LocalTime(8, 0)))
        assertEquals(LocalTime(14, 0), coerceWeekdayTime(LocalTime(13, 30)))
        assertEquals(LocalTime(16, 0), coerceWeekdayTime(LocalTime(16, 0)))
        assertEquals(LocalTime(20, 0), coerceWeekdayTime(LocalTime(20, 30)))
        assertEquals(LocalTime(20, 0), coerceWeekdayTime(LocalTime(22, 15)))
        assertEquals(LocalTime(14, 0), coerceWeekdayTime(LocalTime(14, 0)))
        assertEquals(LocalTime(20, 0), coerceWeekdayTime(LocalTime(20, 0)))
    }

    @Test
    fun weekdayTimeLabel_formatsTime() {
        assertEquals("Ngày thường nhắc lúc 07:05", ReminderCopy.weekdayTimeLabel(LocalTime(7, 5)))
    }
}
