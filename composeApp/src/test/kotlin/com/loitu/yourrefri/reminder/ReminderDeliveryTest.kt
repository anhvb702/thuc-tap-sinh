package com.loitu.yourrefri.reminder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReminderDeliveryTest {
    @Test
    fun postsFirstThenMarksSentForRealReminder() {
        val events = mutableListOf<String>()
        deliverDailyReminder(
            isTest = false,
            post = { events += "post" },
            markSent = { events += "mark" },
        )
        assertEquals(listOf("post", "mark"), events)
    }

    @Test
    fun doesNotMarkSentWhenPostFails() {
        val events = mutableListOf<String>()
        assertFailsWith<IllegalStateException> {
            deliverDailyReminder(
                isTest = false,
                post = { events += "post"; error("notify failed") },
                markSent = { events += "mark" },
            )
        }
        assertEquals(listOf("post"), events)
    }

    @Test
    fun testButtonNeverMarksSent() {
        val events = mutableListOf<String>()
        deliverDailyReminder(
            isTest = true,
            post = { events += "post" },
            markSent = { events += "mark" },
        )
        assertEquals(listOf("post"), events)
    }
}
