package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RandomWheelTest {
    private fun recipe(id: String) = Recipe(
        id = id,
        title = id,
        steps = listOf("nấu"),
        required = listOf(Slot(listOf("trứng"))),
    )

    private fun assertHasTail(pool: List<Recipe>, winner: Recipe) {
        val (items, winnerIndex) = wheelReel(pool, winner)
        assertEquals(winner.id, items[winnerIndex].id)
        assertTrue(items.size - 1 - winnerIndex >= 3)
    }

    @Test
    fun oneRecipeWrapsTail() {
        val pool = listOf(recipe("one"))
        assertHasTail(pool, pool.single())
    }

    @Test
    fun twoRecipesWrapAround() {
        val pool = listOf(recipe("one"), recipe("two"))
        assertHasTail(pool, pool.last())
    }

    @Test
    fun fifteenRecipesKeepWinnerAndTail() {
        val pool = (1..15).map { recipe("recipe-$it") }
        assertHasTail(pool, pool[7])
    }

    @Test
    fun lastRecipeNeverLeavesEmptyRightSide() {
        val pool = (1..15).map { recipe("recipe-$it") }
        val (items, winnerIndex) = wheelReel(pool, pool.last())
        assertTrue(items.size - 1 - winnerIndex >= 3)
        assertEquals(pool.last().id, items[winnerIndex].id)
    }
}
