package com.loitu.yourrefri.feature.home.ui

import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class HomeGachaReelTest {
    private fun recipe(id: String) = Recipe(id, "Món $id", listOf("Nấu"), listOf(Slot(listOf(id))))

    @Test
    fun gachaReel_winnerIsAtReturnedIndex() {
        val candidates = listOf("a", "b", "c").map(::recipe)
        val winner = recipe("w")
        val (reel, index) = gachaReel(candidates, winner)
        assertSame(winner, reel[index])
    }

    @Test
    fun gachaReel_hasDecoyItemsOnBothSidesOfWinner() {
        val candidates = listOf("a", "b", "c", "d").map(::recipe)
        val (reel, index) = gachaReel(candidates, recipe("w"))
        // Bên trái: 3 vòng; bên phải: ít nhất 3 món đệm, nên dải không bị trống khi dừng.
        assertTrue(index >= candidates.size * 3)
        assertTrue(reel.size - 1 - index >= 3)
        assertEquals(candidates.size * 3 + 1 + 3, reel.size)
    }
}
