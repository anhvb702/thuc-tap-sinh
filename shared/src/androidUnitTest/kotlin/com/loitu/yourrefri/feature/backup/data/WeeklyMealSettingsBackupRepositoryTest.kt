package com.loitu.yourrefri.feature.backup.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanEntry
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanRepositoryImpl
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanSettings
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WeeklyMealSettingsBackupRepositoryTest {
    private lateinit var driver: SqlDriver
    private lateinit var database: YourRefriDatabase

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        YourRefriDatabase.Schema.create(driver)
        database = createDatabase(driver)
    }

    @AfterTest
    fun tearDown() = driver.close()

    private fun backupRepository() = PantryBackupRepositoryImpl(database, Dispatchers.Unconfined)
    private fun planRepository() = WeeklyMealPlanRepositoryImpl(database, Dispatchers.Unconfined)

    private fun sampleSettings(goal: String = "balanced") = WeeklyMealPlanSettings(
        weekStart = LocalDate(2026, 11, 9),
        goalId = goal,
        preferenceIds = setOf("seafood", "vegetarian"),
        swappedOffsets = mapOf(1 to 2, 5 to 1),
        uncheckedIds = setOf("rice", "egg"),
        includeLunchMealPrep = true,
    )

    @Test
    fun exportAndRestore_replaceSettingsAndWeeklyMeals() = runTest {
        val original = sampleSettings()
        planRepository().saveSettings(original)
        planRepository().save(WeeklyMealPlanEntry(LocalDate(2026, 11, 9), MealSlot.DINNER, "stew", 2))

        val payload = backupRepository().exportJson()
        val decoded = BackupCodec.decode(payload)
        planRepository().saveSettings(sampleSettings(goal = "light"))
        planRepository().save(WeeklyMealPlanEntry(LocalDate(2026, 11, 10), MealSlot.LUNCH, "soup", 1))

        backupRepository().restoreJson(payload)

        assertEquals(original, planRepository().observeSettings().first())
        assertEquals(
            listOf(WeeklyMealPlanEntry(LocalDate(2026, 11, 9), MealSlot.DINNER, "stew", 2)),
            planRepository().observeAll().first(),
        )
        assertEquals(original.preferenceIds, decoded.weeklySettings?.preferenceIds?.toSet())
    }

    @Test
    fun restoreLegacyVersionTwo_clearsExistingSettings() = runTest {
        planRepository().saveSettings(sampleSettings())
        val legacy = """{"format":"yourrefri-backup","version":2,"pantry":[],"shelfOverrides":[],"quantities":[],"cookLogs":[],"weeklyMeals":[]}"""

        backupRepository().restoreJson(legacy)

        assertEquals(null, planRepository().observeSettings().first())
    }

    @Test
    fun restoreFailure_rollsBackSettingsWithOtherBackupTables() = runTest {
        val original = sampleSettings()
        planRepository().saveSettings(original)
        driver.execute(
            null,
            "CREATE TRIGGER reject_backup_log BEFORE INSERT ON CookLogEntity WHEN NEW.recipeId = 'fail' BEGIN SELECT RAISE(ABORT, 'forced insert failure'); END",
            0,
        )
        val invalidRestore = PantryBackupSnapshot(
            format = BackupCodec.FORMAT,
            version = BackupCodec.VERSION,
            pantry = emptyList(),
            shelfOverrides = emptyList(),
            quantities = emptyList(),
            cookLogs = listOf(CookLogBackup(99, "fail", "2026-11-12", null, emptyList())),
            weeklySettings = WeeklyMealSettingsBackup(
                "2026-11-16", "quick", listOf("meat"), emptyList(), emptyList(), false,
            ),
        )

        assertFailsWith<Exception> { backupRepository().restoreJson(BackupCodec.encode(invalidRestore)) }

        assertEquals(original, planRepository().observeSettings().first())
    }
}
