package com.loitu.yourrefri.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loitu.yourrefri.ui.NAV_LABEL_MAX_LINES
import com.loitu.yourrefri.ui.NavTab
import com.loitu.yourrefri.ui.navLabelFontSizeSp
import com.loitu.yourrefri.ui.theme.AppTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal fun circleBarNotchCenterFraction(tabIndex: Int, tabCount: Int): Float =
    if (tabCount <= 0) 0.5f else (tabIndex.coerceIn(0, tabCount - 1) + 0.5f) / tabCount

internal fun homeDoorOpenAmount(progress: Float): Float {
    val p = progress.coerceIn(0f, 1f)
    return 0.85f * p * p * (3f - 2f * p)
}

internal fun fridgeCompartmentOpenAmounts(progress: Float): Pair<Float, Float> {
    val p = progress.coerceIn(0f, 1f)
    return if (p < 0.5f) sin(p * 2 * PI).toFloat() to 0f
    else 0f to sin((p - 0.5f) * 2 * PI).toFloat()
}

internal fun potWobbleDegrees(progress: Float): Float {
    val p = progress.coerceIn(0f, 1f)
    return sin(p * 4 * PI).toFloat() * 8f * (1f - p)
}

internal fun potSteamAlpha(progress: Float): Float {
    val p = progress.coerceIn(0f, 1f)
    return if (p == 1f) 0f else sin(p * PI).toFloat()
}

internal fun gearRotationDegrees(progress: Float): Float = progress.coerceIn(0f, 1f) * 180f

private val BubbleSize = 52.dp
private val BarTop = 28.dp

