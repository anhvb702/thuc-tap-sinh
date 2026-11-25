package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.logic.MysteryUnlockStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CookbookRewardFabLogicTest {
    @Test
    fun taglineEncouragesHomeCooking() {
        assertEquals("Chăm nấu tại nhà, mở thêm món mới", REWARD_BANNER_TAGLINE)
    }

    @Test
    fun fabHiddenWhenStatusNullOrAllUnlockedAndShownOtherwise() {
        assertFalse(shouldShowRewardFab(null))
        assertFalse(shouldShowRewardFab(status(cooksIntoCurrentCycle = 0, availableSpins = 0, allUnlocked = true)))
        assertTrue(shouldShowRewardFab(status(cooksIntoCurrentCycle = 0, availableSpins = 0, allUnlocked = false)))
        assertTrue(shouldShowRewardFab(status(cooksIntoCurrentCycle = 2, availableSpins = 0, allUnlocked = false)))
        assertTrue(shouldShowRewardFab(status(cooksIntoCurrentCycle = 0, availableSpins = 1, allUnlocked = false)))
    }

    @Test
    fun readyRedDotAndWheelSpinActivateAtExactOneSpinAndStopWhenAllUnlocked() {
        val cases = listOf(
            null to false,
            status(cooksIntoCurrentCycle = 4, availableSpins = 0) to false,
            status(cooksIntoCurrentCycle = 0, availableSpins = 1) to true,
            status(cooksIntoCurrentCycle = 2, availableSpins = 2) to true,
            status(cooksIntoCurrentCycle = 0, availableSpins = 1, allUnlocked = true) to false,
        )
        cases.forEach { (item, expected) ->
            assertEquals(expected, isRewardFabReady(item), "ready for $item")
            assertEquals(expected, shouldShowRewardRedDot(item), "redDot for $item")
            assertEquals(expected, shouldSpinRewardWheel(item), "spinWheel for $item")
        }
    }

    @Test
    fun progressLabelShowsCycleFractionOrFullFiveWhenSpinReady() {
        assertEquals("0/5", rewardFabProgressLabel(status(cooksIntoCurrentCycle = 0, availableSpins = 0)))
        assertEquals("2/5", rewardFabProgressLabel(status(cooksIntoCurrentCycle = 2, availableSpins = 0)))
        assertEquals("4/5", rewardFabProgressLabel(status(cooksIntoCurrentCycle = 4, availableSpins = 0)))
        assertEquals("5/5", rewardFabProgressLabel(status(cooksIntoCurrentCycle = 0, availableSpins = 1)))
        assertEquals("5/5", rewardFabProgressLabel(status(cooksIntoCurrentCycle = 2, availableSpins = 1)))
        assertEquals("0/0", rewardFabProgressLabel(status(cooksIntoCurrentCycle = 2, cooksPerUnlock = 0, availableSpins = 0)))
    }

    @Test
    fun progressFractionNormalizesBoundsAndReturnsFullWhenSpinReady() {
        assertEquals(0f, rewardFabProgressFraction(status(cooksIntoCurrentCycle = 0, availableSpins = 0)), 0.0001f)
        assertEquals(0.4f, rewardFabProgressFraction(status(cooksIntoCurrentCycle = 2, availableSpins = 0)), 0.0001f)
        assertEquals(1f, rewardFabProgressFraction(status(cooksIntoCurrentCycle = 0, availableSpins = 1)), 0.0001f)
        assertEquals(1f, rewardFabProgressFraction(status(cooksIntoCurrentCycle = 7, availableSpins = 0)), 0.0001f)
        assertEquals(0f, rewardFabProgressFraction(status(cooksIntoCurrentCycle = -1, availableSpins = 0)), 0.0001f)
        assertEquals(0f, rewardFabProgressFraction(status(cooksIntoCurrentCycle = 2, cooksPerUnlock = 0, availableSpins = 0)), 0.0001f)
    }

    @Test
    fun hintMessageAndContentDescriptionExplainProgressWithoutForbiddenWords() {
        val partial = status(cooksIntoCurrentCycle = 2, cooksNeededForNext = 3, availableSpins = 0)
        val ready = status(cooksIntoCurrentCycle = 0, cooksNeededForNext = 0, availableSpins = 1)
        val hint = rewardProgressHintMessage(partial)
        val partialDesc = rewardFabContentDescription(partial)
        val readyDesc = rewardFabContentDescription(ready)

        assertTrue("2/5" in hint)
        assertTrue("Thêm 3 bữa nữa" in hint)
        assertTrue(REWARD_BANNER_TAGLINE in hint)
        assertTrue("2/5" in partialDesc)
        assertTrue("1 lượt quay" in readyDesc)
        val allText = listOf(hint, partialDesc, readyDesc, REWARD_BANNER_TAGLINE).joinToString(" ").lowercase()
        listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn").forEach { forbidden -> // lint-copy: rule
            assertFalse(forbidden in allText, "Found forbidden phrase: $forbidden")
        }
    }

    private fun status(
        cooksIntoCurrentCycle: Int,
        availableSpins: Int,
        cooksPerUnlock: Int = 5,
        cooksNeededForNext: Int = (cooksPerUnlock - cooksIntoCurrentCycle).coerceAtLeast(0),
        allUnlocked: Boolean = false,
    ) = MysteryUnlockStatus(
        totalCooks = cooksIntoCurrentCycle,
        cooksPerUnlock = cooksPerUnlock,
        cooksIntoCurrentCycle = cooksIntoCurrentCycle,
        cooksNeededForNext = cooksNeededForNext,
        unlockedMysteryCount = if (allUnlocked) 6 else 0,
        remainingLockedCount = if (allUnlocked) 0 else 6,
        availableSpins = availableSpins,
        allUnlocked = allUnlocked,
    )
}
