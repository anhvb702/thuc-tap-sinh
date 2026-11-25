package com.loitu.yourrefri.ui.components

import android.animation.ValueAnimator
import android.os.Build

internal fun areSystemAnimatorsEnabled(): Boolean =
    Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled()
