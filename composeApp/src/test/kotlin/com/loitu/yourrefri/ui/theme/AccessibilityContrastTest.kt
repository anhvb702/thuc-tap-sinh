package com.loitu.yourrefri.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertTrue

class AccessibilityContrastTest {
    @Test
    fun bodyTextColors_meetWcagAaOnLightSurfaces() {
        listOf(DeepCharcoal, MutedGray, SageGreenText, TerracottaText).forEach { foreground ->
            assertTrue(contrast(foreground, WarmIvory) >= 4.5, "$foreground on WarmIvory")
            assertTrue(contrast(foreground, SurfaceWhite) >= 4.5, "$foreground on white")
        }
    }

    @Test
    fun primaryButton_whiteLabelMeetsWcagAa() {
        assertTrue(contrast(Color.White, SageGreenStrong) >= 4.5)
    }

    @Test
    fun bodySmallTypography_isAtLeastFourteenSp() {
        assertTrue(AppTypography.bodySmall.fontSize.value >= 14f)
    }

    private fun contrast(foreground: Color, background: Color): Double {
        val foregroundLuminance = luminance(foreground)
        val backgroundLuminance = luminance(background)
        return (maxOf(foregroundLuminance, backgroundLuminance) + 0.05) /
            (minOf(foregroundLuminance, backgroundLuminance) + 0.05)
    }

    private fun luminance(color: Color): Double {
        fun linear(channel: Float): Double {
            val value = channel.toDouble()
            return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
        }

        return 0.2126 * linear(color.red) + 0.7152 * linear(color.green) + 0.0722 * linear(color.blue)
    }
}
