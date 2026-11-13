package com.loitu.yourrefri.ui.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThemeModeTest {
    @Test
    fun systemModeFollowsTheDevice() {
        assertTrue(ThemeMode.SYSTEM.isDark(systemDark = true))
        assertFalse(ThemeMode.SYSTEM.isDark(systemDark = false))
    }

    @Test
    fun explicitModesIgnoreTheDevice() {
        assertFalse(ThemeMode.LIGHT.isDark(systemDark = true))
        assertFalse(ThemeMode.LIGHT.isDark(systemDark = false))
        assertTrue(ThemeMode.DARK.isDark(systemDark = true))
        assertTrue(ThemeMode.DARK.isDark(systemDark = false))
    }

    @Test
    fun storageRoundTripsEveryMode() {
        ThemeMode.entries.forEach { mode ->
            assertEquals(mode, ThemeMode.fromStorage(mode.storageValue))
        }
    }

    @Test
    fun unknownOrMissingStorageFallsBackToSystem() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage(""))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage("neon"))
    }

    @Test
    fun storageValuesAndLabelsAreUnique() {
        assertEquals(ThemeMode.entries.size, ThemeMode.entries.map { it.storageValue }.toSet().size)
        assertEquals(ThemeMode.entries.size, ThemeMode.entries.map { it.label }.toSet().size)
    }

    @Test
    fun labelsAreShortGentleVietnamese() {
        assertEquals(listOf("Theo máy", "Sáng", "Tối"), ThemeMode.entries.map { it.label })
        assertEquals("Giao diện", ThemeModeCopy.title)
        assertTrue(ThemeModeCopy.subtitle.isNotBlank())
    }
}
