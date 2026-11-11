package com.loitu.yourrefri.feature.pantry.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PantryScreenCopyTest {
    @Test
    fun pantryHintExplainsAddAndStatusChangeWhileKeepingEstimateQualifier() {
        assertTrue(PantryScreenCopy.pantryHint.contains("thêm vào tủ"))
        assertTrue(PantryScreenCopy.pantryHint.contains("đổi trạng thái"))
        assertTrue(PantryScreenCopy.pantryHint.contains("ước tính"))
    }

    @Test
    fun addFoodButtonHasActionDescription() {
        assertEquals("Thêm thực phẩm vào tủ", PantryScreenCopy.addFoodDescription)
    }
}
