package com.loitu.yourrefri.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals

class TabTransitionSpecTest {
    @Test
    fun tabSlideDirection_returnsPositiveWhenMovingRight_negativeWhenLeft_zeroWhenSame() {
        assertEquals(1, tabSlideDirection(fromIndex = 0, toIndex = 1))
        assertEquals(1, tabSlideDirection(fromIndex = 0, toIndex = 3))
        assertEquals(-1, tabSlideDirection(fromIndex = 3, toIndex = 2))
        assertEquals(-1, tabSlideDirection(fromIndex = 2, toIndex = 0))
        assertEquals(0, tabSlideDirection(fromIndex = 1, toIndex = 1))
    }

    @Test
    fun pressScaleTarget_scalesDownOnlyWhenPressedAndAnimatorsEnabled() {
        assertEquals(PRESS_SCALE_DOWN, pressScaleTarget(pressed = true, animatorsEnabled = true))
        assertEquals(1f, pressScaleTarget(pressed = false, animatorsEnabled = true))
        assertEquals(1f, pressScaleTarget(pressed = true, animatorsEnabled = false))
        assertEquals(1f, pressScaleTarget(pressed = false, animatorsEnabled = false))
    }

    @Test
    fun tabContentDurationMs_respectsReduceMotion() {
        assertEquals(TAB_TRANSITION_MS, tabContentDurationMs(animatorsEnabled = true))
        assertEquals(0, tabContentDurationMs(animatorsEnabled = false))
    }

    @Test
    fun navItemScaleTarget_prioritizesPressThenSelectedAndRespectsReduceMotion() {
        assertEquals(PRESS_SCALE_DOWN, navItemScaleTarget(selected = true, pressed = true, animatorsEnabled = true))
        assertEquals(PRESS_SCALE_DOWN, navItemScaleTarget(selected = false, pressed = true, animatorsEnabled = true))
        assertEquals(NAV_SELECTED_SCALE, navItemScaleTarget(selected = true, pressed = false, animatorsEnabled = true))
        assertEquals(1f, navItemScaleTarget(selected = false, pressed = false, animatorsEnabled = true))
        assertEquals(1f, navItemScaleTarget(selected = true, pressed = true, animatorsEnabled = false))
        assertEquals(1f, navItemScaleTarget(selected = true, pressed = false, animatorsEnabled = false))
    }
}
