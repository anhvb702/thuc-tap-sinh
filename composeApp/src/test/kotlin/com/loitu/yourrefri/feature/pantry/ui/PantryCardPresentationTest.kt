package com.loitu.yourrefri.feature.pantry.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PantryCardPresentationTest {
    @Test
    fun absentItemExplainsHowToAddWithoutInferringDatabaseHistory() {
        val copy = pantryCardCopy(have = false, quantity = 1)

        assertEquals("Chưa có", copy.statusLabel)
        assertEquals("Chạm để thêm vào tủ", copy.absentHint)
        assertTrue(!copy.showEstimatedStatus)
        assertNull(copy.quantityHint)
    }

    @Test
    fun presentItemAtOneExplainsThatDecreaseMarksItOut() {
        val copy = pantryCardCopy(have = true, quantity = 1)

        assertEquals("Có trong tủ", copy.statusLabel)
        assertNull(copy.absentHint)
        assertTrue(copy.showEstimatedStatus)
        assertEquals("− để đánh dấu hết", copy.quantityHint)
    }

    @Test
    fun presentItemAboveOneHasNoMarkOutHint() {
        val copy = pantryCardCopy(have = true, quantity = 2)

        assertEquals("Có trong tủ", copy.statusLabel)
        assertNull(copy.absentHint)
        assertTrue(copy.showEstimatedStatus)
        assertNull(copy.quantityHint)
    }
}
