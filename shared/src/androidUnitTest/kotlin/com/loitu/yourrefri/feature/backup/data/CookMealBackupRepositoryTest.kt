package com.loitu.yourrefri.feature.backup.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.loitu.yourrefri.feature.cooklog.data.CookLogRepositoryImpl
import com.loitu.yourrefri.feature.recipe.data.MealSlot
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

class CookMealBackupRepositoryTest {
    private lateinit var driver: SqlDriver
    private lateinit var db: YourRefriDatabase
    private val day = LocalDate(2026, 11, 13)

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        YourRefriDatabase.Schema.create(driver)
        db = createDatabase(driver)
    }

    @AfterTest
    fun tearDown() = driver.close()

    private fun backup() = PantryBackupRepositoryImpl(db, Dispatchers.Unconfined)
    private fun logs() = CookLogRepositoryImpl(db, Dispatchers.Unconfined)

    @Test
    fun export_includesMealsOfCookLogs() = runTest {
        val id = logs().add("soup", day, listOf("egg"), MealSlot.LUNCH)
        logs().add("plain", day, emptyList())
        val snapshot = BackupCodec.decode(backup().exportJson())
        assertEquals(listOf(CookMealBackup(id, "LUNCH")), snapshot.cookMeals)
    }

    @Test
    fun restore_bringsBackMealsAndReplacesOldOnes() = runTest {
        val id = logs().add("soup", day, listOf("egg"), MealSlot.DINNER)
        val json = backup().exportJson()

        logs().delete(id)
        logs().add("other", day, emptyList(), MealSlot.LUNCH)
        backup().restoreJson(json)

        val restored = logs().observeAll().first()
        assertEquals(listOf("soup"), restored.map { it.recipeId })
        assertEquals(MealSlot.DINNER, restored.single().meal)
        assertEquals(listOf(id), db.cookLogMealEntityQueries.selectAll().executeAsList().map { it.cookLogId })
    }

    @Test
    fun restore_ofBackupWithoutMeals_clearsMealsAndKeepsLogs() = runTest {
        logs().add("soup", day, emptyList(), MealSlot.LUNCH)
        val old = """{"format":"yourrefri-backup","version":2,"pantry":[],"shelfOverrides":[],"quantities":[],"cookLogs":[{"id":3,"recipeId":"stew","cookedOn":"2026-11-02","photoUri":null,"savedIngredientIds":[]}]}"""
        backup().restoreJson(old)
        val restored = logs().observeAll().first()
        assertEquals(listOf("stew"), restored.map { it.recipeId })
        assertEquals(null, restored.single().meal)
    }
}
