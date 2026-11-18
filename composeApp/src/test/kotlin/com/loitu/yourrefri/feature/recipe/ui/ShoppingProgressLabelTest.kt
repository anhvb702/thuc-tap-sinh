package com.loitu.yourrefri.feature.recipe.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ShoppingProgressLabelTest {
    @Test
    fun showsBoughtCountAndTickHint() {
        assertEquals("1/5 món đã mua · Chạm để tick hoặc bỏ tick.", shoppingProgressLabel(bought = 1, total = 5))
    }

    @Test
    fun emptyListSaysZeroOfZero() {
        assertEquals("0/0 món đã mua · Chạm để tick hoặc bỏ tick.", shoppingProgressLabel(bought = 0, total = 0))
    }
}
