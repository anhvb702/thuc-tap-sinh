package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CanCookTest {
    private fun item(id: String, have: Boolean = true) = PantryItem(id, have, LocalDate(2026, 1, 1))
    private fun recipe(required: List<Slot>, optional: List<Slot> = emptyList()) =
        Recipe("dish", "Món", listOf("Nấu"), required, optional)

    @Test
    fun allRequiredSlotsMatched_returnsTrue() {
        assertTrue(canCook(recipe(listOf(Slot(listOf("egg")), Slot(listOf("rice")))), listOf(item("egg"), item("rice"))))
    }

    @Test
    fun oneRequiredSlotMissing_returnsFalse() {
        assertFalse(canCook(recipe(listOf(Slot(listOf("egg")), Slot(listOf("rice")))), listOf(item("egg"))))
    }

    @Test
    fun alternativeAtEitherPosition_isEnough() {
        val recipe = recipe(listOf(Slot(listOf("pork", "chicken"))))
        assertTrue(canCook(recipe, listOf(item("pork"))))
        assertTrue(canCook(recipe, listOf(item("chicken"))))
    }

    @Test
    fun alternativeNotPresent_returnsFalse() {
        assertFalse(canCook(recipe(listOf(Slot(listOf("pork", "chicken")))), listOf(item("beef"))))
    }

    @Test
    fun haveFalseDoesNotMatch_evenWithOtherItemsPresent() {
        assertFalse(canCook(recipe(listOf(Slot(listOf("egg")))), listOf(item("egg", false), item("rice"))))
    }

    @Test
    fun optionalMissingDoesNotBlock() {
        assertTrue(canCook(recipe(listOf(Slot(listOf("egg"))), listOf(Slot(listOf("milk")))), listOf(item("egg"))))
    }

    @Test
    fun optionalPresentDoesNotReplaceMissingRequired() {
        assertFalse(canCook(recipe(listOf(Slot(listOf("egg"))), listOf(Slot(listOf("milk")))), listOf(item("milk"))))
    }

    @Test
    fun idsAreExactAndCaseSensitive() {
        val recipe = recipe(listOf(Slot(listOf("egg"))))
        assertFalse(canCook(recipe, listOf(item("eggs"))))
        assertFalse(canCook(recipe, listOf(item("EGG"))))
    }

    @Test
    fun emptyPantry_returnsFalse() {
        assertFalse(canCook(recipe(listOf(Slot(listOf("egg")))), emptyList()))
    }

    @Test
    fun orderAndDuplicatePantry_doNotChangeOrMutateInputs() {
        val required = mutableListOf(Slot(listOf("egg", "duck-egg")), Slot(listOf("egg")))
        val pantry = mutableListOf(item("rice"), item("egg"), item("egg"))
        val requiredBefore = required.toList()
        val pantryBefore = pantry.toList()
        val recipe = recipe(required)
        assertTrue(canCook(recipe, pantry))
        assertTrue(canCook(recipe.copy(required = required.reversed()), pantry.reversed()))
        assertEquals(requiredBefore, required)
        assertEquals(pantryBefore, pantry)
    }
}
