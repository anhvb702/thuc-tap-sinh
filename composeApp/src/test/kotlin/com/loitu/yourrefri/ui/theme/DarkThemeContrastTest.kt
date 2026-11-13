package com.loitu.yourrefri.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DarkThemeContrastTest {
    @Test
    fun darkBodyText_meetsWcagAaOnDarkSurfaces() {
        listOf(DarkText, DarkMutedText).forEach { foreground ->
            assertTrue(contrast(foreground, DarkBackground) >= 4.5, "$foreground on DarkBackground")
            assertTrue(contrast(foreground, DarkSurface) >= 4.5, "$foreground on DarkSurface")
        }
    }

    @Test
    fun darkPrimaryButton_labelMeetsWcagAa() {
        assertTrue(contrast(DarkOnPrimary, DarkPrimary) >= 4.5)
    }

    @Test
    fun softStatusLabels_meetWcagAaInBothThemes() {
        assertTrue(contrast(TerracottaText, TerracottaSoft) >= 4.5, "light warn")
        assertTrue(contrast(WarmBeigeText, WarmBeigeSoft) >= 4.5, "light beige")
        assertTrue(contrast(DarkWarnText, DarkWarnSoft) >= 4.5, "dark warn")
        assertTrue(contrast(DarkBeigeText, DarkBeigeSoft) >= 4.5, "dark beige")
        assertTrue(contrast(DarkText, DarkPrimarySoft) >= 4.5, "dark text on soft green")
    }

    @Test
    fun darkScheme_usesDarkTokens() {
        assertEquals(DarkBackground, DarkColorScheme.background)
        assertEquals(DarkPrimary, DarkColorScheme.primary)
        assertEquals(DarkOnPrimary, DarkColorScheme.onPrimary)
    }

    private fun contrast(foreground: Color, background: Color): Double {
        val a = luminance(foreground)
        val b = luminance(background)
        return (maxOf(a, b) + 0.05) / (minOf(a, b) + 0.05)
    }

    private fun luminance(color: Color): Double {
        fun linear(channel: Float): Double {
            val v = channel.toDouble()
            return if (v <= 0.04045) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * linear(color.red) + 0.7152 * linear(color.green) + 0.0722 * linear(color.blue)
    }
}
