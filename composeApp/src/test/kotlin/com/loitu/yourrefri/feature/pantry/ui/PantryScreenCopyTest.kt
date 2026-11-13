package com.loitu.yourrefri.feature.pantry.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PantryScreenCopyTest {
    @Test
    fun pantryHintExplainsAddAndStatusChangeWhileKeepingEstimateQualifier() {
        // A-14 (2026-11-13, người dùng cho phép sửa): rút gọn để bớt chữ.
        assertEquals("Chạm để thêm hoặc đổi trạng thái. Hạn là ước tính.", PantryScreenCopy.pantryHint)
        assertTrue(PantryScreenCopy.pantryHint.contains("thêm"))
        assertTrue(PantryScreenCopy.pantryHint.contains("đổi trạng thái"))
        assertTrue(PantryScreenCopy.pantryHint.contains("ước tính"))
    }

    @Test
    fun addFoodButtonHasActionDescription() {
        assertEquals("Thêm thực phẩm vào tủ", PantryScreenCopy.addFoodDescription)
    }
}
