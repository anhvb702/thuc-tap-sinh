package com.loitu.yourrefri.feature.home.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class HomeEmptyFridgeCopyTest {
    @Test
    fun emptyCardTitleMatchesFigma13() {
        assertEquals("Tủ đang trống.", HomeEmptyFridgeCopy.title)
        assertEquals("Chạm để chọn món", HomeEmptyFridgeCopy.subtitle)
    }

    @Test
    fun emptyCardButtonMatchesFigma13() {
        assertEquals("Chọn món cho bữa trưa  →", HomeEmptyFridgeCopy.chooseAction)
    }
}
