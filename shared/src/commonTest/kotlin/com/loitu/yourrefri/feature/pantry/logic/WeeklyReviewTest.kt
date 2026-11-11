package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.PantryItem
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class WeeklyReviewTest {
    private val egg = PantryItem("egg", true, LocalDate(2026, 10, 1))
    private val rice = PantryItem("rice", false, LocalDate(2026, 9, 2))

    @Test
    fun defaultReview_keepsEveryField() {
        assertEquals(listOf(egg, rice), weeklyReview(listOf(egg, rice)))
    }

    @Test
    fun explicitEmptyReview_keepsEveryField() {
        assertEquals(listOf(egg, rice), weeklyReview(listOf(egg, rice), emptyMap()))
    }

    @Test
    fun onlyTouchedItemChanges() {
        assertEquals(listOf(egg.copy(have = false), rice), weeklyReview(listOf(egg, rice), mapOf("egg" to false)))
    }

    @Test
    fun selectedOutItemBecomesHave_withoutRefreshingMarkedAt() {
        assertEquals(listOf(egg, rice.copy(have = true)), weeklyReview(listOf(egg, rice), mapOf("rice" to true)))
    }

    @Test
    fun selectedValueIsFinal_notAToggle() {
        assertEquals(listOf(egg, rice), weeklyReview(listOf(egg, rice), mapOf("egg" to true, "rice" to false)))
    }

    @Test
    fun unknownIds_areIgnored() {
        assertEquals(listOf(egg, rice), weeklyReview(listOf(egg, rice), mapOf("unknown" to true, "other" to false)))
    }

    @Test
    fun multipleChanges_preservePantryOrder() {
        val pantry = listOf(egg.copy(ingredientId = "z"), rice.copy(ingredientId = "a"), egg.copy(ingredientId = "b"))
        assertEquals(
            listOf(pantry[0].copy(have = false), pantry[1], pantry[2].copy(have = false)),
            weeklyReview(pantry, linkedMapOf("b" to false, "z" to false)),
        )
    }

    @Test
    fun emptyPantry_returnsEmpty() {
        assertEquals(emptyList(), weeklyReview(emptyList(), mapOf("egg" to true)))
    }

    @Test
    fun inputsRemainUnchanged_acrossRepeatedCalls() {
        val pantry = mutableListOf(egg, rice)
        val changes = mutableMapOf("egg" to false, "rice" to true)
        val pantryBefore = pantry.toList()
        val changesBefore = changes.toMap()
        val first = weeklyReview(pantry, changes)
        assertEquals(listOf(egg.copy(have = false), rice.copy(have = true)), first)
        assertEquals(first, weeklyReview(pantry, changes))
        assertEquals(pantryBefore, pantry)
        assertEquals(changesBefore, changes)
    }
}
