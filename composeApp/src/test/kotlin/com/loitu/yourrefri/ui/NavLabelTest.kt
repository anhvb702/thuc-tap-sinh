package com.loitu.yourrefri.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Contract W4-03: khi cỡ chữ hệ thống lớn ở 360dp, nhãn tab phải xuống dòng đủ chữ và
// chữ hiển thị không vượt quá NAV_LABEL_MAX_DISPLAY_SP, để nhãn không tràn mép ô tab.
class NavLabelTest {
    @Test
    fun navLabel_allowsWrappingSoLongerLabelsAreNotTruncated() {
        assertTrue(NAV_LABEL_MAX_LINES >= 2, "NAV_LABEL_MAX_LINES=$NAV_LABEL_MAX_LINES")
    }

    @Test
    fun navLabel_tabLabelsAreNonEmptyAndUnique() {
        val labels = NavTab.entries.map { it.label }
        assertTrue(labels.all { it.isNotBlank() })
        assertTrue(labels.toSet().size == labels.size)
    }

    @Test
    fun navLabel_normalScaleKeepsBaseSize() {
        assertEquals(14f, navLabelFontSizeSp(1.0f), 0.01f)
    }

    @Test
    fun navLabel_displayedSizeIsCappedAtLargeScales() {
        listOf(1.0f, 1.3f, 1.5f, 2.0f).forEach { scale ->
            val displayed = navLabelFontSizeSp(scale) * scale
            assertTrue(displayed <= NAV_LABEL_MAX_DISPLAY_SP + 0.01f, "scale=$scale displayed=$displayed")
        }
    }
}
