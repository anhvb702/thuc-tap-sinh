package com.loitu.yourrefri.feature.pantry.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class AddFoodSelectionTest {
    @Test
    fun emptySelectionUsesPlainLabel() {
        assertEquals("Thêm nguyên liệu vào tủ", addSelectionLabel(0))
    }

    @Test
    fun selectionCountIsShownInLabel() {
        assertEquals("Thêm 4 nguyên liệu vào tủ", addSelectionLabel(4))
    }

    @Test
    fun blankQueryHidesNotFoundSection() {
        assertEquals(false, notFoundSectionVisible("", 0))
        assertEquals(false, notFoundSectionVisible("   ", 0))
    }

    @Test
    fun unmatchedQueryShowsNotFoundSection() {
        assertEquals(true, notFoundSectionVisible("rau", 0))
    }

    @Test
    fun matchedQueryHidesNotFoundSection() {
        assertEquals(false, notFoundSectionVisible("rau", 2))
    }
}
