@file:OptIn(ExperimentalCoroutinesApi::class)

package com.loitu.yourrefri.feature.pantry.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LeftoverRepositoryTest {
    private lateinit var driver: SqlDriver
    private lateinit var repository: LeftoverRepository

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        YourRefriDatabase.Schema.create(driver)
        repository = LeftoverRepositoryImpl(createDatabase(driver), Dispatchers.Unconfined)
    }

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    @Test
    fun addDefaultsShelfDaysByCompartmentAndUpsertDeleteRoundTrip() = runTest {
        val day = LocalDate(2026, 11, 10)
        val chill = repository.add(title = "  Thịt kho  ", markedAt = day)
        assertEquals("Thịt kho", chill.title)
        assertEquals(PantryCompartment.CHILL, chill.compartment)
        assertEquals(2, chill.shelfDays)

        val freeze = repository.add(
            title = "Canh chua",
            compartment = PantryCompartment.FREEZE,
            markedAt = day,
        )
        assertEquals(PantryCompartment.FREEZE, freeze.compartment)
        assertEquals(14, freeze.shelfDays)

        val items = repository.observeAll().first()
        assertEquals(2, items.size)
        assertTrue(chill in items && freeze in items)

        val updated = chill.copy(title = "Thịt kho trứng", shelfDays = 3)
        repository.upsert(updated)
        assertTrue(updated in repository.observeAll().first())

        repository.delete(chill.id)
        assertEquals(listOf(freeze), repository.observeAll().first())
    }

    @Test
    fun rejectsInvalidTitleIdAndShelfDays() = runTest {
        val day = LocalDate(2026, 11, 10)
        assertFailsWith<IllegalArgumentException> {
            repository.add(title = " ", markedAt = day)
        }
        assertFailsWith<IllegalArgumentException> {
            repository.add(title = "Cơm", markedAt = day, shelfDays = -1)
        }
        assertFailsWith<IllegalArgumentException> {
            LeftoverItem(id = "", title = "Cơm", markedAt = day)
        }
        assertFailsWith<IllegalArgumentException> {
            repository.delete(" ")
        }
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun observeAllEmitsAfterAddAndDelete() = runTest {
        val seen = mutableListOf<List<LeftoverItem>>()
        backgroundScope.launch { repository.observeAll().collect { seen += it } }
        runCurrent()
        assertEquals(emptyList(), seen.single())

        val added = repository.add(title = "Cá kho", markedAt = LocalDate(2026, 11, 12))
        runCurrent()
        assertEquals(listOf(added), seen.last())

        repository.delete(added.id)
        runCurrent()
        assertEquals(emptyList(), seen.last())
    }
}
