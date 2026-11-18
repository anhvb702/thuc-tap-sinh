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
    fun reminderIsSentWhenNoLegacyChannelOrLegacyChannelEnabled() {
        assertEquals(true, ReminderChannel.shouldNotify(null))
        assertEquals(true, ReminderChannel.shouldNotify(NotificationManager.IMPORTANCE_DEFAULT))
    }

    @Test
    fun reminderIsNotSentWhenUserBlockedLegacyChannel() {
        assertEquals(false, ReminderChannel.shouldNotify(NotificationManager.IMPORTANCE_NONE))
    }

    @Test
    fun newChannelUsesNewIdBecauseOldChannelImportanceCannotChange() {
        assertEquals("daily_reminder_hi", ReminderChannel.ID)
        assertEquals("daily_reminder", ReminderChannel.LEGACY_ID)
        assertNotEquals(ReminderChannel.LEGACY_ID, ReminderChannel.ID)
    }
}
