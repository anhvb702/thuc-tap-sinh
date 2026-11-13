package com.loitu.yourrefri.feature.recipe.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.loitu.yourrefri.feature.backup.data.BackupCodec
import com.loitu.yourrefri.feature.backup.data.CookLogBackup
import com.loitu.yourrefri.feature.backup.data.FavoriteRecipeBackup
import com.loitu.yourrefri.feature.backup.data.PantryBackupRepositoryImpl
import com.loitu.yourrefri.feature.backup.data.PantryBackupSnapshot
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class FavoriteRecipeRepositoryTest {
    private lateinit var driver: SqlDriver
    private lateinit var db: YourRefriDatabase
    private var nowMs = 1_000L
    private val clock = object : Clock { override fun now(): Instant = Instant.fromEpochMilliseconds(nowMs) }

    @BeforeTest fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        YourRefriDatabase.Schema.create(driver)
        db = createDatabase(driver)
    }

    @AfterTest fun tearDown() = driver.close()

    private fun repo() = FavoriteRecipeRepositoryImpl(db, Dispatchers.Unconfined, clock)
    private fun backup() = PantryBackupRepositoryImpl(db, Dispatchers.Unconfined)

    @Test fun pureToggleAddsAndRemoves() {
        assertEquals(setOf("a"), toggleFavorite(emptySet(), "a"))
        assertEquals(emptySet(), toggleFavorite(setOf("a"), "a"))
        assertEquals(setOf("a", "b"), toggleFavorite(setOf("a"), "b"))
    }

    @Test fun startsEmptyAndToggleFlipsState() = runTest {
        val r = repo()
        assertEquals(emptySet(), r.observeIds().first())
        assertTrue(r.toggle("soup"))
        assertEquals(setOf("soup"), r.observeIds().first())
        assertEquals(false, r.toggle("soup"))
        assertEquals(emptySet(), r.observeIds().first())
    }

    @Test fun setFavoriteIsIdempotentAndKeepsFirstCreatedAt() = runTest {
        val r = repo()
        r.setFavorite("soup", true)
        nowMs = 9_999L
        r.setFavorite("soup", true)
        assertEquals(1_000L, db.favoriteRecipeEntityQueries.selectAll().executeAsOne().createdAtEpochMs)
        r.setFavorite("soup", false)
        r.setFavorite("soup", false)
        assertEquals(emptySet(), r.observeIds().first())
    }

    @Test fun blankIdRejectedAndNothingSaved() = runTest {
        val r = repo()
        assertFailsWith<IllegalArgumentException> { r.toggle("") }
        assertFailsWith<IllegalArgumentException> { r.setFavorite("  ", true) }
        assertEquals(emptySet(), r.observeIds().first())
    }

    @Test fun migrationFromV5KeepsExistingDataAndAddsEmptyFavorites() = runTest {
        val old = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            YourRefriDatabase.Schema.create(old)
            old.execute(null, "DROP TABLE FavoriteRecipeEntity", 0)
            old.execute(null, "INSERT INTO PantryEntity(ingredientId, have, markedAt) VALUES ('egg', 1, '2026-03-04')", 0)
            YourRefriDatabase.Schema.migrate(old, 5, 6)
            val migrated = createDatabase(old)
            assertEquals("egg", migrated.pantryEntityQueries.selectAll().executeAsOne().ingredientId)
            val r = FavoriteRecipeRepositoryImpl(migrated, Dispatchers.Unconfined, clock)
            assertEquals(emptySet(), r.observeIds().first())
            r.setFavorite("soup", true)
            assertEquals(setOf("soup"), r.observeIds().first())
        } finally { old.close() }
    }

    @Test fun exportRestoreReplacesFavoritesCompletely() = runTest {
        val r = repo()
        r.setFavorite("soup", true)
        nowMs = 2_000L
        r.setFavorite("egg", true)
        val payload = backup().exportJson()
        assertEquals(listOf("soup", "egg"), BackupCodec.decode(payload).favorites.map { it.recipeId })
        r.setFavorite("soup", false)
        r.setFavorite("rice", true)
        backup().restoreJson(payload)
        assertEquals(setOf("soup", "egg"), r.observeIds().first())
        assertEquals(1_000L, db.favoriteRecipeEntityQueries.selectAll().executeAsList().first().createdAtEpochMs)
    }

    @Test fun restoringOldBackupWithoutFavoritesClearsFavorites() = runTest {
        repo().setFavorite("soup", true)
        backup().restoreJson("""{"format":"yourrefri-backup","version":2,"pantry":[],"shelfOverrides":[],"quantities":[],"cookLogs":[]}""")
        assertEquals(emptySet(), repo().observeIds().first())
    }

    @Test fun failedRestoreRollsBackFavorites() = runTest {
        repo().setFavorite("soup", true)
        driver.execute(null, "CREATE TRIGGER reject_fav_log BEFORE INSERT ON CookLogEntity WHEN NEW.recipeId = 'fail' BEGIN SELECT RAISE(ABORT, 'forced'); END", 0)
        val bad = PantryBackupSnapshot(
            BackupCodec.FORMAT, BackupCodec.VERSION, emptyList(), emptyList(), emptyList(),
            listOf(CookLogBackup(99, "fail", "2026-11-12", null, emptyList())),
            favorites = listOf(FavoriteRecipeBackup("other", 1)),
        )
        assertFailsWith<Exception> { backup().restoreJson(BackupCodec.encode(bad)) }
        assertEquals(setOf("soup"), repo().observeIds().first())
    }
}