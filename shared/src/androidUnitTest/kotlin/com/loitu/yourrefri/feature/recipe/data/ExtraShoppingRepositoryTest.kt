@file:OptIn(ExperimentalCoroutinesApi::class)

package com.loitu.yourrefri.feature.recipe.data

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

class ExtraShoppingRepositoryTest {
    private lateinit var driver: SqlDriver
    private lateinit var repository: ExtraShoppingRepository

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        YourRefriDatabase.Schema.create(driver)
        repository = ExtraShoppingRepositoryImpl(createDatabase(driver), Dispatchers.Unconfined)
    }

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    @Test
    fun addUpsertRemoveAndClearRoundTrip() = runTest {
        val day1 = LocalDate(2026, 11, 1)
        val day2 = LocalDate(2026, 11, 3)

        repository.add("egg", day1)
        repository.add("milk", day2)
        assertEquals(
            listOf(ExtraShoppingItem("egg", day1), ExtraShoppingItem("milk", day2)),
            repository.observeAll().first(),
        )

        repository.add("egg", day2)
        assertEquals(
            listOf(ExtraShoppingItem("egg", day2), ExtraShoppingItem("milk", day2)),
            repository.observeAll().first(),
        )

        repository.remove("egg")
        assertEquals(listOf(ExtraShoppingItem("milk", day2)), repository.observeAll().first())

        repository.clear()
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun rejectsBlankIngredientId() = runTest {
        assertFailsWith<IllegalArgumentException> { repository.add(" ", LocalDate(2026, 11, 1)) }
        assertFailsWith<IllegalArgumentException> { repository.remove("") }
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun observeAllEmitsAfterAddRemoveAndClear() = runTest {
        val seen = mutableListOf<List<ExtraShoppingItem>>()
        backgroundScope.launch { repository.observeAll().collect { seen += it } }
        runCurrent()
        assertEquals(emptyList(), seen.single())

        val day = LocalDate(2026, 11, 5)
        repository.add("carrot", day)
        runCurrent()
        assertEquals(listOf(ExtraShoppingItem("carrot", day)), seen.last())

        repository.remove("carrot")
        runCurrent()
        assertEquals(emptyList(), seen.last())

        repository.add("onion", day)
        runCurrent()
        repository.clear()
        runCurrent()
        assertEquals(emptyList(), seen.last())
    }
}
