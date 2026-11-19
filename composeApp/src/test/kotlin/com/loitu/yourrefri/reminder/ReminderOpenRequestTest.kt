package com.loitu.yourrefri.reminder

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReminderOpenRequestTest {
    @AfterTest
    fun reset() = ReminderOpenRequest.consume()

    @Test
    fun requestThenConsumeHandsOffRecipeOnce() {
        ReminderOpenRequest.request("thit-kho-trung")
        assertEquals("thit-kho-trung", ReminderOpenRequest.recipeId.value)
        ReminderOpenRequest.consume()
        assertNull(ReminderOpenRequest.recipeId.value)
    }

    @Test
    fun blankRequestOpensNothing() {
        ReminderOpenRequest.request("  ")
        assertNull(ReminderOpenRequest.recipeId.value)
    }
}
