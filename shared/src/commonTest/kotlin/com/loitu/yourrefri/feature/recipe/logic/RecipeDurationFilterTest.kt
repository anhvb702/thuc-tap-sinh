package com.loitu.yourrefri.feature.recipe.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RecipeDurationFilterTest {
    private data class Timed(val id: String, val minutes: Int)
    private val recipes = listOf(Timed("twenty-five", 25), Timed("quick", 12), Timed("long", 35))

    @Test
    fun all_returnsAllInOriginalOrder() {
        assertEquals(recipes, filterRecipesByDuration(recipes, null) { it.minutes })
    }

    @Test
    fun under15_isExclusiveAndSkipsUnknown() {
        val values = listOf(Timed("14", 14), Timed("15", 15), Timed("unknown", 0))
        assertEquals(listOf(Timed("14", 14)), filterRecipesByDuration(values, 15) { it.minutes })
    }

    @Test
    fun under30_includes15To29ButNot30() {
        val values = listOf(Timed("14", 14), Timed("15", 15), Timed("29", 29), Timed("30", 30))
        assertEquals(values.take(3), filterRecipesByDuration(values, 30) { it.minutes })
    }

    @Test
    fun emptyInput_returnsEmpty() {
        assertEquals(emptyList(), filterRecipesByDuration(emptyList<Timed>(), 15) { it.minutes })
    }

    @Test
    fun filter_doesNotMutateInput() {
        val input = recipes.toMutableList()
        val before = input.toList()
        filterRecipesByDuration(input, 30) { it.minutes }
        assertEquals(before, input)
    }

    @Test
    fun invalidThreshold_isRejected() {
        assertFailsWith<IllegalArgumentException> { filterRecipesByDuration(recipes, 0) { it.minutes } }
    }
}
