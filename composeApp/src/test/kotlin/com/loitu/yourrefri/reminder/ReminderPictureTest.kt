package com.loitu.yourrefri.reminder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReminderPictureTest {
    @Test
    fun pictureIsOneOfTheBundledDishPhotos() {
        for (day in 1..366) {
            assertTrue(ReminderPicture.assetFor(day) in ReminderPicture.ASSETS, "ngày $day")
        }
    }

    @Test
    fun pictureRotatesEveryDayAndRepeatsAfterAllDishes() {
        val picks = (1..ReminderPicture.ASSETS.size).map { ReminderPicture.assetFor(it) }
        assertEquals(ReminderPicture.ASSETS.size, picks.toSet().size)
        assertEquals(ReminderPicture.assetFor(1), ReminderPicture.assetFor(1 + ReminderPicture.ASSETS.size))
    }

    @Test
    fun pictureCoversAllBundledPhotos() {
        assertEquals(4, ReminderPicture.ASSETS.size)
        assertTrue(ReminderPicture.ASSETS.all { it.startsWith("notify/") && it.endsWith(".jpg") })
    }
}
