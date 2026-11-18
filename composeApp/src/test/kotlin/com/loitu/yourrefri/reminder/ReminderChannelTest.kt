package com.loitu.yourrefri.reminder

import android.app.NotificationManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ReminderChannelTest {
    @Test
    fun newChannelIsHighImportanceSoReminderShowsAsPopup() {
        assertEquals(NotificationManager.IMPORTANCE_HIGH, ReminderChannel.IMPORTANCE)
    }

    @Test
    fun newChannelUsesNewIdBecauseOldChannelImportanceCannotChange() {
        assertEquals("daily_reminder_hi", ReminderChannel.ID)
        assertEquals("daily_reminder", ReminderChannel.LEGACY_ID)
        assertNotEquals(ReminderChannel.LEGACY_ID, ReminderChannel.ID)
    }
}
