package com.loitu.yourrefri.feature.recipe.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class RecipeDurationFilterOptionsTest {
    @Test
    fun optionsUseInclusiveLabelsAndThresholds() {
        assertEquals(
            listOf(
                RecipeDurationFilterOption("Tất cả", null),
                RecipeDurationFilterOption("Tối đa 15 phút", 15),
                RecipeDurationFilterOption("Tối đa 30 phút", 30),
            ),
            recipeDurationFilterOptions(),
        )
    }
}
