package com.loitu.yourrefri.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AppColorsTest {
    private val palettes = listOf("light" to appColorsFor(dark = false), "dark" to appColorsFor(dark = true))

    @Test
    fun everyTextRoleMeetsWcagAaOnItsSurfaces() {
        palettes.forEach { (name, c) ->
            val pairs = listOf(
                "text/background" to (c.text to c.background),
                "text/surface" to (c.text to c.surface),
                "text/surfaceSoft" to (c.text to c.surfaceSoft),
                "text/primarySoft" to (c.text to c.primarySoft),
                "muted/background" to (c.muted to c.background),
                "muted/surface" to (c.muted to c.surface),
                "muted/primarySoft" to (c.muted to c.primarySoft),
                "accentText/background" to (c.accentText to c.background),
                "accentText/surface" to (c.accentText to c.surface),
                "accentText/surfaceSoft" to (c.accentText to c.surfaceSoft),
                "onPrimary/primary" to (c.onPrimary to c.primary),
                "warnText/warnSoft" to (c.warnText to c.warnSoft),
                "warnText/surface" to (c.warnText to c.surface),
                "warnText/background" to (c.warnText to c.background),
                "beigeText/beigeSoft" to (c.beigeText to c.beigeSoft),
                "onCta/cta" to (c.onCta to c.cta),
                "ctaText/background" to (c.ctaText to c.background),
                "ctaText/surface" to (c.ctaText to c.surface),
                "ctaText/ctaSoft" to (c.ctaText to c.ctaSoft),
                "onSnackbar/snackbar" to (c.onSnackbar to c.snackbar),
                "snackbarAction/snackbar" to (c.snackbarAction to c.snackbar),
            )
            pairs.forEach { (label, pair) ->
                val ratio = contrast(pair.first, pair.second)
                assertTrue(ratio >= 4.5, "$name $label = $ratio")
            }
        }
    }

    @Test
    fun paletteSelectionFollowsTheDarkFlag() {
        assertEquals(false, appColorsFor(dark = false).isDark)
        assertEquals(true, appColorsFor(dark = true).isDark)
        assertNotEquals(appColorsFor(false).background, appColorsFor(true).background)
        assertEquals(WarmIvory, appColorsFor(false).background)
        assertEquals(DarkBackground, appColorsFor(true).background)
    }

    @Test
    fun darkPaletteIsNotPureBlackOrPureWhite() {
        val c = appColorsFor(true)
        assertNotEquals(Color.Black, c.background)
        assertNotEquals(Color.White, c.text)
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
