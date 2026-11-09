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
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PantryBackupRepositoryTest {
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

    private fun snapshot(
        pantryId: String = "egg",
        have: Boolean = true,
        markedAt: String = "2026-11-01",
        shelfId: String = pantryId,
        shelfDays: Int = 7,
        quantityId: String = pantryId,
        quantity: Int = 3,
        unitId: String = pantryId,
        unit: String = "quả",
        logId: Long = 10,
        recipeId: String = "soup",
        cookedOn: String = "2026-11-02",
    ) = PantryBackupSnapshot(
        format = BackupCodec.FORMAT,
        version = BackupCodec.VERSION,
        pantry = listOf(PantryBackupItem(pantryId, have, markedAt)),
        shelfOverrides = listOf(ShelfOverrideBackup(shelfId, shelfDays)),
        quantities = listOf(QuantityOverrideBackup(quantityId, quantity)),
        units = listOf(UnitOverrideBackup(unitId, unit)),
        cookLogs = listOf(CookLogBackup(logId, recipeId, cookedOn, "content://photos/$logId", listOf(pantryId))),
    )

    private fun insertSnapshotRows(data: PantryBackupSnapshot) {
        data.pantry.forEach { db.pantryEntityQueries.upsert(it.ingredientId, it.have, it.markedAt) }
        data.shelfOverrides.forEach { db.userShelfEntityQueries.upsert(it.ingredientId, it.shelfDays.toLong()) }
        data.quantities.forEach { db.pantryQuantityEntityQueries.upsert(it.ingredientId, it.quantity.toLong()) }
        data.units.forEach { db.pantryUnitEntityQueries.upsert(it.ingredientId, it.unit) }
        data.cookLogs.forEach {
            db.cookLogEntityQueries.insertWithId(it.id, it.recipeId, it.cookedOn, it.photoUri, it.savedIngredientIds)
        }
    }

    private fun readSnapshot(): PantryBackupSnapshot = PantryBackupSnapshot(
        format = BackupCodec.FORMAT,
        version = BackupCodec.VERSION,
        pantry = db.pantryEntityQueries.selectAll().executeAsList().map {
            PantryBackupItem(it.ingredientId, it.have, it.markedAt)
        },
        shelfOverrides = db.userShelfEntityQueries.selectAll().executeAsList().map {
            ShelfOverrideBackup(it.ingredientId, it.userShelfDays.toInt())
        },
        quantities = db.pantryQuantityEntityQueries.selectAll().executeAsList().map {
            QuantityOverrideBackup(it.ingredientId, it.quantity.toInt())
        },
        units = db.pantryUnitEntityQueries.selectAll().executeAsList().map {
            UnitOverrideBackup(it.ingredientId, it.unit)
        },
        cookLogs = db.cookLogEntityQueries.selectAll().executeAsList().map {
            CookLogBackup(it.id, it.recipeId, it.cookedOn, it.photoUri, it.savedIngredientIds)
        },
    )

    @Test
    fun export_includesRowsFromAllTables() = runTest {
        val expected = PantryBackupSnapshot(
            format = BackupCodec.FORMAT,
            version = BackupCodec.VERSION,
            pantry = listOf(PantryBackupItem("egg", false, "2026-11-01")),
            shelfOverrides = listOf(ShelfOverrideBackup("egg", 0)),
            quantities = listOf(QuantityOverrideBackup("egg", 4)),
            units = listOf(UnitOverrideBackup("egg", "kg")),
            cookLogs = listOf(CookLogBackup(1, "soup", "2026-11-02", "content://photos/1", listOf("egg"))),
        )
        insertSnapshotRows(expected)

        val exported = BackupCodec.decode(repository().exportJson())

        assertEquals(expected, exported)
    }

    @Test
    fun restore_replacesExistingRowsInEveryTable() = runTest {
        insertSnapshotRows(snapshot(pantryId = "old", shelfId = "old", quantityId = "old", logId = 1))
        val replacement = PantryBackupSnapshot(
            BackupCodec.FORMAT, BackupCodec.VERSION,
            listOf(PantryBackupItem("new", false, "2026-11-05")),
            listOf(ShelfOverrideBackup("new", 0)),
            listOf(QuantityOverrideBackup("new", 2)),
            listOf(CookLogBackup(25, "stew", "2026-11-06", null, listOf("new"))),
            listOf(UnitOverrideBackup("new", "chai")),
        )

        repository().restoreJson(BackupCodec.encode(replacement))

        assertEquals(replacement, readSnapshot())
    }

    @Test
    fun restore_preservesCookLogIdsAndSameDayOrder() = runTest {
        val payload = PantryBackupSnapshot(
            BackupCodec.FORMAT, BackupCodec.VERSION,
            pantry = emptyList(), shelfOverrides = emptyList(), quantities = emptyList(),
            cookLogs = listOf(
                CookLogBackup(9, "first", "2026-11-02", null, emptyList()),
                CookLogBackup(4, "second", "2026-11-02", null, emptyList()),
            ),
            units = listOf(UnitOverrideBackup("egg", "quả")),
        )

        repository().restoreJson(BackupCodec.encode(payload))

        val logs = db.cookLogEntityQueries.selectAll().executeAsList()
        assertEquals(listOf(9L, 4L), logs.map { it.id })
        assertEquals(listOf("first", "second"), logs.map { it.recipeId })
    }

    @Test
    fun restore_invalidPayload_leavesDatabaseUnchanged() = runTest {
        val original = snapshot(have = false, shelfDays = 0)
        insertSnapshotRows(original)

        assertFailsWith<IllegalArgumentException> { repository().restoreJson("{broken") }

        assertEquals(original, readSnapshot())
    }

    @Test
    fun restoreDatabaseFailure_rollsBackAllTables() = runTest {
        val original = snapshot(have = false, shelfDays = 0)
        insertSnapshotRows(original)
        driver.execute(
            identifier = null,
            sql = """CREATE TRIGGER reject_backup_log BEFORE INSERT ON CookLogEntity
                WHEN NEW.recipeId = 'fail'
                BEGIN SELECT RAISE(ABORT, 'forced insert failure'); END""",
            parameters = 0,
        )
        val replacement = PantryBackupSnapshot(
            BackupCodec.FORMAT, BackupCodec.VERSION,
            listOf(PantryBackupItem("replacement", true, "2026-11-07")),
            listOf(ShelfOverrideBackup("replacement", 2)),
            listOf(QuantityOverrideBackup("replacement", 5)),
            listOf(CookLogBackup(99, "fail", "2026-11-08", null, emptyList())),
            listOf(UnitOverrideBackup("replacement", "kg")),
        )

        assertFailsWith<Exception> { repository().restoreJson(BackupCodec.encode(replacement)) }

        assertEquals(original, readSnapshot())
    }

    @Test
    fun restore_emptySnapshot_clearsAllUserData() = runTest {
        insertSnapshotRows(snapshot())
        val empty = PantryBackupSnapshot(BackupCodec.FORMAT, BackupCodec.VERSION, emptyList(), emptyList(), emptyList(), emptyList())

        repository().restoreJson(BackupCodec.encode(empty))

        assertEquals(empty, readSnapshot())
        assertTrue(db.pantryEntityQueries.selectAll().executeAsList().isEmpty())
        assertTrue(db.userShelfEntityQueries.selectAll().executeAsList().isEmpty())
        assertTrue(db.pantryQuantityEntityQueries.selectAll().executeAsList().isEmpty())
        assertTrue(db.pantryUnitEntityQueries.selectAll().executeAsList().isEmpty())
        assertTrue(db.cookLogEntityQueries.selectAll().executeAsList().isEmpty())
    }
}
