package com.loitu.yourrefri.reminder

import android.app.NotificationManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DishImageNameTest {
    @Test
    fun mapsHyphenatedRecipeIdsToDrawableNames() {
        assertEquals("dish_ca_kho", dishImageResName("ca-kho"))
    }

    @Test
    fun preservesPlainIdsAndNormalizesCase() {
        assertEquals("dish_com_ga_2", dishImageResName("Com_Ga_2"))
    }

    @Test
    fun replacesAccentsAndSpacesWithUnderscores() {
        assertEquals("dish_c__kho_n_ng", dishImageResName("cá kho nóng"))
    }

    @Test
    fun usesNewHighImportanceChannelId() {
        assertEquals("daily_reminder_v2", ReminderNotificationChannel.ID)
        assertNotEquals("daily_reminder", ReminderNotificationChannel.ID)
        assertEquals("Nhắc nhở YourRefri", ReminderNotificationChannel.NAME)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, ReminderNotificationChannel.IMPORTANCE)
    }
}
