@file:OptIn(ExperimentalCoroutinesApi::class)

package com.loitu.yourrefri.feature.backup.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExtendedBackupRepositoryTest {
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

    private fun TestScope.repository(): PantryBackupRepositoryImpl =
        PantryBackupRepositoryImpl(db, StandardTestDispatcher(testScheduler))

    @Test
    fun exportAndRestoreRoundTripNewTables() = runTest {
        val expected = PantryBackupSnapshot(
            format = BackupCodec.FORMAT,
            version = BackupCodec.VERSION,
            pantry = emptyList(),
            shelfOverrides = emptyList(),
            quantities = emptyList(),
            cookLogs = emptyList(),
            compartments = listOf(
                CompartmentBackup("beef", "FREEZE"),
                CompartmentBackup("egg", "CHILL"),
            ),
            customRecipes = listOf(
                CustomRecipeBackup(
                    recipeId = "custom-1",
                    title = "Trứng chiên",
                    steps = listOf("Đánh trứng", "Chiên vàng"),
                    required = listOf(listOf("egg")),
                    optional = listOf(listOf("scallion")),
                    minutes = 10,
                ),
            ),
            leftovers = listOf(
                LeftoverBackup("leftover-1", "Thịt kho", "CHILL", 20400L, 2),
                LeftoverBackup("leftover-2", "Sườn hầm", "FREEZE", 20401L, 14),
            ),
            extraShoppingItems = listOf(
                ExtraShoppingBackup("milk", 20400L),
                ExtraShoppingBackup("rice", 20401L),
            ),
        )

        repository().restoreJson(BackupCodec.encode(expected))
        val exported = BackupCodec.decode(repository().exportJson())

        assertEquals(expected, exported)
    }

    @Test
    fun restoreWithoutNewFieldsClearsNewTables() = runTest {
        val initial = PantryBackupSnapshot(
            format = BackupCodec.FORMAT,
            version = BackupCodec.VERSION,
            pantry = emptyList(),
            shelfOverrides = emptyList(),
            quantities = emptyList(),
            cookLogs = emptyList(),
            compartments = listOf(CompartmentBackup("beef", "FREEZE")),
            customRecipes = listOf(CustomRecipeBackup("custom-1", "Món", required = listOf(listOf("egg")))),
            leftovers = listOf(LeftoverBackup("leftover-1", "Cơm", "CHILL", 20400L, 2)),
            extraShoppingItems = listOf(ExtraShoppingBackup("milk", 20400L)),
        )
        repository().restoreJson(BackupCodec.encode(initial))

        val olderV2Json = """{"format":"yourrefri-backup","version":2,"pantry":[],"shelfOverrides":[],"quantities":[],"cookLogs":[]}"""
        repository().restoreJson(olderV2Json)

        assertTrue(db.pantryCompartmentEntityQueries.selectAll().executeAsList().isEmpty())
        assertTrue(db.customRecipeEntityQueries.selectAll().executeAsList().isEmpty())
        assertTrue(db.leftoverEntityQueries.selectAll().executeAsList().isEmpty())
        assertTrue(db.extraShoppingItemEntityQueries.selectAll().executeAsList().isEmpty())
    }

    @Test
    fun createDatabaseCreatesNewTablesOnExistingDatabase() {
        val legacyDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            YourRefriDatabase.Schema.create(legacyDriver)
            legacyDriver.execute(null, "DROP TABLE IF EXISTS ExtraShoppingItemEntity", 0)
            legacyDriver.execute(null, "DROP TABLE IF EXISTS LeftoverEntity", 0)

            val upgradedDb = createDatabase(legacyDriver)
            upgradedDb.extraShoppingItemEntityQueries.upsert("milk", 20400L)
            upgradedDb.leftoverEntityQueries.upsert("leftover-1", "Cơm", "CHILL", 20400L, 2L)

            assertEquals(1, upgradedDb.extraShoppingItemEntityQueries.selectAll().executeAsList().size)
            assertEquals(1, upgradedDb.leftoverEntityQueries.selectAll().executeAsList().size)
        } finally {
            legacyDriver.close()
        }
    }
}
