package com.loitu.yourrefri.feature.pantry.ui

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

internal data class RadialOffset(val x: Float, val y: Float)

internal enum class RadialTapOutcome { PREVIEW, TRIGGER }

internal data class RadialTapDecision(val focusedIndex: Int, val outcome: RadialTapOutcome)

internal fun radialFanAnglesDegrees(count: Int, startDeg: Float = 180f, endDeg: Float = 90f): List<Float> =
    when {
        count <= 0 -> emptyList()
        count == 1 -> listOf((startDeg + endDeg) / 2f)
        else -> List(count) { i -> startDeg + (endDeg - startDeg) * i / (count - 1) }
    }

internal fun radialFanOffset(angleDeg: Float, radiusPx: Float): RadialOffset {
    val rad = angleDeg * PI / 180.0
    return RadialOffset((cos(rad) * radiusPx).toFloat(), (-sin(rad) * radiusPx).toFloat())
}

internal fun resolveRadialHitIndex(
    dragX: Float,
    dragY: Float,
    count: Int,
    radiusPx: Float,
    minActivationPx: Float = radiusPx * 0.35f,
    maxSnapDistancePx: Float = radiusPx * 0.6f,
): Int? {
    if (count <= 0 || radiusPx <= 0f) return null
    if (hypot(dragX.toDouble(), dragY.toDouble()) < minActivationPx) return null

    var closestIndex: Int? = null
    var closestDistance = Double.POSITIVE_INFINITY
    radialFanAnglesDegrees(count).forEachIndexed { index, angle ->
        val offset = radialFanOffset(angle, radiusPx)
        val distance = hypot(dragX.toDouble() - offset.x, dragY.toDouble() - offset.y)
        if (distance < closestDistance) {
            closestIndex = index
            closestDistance = distance
        }
    }
    return closestIndex.takeIf { closestDistance <= maxSnapDistancePx }
}

internal fun resolveRadialTap(focusedIndex: Int?, tappedIndex: Int): RadialTapDecision =
    RadialTapDecision(
        focusedIndex = tappedIndex,
        outcome = if (focusedIndex == tappedIndex) RadialTapOutcome.TRIGGER else RadialTapOutcome.PREVIEW,
    )
