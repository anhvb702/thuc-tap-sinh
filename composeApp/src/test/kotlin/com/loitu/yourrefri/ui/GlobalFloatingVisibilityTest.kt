package com.loitu.yourrefri.ui

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GlobalFloatingVisibilityTest {
    @Test
    fun visibleOnlyOnCleanHomeScreen() {
        assertTrue(shouldShowGlobalFloatingActions(NavTab.HOME, hasCookDone = false, hasHandsFree = false, showHistory = false))
    }

    @Test
    fun hiddenOnOtherTabs() {
        assertFalse(shouldShowGlobalFloatingActions(NavTab.PANTRY, hasCookDone = false, hasHandsFree = false, showHistory = false))
        assertFalse(shouldShowGlobalFloatingActions(NavTab.RECIPES, hasCookDone = false, hasHandsFree = false, showHistory = false))
        assertFalse(shouldShowGlobalFloatingActions(NavTab.SETTINGS, hasCookDone = false, hasHandsFree = false, showHistory = false))
    }

    @Test
    fun hiddenWhenOverlayScreensActiveOnHomeTab() {
        assertFalse(shouldShowGlobalFloatingActions(NavTab.HOME, hasCookDone = true, hasHandsFree = false, showHistory = false))
        assertFalse(shouldShowGlobalFloatingActions(NavTab.HOME, hasCookDone = false, hasHandsFree = true, showHistory = false))
        assertFalse(shouldShowGlobalFloatingActions(NavTab.HOME, hasCookDone = false, hasHandsFree = false, showHistory = true))
    }
}
