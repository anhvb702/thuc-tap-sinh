package com.loitu.yourrefri.feature.pantry.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class PantryAddActionsCopyTest {
    @Test
    fun addAndVoiceActionsUseFigmaLabels() {
        assertEquals("+ Thêm nguyên liệu", PantryScreenCopy.addPrimaryAction)
        assertEquals("Nói", PantryScreenCopy.voiceAction)
    }
}
