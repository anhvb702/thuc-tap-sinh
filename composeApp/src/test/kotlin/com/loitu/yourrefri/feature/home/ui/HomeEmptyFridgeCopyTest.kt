package com.loitu.yourrefri.feature.home.ui

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertEquals

class HomeEmptyFridgeCopyTest {
    @Test
    fun emptyCardTitleMatchesFigma13() {
        assertEquals("Tủ nhà mình đang trống, hết đồ để nấu rồi", HomeEmptyFridgeCopy.title)
        assertEquals("Mình đi chợ thêm ít đồ tươi rồi quay lại chọn món nhé.", HomeEmptyFridgeCopy.subtitle)
    }

    @Test
    fun emptyCardButtonMatchesFigma13() {
        assertEquals("Đi chợ thôi nào", HomeEmptyFridgeCopy.chooseAction)
    }

    @Test
    fun emptyCardCopyAvoidsForbiddenWords() {
        val copy = listOf(HomeEmptyFridgeCopy.title, HomeEmptyFridgeCopy.subtitle, HomeEmptyFridgeCopy.chooseAction)
        listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên", "điểm", "streak").forEach { word -> // lint-copy: rule
            copy.forEach { text -> assertFalse(text.contains(word, ignoreCase = true), "$text chứa từ cấm: $word") }
        }
    }
}
