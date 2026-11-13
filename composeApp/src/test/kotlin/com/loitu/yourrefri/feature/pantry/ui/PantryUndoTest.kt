package com.loitu.yourrefri.feature.pantry.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PantryUndoTest {
    @Test
    fun decreaseMarksOutOnlyWhenQuantityIsOneOrLess() {
        assertTrue(decreaseMarksOut(1))
        assertTrue(decreaseMarksOut(0))
        assertFalse(decreaseMarksOut(2))
        assertFalse(decreaseMarksOut(10))
    }

    @Test
    fun tapOffersUndoOnlyForItemsThatAreInThePantry() {
        assertTrue(tapMarksOut(have = true))
        assertFalse(tapMarksOut(have = false))
    }

    @Test
    fun undoCopyIsGentleAndNamesTheItem() {
        assertEquals("Đã đánh dấu hết Cà chua", markedOutMessage("Cà chua"))
        assertEquals("Hoàn tác", UNDO_LABEL)
        listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên").forEach { banned ->
            assertFalse(markedOutMessage("Trứng gà").contains(banned, ignoreCase = true))
        }
    }
}
