package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PantrySetQuantityTest {
    private val today = LocalDate(2026, 11, 20)
    private val id = "ca-chua"
    private val ingredients = listOf(Ingredient(id, "Cà chua", "veg", 5))

    private fun stateHave(quantities: Map<String, Int> = emptyMap()) = PantryGridState(
        today = today,
        ingredients = ingredients,
        pantry = mapOf(id to PantryItem(id, true, today)),
        quantities = quantities,
    )

    private fun stateNotHave() = PantryGridState(today = today, ingredients = ingredients)

    @Test
    fun validQuantityWhenHaveUpdatesAndSaves() {
        val update = reduce(stateHave(), PantryGridAction.SetQuantity(id, 12))
        assertEquals(12, update.state.quantities[id])
        assertEquals(listOf<PantryGridEffect>(PantryGridEffect.SaveQuantity(id, 12)), update.effects)
    }

    @Test
    fun quantityOneRemovesOverrideAndSaves() {
        val update = reduce(stateHave(mapOf(id to 5)), PantryGridAction.SetQuantity(id, 1))
        assertFalse(update.state.quantities.containsKey(id))
        assertEquals(listOf<PantryGridEffect>(PantryGridEffect.SaveQuantity(id, 1)), update.effects)
    }

    @Test
    fun quantityAboveMaxIsClamped() {
        val update = reduce(stateHave(), PantryGridAction.SetQuantity(id, 1500))
        assertEquals(999, update.state.quantities[id])
        assertEquals(
            listOf<PantryGridEffect>(PantryGridEffect.SaveQuantity(id, 999)),
            update.effects,
        )
    }

    @Test
    fun zeroOrNegativeWhenHaveMarksOutAndClearsOverride() {
        for (q in listOf(0, -3)) {
            val update = reduce(stateHave(mapOf(id to 7)), PantryGridAction.SetQuantity(id, q))
            assertEquals(false, update.state.pantry[id]?.have, "q=$q")
            assertFalse(update.state.quantities.containsKey(id), "q=$q")
            assertEquals(
                listOf(
                    PantryGridEffect.SaveHave(id, false, today),
                    PantryGridEffect.SaveQuantity(id, 1),
                ),
                update.effects,
                "q=$q",
            )
        }
    }

    @Test
    fun zeroOrNegativeWhenNotHaveIsNoOp() {
        for (q in listOf(0, -3)) {
            for (state in listOf(stateNotHave(), PantryGridState(today = today, ingredients = ingredients, pantry = mapOf(id to PantryItem(id, false, today))))) {
                val update = reduce(state, PantryGridAction.SetQuantity(id, q))
                assertEquals(state, update.state, "q=$q")
                assertTrue(update.effects.isEmpty(), "q=$q")
            }
        }
    }

    @Test
    fun positiveWhenNotHaveMarksHaveAndSavesBoth() {
        val explicitFalse = PantryGridState(
            today = today,
            ingredients = ingredients,
            pantry = mapOf(id to PantryItem(id, false, today)),
        )
        for (state in listOf(stateNotHave(), explicitFalse)) {
            val update = reduce(state, PantryGridAction.SetQuantity(id, 4))
            assertEquals(true, update.state.pantry[id]?.have)
            assertEquals(today, update.state.pantry[id]?.markedAt)
            assertEquals(4, update.state.quantities[id])
            assertEquals(
                listOf(
                    PantryGridEffect.SaveHave(id, true, today),
                    PantryGridEffect.SaveQuantity(id, 4),
                ),
                update.effects,
            )
        }
    }

    @Test
    fun unknownIngredientIsNoOp() {
        val state = stateHave()
        val update = reduce(state, PantryGridAction.SetQuantity("khong-co", 5))
        assertEquals(state, update.state)
        assertTrue(update.effects.isEmpty())
    }
}
