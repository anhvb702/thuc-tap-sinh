package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlinx.datetime.LocalDate
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
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
    fun noCookableRecipeReturnsNull() {
        val recipes = listOf(recipe("a", "trung"), recipe("b", "ca"))
        assertNull(pickRandomRecipe(recipes, stateWith("thit-bo"), null, Random(0)))
    }

    @Test
    fun choosesOnlyCookableRecipes() {
        val recipes = listOf(recipe("a", "trung"), recipe("b", "ca"), recipe("c", "dau"))
        repeat(40) { seed ->
            assertEquals("a", pickRandomRecipe(recipes, stateWith("trung"), null, Random(seed))?.recipe?.id)
        }
    }

    @Test
    fun excludesPreviousWhenMultipleCookableRecipesExist() {
        val recipes = listOf(recipe("a", "trung"), recipe("b", "ca"), recipe("missing", "dau"))
        repeat(40) { seed ->
            assertEquals("b", pickRandomRecipe(recipes, stateWith("trung", "ca"), "a", Random(seed))?.recipe?.id)
        }
    }

    @Test
    fun keepsTheOnlyCookableRecipeEvenWhenExcluded() {
        val only = recipe("a", "trung")
        repeat(20) {
            assertEquals(only, pickRandomRecipe(listOf(only, recipe("missing", "ca")), stateWith("trung"), "a", Random(it))?.recipe)
        }
    }

    @Test
    fun everyPickIsMarkedCookable() {
        val pick = pickRandomRecipe(listOf(recipe("a", "trung")), stateWith("trung"), null, Random(0))
        assertTrue(pick!!.canCookNow)
    }

    @Test
    fun cookableRecipesFiltersMissingIngredients() {
        val recipes = listOf(recipe("a", "trung"), recipe("b", "ca"))
        assertEquals(listOf("a"), cookableRecipes(recipes, stateWith("trung")).map { it.id })
    }

    @Test
    fun samplesCookableRecipesWithUniformFrequency() {
        val recipes = listOf(
            recipe("cookable", "trung"),
            recipe("cookable-2", "ca"),
            recipe("cookable-3", "dau"),
            recipe("missing", "thit-bo"),
        )
        val counts = recipes.associate { it.id to 0 }.toMutableMap()
        repeat(15_000) { seed ->
            val id = pickRandomRecipe(recipes, stateWith("trung", "ca", "dau"), null, Random(seed))!!.recipe.id
            counts[id] = counts.getValue(id) + 1
        }
        assertEquals(0, counts.getValue("missing"))
        val expected = 15_000 / 3
        val margin = expected * 0.15
        counts.filterKeys { it != "missing" }.values.forEach { count ->
            assertTrue(count in (expected - margin).toInt()..(expected + margin).toInt(), "count=$count")
        }
    }
}
