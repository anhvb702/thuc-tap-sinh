package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpiringListTest {
    private val today = LocalDate(2026, 11, 5)
    private fun ingredient(id: String, shelf: Int = 2, override: Int? = null) =
        Ingredient(id, "Tên $id", "khac", shelf, override)
    private fun item(id: String, have: Boolean = true, date: LocalDate = today) =
        PantryItem(id, have, date)

    @Test
    fun emptyInputs_areEmpty() {
        assertEquals(emptyList(), expiringList(emptyList(), listOf(ingredient("egg")), today))
        assertEquals(emptyList(), expiringList(listOf(item("egg")), emptyList(), today))
    }

    @Test
    fun onlyHaveTrue_isIncluded() {
        val ingredients = listOf(ingredient("rice"), ingredient("egg"))
        assertEquals(
            listOf("egg"),
            expiringList(listOf(item("rice", false), item("egg")), ingredients, today)
                .map { it.item.ingredientId },
        )
    }

    @Test
    fun unknownIds_areSkipped() {
        assertEquals(
            listOf("egg"),
            expiringList(listOf(item("unknown"), item("egg")), listOf(ingredient("egg")), today)
                .map { it.item.ingredientId },
        )
    }

    @Test
    fun cutoff_includesExpiredTodayAndTwoDays_excludesThree() {
        val ingredients = listOf(ingredient("old", 1), ingredient("today", 1), ingredient("two", 2), ingredient("three", 3))
        val pantry = listOf(item("three"), item("two"), item("today", date = LocalDate(2026, 11, 4)), item("old", date = LocalDate(2026, 11, 1)))
        val result = expiringList(pantry, ingredients, today)
        assertEquals(listOf("old", "today", "two"), result.map { it.item.ingredientId })
        assertEquals(listOf(-3, 0, 2), result.map { it.daysLeft })
        assertEquals(listOf(Urgency.EXPIRED, Urgency.SOON, Urgency.SOON), result.map { it.urgency })
    }

    @Test
    fun order_isDaysAscendingThenId() {
        val ingredients = listOf("two", "z", "a", "old", "zero").map { ingredient(it, 1) }
        val pantry = listOf(
            item("two", date = LocalDate(2026, 11, 6)),
            item("z", date = LocalDate(2026, 11, 3)),
            item("a", date = LocalDate(2026, 11, 3)),
            item("old", date = LocalDate(2026, 11, 1)),
            item("zero", date = LocalDate(2026, 11, 4)),
        )
        assertEquals(listOf("old", "a", "z", "zero", "two"), expiringList(pantry, ingredients, today).map { it.item.ingredientId })
        assertEquals(expiringList(pantry, ingredients, today), expiringList(pantry.reversed(), ingredients.reversed(), today))
    }

    @Test
    fun overrideIncludingZero_controlsDateAndResultFields() {
        val zero = ingredient("zero", 30, 0)
        val one = ingredient("one", 30, 1)
        val zeroItem = item("zero")
        val oneItem = item("one", date = LocalDate(2026, 11, 3))
        assertEquals(
            listOf(ExpiringItem(oneItem, one, -1, Urgency.EXPIRED), ExpiringItem(zeroItem, zero, 0, Urgency.SOON)),
            expiringList(listOf(zeroItem, oneItem), listOf(zero, one), today),
        )
    }

    @Test
    fun todayAcrossMonthAndYear_isUsedWithoutMutatingInputs() {
        val pantry = mutableListOf(item("egg", date = LocalDate(2026, 12, 31)))
        val ingredients = mutableListOf(ingredient("egg", 2))
        val pantryBefore = pantry.toList()
        val ingredientsBefore = ingredients.toList()
        assertEquals(listOf(1), expiringList(pantry, ingredients, LocalDate(2027, 1, 1)).map { it.daysLeft })
        assertEquals(listOf(-1), expiringList(pantry, ingredients, LocalDate(2027, 1, 3)).map { it.daysLeft })
        assertEquals(pantryBefore, pantry)
        assertEquals(ingredientsBefore, ingredients)
    }
}
