package com.loitu.yourrefri.feature.recipe.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.loitu.yourrefri.feature.backup.data.PantryBackupRepositoryImpl
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class MealToggleRepositoryTest {
    private val day = LocalDate(2026, 11, 9)
    @Test fun toggleDefaultsToEnabled() = runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            YourRefriDatabase.Schema.create(driver)
            val repo = WeeklyMealPlanRepositoryImpl(createDatabase(driver), Dispatchers.Unconfined)
            assertEquals(emptyList(), repo.observeDisabledMeals().first())
            repo.setMealEnabled(day, MealSlot.LUNCH, false)
            assertEquals(listOf(MealSlotKey(day, MealSlot.LUNCH)), repo.observeDisabledMeals().first())
        } finally { driver.close() }
    }
    @Test fun reEnablingRestoresSameDishes() = runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            YourRefriDatabase.Schema.create(driver)
            val repo = WeeklyMealPlanRepositoryImpl(createDatabase(driver), Dispatchers.Unconfined)
            val entry = WeeklyMealPlanEntry(day, MealSlot.DINNER, "soup", 3)
            repo.save(entry)
            repo.setMealEnabled(day, MealSlot.DINNER, false)
            assertEquals(listOf(entry), repo.observeAll().first())
            repo.setMealEnabled(day, MealSlot.DINNER, true)
            assertEquals(emptyList(), repo.observeDisabledMeals().first())
            assertEquals(listOf(entry), repo.observeAll().first())
        } finally { driver.close() }
    }
    @Test fun toggleSurvivesBackupRoundTrip() = runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            YourRefriDatabase.Schema.create(driver)
            val db = createDatabase(driver)
            val repo = WeeklyMealPlanRepositoryImpl(db, Dispatchers.Unconfined)
            val backup = PantryBackupRepositoryImpl(db, Dispatchers.Unconfined)
            repo.setMealEnabled(day, MealSlot.LUNCH, false)
            val payload = backup.exportJson()
            repo.setMealEnabled(day, MealSlot.LUNCH, true)
            repo.setMealEnabled(day, MealSlot.DINNER, false)
            backup.restoreJson(payload)
            assertEquals(listOf(MealSlotKey(day, MealSlot.LUNCH)), repo.observeDisabledMeals().first())
        } finally { driver.close() }
    }
    @Test fun migrationKeepsExistingWeeklyRows() = runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            driver.execute(null, "CREATE TABLE WeeklyMealPlanEntity (day TEXT NOT NULL, mealSlot TEXT NOT NULL, recipeId TEXT NOT NULL, portions INTEGER NOT NULL, PRIMARY KEY (day, mealSlot))", 0)
            driver.execute(null, "INSERT INTO WeeklyMealPlanEntity VALUES ('2026-11-09', 'LUNCH', 'eggs', 2)", 0)
            YourRefriDatabase.Schema.migrate(driver, 4, 5)
            val repo = WeeklyMealPlanRepositoryImpl(createDatabase(driver), Dispatchers.Unconfined)
            assertEquals(listOf(WeeklyMealPlanEntry(day, MealSlot.LUNCH, "eggs", 2)), repo.observeAll().first())
            assertEquals(emptyList(), repo.observeDisabledMeals().first())
        } finally { driver.close() }
    }
}
