package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loitu.yourrefri.ui.components.areSystemAnimatorsEnabled
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Hẹn giờ 4 phút cho một bước nấu, theo Figma 08. */
internal const val COOK_TIMER_TOTAL_SEC = 240

@Composable
internal fun CookTimerCard(
    modifier: Modifier = Modifier,
    totalSeconds: Int = 5 * 60,
    stepKey: Int = 0,
    dialSize: Dp = 136.dp,
) {
    val colors = AppTheme.colors
    var timer by remember(stepKey, totalSeconds) {
        mutableStateOf(TimerState(remaining = totalSeconds, running = false))
    }
    val running = timer.running
    val tapHint = cookTimerTapHint(timer, totalSeconds)
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(stepKey, totalSeconds, running) {
        while (timer.running) {
            delay(1000)
            val prevRemaining = timer.remaining
            val next = tickTimer(timer)
            timer = next
            if (prevRemaining > 0 && next.remaining == 0) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(start = 14.dp, top = 8.dp, end = 14.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "HẸN GIỜ BƯỚC NÀY",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.accentText,
            )

            key(stepKey, totalSeconds) {
                CookTimerDial(timer = timer, totalSeconds = totalSeconds, tapHint = tapHint, dialSize = dialSize) {
                    timer = toggleTimer(timer, totalSeconds)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = tapHint,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                    textAlign = TextAlign.Center,
                )
                if (canResetCookTimer(timer, totalSeconds)) {
                    TextButton(
                        onClick = { timer = resetCookTimerToFull(totalSeconds) },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Icon(
                            AppIcons.Reset,
                            contentDescription = null,
                            tint = colors.ctaText,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Đặt lại",
                            modifier = Modifier.padding(start = 4.dp),
                            color = colors.ctaText,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CookTimerDial(
    timer: TimerState,
    totalSeconds: Int,
    tapHint: String,
    dialSize: Dp,
    onTap: () -> Unit,
) {
    val colors = AppTheme.colors
    val phase = cookTimerPhase(timer, totalSeconds)
    val timeLabel = timerLabel(timer.remaining)
    val statusLabel = cookTimerStatusLabel(timer, totalSeconds)
    val lowOrDone = phase == CookTimerPhase.RUNNING_LOW || phase == CookTimerPhase.DONE
    val dialColor = when {
        lowOrDone -> colors.ctaSoft
        phase == CookTimerPhase.RUNNING -> colors.primarySoft
        else -> colors.surfaceSoft
    }
    val statusColor = when {
        lowOrDone -> colors.ctaText
        phase == CookTimerPhase.RUNNING -> colors.accentText
        else -> colors.muted
    }
    val progressColor = if (lowOrDone) colors.cta else colors.primary
    val icon = when (phase) {
        CookTimerPhase.RUNNING, CookTimerPhase.RUNNING_LOW -> AppIcons.Pause
        CookTimerPhase.DONE -> AppIcons.Reset
        CookTimerPhase.READY, CookTimerPhase.PAUSED -> AppIcons.Play
    }

    val animatorsEnabled = areSystemAnimatorsEnabled()
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val targetProgress = timerProgress(timer.remaining, totalSeconds)
    val animatedProgress = if (animatorsEnabled) {
        animateFloatAsState(targetProgress, tween(durationMillis = 350), label = "cookTimerArc").value
    } else {
        targetProgress
    }
    val scale = if (animatorsEnabled) {
        animateFloatAsState(if (pressed) 0.95f else 1f, spring(), label = "cookTimerPress").value
    } else {
        1f
    }

    Box(
        modifier = Modifier
            .size(dialSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Button,
                onClickLabel = tapHint,
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onTap()
            }
            .semantics {
                stateDescription = statusLabel
                contentDescription = "Đồng hồ hẹn giờ $timeLabel"
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bezelRadius = size.minDimension / 2f - 1.dp.toPx()
            val tickRadius = bezelRadius - 3.dp.toPx()
            val arcRadius = bezelRadius - 16.dp.toPx()
            val arcTopLeft = center - Offset(arcRadius, arcRadius)
            val arcSize = Size(2f * arcRadius, 2f * arcRadius)
            val arcStroke = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)

            drawCircle(dialColor, radius = bezelRadius)
            drawCircle(colors.line, radius = bezelRadius, style = Stroke(width = 1.dp.toPx()))
            for (tick in 0 until 60) {
                val major = tick % 5 == 0
                val angle = (tick * 6.0 - 90.0) * PI / 180.0
                val direction = Offset(cos(angle).toFloat(), sin(angle).toFloat())
                val tickLength = if (major) 6.dp else 3.dp
                drawLine(
                    color = if (major) colors.muted.copy(alpha = 0.55f) else colors.line,
                    start = center + direction * (tickRadius - tickLength.toPx()),
                    end = center + direction * tickRadius,
                    strokeWidth = if (major) 1.5.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            drawCircle(colors.line.copy(alpha = 0.45f), radius = arcRadius, style = arcStroke)
            if (animatedProgress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = arcStroke,
                )
                val angle = timerKnobAngleDegrees(animatedProgress) * PI / 180.0
                val knobCenter = center + Offset(cos(angle).toFloat(), sin(angle).toFloat()) * arcRadius
                drawCircle(colors.surface, radius = 5.dp.toPx(), center = knobCenter)
                drawCircle(progressColor, radius = 3.dp.toPx(), center = knobCenter)
            }
        }

        val density = LocalDensity.current
        // Cap only the dial face so large system text cannot overlap the ring.
        CompositionLocalProvider(LocalDensity provides Density(density.density, density.fontScale.coerceAtMost(1.1f))) {
            Column(
                modifier = Modifier.clearAndSetSemantics {},
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = minOf(26f, 132f / timeLabel.length).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontFeatureSettings = "tnum",
                    ),
                    color = colors.text,
                    maxLines = 1,
                )
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    maxLines = 1,
                )
            }
        }
    }
}
