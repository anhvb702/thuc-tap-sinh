package com.loitu.yourrefri.ui.components

internal const val TAB_TRANSITION_MS = 110
internal const val PRESS_SCALE_DOWN = 0.95f
internal const val NAV_SELECTED_SCALE = 1.04f

internal fun pressScaleTarget(pressed: Boolean, animatorsEnabled: Boolean): Float =
    if (pressed && animatorsEnabled) PRESS_SCALE_DOWN else 1f

internal fun tabContentDurationMs(animatorsEnabled: Boolean): Int =
    if (animatorsEnabled) TAB_TRANSITION_MS else 0

internal fun navItemScaleTarget(selected: Boolean, pressed: Boolean, animatorsEnabled: Boolean): Float =
    when {
        !animatorsEnabled -> 1f
        pressed -> PRESS_SCALE_DOWN
        selected -> NAV_SELECTED_SCALE
        else -> 1f
    }
