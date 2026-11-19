package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlinx.datetime.LocalDate
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RandomRecipeTest {
    private val today = LocalDate.parse("2026-11-13")

    private fun recipe(id: String, vararg needs: String) =
        Recipe(id = id, title = id, steps = listOf("nấu"), required = needs.map { Slot(listOf(it)) })

    private fun stateWith(vararg have: String) = PantryGridState(
        today = today,
        pantry = have.associateWith { PantryItem(it, have = true, markedAt = today) },
    )

    @Test
    fun emptyReturnsNull() {
        assertNull(pickRandomRecipe(emptyList(), stateWith(), null, Random(0)))
    }

    @Test
    fun excludesPreviousWhenMultipleRecipesExist() {
        val recipes = listOf(recipe("a", "trung"), recipe("b", "ca"))
        repeat(40) { seed ->
            assertEquals("b", pickRandomRecipe(recipes, stateWith(), "a", Random(seed))?.recipe?.id)
        }
    }

    @Test
    fun repeatsSingleRecipeEvenWhenItIsExcluded() {
        val only = recipe("a", "trung")
        repeat(20) {
            assertEquals(only, pickRandomRecipe(listOf(only), stateWith(), "a", Random(it))?.recipe)
        }
    }

    @Test
    fun marksCanCookNowCorrectly() {
        val cookable = recipe("a", "trung")
        val missing = recipe("b", "thit-bo")
        assertTrue(pickRandomRecipe(listOf(cookable), stateWith("trung"), null, Random(0))!!.canCookNow)
        assertFalse(pickRandomRecipe(listOf(missing), stateWith("trung"), null, Random(0))!!.canCookNow)
    }

    @Test
    fun samplesEveryRecipeWithUniformFrequency() {
        val recipes = listOf(
            recipe("cookable", "trung"),
            recipe("missing-1", "ca"),
            recipe("missing-2", "thit-bo"),
            recipe("missing-3", "dau"),
        )
        val counts = recipes.associate { it.id to 0 }.toMutableMap()
        repeat(15_000) { seed ->
            val id = pickRandomRecipe(recipes, stateWith("trung"), null, Random(seed))!!.recipe.id
            counts[id] = counts.getValue(id) + 1
        }
        val expected = 15_000 / recipes.size
        val margin = expected * 0.15
        counts.values.forEach { count ->
            assertTrue(count in (expected - margin).toInt()..(expected + margin).toInt(), "count=$count")
        }
    }
}
