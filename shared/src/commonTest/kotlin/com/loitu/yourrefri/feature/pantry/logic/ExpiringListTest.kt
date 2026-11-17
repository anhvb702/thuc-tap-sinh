package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.IngredientKind
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExpiringListTest {
    private val today = LocalDate(2026, 11, 5)
    private fun ingredient(
        id: String,
        shelf: Int = 2,
        override: Int? = null,
        kind: IngredientKind = IngredientKind.FRESH,
        freezerShelfDays: Int? = null,
    ) = Ingredient(id, "Tên $id", "khac", shelf, override, kind, freezerShelfDays)
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
    fun frozenItems_hiddenUntilOverdue_thenIncludedWithIsExpiredFrozen() {
        val chilled = item("egg")
        val frozenSoon = item("fish", date = LocalDate(2026, 11, 5)).copy(compartment = PantryCompartment.FREEZE)
        val frozenDueToday = item("chicken", date = LocalDate(2026, 11, 3)).copy(compartment = PantryCompartment.FREEZE)
        val frozenOverdue = item("meat", date = LocalDate(2026, 11, 1)).copy(compartment = PantryCompartment.FREEZE)
        val ingredients = listOf(
            ingredient("egg", shelf = 2),
            ingredient("fish", shelf = 2, freezerShelfDays = 2),
            ingredient("chicken", shelf = 2, freezerShelfDays = 2),
            ingredient("meat", shelf = 2, freezerShelfDays = 3),
        )

        val result = expiringList(listOf(frozenSoon, frozenDueToday, frozenOverdue, chilled), ingredients, today)
        assertEquals(listOf("meat", "egg"), result.map { it.item.ingredientId })
        assertTrue(result[0].isExpiredFrozen)
        assertFalse(result[1].isExpiredFrozen)
    }

    @Test
    fun seasoningItems_areExcludedFromExpiringList() {
        val salt = ingredient("muoi", shelf = 0, kind = IngredientKind.SEASONING)
        val sauce = ingredient("nuoc-mam", shelf = 1, kind = IngredientKind.SEASONING)
        val egg = ingredient("egg", shelf = 2)
        val result = expiringList(
            listOf(item("muoi", date = LocalDate(2026, 11, 1)), item("nuoc-mam"), item("egg")),
            listOf(salt, sauce, egg),
            today,
        )
        assertEquals(listOf("egg"), result.map { it.item.ingredientId })
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
