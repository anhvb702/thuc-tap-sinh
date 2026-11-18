package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.ui.components.ChunkyButton
import com.loitu.yourrefri.ui.theme.AppTheme
import kotlinx.coroutines.delay

/** Hẹn giờ 4 phút cho một bước nấu, theo Figma 08. */
internal const val COOK_TIMER_TOTAL_SEC = 240

@Composable
internal fun CookTimerCard(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    var timer by remember { mutableStateOf(TimerState(remaining = 0, running = false)) }
    val remaining = timer.remaining
    val running = timer.running

    LaunchedEffect(running) {
        while (timer.running) {
            delay(1000)
            timer = tickTimer(timer)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Hẹn giờ bước này",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.accentText,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = timerLabel(remaining),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
                ChunkyButton(
                    onClick = { timer = toggleTimer(timer, COOK_TIMER_TOTAL_SEC) },
                    container = colors.primary,
                    edge = colors.primaryEdge,
                    minHeight = 48.dp,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = when {
                            running -> "Tạm dừng"
                            remaining > 0 -> "Tiếp tục"
                            else -> "Hẹn giờ 4 phút"
                        },
                        color = colors.onPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { timerProgress(remaining, COOK_TIMER_TOTAL_SEC) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 4.dp, max = 4.dp),
                color = colors.primary,
                trackColor = colors.line,
            )
            if (remaining > 0 || running) {
                TextButton(
                    onClick = { timer = resetTimer() },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Đặt lại", color = colors.accentText, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
