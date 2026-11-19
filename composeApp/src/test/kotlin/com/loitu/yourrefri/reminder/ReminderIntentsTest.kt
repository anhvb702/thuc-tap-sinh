package com.loitu.yourrefri.reminder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReminderIntentsTest {
    @Test
    fun recipeIdIsReadWhenPresent() {
        assertEquals("thit-kho-trung", ReminderIntents.recipeIdFromExtra("thit-kho-trung"))
        assertEquals("thit-kho-trung", ReminderIntents.recipeIdFromExtra("  thit-kho-trung "))
    }

    @Test
    fun missingOrBlankRecipeIdOpensNothing() {
        assertNull(ReminderIntents.recipeIdFromExtra(null))
        assertNull(ReminderIntents.recipeIdFromExtra(""))
        assertNull(ReminderIntents.recipeIdFromExtra("   "))
    }
}
