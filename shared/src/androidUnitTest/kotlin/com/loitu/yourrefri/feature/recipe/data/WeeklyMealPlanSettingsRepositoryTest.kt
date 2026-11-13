package com.loitu.yourrefri.feature.recipe.data

import java.io.File
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyMealPlanSettingsRepositoryTest {
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

    private fun repository(db: YourRefriDatabase = database) = WeeklyMealPlanRepositoryImpl(
        db = db,
        ioContext = Dispatchers.Unconfined,
    )

    @Test
    fun observeSettings_returnsNullWhenNoSettingsWereSaved() = runTest {
        assertEquals(null, repository().observeSettings().first())
    }

    @Test
    fun saveSettings_roundTripsAllFieldsAndReplacement() = runTest {
        val repository = repository()
        val initial = WeeklyMealPlanSettings(
            weekStart = LocalDate(2026, 11, 9),
            goalId = "balanced",
            preferenceIds = setOf("seafood", "vegetarian"),
            swappedOffsets = mapOf(1 to 2, 4 to 1),
            uncheckedIds = setOf("rice", "egg"),
            includeLunchMealPrep = true,
        )
        val replacement = WeeklyMealPlanSettings(
            weekStart = LocalDate(2026, 11, 16),
            goalId = "quick",
            preferenceIds = setOf("budget"),
            swappedOffsets = mapOf(0 to 3),
            uncheckedIds = setOf("tofu"),
            includeLunchMealPrep = false,
        )

        repository.saveSettings(initial)
        assertEquals(initial, repository.observeSettings().first())
        repository.saveSettings(replacement)
        assertEquals(replacement, repository.observeSettings().first())
    }

    @Test
    fun saveSettings_invalidValuesAreRejectedWithoutChangingStoredState() = runTest {
        val repository = repository()
        val valid = sampleSettings()
        repository.saveSettings(valid)
        val invalid = listOf(
            valid.copy(goalId = " "),
            valid.copy(preferenceIds = setOf("")),
            valid.copy(uncheckedIds = setOf("")),
            valid.copy(swappedOffsets = mapOf(7 to 1)),
            valid.copy(swappedOffsets = mapOf(0 to -1)),
        )

        invalid.forEach { settings ->
            assertFailsWith<IllegalArgumentException> { repository.saveSettings(settings) }
        }

        assertEquals(valid, repository.observeSettings().first())
    }

    @Test
    fun observeSettings_subscriberReceivesInitialSaveAndReplacementEmissions() = runTest {
        val repository = repository()
        val emissions = mutableListOf<WeeklyMealPlanSettings?>()
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeSettings().collect(emissions::add)
        }
        runCurrent()
        assertEquals(listOf<WeeklyMealPlanSettings?>(null), emissions)

        val initial = sampleSettings()
        repository.saveSettings(initial)
        runCurrent()
        val replacement = initial.copy(goalId = "quick", includeLunchMealPrep = false)
        repository.saveSettings(replacement)
        runCurrent()

        assertEquals(listOf(null, initial, replacement), emissions)
        collector.cancel()
    }

    @Test
    fun saveSettings_fileBackedSettingsSurviveDriverReopen() = runTest {
        val file = File.createTempFile("weekly-settings-", ".db")
        val url = "jdbc:sqlite:${file.absolutePath}"
        val settings = sampleSettings()
        try {
            val firstDriver = JdbcSqliteDriver(url)
            try {
                YourRefriDatabase.Schema.create(firstDriver)
                repository(createDatabase(firstDriver)).saveSettings(settings)
            } finally {
                firstDriver.close()
            }

            val reopenedDriver = JdbcSqliteDriver(url)
            try {
                assertEquals(settings, repository(createDatabase(reopenedDriver)).observeSettings().first())
            } finally {
                reopenedDriver.close()
            }
        } finally {
            file.delete()
        }
    }

    @Test
    fun migrateV3ToV4_createsSettingsAndPlanTables() = runTest {
        val file = File.createTempFile("yourrefri-v3-", ".db")
        val url = "jdbc:sqlite:${file.absolutePath}"
        val oldDriver = JdbcSqliteDriver(url)
        try {
            YourRefriDatabase.Schema.migrate(oldDriver, oldVersion = 3, newVersion = 4)
            val migratedDb = createDatabase(oldDriver)
            // Schema v5 thêm MealSlotToggleEntity (UX-21, chỉ thị người dùng 2026-11-12).
            // Schema v7 thêm CookLogMealEntity (A-11, 2026-11-13; người dùng cho phép đổi con số ghim này).
            assertEquals(7L, YourRefriDatabase.Schema.version)
            assertEquals(emptyList(), repository(migratedDb).observeAll().first())
            val settings = sampleSettings()
            repository(migratedDb).saveSettings(settings)
            assertEquals(settings, repository(migratedDb).observeSettings().first())
        } finally {
            oldDriver.close()
            file.delete()
        }
    }

    @Test
    fun openExistingV4Database_createsSettingsTableAndKeepsWeeklyEntries() = runTest {
        val legacyDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        legacyDriver.execute(
            null,
            "CREATE TABLE WeeklyMealPlanEntity (day TEXT NOT NULL, mealSlot TEXT NOT NULL, recipeId TEXT NOT NULL, portions INTEGER NOT NULL, PRIMARY KEY (day, mealSlot))",
            0,
        )
        legacyDriver.execute(
            null,
            "INSERT INTO WeeklyMealPlanEntity VALUES ('2026-11-09', 'DINNER', 'stew', 3)",
            0,
        )
        legacyDriver.execute(null, "PRAGMA user_version = 4", 0)

        try {
            val existingPlan = WeeklyMealPlanEntry(LocalDate(2026, 11, 9), MealSlot.DINNER, "stew", 3)
            val migratedRepository = repository(createDatabase(legacyDriver))
            assertEquals(listOf(existingPlan), migratedRepository.observeAll().first())
            assertEquals(null, migratedRepository.observeSettings().first())

            val settings = sampleSettings()
            migratedRepository.saveSettings(settings)
            assertEquals(settings, migratedRepository.observeSettings().first())
        } finally {
            legacyDriver.close()
        }
    }

    private fun sampleSettings() = WeeklyMealPlanSettings(
        weekStart = LocalDate(2026, 11, 9),
        goalId = "balanced",
        preferenceIds = setOf("seafood", "budget"),
        swappedOffsets = mapOf(2 to 1, 5 to 3),
        uncheckedIds = setOf("rice", "tomato"),
        includeLunchMealPrep = true,
    )
}
