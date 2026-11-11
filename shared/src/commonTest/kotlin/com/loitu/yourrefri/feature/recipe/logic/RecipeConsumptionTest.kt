package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlin.test.Test
import kotlin.test.assertEquals

class RecipeConsumptionTest {
    private fun recipe(required: List<Slot>) = Recipe(
        id = "dish",
        title = "Món nhà",
        steps = listOf("Nấu"),
        required = required,
    )

    @Test
    fun consumptionUsesOneAvailableChoicePerRequiredSlot() {
        val dish = recipe(listOf(Slot(listOf("pork", "chicken")), Slot(listOf("rice"))))
        assertEquals(listOf("chicken", "rice"), ingredientIdsToConsume(dish, setOf("chicken", "rice")))
    }

    @Test
    fun consumptionSkipsUnavailableSlotsAndDuplicateIds() {
        val dish = recipe(listOf(Slot(listOf("missing")), Slot(listOf("rice")), Slot(listOf("rice", "egg"))))
        assertEquals(listOf("rice"), ingredientIdsToConsume(dish, setOf("rice", "egg")))
    }

    @Test
    fun consumptionUsesPreferredAvailableAlternative() {
        val dish = recipe(listOf(Slot(listOf("pork", "chicken"))))
        assertEquals(
            listOf("chicken"),
            ingredientIdsToConsume(dish, setOf("pork", "chicken"), setOf("chicken")),
        )
    }
}
