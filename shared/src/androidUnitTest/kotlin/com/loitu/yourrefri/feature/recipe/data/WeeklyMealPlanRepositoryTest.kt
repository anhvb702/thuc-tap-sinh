package com.loitu.yourrefri.feature.recipe.data

import java.io.File
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.loitu.yourrefri.shared.db.CookLogEntity
import com.loitu.yourrefri.shared.db.PantryEntity
import com.loitu.yourrefri.shared.db.PantryQuantityEntity
import com.loitu.yourrefri.shared.db.PantryUnitEntity
import com.loitu.yourrefri.shared.db.UserShelfEntity
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
class WeeklyMealPlanRepositoryTest {
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

    private fun repository(database: YourRefriDatabase = db) = WeeklyMealPlanRepositoryImpl(
        db = database,
        ioContext = Dispatchers.Unconfined,
    )

    @Test
    fun saveAndObserve_roundTripsDateMealRecipeAndPortions() = runTest {
        val entries = listOf(
            WeeklyMealPlanEntry(LocalDate(2026, 12, 31), MealSlot.LUNCH, "omelette", 2),
            WeeklyMealPlanEntry(LocalDate(2026, 12, 31), MealSlot.DINNER, "soup", 1),
            WeeklyMealPlanEntry(LocalDate(2027, 1, 1), MealSlot.LUNCH, "fried-rice", 4),
        )
        val repository = repository()

        entries.forEach { repository.save(it) }

        assertEquals(entries, repository.observeAll().first())
    }

    @Test
    fun observeAll_emptyScheduleRemainsEmpty() = runTest {
        assertEquals(emptyList(), repository().observeAll().first())
    }

    @Test
    fun observeAll_subscriberReceivesInitialSaveAndReplacementEmissions() = runTest {
        val repository = repository()
        val emissions = mutableListOf<List<WeeklyMealPlanEntry>>()
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeAll().collect(emissions::add)
        }
        runCurrent()
        assertEquals(listOf(emptyList()), emissions)

        val original = WeeklyMealPlanEntry(LocalDate(2026, 11, 9), MealSlot.LUNCH, "eggs", 2)
        repository.save(original)
        runCurrent()
        val replacement = original.copy(recipeId = "omelette", portions = 4)
        repository.save(replacement)
        runCurrent()

