package com.loitu.yourrefri.feature.recipe.ui

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import kotlinx.coroutines.delay

private val NavButtonHeight = 72.dp
private val NavButtonGap = 16.dp

/**
 * Chế độ nấu rảnh tay: mỗi lần một bước, chữ lớn, hai nút to, màn hình luôn sáng.
 * Không tự điều hướng: người gọi nối [onClose] (thoát ngay, không hộp thoại) và [onFinished].
 */
@Composable
fun HandsFreeCookScreen(
    recipe: Recipe,
    onClose: () -> Unit,
    onFinished: () -> Unit,
    onKeepLeftovers: () -> Unit = {},
    deductionPreview: String = "",
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val total = recipe.steps.size
    var savedIndex by rememberSaveable { mutableIntStateOf(0) }
    val state = HandsFreeState.of(savedIndex, total)

    // Giữ màn hình sáng khi đang nấu, trả lại như cũ khi rời. Không cần quyền nào.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(48.dp).semantics { contentDescription = HandsFreeCopy.CLOSE_DESCRIPTION },
            ) {
                Icon(AppIcons.Close, contentDescription = null, tint = colors.text, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
                if (!state.isEmpty) {
                    Text(text = state.label, style = MaterialTheme.typography.bodyMedium, color = colors.muted)
                }
            }
        }

        if (state.isEmpty) {
            EmptySteps(onBack = onClose, modifier = Modifier.weight(1f))
            return@Column
        }

        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).heightIn(min = 4.dp, max = 4.dp),
            color = colors.primary,
            trackColor = colors.line,
        )

        val stepText = recipe.steps[state.stepIndex]
        val stepDurationSeconds = remember(stepText) { parseStepDurationSeconds(stepText) }
        // Cuộn được khi chữ rất dài hoặc cỡ chữ hệ thống lớn, không cắt chữ.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stepText,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 30.sp,
                    lineHeight = 42.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.text,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = state.announcement(stepText)
                },
            )
            if (stepDurationSeconds != null) {
                StepTimerCard(
                    stepIndex = state.stepIndex,
                    initialSeconds = stepDurationSeconds,
                )
            }
        }

        if (state.isLast && deductionPreview.isNotBlank()) {
            Text(
                text = deductionPreview,
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(NavButtonGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Giữ chỗ nút "Quay lại" ở bước đầu để nút "Tiếp" không nhảy vị trí.
            if (state.isFirst) {
                Spacer(Modifier.weight(1f))
            } else {
                Button(
                    onClick = { savedIndex = state.previous().stepIndex },
                    modifier = Modifier.weight(1f).heightIn(min = NavButtonHeight),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceSoft, contentColor = colors.text),
                ) {
                    Text(HandsFreeCopy.BACK, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
            Button(
                onClick = { if (state.isLast) onFinished() else savedIndex = state.next().stepIndex },
                modifier = Modifier.weight(if (state.isLast) 1.6f else 1f).heightIn(min = NavButtonHeight),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta),
            ) {
                Text(state.nextLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
        if (state.isLast) TextButton(onClick = onKeepLeftovers, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text("Giữ lại đồ còn dư", color = colors.accentText)
        }
    }
}

@Composable
private fun StepTimerCard(
    stepIndex: Int,
    initialSeconds: Int,
) {
    val colors = AppTheme.colors
    val haptic = LocalHapticFeedback.current
    var targetSeconds by remember(stepIndex, initialSeconds) { mutableIntStateOf(initialSeconds) }
    var remainingSeconds by remember(stepIndex, initialSeconds) { mutableIntStateOf(initialSeconds) }
    var running by remember(stepIndex, initialSeconds) { mutableStateOf(false) }
    var finished by remember(stepIndex, initialSeconds) { mutableStateOf(false) }

    LaunchedEffect(stepIndex, running) {
        if (!running) return@LaunchedEffect
        while (running && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds = (remainingSeconds - 1).coerceAtLeast(0)
        }
        if (running && remainingSeconds == 0) {
            running = false
            finished = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            runCatching {
                val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75)
                try {
                    tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
                    delay(400L)
                } finally {
                    tone.release()
                }
            }
        }
    }

    val progress = if (targetSeconds <= 0) 0f else (remainingSeconds.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (finished) colors.warnSoft else colors.surface,
        border = BorderStroke(1.dp, if (running || finished) colors.primary else colors.line),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = AppIcons.Clock,
                    contentDescription = null,
                    tint = if (finished) colors.warnText else colors.accentText,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = if (finished) HandsFreeCopy.TIMER_DONE else HandsFreeCopy.TIMER_TITLE,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (finished) colors.warnText else colors.accentText,
                    modifier = if (finished) Modifier.semantics { liveRegion = LiveRegionMode.Assertive } else Modifier,
                )
            }

            Text(
                text = formatTimerClock(remainingSeconds),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = colors.text,
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().heightIn(min = 6.dp, max = 6.dp),
                color = if (finished) colors.warnText else colors.primary,
                trackColor = colors.line,
            )

            if (!running) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            val next = (remainingSeconds - 60).coerceAtLeast(30)
                            remainingSeconds = next
                            targetSeconds = next
                            finished = false
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("-1 phút", color = colors.text)
                    }
                    OutlinedButton(
                        onClick = {
                            val next = remainingSeconds + 60
                            remainingSeconds = next
                            targetSeconds = next.coerceAtLeast(targetSeconds)
                            finished = false
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("+1 phút", color = colors.text)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = {
                        if (remainingSeconds == 0) {
                            remainingSeconds = targetSeconds
                            finished = false
                            running = true
                        } else {
                            finished = false
                            running = !running
                        }
                    },
                    modifier = Modifier.weight(1.4f).heightIn(min = 52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (running) colors.surfaceSoft else colors.cta,
                        contentColor = if (running) colors.text else colors.onCta,
                    ),
                ) {
                    val label = when {
                        running -> HandsFreeCopy.TIMER_PAUSE
                        remainingSeconds < targetSeconds && remainingSeconds > 0 -> HandsFreeCopy.TIMER_RESUME
                        else -> HandsFreeCopy.TIMER_START
                    }
                    Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                if (running || remainingSeconds != targetSeconds || finished) {
                    OutlinedButton(
                        onClick = {
                            running = false
                            finished = false
                            targetSeconds = initialSeconds
                            remainingSeconds = initialSeconds
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(HandsFreeCopy.TIMER_RESET, color = colors.text, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySteps(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = HandsFreeCopy.EMPTY_TITLE,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = colors.text,
        )
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().heightIn(min = NavButtonHeight),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceSoft, contentColor = colors.text),
        ) {
            Text(HandsFreeCopy.EMPTY_BACK, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
