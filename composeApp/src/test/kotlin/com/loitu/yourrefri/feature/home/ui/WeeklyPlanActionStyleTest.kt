package com.loitu.yourrefri.feature.home.ui

import androidx.compose.ui.graphics.Color
import com.loitu.yourrefri.ui.theme.SageGreenStrong
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Contract UX-07: nút "Lập thực đơn tuần & đi chợ" trên Trang chủ phải có chữ đủ tương phản (WCAG AA >= 4.5).
class WeeklyPlanActionStyleTest {
    @Test
    fun weeklyPlanButton_usesSageBackgroundWithWhiteLabel() {
        assertEquals(SageGreenStrong, weeklyPlanActionStyle.container)
        assertEquals(Color.White, weeklyPlanActionStyle.content)
    }

    @Test
    fun weeklyPlanButton_labelMeetsWcagAa() {
        val style = weeklyPlanActionStyle
        assertTrue(contrast(style.content, style.container) >= 4.5)
    }

    private fun contrast(foreground: Color, background: Color): Double {
        val a = luminance(foreground)
        val b = luminance(background)
        return (maxOf(a, b) + 0.05) / (minOf(a, b) + 0.05)
    }

    private fun luminance(color: Color): Double {
        fun linear(channel: Float): Double {
            val value = channel.toDouble()
            return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * linear(color.red) + 0.7152 * linear(color.green) + 0.0722 * linear(color.blue)
    }
}
