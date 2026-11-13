package com.loitu.yourrefri.feature.backup.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WeeklyMealSettingsBackupCodecTest {
    private val settings = WeeklyMealSettingsBackup(
        weekStart = "2026-11-09",
        goalId = "quick",
        preferenceIds = listOf("seafood", "vegetarian"),
        swappedOffsets = listOf(WeeklyMealSwapOffsetBackup(1, 2), WeeklyMealSwapOffsetBackup(5, 1)),
        uncheckedIds = listOf("rice", "egg"),
        includeLunchMealPrep = true,
    )

    private fun snapshot(settings: WeeklyMealSettingsBackup? = this.settings) = PantryBackupSnapshot(
        format = BackupCodec.FORMAT,
        version = BackupCodec.VERSION,
        pantry = emptyList(),
        shelfOverrides = emptyList(),
        quantities = emptyList(),
        cookLogs = emptyList(),
        weeklyMeals = listOf(WeeklyMealBackup("2026-11-09", "DINNER", "stew", 2)),
        weeklySettings = settings,
    )

    @Test
    fun versionTwo_roundTripsEveryWeeklySetting() {
        val expected = snapshot()
        val encoded = BackupCodec.encode(expected)

        assertEquals(expected, BackupCodec.decode(encoded))
        assertEquals(true, encoded.contains("\"includeLunchMealPrep\":true"))
    }

    @Test
    fun decode_legacyVersionTwoWithoutSettings_defaultsToNullAndKeepsMeals() {
        val json = """{"format":"yourrefri-backup","version":2,"pantry":[],"shelfOverrides":[],"quantities":[],"cookLogs":[],"weeklyMeals":[{"day":"2026-11-09","meal":"DINNER","recipeId":"stew","portions":2}]}"""

        val decoded = BackupCodec.decode(json)

        assertEquals(2, decoded.version)
        assertEquals(null, decoded.weeklySettings)
        assertEquals(snapshot(null).weeklyMeals, decoded.weeklyMeals)
    }

    @Test
    fun encode_rejectsInvalidWeeklySettings() {
        val invalid = listOf(
            settings.copy(weekStart = "2026-02-30"),
            settings.copy(goalId = " "),
            settings.copy(preferenceIds = listOf("seafood", "seafood")),
            settings.copy(preferenceIds = listOf("")),
            settings.copy(swappedOffsets = listOf(WeeklyMealSwapOffsetBackup(7, 1))),
            settings.copy(swappedOffsets = listOf(WeeklyMealSwapOffsetBackup(1, -1))),
            settings.copy(swappedOffsets = listOf(WeeklyMealSwapOffsetBackup(1, 1), WeeklyMealSwapOffsetBackup(1, 2))),
            settings.copy(uncheckedIds = listOf("egg", "egg")),
            settings.copy(uncheckedIds = listOf("")),
        )

        invalid.forEach { value ->
            assertFailsWith<IllegalArgumentException> { BackupCodec.encode(snapshot(value)) }
        }
    }
}
