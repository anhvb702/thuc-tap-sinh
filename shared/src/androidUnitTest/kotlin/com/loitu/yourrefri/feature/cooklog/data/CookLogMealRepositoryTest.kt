package com.loitu.yourrefri.feature.cooklog.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CookLogMealRepositoryTest {
    private lateinit var driver: SqlDriver
    private lateinit var db: YourRefriDatabase
    private lateinit var repository: CookLogRepository
    private val day = LocalDate(2026, 11, 13)

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        YourRefriDatabase.Schema.create(driver)
        db = createDatabase(driver)
        repository = CookLogRepositoryImpl(db, Dispatchers.Unconfined)
    }

    @AfterTest
    fun tearDown() = driver.close()

    @Test
    fun schemaVersionIs7() {
        assertEquals(7L, YourRefriDatabase.Schema.version)
    }

    @Test
    fun add_withMeal_roundTripsMeal() = runTest {
        repository.add("lunch-dish", day, listOf("egg"), MealSlot.LUNCH)
        repository.add("dinner-dish", day, emptyList(), MealSlot.DINNER)
        val logs = repository.observeAll().first().associateBy { it.recipeId }
        assertEquals(MealSlot.LUNCH, logs.getValue("lunch-dish").meal)
        assertEquals(MealSlot.DINNER, logs.getValue("dinner-dish").meal)
    }

    @Test
    fun add_withoutMeal_readsAsNullMeal() = runTest {
        repository.add("old-style", day, emptyList())
        assertNull(repository.observeAll().first().single().meal)
    }

    @Test
    fun delete_removesTheMealRowToo() = runTest {
        val id = repository.add("x", day, emptyList(), MealSlot.LUNCH)
        repository.delete(id)
        assertTrue(repository.observeAll().first().isEmpty())
        assertTrue(db.cookLogMealEntityQueries.selectAll().executeAsList().isEmpty())
    }

    @Test
    fun observeAll_keepsNewestDayThenNewestIdOrder() = runTest {
        val a = repository.add("a", LocalDate(2026, 11, 12), emptyList(), MealSlot.DINNER)
        val b = repository.add("b", day, emptyList(), MealSlot.LUNCH)
        val c = repository.add("c", day, emptyList())
        assertEquals(listOf(c, b, a), repository.observeAll().first().map { it.id })
    }

    @Test
    fun migrateV6ToV7_keepsOldLogsWithNullMeal() = runTest {
        val file = File.createTempFile("yourrefri-v6-", ".db")
        val url = "jdbc:sqlite:${file.absolutePath}"
        val seed = JdbcSqliteDriver(url)
        try {
            seed.execute(null, "CREATE TABLE CookLogEntity (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, recipeId TEXT NOT NULL, cookedOn TEXT NOT NULL, photoUri TEXT, savedIngredientIds TEXT NOT NULL)", 0)
            seed.execute(null, "INSERT INTO CookLogEntity VALUES (7, 'soup', '2026-11-01', NULL, 'egg,rice')", 0)
        } finally {
            seed.close()
        }
        val old = JdbcSqliteDriver(url)
        try {
            YourRefriDatabase.Schema.migrate(old, oldVersion = 6, newVersion = 7)
            val migrated = CookLogRepositoryImpl(createDatabase(old), Dispatchers.Unconfined)
            val log = migrated.observeAll().first().single()
            assertEquals(7L, log.id)
            assertEquals(listOf("egg", "rice"), log.savedIngredientIds)
            assertNull(log.meal)
            val newId = migrated.add("stew", day, emptyList(), MealSlot.DINNER)
            assertEquals(MealSlot.DINNER, migrated.observeAll().first().first { it.id == newId }.meal)
        } finally {
            old.close()
            file.delete()
        }
    }
}
