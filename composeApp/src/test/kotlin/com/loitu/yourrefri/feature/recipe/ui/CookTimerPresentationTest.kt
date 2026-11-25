package com.loitu.yourrefri.feature.recipe.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CookTimerPresentationTest {
    @Test
    fun phaseDistinguishesReadyRunningLowPausedAndDone() {
        assertEquals(CookTimerPhase.READY, cookTimerPhase(TimerState(remaining = 600, running = false), 600))
        assertEquals(CookTimerPhase.RUNNING, cookTimerPhase(TimerState(remaining = 300, running = true), 600))
        assertEquals(CookTimerPhase.RUNNING_LOW, cookTimerPhase(TimerState(remaining = 120, running = true), 600))
        assertEquals(CookTimerPhase.RUNNING, cookTimerPhase(TimerState(remaining = 121, running = true), 600))
        assertEquals(CookTimerPhase.PAUSED, cookTimerPhase(TimerState(remaining = 250, running = false), 600))
        assertEquals(CookTimerPhase.DONE, cookTimerPhase(TimerState(remaining = 0, running = false), 600))
        assertEquals(CookTimerPhase.DONE, cookTimerPhase(TimerState(remaining = 0, running = true), 600))
    }

    @Test
    fun tapHintAndStatusLabelMatchEveryPhaseWithoutForbiddenWords() {
        val ready = TimerState(remaining = 600, running = false)
        val running = TimerState(remaining = 400, running = true)
        val runningLow = TimerState(remaining = 60, running = true)
        val paused = TimerState(remaining = 400, running = false)
        val done = TimerState(remaining = 0, running = false)

        assertEquals("Chạm đồng hồ để bắt đầu", cookTimerTapHint(ready, 600))
        assertEquals("Chạm đồng hồ để tạm dừng", cookTimerTapHint(running, 600))
        assertEquals("Chạm đồng hồ để tạm dừng", cookTimerTapHint(runningLow, 600))
        assertEquals("Chạm đồng hồ để tiếp tục", cookTimerTapHint(paused, 600))
        assertEquals("Chạm đồng hồ để hẹn lại", cookTimerTapHint(done, 600))

        assertEquals("Sẵn sàng", cookTimerStatusLabel(ready, 600))
        assertEquals("Đang đếm", cookTimerStatusLabel(running, 600))
        assertEquals("Đang đếm", cookTimerStatusLabel(runningLow, 600))
        assertEquals("Tạm dừng", cookTimerStatusLabel(paused, 600))
        assertEquals("Hết giờ", cookTimerStatusLabel(done, 600))

        val combined = listOf(ready, running, runningLow, paused, done)
            .flatMap { listOf(cookTimerTapHint(it, 600), cookTimerStatusLabel(it, 600)) }
            .joinToString(" ")
            .lowercase()
        listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn").forEach { forbidden -> // lint-copy: rule
            assertFalse(forbidden in combined, "Forbidden phrase: $forbidden")
        }
    }

    @Test
    fun resetAllowedWhenRunningOrElapsedAndRestoresFullPausedTimer() {
        assertFalse(canResetCookTimer(TimerState(remaining = 600, running = false), 600))
        assertTrue(canResetCookTimer(TimerState(remaining = 600, running = true), 600))
        assertTrue(canResetCookTimer(TimerState(remaining = 450, running = false), 600))
        assertTrue(canResetCookTimer(TimerState(remaining = 0, running = false), 600))

        assertEquals(TimerState(remaining = 600, running = false), resetCookTimerToFull(600))
        assertEquals(TimerState(remaining = 0, running = false), resetCookTimerToFull(-5))
    }

    @Test
    fun knobAngleStartsAtTopAndClampsProgress() {
        assertEquals(-90f, timerKnobAngleDegrees(0f), 0.001f)
        assertEquals(90f, timerKnobAngleDegrees(0.5f), 0.001f)
        assertEquals(270f, timerKnobAngleDegrees(1f), 0.001f)
        assertEquals(-90f, timerKnobAngleDegrees(-0.3f), 0.001f)
        assertEquals(270f, timerKnobAngleDegrees(1.5f), 0.001f)
    }
}
