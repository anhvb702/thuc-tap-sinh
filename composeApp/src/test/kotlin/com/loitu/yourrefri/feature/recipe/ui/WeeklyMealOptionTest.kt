package com.loitu.yourrefri.feature.recipe.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WeeklyMealOptionTest {
    @Test
    fun optionsExposeMealLabelsAndLunchPrepFlag() {
        assertEquals("Bữa tối", WeeklyMealOption.DINNER.labelVi)
        assertEquals("Bữa tối", WeeklyMealOption.DINNER.cardMealLabelVi)
        assertFalse(WeeklyMealOption.DINNER.includesLunchPrep)

        assertEquals("Bữa trưa", WeeklyMealOption.LUNCH.labelVi)
        assertEquals("Bữa trưa", WeeklyMealOption.LUNCH.cardMealLabelVi)
        assertFalse(WeeklyMealOption.LUNCH.includesLunchPrep)

        assertEquals("Cả trưa & tối", WeeklyMealOption.BOTH.labelVi)
        assertEquals("Bữa trưa & tối", WeeklyMealOption.BOTH.cardMealLabelVi)
        assertTrue(WeeklyMealOption.BOTH.includesLunchPrep)
    }

    @Test
    fun displayLabelUsesCardLabelAndContainsNoBannedCopy() {
        val labels = WeeklyMealOption.entries.map(::mealOptionDisplayLabel)
        assertEquals(listOf("Bữa tối", "Bữa trưa", "Bữa trưa & tối"), labels)
        listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên", "điểm", "streak").forEach { banned -> // lint-copy: rule
            labels.forEach { label -> assertFalse(label.contains(banned, ignoreCase = true)) }
        }
    }
}
