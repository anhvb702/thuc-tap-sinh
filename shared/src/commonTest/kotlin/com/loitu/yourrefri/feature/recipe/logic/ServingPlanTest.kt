package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ServingPlanTest {
    private val today = LocalDate(2026, 11, 19)

    private fun recipe(vararg slots: Slot) = Recipe("dish", "Món nhà", listOf("Nấu"), slots.toList())

    private fun state(have: List<String>, quantities: Map<String, Int> = emptyMap()) = PantryGridState(
        today = today,
        ingredients = listOf("rice", "egg", "salt").map { Ingredient(it, it, "khac", 7) },
        pantry = have.associateWith { PantryItem(it, true, today) },
        quantities = quantities,
    )

    @Test
    fun oneUnitCanServeTwoAndScalesDownFromFour() {
        val plan = planServings(recipe(Slot(listOf("rice"))), state(listOf("rice")), desiredServings = 4)

        assertEquals(2, plan.maxServings)
        assertTrue(plan.isScaledDown)
        assertEquals(2, plan.recommendedServings)
        assertTrue(plan.canCookAtAll)
    }

    @Test
    fun missingRequiredIngredientCannotCook() {
        val plan = planServings(
            recipe(Slot(listOf("rice")), Slot(listOf("egg"))),
            state(listOf("rice")),
        )

        assertFalse(plan.canCookAtAll)
        assertEquals(0, plan.recommendedServings)
    }

    @Test
    fun servingsRoundUpToWholeUnits() {
        assertEquals(1, unitsToConsumeForServings(1))
        assertEquals(1, unitsToConsumeForServings(2))
        assertEquals(2, unitsToConsumeForServings(3))
        assertEquals(2, unitsToConsumeForServings(4))
    }

    @Test
    fun servingNoteHasNoForbiddenCopy() {
        val notes = listOf(
            servingNoteVi(ServingPlan(2, 2, 2, false, true)),
            servingNoteVi(ServingPlan(4, 2, 2, true, true)),
            servingNoteVi(ServingPlan(2, 0, 0, false, false)),
        )
        assertTrue(notes.all { note -> listOf("quên", "lãng phí", "bỏ lỡ", "hết hạn", "streak").none(note::contains) })
    }
}
