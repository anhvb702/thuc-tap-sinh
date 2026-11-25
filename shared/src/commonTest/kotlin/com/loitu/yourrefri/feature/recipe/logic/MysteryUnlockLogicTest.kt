package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.RecipeBundle
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MysteryUnlockLogicTest {
    private fun recipe(id: String, mystery: Boolean) =
        Recipe(id, "Món $id", listOf("Nấu"), listOf(Slot(listOf("egg"))), isMystery = mystery)

    private val normal = recipe("n1", false)
    private val m1 = recipe("m1", true)
    private val m2 = recipe("m2", true)
    private val m3 = recipe("m3", true)
    private val all = listOf(normal, m1, m2, m3)

    @Test
    fun zeroCooks_noSpin_needsFive() {
        val s = computeMysteryUnlockStatus(0, all, emptySet())
        assertEquals(0, s.availableSpins)
        assertEquals(0, s.cooksIntoCurrentCycle)
        assertEquals(5, s.cooksNeededForNext)
        assertEquals(3, s.remainingLockedCount)
        assertFalse(s.allUnlocked)
    }

    @Test
    fun fourCooks_noSpin_needsOne() {
        val s = computeMysteryUnlockStatus(4, all, emptySet())
        assertEquals(0, s.availableSpins)
        assertEquals(4, s.cooksIntoCurrentCycle)
        assertEquals(1, s.cooksNeededForNext)
    }

    @Test
    fun fiveCooks_oneSpin() {
        val s = computeMysteryUnlockStatus(5, all, emptySet())
        assertEquals(1, s.availableSpins)
        assertEquals(0, s.cooksIntoCurrentCycle)
        assertEquals(5, s.cooksNeededForNext)
    }

    @Test
    fun twelveCooksOneUnlocked_oneSpinLeft() {
        val s = computeMysteryUnlockStatus(12, all, setOf("m1"))
        assertEquals(1, s.availableSpins) // earned 2 - unlocked 1
        assertEquals(2, s.cooksIntoCurrentCycle)
        assertEquals(3, s.cooksNeededForNext)
        assertEquals(1, s.unlockedMysteryCount)
        assertEquals(2, s.remainingLockedCount)
    }

    @Test
    fun spinsCappedByRemainingLocked() {
        val s = computeMysteryUnlockStatus(100, all, setOf("m1"))
        assertEquals(2, s.availableSpins)
    }

    @Test
    fun allUnlocked_noSpinNothingNeeded() {
        val s = computeMysteryUnlockStatus(100, all, setOf("m1", "m2", "m3"))
        assertTrue(s.allUnlocked)
        assertEquals(0, s.availableSpins)
        assertEquals(0, s.cooksNeededForNext)
    }

    @Test
    fun noMysteryRecipes_notAllUnlocked() {
        val s = computeMysteryUnlockStatus(10, listOf(normal), emptySet())
        assertFalse(s.allUnlocked)
        assertEquals(0, s.availableSpins)
    }

    @Test
    fun unlockedIdsOfNonMysteryOrUnknown_areIgnored() {
        val s = computeMysteryUnlockStatus(5, all, setOf("n1", "ghost"))
        assertEquals(0, s.unlockedMysteryCount)
        assertEquals(1, s.availableSpins)
    }

    @Test
    fun negativeCooks_treatedAsZero() {
        val s = computeMysteryUnlockStatus(-3, all, emptySet())
        assertEquals(0, s.totalCooks)
        assertEquals(0, s.availableSpins)
    }

    @Test
    fun invalidCooksPerUnlock_throws() {
        assertFailsWith<IllegalArgumentException> { computeMysteryUnlockStatus(1, all, emptySet(), 0) }
    }

    @Test
    fun availableForPlanning_hidesLockedMystery() {
        assertEquals(listOf(normal, m2), availableRecipesForPlanning(all, setOf("m2")))
        assertEquals(listOf(normal), availableRecipesForPlanning(all, emptySet()))
    }

    @Test
    fun lockedMystery_listsOnlyLocked() {
        assertEquals(listOf(m1, m3), lockedMysteryRecipes(all, setOf("m2")))
    }

    @Test
    fun pick_usesRandomIndexOverLockedOnly() {
        var passedBound = -1
        val picked = pickMysteryRecipeToUnlock(all, setOf("m2")) { n ->
            passedBound = n
            n - 1
        }
        assertEquals(2, passedBound)
        assertEquals(m3, picked)
        assertEquals(m1, pickMysteryRecipeToUnlock(all, setOf("m2")) { 0 })
    }

    @Test
    fun deletedLogsAfterUnlock_clampsSpinsToZeroAndComputesAccurateNeededCooks() {
        // Đã nấu 5 bữa, mở 1 món, sau đó xóa 1 nhật ký còn 4 bữa -> cần tới mốc 10 (thêm 6 bữa).
        val s = computeMysteryUnlockStatus(4, all, setOf("m1"))
        assertEquals(0, s.availableSpins)
        assertEquals(0, s.cooksIntoCurrentCycle)
        assertEquals(6, s.cooksNeededForNext)
    }

    @Test
    fun pick_nullWhenNothingLocked() {
        assertNull(pickMysteryRecipeToUnlock(all, setOf("m1", "m2", "m3")) { 0 })
        assertNull(pickMysteryRecipeToUnlock(listOf(normal), emptySet()) { 0 })
    }

    @Test
    fun pick_outOfRangeIndexIsClamped() {
        assertEquals(m3, pickMysteryRecipeToUnlock(all, emptySet()) { 99 })
        assertEquals(m1, pickMysteryRecipeToUnlock(all, emptySet()) { -5 })
    }

    @Test
    fun bundle_parsesMysteryFlag_defaultFalse() {
        val json = """[
            {"id":"a","title":"A","steps":["x"],"required":[{"anyOf":["egg"]}],"mystery":true},
            {"id":"b","title":"B","steps":["x"],"required":[{"anyOf":["egg"]}]}
        ]"""
        val parsed = RecipeBundle.parse(json)
        assertTrue(parsed[0].isMystery)
        assertFalse(parsed[1].isMystery)
    }
}
