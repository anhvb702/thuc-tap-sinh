package com.loitu.yourrefri.ui.components

import com.loitu.yourrefri.feature.recipe.ui.fireworkParticleAlpha
import com.loitu.yourrefri.feature.recipe.ui.fireworkParticleOffset
import java.io.File
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CircleBarAndFireworksTest {
    @Test
    fun notchCentersEachSlotAndClampsInvalidIndices() {
        listOf(0.125f, 0.375f, 0.625f, 0.875f).forEachIndexed { tab, expected ->
            assertEquals(expected, circleBarNotchCenterFraction(tab, 4))
        }
        assertEquals(0.125f, circleBarNotchCenterFraction(Int.MIN_VALUE, 4))
        assertEquals(0.875f, circleBarNotchCenterFraction(Int.MAX_VALUE, 4))
        assertEquals(0.5f, circleBarNotchCenterFraction(3, 1))
        assertEquals(0.5f, circleBarNotchCenterFraction(3, 0))
        assertEquals(0.5f, circleBarNotchCenterFraction(3, -1))
    }

    @Test
    fun homeDoorEasesFromClosedToOpenAndStaysWithinTravel() {
        assertEquals(0f, homeDoorOpenAmount(-1f))
        assertEquals(0f, homeDoorOpenAmount(0f))
        val firstQuarter = homeDoorOpenAmount(0.25f)
        val midpoint = homeDoorOpenAmount(0.5f)
        val lastQuarter = homeDoorOpenAmount(0.75f)
        assertTrue(firstQuarter > 0f && firstQuarter < 0.85f / 4f)
        assertEquals(0.425f, midpoint, 0.0001f)
        assertTrue(lastQuarter > 0.85f * 0.75f && lastQuarter < 0.85f)
        assertEquals(0.85f, homeDoorOpenAmount(1f), 0.0001f)
        assertEquals(0.85f, homeDoorOpenAmount(2f), 0.0001f)
    }

    @Test
    fun freezerAndFridgeEachOpenAndCloseInTheirOwnHalf() {
        val halfPeak = 0.7071068f
        val frames = listOf(
            -0.1f to (0f to 0f),
            0f to (0f to 0f),
            0.125f to (halfPeak to 0f),
            0.25f to (1f to 0f),
            0.375f to (halfPeak to 0f),
            0.5f to (0f to 0f),
            0.625f to (0f to halfPeak),
            0.75f to (0f to 1f),
            0.875f to (0f to halfPeak),
            1f to (0f to 0f),
            1.1f to (0f to 0f),
        )
        for ((progress, expected) in frames) {
            val (freezer, fridge) = fridgeCompartmentOpenAmounts(progress)
            assertEquals(expected.first, freezer, 0.0001f, "Freezer at $progress")
            assertEquals(expected.second, fridge, 0.0001f, "Fridge at $progress")
        }
    }

    @Test
    fun potWobblesInBothDirectionsWithDiminishingAmplitude() {
        assertEquals(0f, potWobbleDegrees(0f), 0.0001f)
        assertEquals(7f, potWobbleDegrees(0.125f), 0.0001f)
        assertEquals(-5f, potWobbleDegrees(0.375f), 0.0001f)
        assertEquals(3f, potWobbleDegrees(0.625f), 0.0001f)
        assertEquals(-1f, potWobbleDegrees(0.875f), 0.0001f)
        assertEquals(0f, potWobbleDegrees(1f), 0.0001f)
        assertEquals(0f, potWobbleDegrees(-1f), 0.0001f)
        assertEquals(0f, potWobbleDegrees(2f), 0.0001f)
    }

    @Test
    fun steamFadesInThenOut() {
        assertEquals(0f, potSteamAlpha(0f), 0.0001f)
        assertEquals(0.7071068f, potSteamAlpha(0.25f), 0.0001f)
        assertEquals(1f, potSteamAlpha(0.5f), 0.0001f)
        assertEquals(0.7071068f, potSteamAlpha(0.75f), 0.0001f)
        assertEquals(0f, potSteamAlpha(1f), 0.0001f)
        assertEquals(0f, potSteamAlpha(-1f), 0.0001f)
        assertEquals(0f, potSteamAlpha(2f), 0.0001f)
    }

    @Test
    fun gearRotatesThroughHalfATurnWithClampedProgress() {
        assertEquals(0f, gearRotationDegrees(-1f))
        assertEquals(0f, gearRotationDegrees(0f))
        assertEquals(45f, gearRotationDegrees(0.25f))
        assertEquals(90f, gearRotationDegrees(0.5f))
        assertEquals(180f, gearRotationDegrees(1f))
        assertEquals(180f, gearRotationDegrees(2f))
    }

    @Test
    fun fireworkOffsetUsesDirectionSpeedAndQuadraticGravity() {
        assertEquals(0f to 0f, fireworkParticleOffset(0f, 100f, -1f))
        assertEquals(0f to 0f, fireworkParticleOffset(0f, 100f, 0f))
        assertEquals(50f to 32.5f, fireworkParticleOffset(0f, 100f, 0.5f))
        assertEquals(100f to 130f, fireworkParticleOffset(0f, 100f, 1f))
        assertEquals(100f to 130f, fireworkParticleOffset(0f, 100f, 2f))

        val (dx, dy) = fireworkParticleOffset((-PI / 2).toFloat(), 100f, 0.25f, gravity = 80f)
        assertEquals(0f, dx, 0.0001f)
        assertEquals(-22.5f, dy, 0.0001f)
    }

    @Test
    fun fireworkAlphaFadesLinearlyAndClampsAtBothEnds() {
        assertEquals(1f, fireworkParticleAlpha(-1f))
        assertEquals(1f, fireworkParticleAlpha(0f))
        assertEquals(0.75f, fireworkParticleAlpha(0.25f))
        assertEquals(0.25f, fireworkParticleAlpha(0.75f))
        assertEquals(0f, fireworkParticleAlpha(1f))
        assertEquals(0f, fireworkParticleAlpha(2f))
    }

    @Test
    fun mainScaffoldUsesCircleTabBar() {
        val source = uiSource("MainScaffold.kt")
        assertTrue(Regex("""\bCircleTabBar\s*\(\s*currentTab\b""").containsMatchIn(source))
        assertFalse(Regex("""\bFloatingTabBar\b""").containsMatchIn(source))
    }

    @Test
    fun circleBarUsesThemeColorsWithoutHexLiterals() {
        val source = uiSource("components/CircleBarNavigation.kt")
        val hexColor = Regex("""(?i)(?:\b0x|#)[a-f0-9_]{3,}(?![a-f0-9_])""")
        assertTrue(source.contains("AppTheme.colors"))
        assertFalse(hexColor.containsMatchIn(source))
    }

    private fun uiSource(file: String): String {
        val path = "src/main/kotlin/com/loitu/yourrefri/ui/$file"
        return listOf(File(path), File("composeApp", path)).first { it.isFile }.readText()
    }
}