        assertEquals(listOf(emptyList(), listOf(original), listOf(replacement)), emissions)
        collector.cancel()
    }

    @Test
    fun save_fileBackedPlanSurvivesDriverReopen() = runTest {
        val file = File.createTempFile("weekly-plan-", ".db")
        val url = "jdbc:sqlite:${file.absolutePath}"
        val entry = WeeklyMealPlanEntry(LocalDate(2026, 11, 9), MealSlot.DINNER, "stew", 3)
        try {
            val firstDriver = JdbcSqliteDriver(url)
            try {
                YourRefriDatabase.Schema.create(firstDriver)
                repository(createDatabase(firstDriver)).save(entry)
            } finally {
                firstDriver.close()
            }

            val reopenedDriver = JdbcSqliteDriver(url)
            try {
                assertEquals(listOf(entry), repository(createDatabase(reopenedDriver)).observeAll().first())
            } finally {
                reopenedDriver.close()
            }
        } finally {
            file.delete()
        }
    }

    @Test
    fun save_sameDateAndMealOverwritesOnlyThatSelection() = runTest {
        val repository = repository()
        val lunch = LocalDate(2026, 11, 9)
        val dinner = WeeklyMealPlanEntry(lunch, MealSlot.DINNER, "soup", 3)
        val nextDay = WeeklyMealPlanEntry(LocalDate(2026, 11, 10), MealSlot.LUNCH, "rice", 2)
        repository.save(WeeklyMealPlanEntry(lunch, MealSlot.LUNCH, "eggs", 2))
        repository.save(dinner)
        repository.save(nextDay)

        val replacement = WeeklyMealPlanEntry(lunch, MealSlot.LUNCH, "omelette", 5)
        repository.save(replacement)

        assertEquals(listOf(replacement, dinner, nextDay), repository.observeAll().first())
    }

    @Test
    fun save_invalidRecipeOrPortions_rejectsWithoutChangingStoredRows() = runTest {
        val repository = repository()
        val existing = WeeklyMealPlanEntry(LocalDate(2026, 11, 9), MealSlot.LUNCH, "soup", 2)
        repository.save(existing)

        listOf("", " ", "   ").forEach { recipeId ->
            assertFailsWith<IllegalArgumentException> { repository.save(existing.copy(recipeId = recipeId)) }
        }
        listOf(0, -1).forEach { portions ->
            assertFailsWith<IllegalArgumentException> { repository.save(existing.copy(portions = portions)) }
        }

        assertEquals(listOf(existing), repository.observeAll().first())
    }

    @Test
    fun migrateV3ToV4_createsPlanTableAndPreservesEveryLegacyColumn() = runTest {
        val file = File.createTempFile("yourrefri-v3-", ".db")
        val url = "jdbc:sqlite:${file.absolutePath}"
        val seedDriver = JdbcSqliteDriver(url)
        try {
            createV3Tables(seedDriver)
            seedV3Tables(seedDriver)
        } finally {
            seedDriver.close()
        }

        val oldDriver = JdbcSqliteDriver(url)
        try {
            YourRefriDatabase.Schema.migrate(oldDriver, oldVersion = 3, newVersion = 4)
            val migratedDb = createDatabase(oldDriver)

            // Schema v5 thêm MealSlotToggleEntity (UX-21, chỉ thị người dùng 2026-11-12).
            // Schema v7 thêm CookLogMealEntity (A-11, 2026-11-13; người dùng cho phép đổi con số ghim này).
            assertEquals(7L, YourRefriDatabase.Schema.version)
            assertEquals(listOf(PantryEntity("egg", true, "2026-11-01")), migratedDb.pantryEntityQueries.selectAll().executeAsList())
            assertEquals(listOf(UserShelfEntity("egg", 7L)), migratedDb.userShelfEntityQueries.selectAll().executeAsList())
            assertEquals(listOf(PantryQuantityEntity("egg", 3L)), migratedDb.pantryQuantityEntityQueries.selectAll().executeAsList())
            assertEquals(listOf(PantryUnitEntity("egg", "kg")), migratedDb.pantryUnitEntityQueries.selectAll().executeAsList())
            assertEquals(
                listOf(CookLogEntity(41L, "soup", "2026-10-31", "content://photo/1", listOf("egg", "rice"))),
                migratedDb.cookLogEntityQueries.selectAll().executeAsList(),
            )

            val planRepository = repository(migratedDb)
            val planned = WeeklyMealPlanEntry(LocalDate(2026, 11, 2), MealSlot.DINNER, "stew", 2)
            planRepository.save(planned)
            assertEquals(listOf(planned), planRepository.observeAll().first())
        } finally {
            oldDriver.close()
            file.delete()
        }
    }

    private fun createV3Tables(driver: SqlDriver) {
        driver.execute(null, "CREATE TABLE PantryEntity (ingredientId TEXT NOT NULL PRIMARY KEY, have INTEGER NOT NULL, markedAt TEXT NOT NULL)", 0)
        driver.execute(null, "CREATE TABLE UserShelfEntity (ingredientId TEXT NOT NULL PRIMARY KEY, userShelfDays INTEGER NOT NULL)", 0)
        driver.execute(null, "CREATE TABLE PantryQuantityEntity (ingredientId TEXT NOT NULL PRIMARY KEY, quantity INTEGER NOT NULL)", 0)
        driver.execute(null, "CREATE TABLE PantryUnitEntity (ingredientId TEXT NOT NULL PRIMARY KEY, unit TEXT NOT NULL)", 0)
        driver.execute(null, "CREATE TABLE CookLogEntity (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, recipeId TEXT NOT NULL, cookedOn TEXT NOT NULL, photoUri TEXT, savedIngredientIds TEXT NOT NULL)", 0)
    }

    private fun seedV3Tables(driver: SqlDriver) {
        driver.execute(null, "INSERT INTO PantryEntity VALUES ('egg', 1, '2026-11-01')", 0)
        driver.execute(null, "INSERT INTO UserShelfEntity VALUES ('egg', 7)", 0)
        driver.execute(null, "INSERT INTO PantryQuantityEntity VALUES ('egg', 3)", 0)
        driver.execute(null, "INSERT INTO PantryUnitEntity VALUES ('egg', 'kg')", 0)
        driver.execute(null, "INSERT INTO CookLogEntity VALUES (41, 'soup', '2026-10-31', 'content://photo/1', 'egg,rice')", 0)
    }
}
