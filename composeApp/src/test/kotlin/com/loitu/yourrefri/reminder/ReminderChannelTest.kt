package com.loitu.yourrefri.reminder

import android.app.NotificationManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ReminderChannelTest {
    @Test
    fun freshInstallGetsHighImportanceChannelSpec() {
        assertEquals(NotificationManager.IMPORTANCE_HIGH, ReminderChannel.IMPORTANCE)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, ReminderChannel.importanceFor(null))
    }

    @Test
    fun upgradeKeepsUsersLegacyChoice() {
        assertEquals(NotificationManager.IMPORTANCE_HIGH, ReminderChannel.importanceFor(NotificationManager.IMPORTANCE_DEFAULT))
        assertEquals(NotificationManager.IMPORTANCE_HIGH, ReminderChannel.importanceFor(NotificationManager.IMPORTANCE_HIGH))
        assertEquals(NotificationManager.IMPORTANCE_LOW, ReminderChannel.importanceFor(NotificationManager.IMPORTANCE_LOW))
        assertEquals(NotificationManager.IMPORTANCE_LOW, ReminderChannel.importanceFor(NotificationManager.IMPORTANCE_MIN))
        assertEquals(NotificationManager.IMPORTANCE_NONE, ReminderChannel.importanceFor(NotificationManager.IMPORTANCE_NONE))
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
