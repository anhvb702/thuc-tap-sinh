package com.loitu.yourrefri.feature.cooklog.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.test.assertNotEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CookLogRepositoryTest {
    private lateinit var driver: SqlDriver
    private lateinit var repository: CookLogRepository
    private val day = LocalDate(2026, 11, 12)

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        YourRefriDatabase.Schema.create(driver)
        repository = CookLogRepositoryImpl(createDatabase(driver), Dispatchers.Unconfined)
    }

    @AfterTest
    fun tearDown() = driver.close()

    @Test
    fun observeAll_ordersNewestDayThenNewestId() = runTest {
        val old = repository.add("old", LocalDate(2026, 11, 11), emptyList())
        val first = repository.add("first", day, emptyList())
        val second = repository.add("second", day, emptyList())
        val olderInsertedLast = repository.add("older", LocalDate(2026, 11, 10), emptyList())
        assertEquals(listOf(second, first, old, olderInsertedLast), repository.observeAll().first().map { it.id })
    }

    @Test
    fun add_returnsInsertedIdAndRoundTripsIngredients() = runTest {
        val first = repository.add("soup", day, listOf("egg", "rice"))
        val second = repository.add("stew", day, listOf("carrot"))
        assertNotEquals(first, second)
        assertEquals(
            listOf(CookLog(id = second, recipeId = "stew", cookedOn = day, savedIngredientIds = listOf("carrot")), CookLog(id = first, recipeId = "soup", cookedOn = day, savedIngredientIds = listOf("egg", "rice"))),
            repository.observeAll().first(),
        )
    }

    @Test
    fun add_allowsEmptyIngredientList() = runTest {
        val id = repository.add("soup", day, emptyList())
        assertEquals(listOf(CookLog(id = id, recipeId = "soup", cookedOn = day, savedIngredientIds = emptyList())), repository.observeAll().first())
    }

    @Test
    fun add_rejectsBlankRecipeId() = runTest {
        val id = repository.add("soup", day, emptyList())
        listOf("", " ", "  ").forEach { recipeId ->
            assertFailsWith<IllegalArgumentException> { repository.add(recipeId, day, emptyList()) }
        }
        assertEquals(listOf(CookLog(id = id, recipeId = "soup", cookedOn = day, savedIngredientIds = emptyList())), repository.observeAll().first())
    }

    @Test
    fun delete_removesOnlyThatRow() = runTest {
        val first = repository.add("soup", day, listOf("egg"))
        val second = repository.add("stew", day, listOf("rice"))
        repository.delete(first)
        assertEquals(listOf(CookLog(id = second, recipeId = "stew", cookedOn = day, savedIngredientIds = listOf("rice"))), repository.observeAll().first())
    }

    @Test
    fun flowEmitsAfterChange() = runTest {
        val emissions = mutableListOf<List<CookLog>>()
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeAll().collect(emissions::add)
        }
        runCurrent()
        val id = repository.add("soup", day, listOf("egg"))
        runCurrent()
        repository.delete(id)
        runCurrent()
        assertEquals(listOf(emptyList(), listOf(CookLog(id = id, recipeId = "soup", cookedOn = day, savedIngredientIds = listOf("egg"))), emptyList()), emissions)
        collector.cancel()
    }
}
