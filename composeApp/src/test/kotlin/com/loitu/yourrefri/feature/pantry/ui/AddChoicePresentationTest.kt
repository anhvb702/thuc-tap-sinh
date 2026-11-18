package com.loitu.yourrefri.feature.pantry.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddChoicePresentationTest {
    @Test fun labels() {
        assertEquals("Thêm nguyên liệu", addChoiceLabel(AddChoice.ADD_INGREDIENT))
        assertEquals("Khác / Thêm món", addChoiceLabel(AddChoice.ADD_OTHER))
    }

    @Test fun blankNameIsBlocked() {
        assertFalse(canAddOther("  ", emptySet()))
    }

    @Test fun duplicateIgnoresCaseAndSpaces() {
        assertFalse(canAddOther("Canh Chua", setOf("canh chua")))
        assertFalse(canAddOther("  canh chua ", setOf("Canh Chua")))
    }

    @Test fun newNameIsAllowed() {
        assertTrue(canAddOther("Bún chả", setOf("canh chua")))
    }
}
