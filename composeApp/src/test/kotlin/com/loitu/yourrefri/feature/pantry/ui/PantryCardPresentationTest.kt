package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Test
    fun q24_canToggleCompartment_hidesFreezeWhenFreezerShelfDaysNull() {
        assertFalse(canToggleCompartment(PantryCompartment.CHILL, freezerShelfDays = null))
        assertTrue(canToggleCompartment(PantryCompartment.CHILL, freezerShelfDays = 30))
        assertTrue(canToggleCompartment(PantryCompartment.FREEZE, freezerShelfDays = null))
    }

    @Test
    fun q19_q20_seasoningCardCopy_showsHaveAndShopAction() {
        assertEquals("Còn", seasoningCardCopy(have = true).statusLabel)
        assertEquals("Hết · Chạm để thêm vào tủ", seasoningCardCopy(have = false).statusLabel)
        assertEquals("Hết rồi → Đi chợ", seasoningCardCopy(have = true).markOutAndShopLabel)
    }
}
