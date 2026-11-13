package com.loitu.yourrefri.feature.recipe.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class CookDoneCopyTest {
    private val banned = listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên", "điểm", "streak")

    @Test
    fun copyIsExact() {
        assertEquals("Xong rồi, dọn cơm thôi!", CookDoneCopy.title)
        assertEquals("Chúc cả nhà ngon miệng", CookDoneCopy.subtitle)
        assertEquals("Về trang chủ", CookDoneCopy.home)
    }

    @Test
    fun doneModelHasOnlyWhatTheScreenShows() {
        // Không có "Hoàn tác" trên màn chúc mừng: nấu xong rồi thì không quay lại được.
        assertEquals(CookDone("Canh chua", "Đã cập nhật trong tủ: Cá."), CookDone("Canh chua", "Đã cập nhật trong tủ: Cá."))
    }

    @Test
    fun copyAvoidsBannedWords() {
        listOf(CookDoneCopy.title, CookDoneCopy.subtitle, CookDoneCopy.home)
            .forEach { t -> banned.forEach { w -> assertFalse(t.contains(w, ignoreCase = true), "$t chứa $w") } }
    }

    @Test
    fun summary_isNullWhenNothingWasUpdated() {
        assertNull(cookDoneSummary(emptyList()))
    }

    @Test
    fun summary_listsTheUpdatedIngredients() {
        assertEquals("Đã cập nhật trong tủ: Cá, Hành.", cookDoneSummary(listOf("Cá", "Hành")))
    }

    @Test
    fun names_mapsIdsInOrder_andSkipsUnknownOnes() {
        val names = mapOf("ca" to "Cá", "hanh" to "Hành")
        assertEquals(listOf("Cá", "Hành"), cookDoneNames(listOf("ca", "la-gi-do", "hanh"), names))
    }

    @Test
    fun names_emptyIdsGivesEmpty() {
        assertEquals(emptyList(), cookDoneNames(emptyList(), mapOf("ca" to "Cá")))
    }
}