@Composable
internal fun CircleTabBar(
    currentTab: NavTab,
    onSelectTab: (NavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val animatorsEnabled = areSystemAnimatorsEnabled()
    val animatedIndex by animateFloatAsState(
        currentTab.ordinal.toFloat(),
        if (animatorsEnabled) spring(dampingRatio = 0.72f, stiffness = 420f) else snap(),
        label = "circleBarIndex",
    )
    val progress = remember(currentTab) { Animatable(0f) }
    LaunchedEffect(currentTab, animatorsEnabled) {
        progress.snapTo(0f)
        if (animatorsEnabled) progress.animateTo(1f, tween(550, easing = FastOutSlowInEasing))
    }
    val tabs = NavTab.entries
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val labelSize = navLabelFontSizeSp(LocalDensity.current.fontScale).sp
    BoxWithConstraints(
        modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        val barWidth = maxWidth
        fun centerFraction(): Float {
            val index = if (animatorsEnabled) animatedIndex else currentTab.ordinal.toFloat()
            val first = circleBarNotchCenterFraction(0, tabs.size)
            val last = circleBarNotchCenterFraction(tabs.lastIndex, tabs.size)
            val fraction = (first + index / tabs.size).coerceIn(first, last)
            return if (rtl) 1f - fraction else fraction
        }

        Canvas(Modifier.matchParentSize()) {
            drawNotchedBar(size.width * centerFraction(), colors.navContainer, colors.line)
        }
        Row(Modifier.fillMaxWidth().selectableGroup()) {
            tabs.forEach { tab ->
                val selected = currentTab == tab
                Column(
                    Modifier.weight(1f).heightIn(min = 56.dp)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelectTab(tab) })
                        .padding(top = BarTop + 4.dp, bottom = 8.dp, start = 2.dp, end = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (selected) {
                        Box(Modifier.size(24.dp))
                    } else {
                        TabAnimatedIcon(tab, selected = false, animProgress = { 0f }, tint = colors.navUnselected)
                    }
                    Text(
                        text = tab.label,
                        modifier = Modifier.padding(top = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = labelSize,
                        maxLines = NAV_LABEL_MAX_LINES,
                        textAlign = TextAlign.Center,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = colors.navUnselected,
                    )
                }
            }
        }
        Box(
            Modifier.size(BubbleSize)
                .graphicsLayer {
                    val width = barWidth.toPx()
                    val left = width * centerFraction() - size.width / 2f
                    translationX = if (rtl) left - (width - size.width) else left
                    translationY = (BarTop - BubbleSize / 2).toPx()
                    shape = CircleShape
                    shadowElevation = 6.dp.toPx()
                }
                .background(colors.cta, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            TabAnimatedIcon(
                tab = currentTab,
                selected = true,
                animProgress = { if (animatorsEnabled) progress.value else 0f },
                tint = colors.onCta,
            )
        }
    }
}

private fun DrawScope.drawNotchedBar(center: Float, fill: Color, line: Color) {
    val top = BarTop.toPx()
    val width = size.width
    val bottom = size.height
    val edgeDistance = minOf(center, width - center)
    val cornerSpace = minOf(8.dp.toPx(), edgeDistance / 5f)
    val halfNotch = minOf(39.dp.toPx(), edgeDistance - cornerSpace)
    val depth = 30.dp.toPx()
    val radius = minOf(26.dp.toPx(), (bottom - top) / 2f, width / 2f)
    val leftRadius = minOf(radius, center - halfNotch)
    val rightRadius = minOf(radius, width - center - halfNotch)
    val outline = Path().apply {
        moveTo(0f, top + leftRadius)
        quadraticBezierTo(0f, top, leftRadius, top)
        lineTo(center - halfNotch, top)
        cubicTo(center - halfNotch * 0.6f, top, center - halfNotch * 0.63f, top + depth, center, top + depth)
        cubicTo(center + halfNotch * 0.63f, top + depth, center + halfNotch * 0.6f, top, center + halfNotch, top)
        lineTo(width - rightRadius, top)
        quadraticBezierTo(width, top, width, top + rightRadius)
        lineTo(width, bottom - radius)
        quadraticBezierTo(width, bottom, width - radius, bottom)
        lineTo(radius, bottom)
        quadraticBezierTo(0f, bottom, 0f, bottom - radius)
        close()
    }
    drawPath(outline, fill)
    clipRect(bottom = top + depth + 1.dp.toPx()) {
        drawPath(outline, line.copy(alpha = 0.24f), style = Stroke(1.dp.toPx()))
    }
}

@Composable
private fun TabAnimatedIcon(tab: NavTab, selected: Boolean, animProgress: () -> Float, tint: Color) {
    Canvas(Modifier.size(if (selected) 32.dp else 24.dp)) {
        val progress = if (selected) animProgress() else 0f
        val stroke = Stroke(width = 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        scale(size.width / 32f, size.height / 32f, pivot = Offset.Zero) {
            when (tab) {
                NavTab.HOME -> {
                    val house = Path().apply {
                        moveTo(4f, 14f)
                        lineTo(16f, 4f)
                        lineTo(28f, 14f)
                        moveTo(7f, 12f)
                        lineTo(7f, 27f)
                        lineTo(12f, 27f)
                        lineTo(12f, 17f)
                        lineTo(20f, 17f)
                        lineTo(20f, 27f)
                        lineTo(25f, 27f)
                        lineTo(25f, 12f)
                    }
                    drawPath(house, tint, style = stroke)
                    drawLine(tint, Offset(12f, 27f), Offset(20f, 27f), stroke.width)
                    drawSwingDoor(12f, 17f, 20f, 27f, homeDoorOpenAmount(progress), tint, stroke)
                }
                NavTab.PANTRY -> {
                    val (freezer, fridge) = fridgeCompartmentOpenAmounts(progress)
                    drawRoundRect(tint, Offset(7f, 3f), Size(18f, 26f), CornerRadius(2f), style = stroke)
                    drawLine(tint, Offset(7f, 13f), Offset(25f, 13f), stroke.width)
                    drawSwingDoor(7f, 4f, 25f, 13f, freezer, tint, stroke)
                    drawSwingDoor(7f, 13f, 25f, 28f, fridge, tint, stroke)
                }
                NavTab.RECIPES -> drawPot(progress, tint, stroke)
                NavTab.SETTINGS -> rotate(gearRotationDegrees(progress), pivot = Offset(16f, 16f)) {
                    val gear = Path().apply {
                        for (point in 0 until 32) {
                            val angle = (point - 1.5) * PI / 16
                            val radius = if (point % 4 in 1..2) 12.5f else 9.5f
                            val x = 16f + cos(angle).toFloat() * radius
                            val y = 16f + sin(angle).toFloat() * radius
                            if (point == 0) moveTo(x, y) else lineTo(x, y)
                        }
                        close()
                    }
                    drawPath(gear, tint, style = stroke)
                    drawCircle(tint, 4f, Offset(16f, 16f), style = stroke)
                }
            }
        }
    }
}

private fun DrawScope.drawSwingDoor(
    left: Float, top: Float, right: Float, bottom: Float, amount: Float, tint: Color, stroke: Stroke,
) {
    val edge = right - (right - left) * amount * 0.74f
    val tilt = 2.5f * amount
    if (amount > 0.01f) {
        val leaf = Path().apply {
            moveTo(left, top)
            lineTo(edge, top + tilt)
            lineTo(edge, bottom - tilt)
            lineTo(left, bottom)
        }
        drawPath(leaf, tint, style = stroke)
    }
    drawLine(tint, Offset(edge - 2.5f, top + 3.5f), Offset(edge - 2.5f, top + 5f), stroke.width, StrokeCap.Round)
}

private fun DrawScope.drawPot(progress: Float, tint: Color, stroke: Stroke) {
    rotate(potWobbleDegrees(progress), pivot = Offset(16f, 21f)) {
        drawRoundRect(tint, Offset(7f, 15f), Size(18f, 12f), CornerRadius(3f), style = stroke)
        drawLine(tint, Offset(6f, 15f), Offset(26f, 15f), stroke.width, StrokeCap.Round)
        drawLine(tint, Offset(14f, 12f), Offset(18f, 12f), stroke.width, StrokeCap.Round)
        drawLine(tint, Offset(4f, 19f), Offset(7f, 19f), stroke.width, StrokeCap.Round)
        drawLine(tint, Offset(25f, 19f), Offset(28f, 19f), stroke.width, StrokeCap.Round)
    }
    val alpha = potSteamAlpha(progress)
    if (alpha <= 0f) return
    for (x in listOf(12f, 20f)) {
        val y = 11f - progress * 4f
        val wisp = Path().apply {
            moveTo(x, y)
            cubicTo(x + 3f, y - 2f, x - 3f, y - 4f, x, y - 6f)
        }
        drawPath(wisp, tint, alpha = alpha, style = stroke)
    }
}
