package com.loitu.yourrefri.feature.recipe.ui

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GachaWheelSpinCurveTest {
    @Test
    fun progressStartsAtZeroAndEndsAtOne() {
        assertEquals(0f, gachaSpinProgress(0f), 0f)
        assertEquals(1f, gachaSpinProgress(1f), 0f)
    }

    @Test
    fun progressClampsFractionsOutsideTheSpin() {
        assertEquals(0f, gachaSpinProgress(-0.5f), 0f)
        assertEquals(1f, gachaSpinProgress(1.5f), 0f)
    }

    @Test
    fun spinCoversMoreThanThreeQuartersOfTheReelInTheFirstQuarter() {
        assertTrue(gachaSpinProgress(0.25f) > 0.75f)
    }

    @Test
    fun spinTravelsAtLeastTwentyTimesFartherInAnEarlyIntervalThanALateOne() {
        val earlyDelta = gachaSpinProgress(0.1f) - gachaSpinProgress(0f)
        val lateDelta = gachaSpinProgress(0.9f) - gachaSpinProgress(0.8f)
        assertTrue(earlyDelta >= 20f * lateDelta)
    }

    @Test
    fun progressStrictlyIncreasesAtEachTenthOfTheSpin() {
        for (step in 1..10) {
            val previous = (step - 1) / 10f
            val current = step / 10f
            assertTrue(
                "Expected progress to increase between $previous and $current",
                gachaSpinProgress(current) > gachaSpinProgress(previous),
            )
        }
    }

    @Test
    fun wheelsUseTheSpecifiedSpinDistancesAndDurations() {
        assertEquals(5, RANDOM_WHEEL_GACHA_LOOPS)
        assertEquals(5400, RANDOM_WHEEL_GACHA_DURATION_MS)
        assertEquals(24, MYSTERY_WHEEL_GACHA_LANDING_INDEX)
        assertEquals(4800, MYSTERY_WHEEL_GACHA_DURATION_MS)
    }

    @Test
    fun bothWheelTweensUseTheSharedGachaEasing() {
        assertWheelUsesGachaTween("RandomWheel.kt", "RANDOM_WHEEL_GACHA_DURATION_MS")
        assertWheelUsesGachaTween("MysteryUnlockWheel.kt", "MYSTERY_WHEEL_GACHA_DURATION_MS")
    }

    private fun assertWheelUsesGachaTween(fileName: String, durationConstant: String) {
        val path = "src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/$fileName"
        val source = listOf(File(path), File("composeApp", path)).first { it.isFile }.readText()
        val gachaTween = Regex(
            """tween\(\s*durationMillis\s*=\s*$durationConstant\s*,\s*easing\s*=\s*GachaSpinEasing\s*,?\s*\)""",
        )
        assertTrue("$fileName should use the shared easing in its spin tween", gachaTween.containsMatchIn(source))
    }
}
