package com.loitu.yourrefri.feature.pantry.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PantryRadialFabLogicTest {
    @Test
    fun fanAnglesUseBothEndsAndCenterASingleAction() {
        assertEquals(emptyList(), radialFanAnglesDegrees(0))
        assertEquals(emptyList(), radialFanAnglesDegrees(-1))
        assertEquals(listOf(135f), radialFanAnglesDegrees(1))
        assertEquals(listOf(180f, 150f, 120f, 90f), radialFanAnglesDegrees(4))
        assertEquals(listOf(180f, 157.5f, 135f, 112.5f, 90f), radialFanAnglesDegrees(5))
        assertEquals(listOf(120f, 90f, 60f), radialFanAnglesDegrees(3, 120f, 60f))
    }

    @Test
    fun offsetsUseAndroidScreenCoordinates() {
        val radius = 100f
        val left = radialFanOffset(180f, radius)
        val up = radialFanOffset(90f, radius)
        val diagonal = radialFanOffset(135f, radius)

        assertEquals(-radius, left.x, 0.001f)
        assertEquals(0f, left.y, 0.001f)
        assertEquals(0f, up.x, 0.001f)
        assertEquals(-radius, up.y, 0.001f)
        assertEquals(-70.7107f, diagonal.x, 0.001f)
        assertEquals(-70.7107f, diagonal.y, 0.001f)
    }

    @Test
    fun eachPetalCenterResolvesToItsOwnIndex() {
        val centers = listOf(
            RadialOffset(-100f, 0f),
            RadialOffset(-92.388f, -38.268f),
            RadialOffset(-70.711f, -70.711f),
            RadialOffset(-38.268f, -92.388f),
            RadialOffset(0f, -100f),
        )
        centers.forEachIndexed { index, center ->
            assertEquals(index, resolveRadialHitIndex(center.x, center.y, 5, 100f))
        }
    }

    @Test
    fun hitTestingHonorsDeadZoneAndInclusiveSnapDistance() {
        assertNull(resolveRadialHitIndex(0f, -34f, 5, 100f, maxSnapDistancePx = 100f))
        assertEquals(4, resolveRadialHitIndex(0f, -35f, 5, 100f, maxSnapDistancePx = 100f))
        assertEquals(0, resolveRadialHitIndex(-160f, 0f, 5, 100f))
        assertEquals(0, resolveRadialHitIndex(-160f, 0f, 5, 100f, maxSnapDistancePx = 60f))
        assertNull(resolveRadialHitIndex(-161f, 0f, 5, 100f))
        assertNull(resolveRadialHitIndex(200f, 200f, 5, 100f))
    }

    @Test
    fun hitTestingRejectsMissingActionsAndNonpositiveRadius() {
        assertNull(resolveRadialHitIndex(-100f, 0f, 0, 100f))
        assertNull(resolveRadialHitIndex(-100f, 0f, -1, 100f))
        assertNull(resolveRadialHitIndex(0f, 0f, 5, 0f))
        assertNull(resolveRadialHitIndex(0f, 0f, 5, -100f))
    }

    @Test
    fun tappingPreviewsNewFocusAndTriggersExistingFocus() {
        assertEquals(RadialTapDecision(2, RadialTapOutcome.PREVIEW), resolveRadialTap(null, 2))
        assertEquals(RadialTapDecision(4, RadialTapOutcome.PREVIEW), resolveRadialTap(2, 4))
        assertEquals(RadialTapDecision(2, RadialTapOutcome.TRIGGER), resolveRadialTap(2, 2))
    }
}
