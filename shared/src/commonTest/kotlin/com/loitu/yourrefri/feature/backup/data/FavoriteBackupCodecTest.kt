package com.loitu.yourrefri.feature.backup.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FavoriteBackupCodecTest {
    private val head = """{"format":"yourrefri-backup","version":2,"pantry":[],"shelfOverrides":[],"quantities":[],"cookLogs":[]"""

    private fun snapshot(vararg favorites: FavoriteRecipeBackup, version: Int = BackupCodec.VERSION) = PantryBackupSnapshot(
        format = BackupCodec.FORMAT, version = version, pantry = emptyList(), shelfOverrides = emptyList(),
        quantities = emptyList(), cookLogs = emptyList(), favorites = favorites.toList(),
    )

    @Test fun oldBackupWithoutFavoritesDecodesToEmpty() {
        assertEquals(emptyList(), BackupCodec.decode(head + "}").favorites)
    }

    @Test fun favoritesRoundTripPreservingOrderAndTime() {
        val original = snapshot(FavoriteRecipeBackup("soup", 100L), FavoriteRecipeBackup("egg", 5L))
        val encoded = BackupCodec.encode(original)
        assertTrue(encoded.contains("\"favorites\""))
        assertEquals(original, BackupCodec.decode(encoded))
    }

    @Test fun rejectsDuplicateBlankAndNegativeFavorites() {
        assertFailsWith<IllegalArgumentException> { BackupCodec.encode(snapshot(FavoriteRecipeBackup("a", 1), FavoriteRecipeBackup("a", 2))) }
        assertFailsWith<IllegalArgumentException> { BackupCodec.encode(snapshot(FavoriteRecipeBackup(" ", 1))) }
        assertFailsWith<IllegalArgumentException> { BackupCodec.encode(snapshot(FavoriteRecipeBackup("a", -1))) }
        assertFailsWith<IllegalArgumentException> { BackupCodec.decode(head + ",\"favorites\":[{\"recipeId\":\"\",\"createdAtEpochMs\":1}]}") }
    }

    @Test fun versionOneCannotContainFavorites() {
        assertFailsWith<IllegalArgumentException> { BackupCodec.encode(snapshot(FavoriteRecipeBackup("a", 1), version = BackupCodec.LEGACY_VERSION)) }
    }
}