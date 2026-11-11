package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IngredientNameMatchTest {
    private val ingredients = listOf(
        Ingredient("egg", "Trứng gà", "khac", 7),
        Ingredient("milk", "Sữa tươi", "khac", 3),
    )

    @Test
    fun matchesTrimmedCaseInsensitiveCatalogName() {
        assertEquals("egg", findIngredientByName(ingredients, "  TRỨNG GÀ ")?.id)
    }

    @Test
    fun unknownNameDoesNotSelectAnUnrelatedIngredient() {
        assertNull(findIngredientByName(ingredients, "Bắp cải"))
    }

    @Test
    fun blankNameDoesNotMatchCatalog() {
        assertNull(findIngredientByName(ingredients, "   "))
    }

    @Test
    fun exactMatchWinsOverLongerCatalogName() {
        val meat = listOf(
            Ingredient("pork", "Thịt heo", "thit", 3),
            Ingredient("ground-pork", "Thịt heo xay", "thit", 2),
        )
        assertEquals("ground-pork", findIngredientByName(meat, "Thịt heo xay")?.id)
    }

    @Test
    fun ambiguousPartialNameDoesNotPickTheFirstCatalogItem() {
        val meat = listOf(
            Ingredient("pork", "Thịt heo", "thit", 3),
            Ingredient("ground-pork", "Thịt heo xay", "thit", 2),
        )
        assertNull(findIngredientByName(meat, "Thịt"))
    }

    @Test
    fun addIngredientPersistsSelectedEstimateAndDoesNotToggleAnExistingItem() {
        val initial = PantryGridState(today = kotlinx.datetime.LocalDate(2026, 11, 6), ingredients = ingredients)
        val added = reduce(initial, PantryGridAction.AddIngredient("egg", 7))
        assertEquals(true, added.state.pantry.getValue("egg").have)
        assertEquals(
            listOf(
                PantryGridEffect.SaveHave("egg", true, initial.today),
                PantryGridEffect.SaveShelfDays("egg", 7),
            ),
            added.effects,
        )

        val unchanged = reduce(added.state, PantryGridAction.AddIngredient("egg", 3))
        assertEquals(added.state, unchanged.state)
        assertEquals(emptyList(), unchanged.effects)
    }

    @Test
    fun addAfterMarkingOldItemOut_usesTodayAsNewPurchaseDate() {
        val old = kotlinx.datetime.LocalDate(2026, 11, 1)
        val initial = PantryGridState(
            today = kotlinx.datetime.LocalDate(2026, 11, 6),
            ingredients = ingredients,
            pantry = mapOf("egg" to PantryItem("egg", true, old)),
        )
        val markedOut = reduce(initial, PantryGridAction.Tap("egg")).state
        val added = reduce(markedOut, PantryGridAction.AddIngredient("egg", 3))

        assertEquals(PantryItem("egg", true, initial.today), added.state.pantry["egg"])
        assertEquals(emptyMap(), added.state.sessionBase)
        assertEquals(
            listOf(
                PantryGridEffect.SaveHave("egg", true, initial.today),
                PantryGridEffect.SaveShelfDays("egg", 3),
            ),
            added.effects,
        )
    }

    @Test
    fun addUsingCatalogEstimate_preservesDefaultWithoutCreatingUserOverride() {
        val ingredient = Ingredient("egg", "Trứng gà", "khac", defaultShelfDays = 21)
        val initial = PantryGridState(today = kotlinx.datetime.LocalDate(2026, 11, 6), ingredients = listOf(ingredient))

        val added = reduce(initial, PantryGridAction.AddIngredient("egg", null))

        assertEquals(PantryItem("egg", true, initial.today), added.state.pantry["egg"])
        assertEquals(null, added.state.ingredients.single().userShelfDays)
        assertEquals(listOf(PantryGridEffect.SaveHave("egg", true, initial.today)), added.effects)
        assertEquals(21, cellsOf(added.state).single().daysLeft)
    }

    @Test
    fun addUsingCatalogEstimate_preservesExistingUserOverride() {
        val ingredient = Ingredient("egg", "Trứng gà", "khac", defaultShelfDays = 21, userShelfDays = 4)
        val initial = PantryGridState(today = kotlinx.datetime.LocalDate(2026, 11, 6), ingredients = listOf(ingredient))

        val added = reduce(initial, PantryGridAction.AddIngredient("egg", null))

        assertEquals(4, added.state.ingredients.single().userShelfDays)
        assertEquals(listOf(PantryGridEffect.SaveHave("egg", true, initial.today)), added.effects)
        assertEquals(4, cellsOf(added.state).single().daysLeft)
    }

    @Test
    fun addIngredient_rejectsInvalidCustomEstimateWithoutEffects() {
        val initial = PantryGridState(today = kotlinx.datetime.LocalDate(2026, 11, 6), ingredients = ingredients)

        val result = reduce(initial, PantryGridAction.AddIngredient("egg", 0))

        assertEquals(initial, result.state)
        assertEquals(emptyList(), result.effects)
    }

    @Test
    fun addIngredient_rejectsUnknownCatalogIdWithoutEffects() {
        val initial = PantryGridState(today = kotlinx.datetime.LocalDate(2026, 11, 6), ingredients = ingredients)

        val result = reduce(initial, PantryGridAction.AddIngredient("unknown", null))

        assertEquals(initial, result.state)
        assertEquals(emptyList(), result.effects)
    }
}
