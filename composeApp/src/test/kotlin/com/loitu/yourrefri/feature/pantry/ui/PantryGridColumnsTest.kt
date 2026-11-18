package com.loitu.yourrefri.feature.pantry.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class PantryGridColumnsTest {
    @Test
    fun normalFontUsesTwoColumns() {
        assertEquals(2, pantryGridColumns(fontScale = 1.0f))
    }

    @Test
    fun largeFontFallsBackToOneColumn() {
        assertEquals(1, pantryGridColumns(fontScale = 1.3f))
        assertEquals(1, pantryGridColumns(fontScale = 2.0f))
    }
}
