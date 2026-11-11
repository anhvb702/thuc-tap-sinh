package com.loitu.yourrefri.reminder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReminderCompletionTest {
    @Test
    fun reschedulesBeforeFinishingBroadcast() {
        val events = mutableListOf<String>()
        completeReminderWork(
            closeDriver = { events += "close" },
            scheduleNext = { events += "schedule" },
            finish = { events += "finish" },
        )
        assertEquals(listOf("close", "schedule", "finish"), events)
    }

    @Test
    fun finishesBroadcastEvenWhenSchedulingFails() {
        var finished = false
        assertFailsWith<IllegalStateException> {
            completeReminderWork(
                closeDriver = {},
                scheduleNext = { error("alarm failure") },
                finish = { finished = true },
            )
        }
        assertEquals(true, finished)
    }
}
