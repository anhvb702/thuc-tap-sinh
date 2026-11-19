package com.loitu.yourrefri.feature.recipe.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ServingsDifficultyLabelTest {
    @Test
    fun quickRecipeIsEasy() {
        assertEquals("2 người · Dễ nấu", servingsDifficultyLabel(minutes = 10))
        assertEquals("2 người · Dễ nấu", servingsDifficultyLabel(minutes = 15))
    }

    @Test
    fun longerRecipeHasNoDifficultyLabel() {
        assertEquals("2 người", servingsDifficultyLabel(minutes = 35))
    }

    @Test
    fun unknownTimeHasNoDifficultyLabel() {
        assertEquals("2 người", servingsDifficultyLabel(minutes = null))
    }
}
