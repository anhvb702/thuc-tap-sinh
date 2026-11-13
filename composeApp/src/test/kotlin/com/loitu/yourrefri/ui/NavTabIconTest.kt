package com.loitu.yourrefri.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NavTabIconTest {
    @Test
    fun everyTabHasItsOwnStrokeIcon() {
        val names = NavTab.entries.map { it.icon.name }
        assertEquals(NavTab.entries.size, names.toSet().size, "icons must be distinct: $names")
        assertTrue(names.all { it.isNotBlank() })
    }

    @Test
    fun tabsKeepTheAgreedOrderAndNames() {
        assertEquals(
            listOf("Trang chủ", "Tủ lạnh", "Món ăn", "Cài đặt"),
            NavTab.entries.map { it.label },
        )
    }
}
