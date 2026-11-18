package com.loitu.yourrefri.feature.recipe.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class CookTimerTest {
    @Test
    fun progressIsRemainingShare() {
        assertEquals(0.65f, timerProgress(remainingSec = 156, totalSec = 240), 0.01f)
    }

    @Test
    fun progressIsZeroWhenDone() {
        assertEquals(0f, timerProgress(remainingSec = 0, totalSec = 240), 0.0001f)
    }

    @Test
    fun progressIsOneWhenFull() {
        assertEquals(1f, timerProgress(remainingSec = 240, totalSec = 240), 0.0001f)
    }

    @Test
    fun progressDoesNotDivideByZero() {
        assertEquals(0f, timerProgress(remainingSec = 0, totalSec = 0), 0.0001f)
    }

    @Test
    fun labelIsMinutesAndSeconds() {
        assertEquals("02:36", timerLabel(156))
        assertEquals("00:00", timerLabel(0))
    }
}
