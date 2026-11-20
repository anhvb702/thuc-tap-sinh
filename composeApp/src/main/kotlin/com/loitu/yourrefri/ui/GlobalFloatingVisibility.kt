package com.loitu.yourrefri.ui

internal fun shouldShowGlobalFloatingActions(
    currentTab: NavTab,
    hasCookDone: Boolean,
    hasHandsFree: Boolean,
    showHistory: Boolean,
): Boolean = currentTab == NavTab.HOME && !hasCookDone && !hasHandsFree && !showHistory
