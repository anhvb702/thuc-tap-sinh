package com.loitu.yourrefri.feature.recipe.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HandsFreeStepsTest {
    @Test
    fun nextAdvancesUntilLastThenStays() {
        var s = HandsFreeState.of(0, 3)
        s = s.next(); assertEquals(1, s.stepIndex)
        s = s.next(); assertEquals(2, s.stepIndex)
        s = s.next(); assertEquals(2, s.stepIndex)
    }

    @Test
    fun previousGoesBackUntilFirstThenStays() {
        var s = HandsFreeState.of(2, 3)
        s = s.previous(); assertEquals(1, s.stepIndex)
        s = s.previous(); assertEquals(0, s.stepIndex)
        s = s.previous(); assertEquals(0, s.stepIndex)
    }

    @Test
    fun firstAndLastFlags() {
        assertTrue(HandsFreeState.of(0, 4).isFirst)
        assertFalse(HandsFreeState.of(0, 4).isLast)
        assertTrue(HandsFreeState.of(3, 4).isLast)
        assertFalse(HandsFreeState.of(3, 4).isFirst)
        val single = HandsFreeState.of(0, 1)
        assertTrue(single.isFirst && single.isLast)
    }

    @Test
    fun ofClampsOutOfRangeIndex() {
        assertEquals(3, HandsFreeState.of(99, 4).stepIndex)
        assertEquals(0, HandsFreeState.of(-5, 4).stepIndex)
        assertEquals(0, HandsFreeState.of(2, 0).stepIndex)
    }

    @Test
    fun labelIsOneBased() {
        assertEquals("Bước 1/4", HandsFreeState.of(0, 4).label)
        assertEquals("Bước 2/4", HandsFreeState.of(1, 4).label)
        assertEquals("Bước 4/4", HandsFreeState.of(3, 4).label)
    }

    @Test
    fun progressRunsFromFirstStepShareToFull() {
        assertEquals(0.25f, HandsFreeState.of(0, 4).progress)
        assertEquals(0.5f, HandsFreeState.of(1, 4).progress)
        assertEquals(1f, HandsFreeState.of(3, 4).progress)
        assertEquals(0f, HandsFreeState.of(0, 0).progress)
    }

    @Test
    fun emptyRecipeHasEmptyStateAndNoNavigation() {
        val s = HandsFreeState.of(0, 0)
        assertTrue(s.isEmpty)
        assertFalse(HandsFreeState.of(0, 2).isEmpty)
        assertEquals(s, s.next())
        assertEquals(s, s.previous())
        assertEquals("Món này chưa có các bước", HandsFreeCopy.EMPTY_TITLE)
    }

    @Test
    fun announcementReadsStepOfTotal() {
        assertEquals("Bước 2 trên 4: Đun nước", HandsFreeState.of(1, 4).announcement("Đun nước"))
    }

    @Test
    fun nextButtonLabelChangesOnLastStep() {
        assertEquals("Tiếp", HandsFreeState.of(1, 4).nextLabel)
        assertEquals("Xong, mình nấu xong rồi", HandsFreeState.of(3, 4).nextLabel)
    }

    @Test
    fun copyAvoidsForbiddenWords() {
        val all = listOf(
            HandsFreeCopy.EMPTY_TITLE, HandsFreeCopy.BACK, HandsFreeCopy.NEXT, HandsFreeCopy.DONE,
            HandsFreeCopy.CLOSE_DESCRIPTION, HandsFreeCopy.EMPTY_BACK,
        ).joinToString(" ").lowercase()
        listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn").forEach { assertFalse(it in all) }
        assertEquals("Thoát chế độ nấu rảnh tay", HandsFreeCopy.CLOSE_DESCRIPTION)
    }

    @Test
    fun parsesStepDurationSecondsFromVietnameseSteps() {
        assertEquals(600, parseStepDurationSeconds("Ướp cá với muối trong 10 phút."))
        assertEquals(900, parseStepDurationSeconds("Ướp thịt trong 15-20 phút."))
        assertEquals(45, parseStepDurationSeconds("Chần sơ khoảng 45 giây."))
        assertEquals(3600, parseStepDurationSeconds("Hầm xương 1 giờ cho ngọt nước."))
        assertEquals(1800, parseStepDurationSeconds("Ủ bột nửa tiếng."))
        assertEquals(60, parseStepDurationSeconds("Đảo thêm một phút rồi dùng nóng."))
        assertEquals(180, parseStepDurationSeconds("Ướp thịt bò trong vài phút."))
        assertEquals(null, parseStepDurationSeconds("Rửa sạch rau rồi để ráo."))
    }

    @Test
    fun formatsTimerClock() {
        assertEquals("00:00", formatTimerClock(0))
        assertEquals("00:05", formatTimerClock(5))
        assertEquals("10:00", formatTimerClock(600))
        assertEquals("1:05:09", formatTimerClock(3909))
    }
}
