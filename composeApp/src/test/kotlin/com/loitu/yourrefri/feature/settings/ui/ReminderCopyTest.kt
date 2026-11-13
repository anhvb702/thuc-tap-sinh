package com.loitu.yourrefri.feature.settings.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReminderCopyTest {
    @Test fun title_keeps1730() = assertEquals("Bật nhắc lúc 17:30", ReminderCopy.title)
    @Test fun subtitle_hasEstimateAndUseFirstWording() {
        assertTrue(ReminderCopy.subtitle.contains("nên dùng trước"))
        assertTrue(ReminderCopy.subtitle.contains("hạn là ước tính"))
    }
    @Test fun subtitle_statesAtMostOncePerDay() = assertTrue(ReminderCopy.subtitle.contains("Mỗi ngày tối đa 1 lời nhắc"))
    @Test fun subtitle_keepsWayOut() = assertTrue(ReminderCopy.subtitle.contains("Tắt cũng không sao, mở app vẫn thấy đủ."))
    @Test fun subtitle_isShorterThan120Chars() = assertTrue(ReminderCopy.subtitle.length < 120)
    @Test fun reminderCopy_hasNoBannedWords() {
        for (word in listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn")) {
            assertFalse((ReminderCopy.title + " " + ReminderCopy.subtitle).contains(word, ignoreCase = true))
        }
    }
}
