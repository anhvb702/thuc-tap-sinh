package com.loitu.yourrefri.feature.recipe.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class CookTimerStateTest {
    private val total = 240

    @Test
    fun toggleFromIdleStartsFullTimer() {
        assertEquals(TimerState(240, true), toggleTimer(TimerState(0, false), total))
    }

    @Test
    fun toggleWhileRunningPausesKeepingRemaining() {
        assertEquals(TimerState(156, false), toggleTimer(TimerState(156, true), total))
    }

    @Test
    fun toggleWhilePausedResumes() {
        assertEquals(TimerState(156, true), toggleTimer(TimerState(156, false), total))
    }

    @Test
    fun tickCountsDownOnlyWhileRunning() {
        assertEquals(TimerState(155, true), tickTimer(TimerState(156, true)))
        assertEquals(TimerState(156, false), tickTimer(TimerState(156, false)))
    }

    @Test
    fun tickAtZeroStopsTimer() {
        assertEquals(TimerState(0, false), tickTimer(TimerState(1, true)))
    }

    @Test
    fun resetClearsAndStops() {
        assertEquals(TimerState(0, false), resetTimer())
    }
}
