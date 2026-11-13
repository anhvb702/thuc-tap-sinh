package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CustomFoodTest {
    private val today = LocalDate(2026, 11, 12)

    @Test
    fun customId_roundTripsTrimmedName() {
        assertEquals("custom:rau cai", customFoodId("  rau\t  cai  "))
        assertEquals("rau cai", customFoodName("custom:rau cai"))
        assertEquals("custom:" + "a".repeat(40), customFoodId("a".repeat(40)))
        assertNull(customFoodName("rau cai"))
        assertNull(customFoodName("custom: rau  cai "))
    }

    @Test
    fun customId_rejectsBlankCommaAndTooLong() {
        assertNull(customFoodId(" \t "))
        assertNull(customFoodId("ca, rot"))
        assertNull(customFoodId("a".repeat(41)))
        assertNull(customFoodName("custom:"))
        assertNull(customFoodName("custom:a,b"))
    }

    @Test
    fun customIngredients_comeOnlyFromPrefixedPantryRows() {
        val pantry = listOf(
            PantryItem("custom:rau cai", true, today),
            PantryItem("custom:ca rot", false, today),
            PantryItem("custom:catalog", true, today),
            PantryItem("other", true, today),
            PantryItem("custom:", true, today),
        )
        assertEquals(
            listOf(Ingredient("custom:rau cai", "rau cai", "tu-them", 3), Ingredient("custom:ca rot", "ca rot", "tu-them", 3)),
            customIngredientsOf(pantry, setOf("custom:catalog")),
        )
    }

    @Test
    fun addCustomIngredient_createsCatalogEntryAndSavesHave() {
        val state = PantryGridState(today)
        val result = reduce(state, PantryGridAction.AddIngredient("custom:rau cai", null))
        assertEquals(listOf(Ingredient("custom:rau cai", "rau cai", "tu-them", 3)), result.state.ingredients)
        assertEquals(PantryItem("custom:rau cai", true, today), result.state.pantry["custom:rau cai"])
        assertEquals(listOf(PantryGridEffect.SaveHave("custom:rau cai", true, today)), result.effects)
        assertEquals(PantryGridUpdate(state), reduce(state, PantryGridAction.AddIngredient("custom:", null)))
        assertEquals(PantryGridUpdate(state), reduce(state, PantryGridAction.AddIngredient("custom:rau", 6)))
    }

    @Test
    fun addCustomIngredient_twiceIsNoOp() {
        val first = reduce(PantryGridState(today), PantryGridAction.AddIngredient("custom:rau", null))
        assertEquals(PantryGridUpdate(first.state), reduce(first.state, PantryGridAction.AddIngredient("custom:rau", null)))
    }

    @Test
    fun unprefixedUnknownId_stillIgnored() {
        val state = PantryGridState(today)
        assertEquals(PantryGridUpdate(state), reduce(state, PantryGridAction.AddIngredient("unknown", null)))
    }

    @Test
    fun customIngredient_usesReferenceShelfDays() {
        val result = reduce(PantryGridState(today), PantryGridAction.AddIngredient("custom:rau", null))
        assertEquals(3, cellsOf(result.state).single().daysLeft)
        val override = reduce(PantryGridState(today), PantryGridAction.AddIngredient("custom:rau", 5))
        assertEquals(5, cellsOf(override.state).single().daysLeft)
        assertEquals(listOf(PantryGridEffect.SaveHave("custom:rau", true, today), PantryGridEffect.SaveShelfDays("custom:rau", 5)), override.effects)
    }
}
