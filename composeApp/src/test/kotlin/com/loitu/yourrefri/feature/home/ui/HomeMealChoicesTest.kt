package com.loitu.yourrefri.feature.home.ui

import com.loitu.yourrefri.feature.recipe.data.MealSlot
import kotlin.test.Test
import kotlin.test.assertEquals

// Spec: docs/superpowers/specs/2026-11-18-ui-feedback-batch-design.md (mục 2: bỏ nút bữa sáng).
class HomeMealChoicesTest {
    @Test
    fun homeOffersOnlyLunchAndDinner() {
        assertEquals(listOf(MealSlot.LUNCH, MealSlot.DINNER), homeMealChoices())
    }

    @Test
    fun savedBreakfastChoiceFallsBackToLunch() {
        assertEquals(MealSlot.LUNCH, homeMealChoice(MealSlot.BREAKFAST))
    }

    @Test
    fun lunchAndDinnerKeepTheirChoice() {
        assertEquals(MealSlot.LUNCH, homeMealChoice(MealSlot.LUNCH))
        assertEquals(MealSlot.DINNER, homeMealChoice(MealSlot.DINNER))
    }
}
