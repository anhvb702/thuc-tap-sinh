package com.loitu.yourrefri.feature.cooklog.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class CookHistoryDeleteCopyTest {
    @Test
    fun deleteButtonUsesFigmaLabel() {
        assertEquals("Xóa dòng ghi nhầm", CookHistoryCopy.deleteEntry)
    }
}
