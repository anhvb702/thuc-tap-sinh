package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WeeklyShopping4BTest {
    @Test
    fun badgeMatchesRecipeAndScheduledDay() {
        val cooked = mapOf(0 to setOf("fish"), 2 to setOf("soup"))
        assertTrue(wasCookedOnDay(cooked, 0, "fish"))
        assertFalse(wasCookedOnDay(cooked, 1, "fish"))
        assertFalse(wasCookedOnDay(cooked, 0, "soup"))
        assertTrue(wasCookedOnDay(cooked, 2, "soup"))
    }

    @Test
    fun checklistGroupsSelectedItemsByCompartmentAndRetainsOrder() {
        val rows = listOf(
            WeeklyChecklistItem("Cá", PantryCompartment.FREEZE, true),
            WeeklyChecklistItem("Rau", PantryCompartment.CHILL, true),
            WeeklyChecklistItem("Trứng", PantryCompartment.CHILL, false),
            WeeklyChecklistItem("Thịt", PantryCompartment.FREEZE, true),
        )
        assertEquals("Ngăn mát\n☐ Rau\n\nNgăn đông\n☐ Cá\n☐ Thịt", weeklyChecklistText(rows))
    }

    @Test
    fun checklistOmitsEmptyGroupsAndAllowsEmptyList() {
        assertEquals("Ngăn đông\n☐ Cá", weeklyChecklistText(listOf(WeeklyChecklistItem("Cá", PantryCompartment.FREEZE, true))))
        assertEquals("", weeklyChecklistText(listOf(WeeklyChecklistItem("Rau", PantryCompartment.CHILL, false))))
    }

    @Test
    fun stockingKeepsQuantitiesAndDefaultsToChill() {
        assertEquals(
            listOf("fish" to PantryCompartment.FREEZE, "fish" to PantryCompartment.FREEZE, "greens" to PantryCompartment.CHILL),
            weeklyStockCompartments(listOf("fish", "fish", "greens"), mapOf("fish" to PantryCompartment.FREEZE)),
        )
        assertTrue(weeklyStockCompartments(emptyList(), emptyMap()).isEmpty())
    }
}
