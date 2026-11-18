package com.loitu.yourrefri.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertTrue

/** Cạnh dưới của nút nổi phải tối hơn mặt nút ở cả hai chế độ, nếu không nút trông phẳng. */
class ChunkyEdgeColorsTest {
    private val palettes = listOf("light" to appColorsFor(dark = false), "dark" to appColorsFor(dark = true))

    @Test
    fun primaryEdgeIsDarkerThanPrimaryFace() {
        palettes.forEach { (name, c) ->
            assertTrue(luminance(c.primaryEdge) < luminance(c.primary), "$name primaryEdge phải tối hơn primary")
        }
    }

    @Test
    fun ctaEdgeIsDarkerThanCtaFace() {
        palettes.forEach { (name, c) ->
            assertTrue(luminance(c.ctaEdge) < luminance(c.cta), "$name ctaEdge phải tối hơn cta")
        }
    }

    private fun luminance(color: Color): Double {
        fun linear(channel: Float): Double {
            val v = channel.toDouble()
            return if (v <= 0.04045) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * linear(color.red) + 0.7152 * linear(color.green) + 0.0722 * linear(color.blue)
    }
}
