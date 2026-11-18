package com.loitu.yourrefri.reminder

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReminderPictureTest {
    private val recipesJson: String = File("src/main/assets/recipes.json").readText()

    @Test
    fun everyPictureMapsToARealRecipeId() {
        for (entry in ReminderPicture.ENTRIES) {
            assertTrue(recipesJson.contains("\"id\": \"${entry.recipeId}\""), "công thức không tồn tại: ${entry.recipeId}")
            assertTrue(entry.assetPath.startsWith("notify/") && entry.assetPath.endsWith(".jpg"), entry.assetPath)
            assertTrue(File("src/main/assets/${entry.assetPath}").exists(), "thiếu ảnh ${entry.assetPath}")
        }
    }

    @Test
    fun pictureRotatesEveryDayAndRepeatsAfterAllDishes() {
        val size = ReminderPicture.ENTRIES.size
        val picks = (1..size).map { ReminderPicture.assetFor(it) }
        assertEquals(size, picks.toSet().size)
        assertEquals(ReminderPicture.assetFor(1), ReminderPicture.assetFor(1 + size))
    }

    @Test
    fun pictureForDayIsAlwaysOneOfTheEntries() {
        for (day in 1..366) {
            assertTrue(ReminderPicture.ENTRIES.any { it.assetPath == ReminderPicture.assetFor(day) }, "ngày $day")
        }
    }

    @Test
    fun entryForDayGivesRecipeAndPictureOfTheSameDish() {
        for (day in 1..366) {
            val entry = ReminderPicture.entryFor(day)
            assertEquals(ReminderPicture.assetFor(day), entry.assetPath)
            assertTrue(recipesJson.contains("\"id\": \"${entry.recipeId}\""), "ngày $day")
        }
    }
}
