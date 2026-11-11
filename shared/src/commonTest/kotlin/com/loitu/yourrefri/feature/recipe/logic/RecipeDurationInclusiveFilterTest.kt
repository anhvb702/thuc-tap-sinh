package com.loitu.yourrefri.feature.recipe.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RecipeDurationInclusiveFilterTest {
    private data class Timed(val id: String, val minutes: Int?)

    @Test
    fun all_returnsEveryItemIncludingDuplicatesInOriginalOrder() {
        val repeated = Timed("repeat", 15)
        val values = listOf(Timed("long", 31), repeated, repeated, Timed("unknown", null))

        assertEquals(values, filterRecipesByDurationInclusive(values, null) { it.minutes })
    }

    @Test
    fun maximum15_includesBoundaryAndRejectsOutsideValues() {
        val values = listOf(
            Timed("one", 1), Timed("fourteen", 14), Timed("fifteen", 15),
            Timed("sixteen", 16), Timed("zero", 0), Timed("negative", -1), Timed("unknown", null),
        )

        assertEquals(values.take(3), filterRecipesByDurationInclusive(values, 15) { it.minutes })
    }

    @Test
    fun maximum30_includesBoundaryAndRejectsOutsideValues() {
        val values = listOf(
            Timed("one", 1), Timed("fifteen", 15), Timed("twenty-nine", 29),
            Timed("thirty", 30), Timed("thirty-one", 31), Timed("zero", 0),
            Timed("negative", -1), Timed("unknown", null),
        )

        assertEquals(values.take(4), filterRecipesByDurationInclusive(values, 30) { it.minutes })
    }

    @Test
    fun emptyInput_returnsEmpty() {
        assertEquals(emptyList(), filterRecipesByDurationInclusive(emptyList<Timed>(), 15) { it.minutes })
    }

    @Test
    fun filter_doesNotMutateInput() {
        val input = mutableListOf(Timed("thirty", 30), Timed("fifteen", 15), Timed("sixteen", 16))
        val before = input.toList()

        filterRecipesByDurationInclusive(input, 30) { it.minutes }

        assertEquals(before, input)
    }

    @Test
    fun nonPositiveThreshold_isRejected() {
        val values = listOf(Timed("one", 1))

        assertFailsWith<IllegalArgumentException> {
            filterRecipesByDurationInclusive(values, 0) { it.minutes }
        }
        assertFailsWith<IllegalArgumentException> {
            filterRecipesByDurationInclusive(values, -1) { it.minutes }
        }
    }
}
