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
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
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

    private val cookable1 = recipe("a", "trung")
    private val cookable2 = recipe("b", "trung", "hanh")
    private val missing = recipe("c", "thit-bo")

    @Test
    fun prefersCookableRecipes() {
        val state = stateWith("trung", "hanh")
        repeat(30) { seed ->
            val pick = pickRandomRecipe(listOf(cookable1, missing, cookable2), state, null, Random(seed))
            assertNotNull(pick)
            assertTrue(pick.canCookNow)
            assertNotEquals("c", pick.recipe.id)
        }
    }

    @Test
    fun reachesEveryCookableRecipeAcrossSeeds() {
        val state = stateWith("trung", "hanh")
        val seen = (0 until 60).map { pickRandomRecipe(listOf(cookable1, cookable2), state, null, Random(it))!!.recipe.id }.toSet()
        assertEquals(setOf("a", "b"), seen)
    }

    @Test
    fun fallsBackToAllWhenNoneCookable_marksNotCookable() {
        val state = stateWith("ca")
        val pick = pickRandomRecipe(listOf(cookable1, missing), state, null, Random(1))
        assertNotNull(pick)
        assertFalse(pick.canCookNow)
    }

    @Test
    fun fallbackPrefersRecipesWithFewestMissingRequiredSlots() {
        val oneMissing = recipe("one", "ca", "hanh")
        val twoMissing = recipe("two", "ca", "hanh", "toi")
        val pick = pickRandomRecipe(listOf(twoMissing, oneMissing), stateWith("ca"), null, Random(5))
        assertEquals("one", pick?.recipe?.id)
        assertFalse(pick!!.canCookNow)
    }

    @Test
    fun neverReturnsExcludedId() {
        val state = stateWith("trung", "hanh")
        repeat(40) { seed ->
            val pick = pickRandomRecipe(listOf(cookable1, cookable2), state, "a", Random(seed))
            assertEquals("b", pick?.recipe?.id)
        }
    }

    @Test
    fun excludedCookableFallsThroughToNonCookable() {
        val state = stateWith("trung")
        val pick = pickRandomRecipe(listOf(cookable1, missing), state, "a", Random(3))
        assertEquals("c", pick?.recipe?.id)
        assertFalse(pick!!.canCookNow)
    }

    @Test
    fun returnsNullWhenOnlyExcludedLeft() {
        assertNull(pickRandomRecipe(listOf(cookable1), stateWith("trung"), "a", Random(0)))
    }

    @Test
    fun returnsNullForEmptyList() {
        assertNull(pickRandomRecipe(emptyList(), stateWith("trung"), null, Random(0)))
    }

    /**
     * Căn cứ quyết định Q2 (2026-11-16): Chế độ ăn là bộ lọc cứng ở mọi nơi gợi ý món,
     * thay thế contract A-03 cũ (trước: nấu ngẫu nhiên không lọc). Lọc xong không còn món
     * thì trả về null, tuyệt đối không lách bộ lọc.
     */
    @Test
    fun returnsNullWhenDietFilterLeavesNoRecipes_decisionQ2() {
        val meatRecipe = recipe("c", "thit-bo")
        val vegetarianPool = filterRecipesByDiet(listOf(meatRecipe), DietRestriction.VEGETARIAN)
        val pick = pickRandomRecipe(vegetarianPool, stateWith(), null, Random(0))
        assertNull(pick)
    }

    @Test
    fun picksOnlyFromDietCompliantRecipes_decisionQ2() {
        val vegRecipe = recipe("veg", "trung")
        val meatRecipe = recipe("meat", "thit-bo")
        val filtered = filterRecipesByDiet(listOf(vegRecipe, meatRecipe), DietRestriction.VEGETARIAN)
        val pick = pickRandomRecipe(filtered, stateWith("trung"), null, Random(0))
        assertNotNull(pick)
        assertEquals("veg", pick.recipe.id)
    }
}
