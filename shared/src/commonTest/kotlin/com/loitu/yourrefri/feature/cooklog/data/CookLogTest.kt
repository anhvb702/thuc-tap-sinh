package com.loitu.yourrefri.feature.cooklog.data

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class CookLogTest {

    private val cookedOn = LocalDate.parse("2026-11-05")

    private fun log(
        id: Long = 7L,
        recipeId: String = "canh-chua-ca",
        cookedOn: LocalDate = this.cookedOn,
        savedIngredientIds: List<String> = listOf("ca-basa", "ca-chua"),
    ) = CookLog(id = id, recipeId = recipeId, cookedOn = cookedOn, savedIngredientIds = savedIngredientIds)

    @Test
    fun cookLog_photoUriDefaultsToNull() {
        assertNull(log().photoUri)
    }

    @Test
    fun cookLog_keepsAllGivenFields() {
        val l = CookLog(
            id = 42L,
            recipeId = "trung-chien",
            cookedOn = LocalDate.parse("2028-02-29"),
            photoUri = "content://media/1",
            savedIngredientIds = listOf("trung-ga", "hanh-la"),
        )
        assertEquals(42L, l.id)
        assertEquals("trung-chien", l.recipeId)
        assertEquals(LocalDate.parse("2028-02-29"), l.cookedOn)
        assertEquals("content://media/1", l.photoUri)
        assertEquals(listOf("trung-ga", "hanh-la"), l.savedIngredientIds)
    }

    // Nấu xong mà không ghi nhận đồ nào là chuyện bình thường, không được coi là lỗi.
    @Test
    fun cookLog_emptySavedIngredientIds_isAllowed() {
        assertEquals(emptyList(), log(savedIngredientIds = emptyList()).savedIngredientIds)
    }

    @Test
    fun cookLog_equalByValue() {
        val a = log()
        val b = log()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun cookLog_differentCookedOn_notEqual() {
        assertNotEquals(log(), log(cookedOn = LocalDate.parse("2026-11-06")))
    }

    @Test
    fun cookLog_differentPhotoUri_notEqual() {
        assertNotEquals(log(), log().copy(photoUri = "content://media/1"))
    }

    @Test
    fun cookLog_copyChangesOnlyGivenField() {
        val original = log()
        val copied = original.copy(savedIngredientIds = listOf("ca-basa"))
        assertEquals(listOf("ca-basa"), copied.savedIngredientIds)
        assertEquals(original.id, copied.id)
        assertEquals(original.recipeId, copied.recipeId)
        assertEquals(original.cookedOn, copied.cookedOn)
        assertNull(copied.photoUri)
    }
}
