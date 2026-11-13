package com.loitu.yourrefri.feature.backup.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CookMealBackupCodecTest {
    private fun snapshot(version: Int = BackupCodec.VERSION, meals: List<CookMealBackup>, logIds: List<Long> = listOf(5L)) =
        PantryBackupSnapshot(
            format = BackupCodec.FORMAT,
            version = version,
            pantry = emptyList(),
            shelfOverrides = emptyList(),
            quantities = emptyList(),
            cookLogs = logIds.map { CookLogBackup(it, "soup", "2026-11-13", null, emptyList()) },
            cookMeals = meals,
        )

    @Test
    fun roundTripsCookMeals() {
        val original = snapshot(meals = listOf(CookMealBackup(5, "LUNCH")))
        assertEquals(listOf(CookMealBackup(5, "LUNCH")), BackupCodec.decode(BackupCodec.encode(original)).cookMeals)
    }

    @Test
    fun oldJsonWithoutCookMeals_decodesToEmpty() {
        val old = """{"format":"yourrefri-backup","version":2,"pantry":[],"shelfOverrides":[],"quantities":[],"cookLogs":[{"id":5,"recipeId":"soup","cookedOn":"2026-11-13","photoUri":null,"savedIngredientIds":[]}]}"""
        assertEquals(emptyList(), BackupCodec.decode(old).cookMeals)
    }

    @Test
    fun rejectsUnknownMealValue() {
        assertFailsWith<IllegalArgumentException> { BackupCodec.encode(snapshot(meals = listOf(CookMealBackup(5, "BREAKFAST")))) }
    }

    @Test
    fun rejectsMealForMissingCookLog() {
        assertFailsWith<IllegalArgumentException> { BackupCodec.encode(snapshot(meals = listOf(CookMealBackup(99, "LUNCH")))) }
    }

    @Test
    fun rejectsDuplicateMealForSameCookLog() {
        val twice = listOf(CookMealBackup(5, "LUNCH"), CookMealBackup(5, "DINNER"))
        assertFailsWith<IllegalArgumentException> { BackupCodec.encode(snapshot(meals = twice)) }
    }

    @Test
    fun version1CannotContainCookMeals() {
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.encode(snapshot(version = BackupCodec.LEGACY_VERSION, meals = listOf(CookMealBackup(5, "LUNCH"))))
        }
    }
}
