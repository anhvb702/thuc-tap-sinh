package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.ui.components.RecipeThumb
import com.loitu.yourrefri.ui.components.areSystemAnimatorsEnabled
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

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
    freezerNames: List<String> = emptyList(),
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
        val animatorsEnabled = areSystemAnimatorsEnabled()
        val timerSeconds = stepTimerSeconds(recipe, state.stepIndex)
        val config = LocalConfiguration.current
        val fontScale = LocalDensity.current.fontScale
        val compactTimerScroll = config.screenHeightDp < 600 || fontScale >= 1.35f
        // Cuộn được khi chữ rất dài hoặc cỡ chữ hệ thống lớn; màn quá thấp/chữ rất to đặt đồng hồ vào vùng cuộn để không ép chữ.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (state.isFirst && freezerNames.isNotEmpty()) {
                    DefrostNoticeBanner(
                        freezerNames = freezerNames,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (state.isLast) {
                    Text(
                        text = "Dọn ra mâm",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.accentText,
                    )
                    RecipeThumb(
                        recipeId = recipe.id,
                        modifier = Modifier.fillMaxWidth(),
                        size = 180.dp,
                        shape = RoundedCornerShape(20.dp),
                    )
                }
                AnimatedContent(
                    targetState = state.stepIndex to stepText,
                    transitionSpec = {
                        val dir = slideDirection(initialState.first, targetState.first)
                        if (!animatorsEnabled || dir == 0) {
                            fadeIn(snap()) togetherWith fadeOut(snap())
                        } else {
                            (slideInHorizontally(tween(260)) { it / 4 * dir } + fadeIn(tween(220))) togetherWith
                                (slideOutHorizontally(tween(200)) { -it / 4 * dir } + fadeOut(tween(160)))
                        }
                    },
                    label = "handsFreeStep",
                ) { (_, text) ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 30.sp,
                            lineHeight = 42.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = colors.text,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = state.announcement(text)
                        },
                    )
                }
                if (timerSeconds != null && compactTimerScroll) {
                    CookTimerCard(
                        modifier = Modifier.padding(top = 4.dp),
                        totalSeconds = timerSeconds,
                        stepKey = state.stepIndex,
                        dialSize = 120.dp,
                    )
                }
            }
        }

        if (timerSeconds != null && !compactTimerScroll) {
            CookTimerCard(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                totalSeconds = timerSeconds,
                stepKey = state.stepIndex,
                dialSize = 136.dp,
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
