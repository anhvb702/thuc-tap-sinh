package com.loitu.yourrefri.feature.pantry.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PantrySoonCopyTest {
    private val banned = listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên")

    @Test
    fun chipLabelShowsCount() {
        assertEquals("Nên dùng sớm · 3", PantryScreenCopy.soonChipLabel(3))
        assertEquals("Nên dùng sớm · 0", PantryScreenCopy.soonChipLabel(0))
    }

    @Test
    fun emptyAndActionTextAreExact() {
        assertEquals("Tủ đang ổn, chưa có món nào cần dùng sớm", PantryScreenCopy.soonEmpty)
        assertEquals("Nấu gì từ các món này?", PantryScreenCopy.cookFromSoonAction)
    }

    @Test
    fun soonCopyAvoidsBannedWords() {
        val all = listOf(PantryScreenCopy.soonChipLabel(2), PantryScreenCopy.soonEmpty, PantryScreenCopy.cookFromSoonAction)
        all.forEach { text -> banned.forEach { word -> assertFalse(text.contains(word, ignoreCase = true), "$text chứa $word") } }
    }
}
