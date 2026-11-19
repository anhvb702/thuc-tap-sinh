package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.ui.components.ChunkyButton
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
) {
    val colors = AppTheme.colors
    var timer by remember(totalSeconds) { mutableStateOf(TimerState(remaining = totalSeconds, running = false)) }
    val remaining = timer.remaining
    val running = timer.running
    val targetProgress = timerProgress(remaining, totalSeconds)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 350),
        label = "cookTimerArc",
    )

    LaunchedEffect(running) {
        while (timer.running) {
            delay(1000)
            timer = tickTimer(timer)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "HẸN GIỜ BƯỚC NÀY",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.accentText,
            )

            Box(
                modifier = Modifier.size(164.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokePx = 10.dp.toPx()
                    val inset = strokePx / 2f + 4.dp.toPx()
                    val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                    val topLeft = Offset(inset, inset)
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerR = size.minDimension / 2f - 1.dp.toPx()

                    // 12 vạch chia nhỏ kiểu đồng hồ điện tử quanh vòng tròn.
                    for (i in 0 until 12) {
                        val angle = (i * 30.0 - 90.0) * PI / 180.0
                        val r1 = outerR - 3.dp.toPx()
                        drawLine(
                            color = colors.line,
                            start = Offset(center.x + (r1 * cos(angle)).toFloat(), center.y + (r1 * sin(angle)).toFloat()),
                            end = Offset(center.x + (outerR * cos(angle)).toFloat(), center.y + (outerR * sin(angle)).toFloat()),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }

                    drawArc(
                        color = colors.surfaceSoft,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = if (remaining == 0) colors.accentText else colors.primary,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timerLabel(remaining),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontFeatureSettings = "tnum",
                        ),
                        color = colors.text,
                    )
                    Text(
                        text = when {
                            running -> "Đang đếm ngược"
                            remaining == 0 -> "Hết giờ rồi"
                            remaining == totalSeconds -> "Sẵn sàng"
                            else -> "Đang tạm dừng"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.muted,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChunkyButton(
                    onClick = { timer = toggleTimer(timer, totalSeconds) },
                    container = colors.primary,
                    edge = colors.primaryEdge,
                    minHeight = 48.dp,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = when {
                            running -> "Tạm dừng"
                            remaining == totalSeconds -> "Bắt đầu"
                            remaining > 0 -> "Tiếp tục"
                            else -> "Hẹn lại ${totalSeconds / 60} phút"
                        },
                        color = colors.onPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (remaining != totalSeconds || running) {
                    TextButton(
                        onClick = { timer = TimerState(remaining = totalSeconds, running = false) },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text("Đặt lại", color = colors.accentText, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
