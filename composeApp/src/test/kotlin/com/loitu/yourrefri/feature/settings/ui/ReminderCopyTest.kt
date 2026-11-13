package com.loitu.yourrefri.feature.settings.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReminderCopyTest {
    @Test fun title_keeps1730() = assertEquals("Bật nhắc lúc 17:30", ReminderCopy.title)
    // A-14 (2026-11-13, người dùng cho phép sửa): rút gọn; Chủ Nhật là lời nhắc đi chợ nên bỏ "nên dùng trước".
    @Test fun subtitle_hasEstimateWording() {
        assertTrue(ReminderCopy.subtitle.contains("hạn là ước tính"))
    }
    @Test fun subtitle_statesAtMostOncePerDay() = assertTrue(ReminderCopy.subtitle.contains("Tối đa 1 lời nhắc mỗi ngày"))
    @Test fun subtitle_isShorterThan90Chars() = assertTrue(ReminderCopy.subtitle.length < 90)
    @Test fun subtitle_keepsWayOut() = assertTrue(ReminderCopy.subtitle.contains("Tắt cũng không sao, mở app vẫn thấy đủ."))
    @Test fun subtitle_isShorterThan120Chars() = assertTrue(ReminderCopy.subtitle.length < 120)
    @Test fun reminderCopy_hasNoBannedWords() {
        for (word in listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn")) {
            assertFalse((ReminderCopy.title + " " + ReminderCopy.subtitle).contains(word, ignoreCase = true))
        }
    }
}
