package com.loitu.yourrefri.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals

class TabTransitionSpecTest {
    @Test
    fun pressScaleTarget_scalesDownOnlyWhenPressedAndAnimatorsEnabled() {
        assertEquals(PRESS_SCALE_DOWN, pressScaleTarget(pressed = true, animatorsEnabled = true))
        assertEquals(1f, pressScaleTarget(pressed = false, animatorsEnabled = true))
        assertEquals(1f, pressScaleTarget(pressed = true, animatorsEnabled = false))
        assertEquals(1f, pressScaleTarget(pressed = false, animatorsEnabled = false))
    }

    @Test
    fun tabContentDurationMs_uses110msAndRespectsReduceMotion() {
        assertEquals(110, tabContentDurationMs(animatorsEnabled = true))
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
