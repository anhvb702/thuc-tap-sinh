@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)

package com.loitu.yourrefri.feature.pantry.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.loitu.yourrefri.shared.db.PantryEntity
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private class FixedUnitPantryClock : Clock {
    override fun now(): Instant = Instant.parse("2026-03-10T12:00:00Z")
}

class PantryUnitRepositoryTest {
    private lateinit var driver: SqlDriver
    private lateinit var db: YourRefriDatabase

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        YourRefriDatabase.Schema.create(driver)
        db = createDatabase(driver)
    }

    @AfterTest
    fun tearDown() = driver.close()

    private fun TestScope.repo() = PantryRepositoryImpl(
        db = db,
        clock = FixedUnitPantryClock(),
        timeZone = TimeZone.UTC,
        ioContext = StandardTestDispatcher(testScheduler),
    )

    @Test
    fun observeUnits_isEmptyByDefaultAndNullMeansPhan() = runTest {
        assertTrue(repo().observeUnits().first().isEmpty())
        // Missing map entries are the default unit "phần"; storage has overrides only.
        assertEquals("phần", repo().observeUnits().first()["egg"] ?: "phần")
    }

    @Test
    fun setUnit_overridePersistsAndEmits() = runTest {
        val repository = repo()
        repository.setUnit("egg", "quả")
        assertEquals(mapOf("egg" to "quả"), repository.observeUnits().first())
    }

    @Test
    fun setUnit_defaultDeletesOverride() = runTest {
        val repository = repo()
        repository.setUnit("egg", "quả")
        repository.setUnit("egg", null)
        assertTrue(repository.observeUnits().first().isEmpty())
    }

    @Test
    fun setUnit_rejectsUnknownUnitWithoutChangingStoredValue() = runTest {
        val repository = repo()
        repository.setUnit("egg", "quả")
        assertFailsWith<IllegalArgumentException> { repository.setUnit("egg", "lon") }
        assertEquals(mapOf("egg" to "quả"), repository.observeUnits().first())
    }

    @Test
    fun setUnit_doesNotCreatePantryRowOrChangeExistingPantry() = runTest {
        val repository = repo()
        repository.setHave("egg", true, kotlinx.datetime.LocalDate(2026, 3, 4))
        repository.setUnit("egg", "quả")
        repository.setUnit("milk", "chai")

        assertEquals(listOf(PantryItem("egg", true, kotlinx.datetime.LocalDate(2026, 3, 4))), repository.observe().first())
        assertEquals(PantryEntity("egg", true, "2026-03-04"), db.pantryEntityQueries.selectById("egg").executeAsOne())
        assertEquals(mapOf("egg" to "quả", "milk" to "chai"), repository.observeUnits().first())
    }

    @Test
    fun schemaMigration_v2ToV3_preservesExistingRows() {
        val oldDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            oldDriver.execute(null, """
                CREATE TABLE PantryEntity (
                    ingredientId TEXT NOT NULL PRIMARY KEY,
                    have INTEGER NOT NULL,
                    markedAt TEXT NOT NULL
                )
            """.trimIndent(), 0)
            oldDriver.execute(null, """
                CREATE TABLE UserShelfEntity (
                    ingredientId TEXT NOT NULL PRIMARY KEY,
                    userShelfDays INTEGER NOT NULL
                )
            """.trimIndent(), 0)
            oldDriver.execute(null, """
                CREATE TABLE PantryQuantityEntity (
                    ingredientId TEXT NOT NULL PRIMARY KEY,
                    quantity INTEGER NOT NULL
                )
            """.trimIndent(), 0)
            oldDriver.execute(null, """
                CREATE TABLE CookLogEntity (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    recipeId TEXT NOT NULL,
                    cookedOn TEXT NOT NULL,
                    photoUri TEXT,
                    savedIngredientIds TEXT NOT NULL
                )
            """.trimIndent(), 0)
            oldDriver.execute(null, "INSERT INTO PantryEntity VALUES ('egg', 1, '2026-03-04')", 0)
            oldDriver.execute(null, "INSERT INTO UserShelfEntity VALUES ('egg', 7)", 0)
            oldDriver.execute(null, "INSERT INTO PantryQuantityEntity VALUES ('egg', 3)", 0)
            oldDriver.execute(null, "INSERT INTO CookLogEntity VALUES (1, 'soup', '2026-03-03', NULL, 'egg')", 0)

            YourRefriDatabase.Schema.migrate(oldDriver, oldVersion = 2, newVersion = 3)
            val migratedDb = createDatabase(oldDriver)

            assertEquals(PantryEntity("egg", true, "2026-03-04"), migratedDb.pantryEntityQueries.selectById("egg").executeAsOne())
            assertEquals(7L, migratedDb.userShelfEntityQueries.selectById("egg").executeAsOne())
            assertEquals(3L, migratedDb.pantryQuantityEntityQueries.selectById("egg").executeAsOne())
            assertEquals("soup", migratedDb.cookLogEntityQueries.selectAll().executeAsOne().recipeId)
            assertTrue(migratedDb.pantryUnitEntityQueries.selectAll().executeAsList().isEmpty())
        } finally {
            oldDriver.close()
        }
    }
}
