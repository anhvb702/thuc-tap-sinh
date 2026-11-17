package com.loitu.yourrefri.feature.recipe.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CustomRecipeRepositoryTest {
    private lateinit var driver: SqlDriver
    private lateinit var repository: CustomRecipeRepository

    @BeforeTest fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        YourRefriDatabase.Schema.create(driver)
        repository = CustomRecipeRepositoryImpl(createDatabase(driver), Dispatchers.Unconfined)
    }
    @AfterTest fun tearDown() = driver.close()

    @Test fun addLiteEmitsValidRecipeAndUpsertDeleteRoundTrips() = runTest {
        val added = repository.addLite("Canh rau", listOf("carrot", "rice"))
        assertEquals(listOf("Chuẩn bị nguyên liệu và chế biến món như thường ngày."), added.steps)
        assertEquals(listOf(Slot(listOf("carrot")), Slot(listOf("rice"))), added.required)
        assertEquals(listOf(added), repository.observeAll().first())

        val replacement = added.copy(title = "Canh rau nhà")
        repository.upsert(replacement)
        assertEquals(listOf(replacement), repository.observeAll().first())
        val other = repository.addLite("Trứng luộc", listOf("egg"))
        repository.delete(added.id)
        assertEquals(listOf(other), repository.observeAll().first())
    }

    @Test fun liteRecipeRejectsBlankTitleAndEmptyIngredients() = runTest {
        assertFailsWith<IllegalArgumentException> { repository.addLite(" ", listOf("egg")) }
        assertFailsWith<IllegalArgumentException> { repository.addLite("Trứng", emptyList()) }
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test fun liteRecipeDeduplicatesSelectedIngredientIds() = runTest {
        val added = repository.addLite("Rau xào", listOf("carrot", "carrot"))
        assertEquals(listOf(Slot(listOf("carrot"))), added.required)
    }

    @Test fun observeAllEmitsAfterInsertAndDelete() = runTest {
        val seen = mutableListOf<List<Recipe>>()
        backgroundScope.launch { repository.observeAll().collect { seen += it } }
        runCurrent()
        assertEquals(emptyList(), seen.single())

        val added = repository.addLite("Canh rau", listOf("carrot"))
        runCurrent()
        assertEquals(listOf(added), seen.last())

        repository.delete(added.id)
        runCurrent()
        assertEquals(emptyList(), seen.last())
    }
}
