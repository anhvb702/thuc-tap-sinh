package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class DirectCookLogicTest {
    private val today = LocalDate(2026, 11, 20)
    private val ingredients = listOf(
        Ingredient("egg", "Trứng gà", "dam", 7),
        Ingredient("tomato", "Cà chua", "rau", 4),
    )

    private fun stateWith(vararg ids: String) = PantryGridState(
        today = today,
        ingredients = ingredients,
        pantry = ids.associateWith { PantryItem(it, true, today) },
    )

    private val recipe = Recipe(
        id = "egg-tomato",
        title = "Trứng sốt cà chua",
        steps = listOf("Chiên trứng", "Sốt cà chua"),
        required = listOf(Slot(listOf("egg")), Slot(listOf("tomato"))),
        minutes = 15,
    )

    @Test
    fun buildDirectCookPlan_returnsConsumedIdsAndSummaryMessage() {
        val plan = buildDirectCookPlan(recipe, stateWith("egg", "tomato"), callbackAvailable = true)
        assertNotNull(plan)
        assertEquals(listOf("egg", "tomato"), plan.ingredientIds)
        assertEquals(cookResultMessage(listOf("Trứng gà", "Cà chua")), plan.resultMessage)
    }

    @Test
    fun buildDirectCookPlan_returnsNullWhenCallbackUnavailable() {
        val plan = buildDirectCookPlan(recipe, stateWith("egg"), callbackAvailable = false)
        assertNull(plan)
    }

    @Test
    fun buildDirectCookPlan_emptyPantryStillProducesValidPlanForCookDone() {
        val plan = buildDirectCookPlan(recipe, stateWith(), callbackAvailable = true)
        assertNotNull(plan)
        assertEquals(emptyList(), plan.ingredientIds)
        assertEquals(cookResultMessage(emptyList()), plan.resultMessage)
    }

    @Test
    fun deduplicateCookIngredientIds_preservesOrderAndRemovesDuplicates() {
        assertEquals(
            listOf("egg", "tomato"),
            deduplicateCookIngredientIds(listOf("egg", "tomato", "egg", "tomato")),
        )
        assertEquals(emptyList(), deduplicateCookIngredientIds(emptyList()))
    }
}
