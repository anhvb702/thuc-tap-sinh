@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)

package com.loitu.yourrefri.feature.pantry.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.loitu.yourrefri.shared.db.PantryEntity
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
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

private class FixedPantryClock : Clock {
    override fun now(): Instant = Instant.parse("2026-03-10T12:00:00Z")
}

class PantryQuantityRepositoryTest {
    private lateinit var driver: SqlDriver
    private lateinit var db: YourRefriDatabase

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        YourRefriDatabase.Schema.create(driver)
        db = createDatabase(driver)
    }

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    private fun TestScope.repo(): PantryRepositoryImpl = PantryRepositoryImpl(
        db = db,
        clock = FixedPantryClock(),
        timeZone = TimeZone.UTC,
        ioContext = StandardTestDispatcher(testScheduler),
    )

    @Test
    fun observeQuantities_emptyByDefault() = runTest {
        assertTrue(repo().observeQuantities().first().isEmpty())
    }

    @Test
    fun setQuantity_aboveDefault_persistsAndEmits() = runTest {
        val repository = repo()
        val emissions = mutableListOf<Map<String, Int>>()
        backgroundScope.launch { repository.observeQuantities().collect(emissions::add) }
        runCurrent()

        repository.setQuantity("egg", 2)
        runCurrent()

        assertEquals(mapOf("egg" to 2), repository.observeQuantities().first())
        assertEquals(mapOf("egg" to 2), emissions.last())
    }

    @Test
    fun setQuantity_default_removesStoredOverride() = runTest {
        val repository = repo()
        repository.setQuantity("egg", 2)

        repository.setQuantity("egg", 1)

        assertTrue(repository.observeQuantities().first().isEmpty())
    }

    @Test
    fun setQuantity_doesNotCreatePantryRowOrChangeExistingPantry() = runTest {
        val repository = repo()
        val markedAt = LocalDate(2026, 3, 4)
        repository.setQuantity("tomato", 4)
        repository.setHave("egg", true, markedAt)
        repository.setQuantity("egg", 3)

        assertEquals(listOf(PantryItem("egg", true, markedAt)), repository.observe().first())
        assertEquals(mapOf("egg" to 3, "tomato" to 4), repository.observeQuantities().first())
    }

    @Test
    fun setQuantity_zeroOrNegative_rejectedWithoutWrite() = runTest {
        val repository = repo()
        repository.setQuantity("egg", 2)

        assertFailsWith<IllegalArgumentException> { repository.setQuantity("egg", 0) }
        assertFailsWith<IllegalArgumentException> { repository.setQuantity("egg", -1) }

        assertEquals(mapOf("egg" to 2), repository.observeQuantities().first())
    }

    @Test
    fun schemaMigration_v1ToV2_preservesExistingRows() {
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
            oldDriver.execute(null, "INSERT INTO CookLogEntity VALUES (1, 'soup', '2026-03-03', NULL, 'egg')", 0)

            YourRefriDatabase.Schema.migrate(oldDriver, oldVersion = 1, newVersion = 2)
            val migratedDb = createDatabase(oldDriver)

            assertEquals(PantryEntity("egg", true, "2026-03-04"), migratedDb.pantryEntityQueries.selectById("egg").executeAsOne())
            assertEquals(7L, migratedDb.userShelfEntityQueries.selectById("egg").executeAsOne())
            assertEquals("soup", migratedDb.cookLogEntityQueries.selectAll().executeAsOne().recipeId)
            assertTrue(migratedDb.pantryQuantityEntityQueries.selectAll().executeAsList().isEmpty())
        } finally {
            oldDriver.close()
        }
    }
}
