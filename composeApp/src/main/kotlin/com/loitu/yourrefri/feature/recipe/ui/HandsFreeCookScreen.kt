package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.ui.components.RecipeThumb
import com.loitu.yourrefri.ui.components.areSystemAnimatorsEnabled
import com.loitu.yourrefri.ui.components.pressScale
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import kotlinx.coroutines.launch

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
    val initialState = HandsFreeState.of(savedIndex, total)

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
            Text(
                text = recipe.title,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (initialState.isEmpty) {
            EmptySteps(onBack = onClose, modifier = Modifier.weight(1f))
            return@Column
        }

        val pagerState = rememberPagerState(initialPage = initialState.stepIndex, pageCount = { total })
        val state = HandsFreeState.of(pagerState.currentPage, total)
        LaunchedEffect(pagerState.currentPage) { savedIndex = pagerState.currentPage }

        val animatorsEnabled = areSystemAnimatorsEnabled()
        val fontScale = LocalDensity.current.fontScale
        val coroutineScope = rememberCoroutineScope()
        fun goToPage(page: Int) {
            coroutineScope.launch {
                if (animatorsEnabled) pagerState.animateScrollToPage(page) else pagerState.scrollToPage(page)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            slideSegmentStates(state.stepIndex, total).forEach { segment ->
                val color = when (segment) {
                    SlideSegmentState.CURRENT -> colors.cta
                    SlideSegmentState.COMPLETED -> colors.primary
                    SlideSegmentState.UPCOMING -> colors.line
                }
                Box(Modifier.weight(1f).height(5.dp).background(color, RoundedCornerShape(999.dp)))
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            pageSpacing = 12.dp,
        ) { page ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(28.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.line),
                shadowElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Surface(shape = RoundedCornerShape(999.dp), color = colors.primarySoft) {
                        Text(
                            text = slideStepPillLabel(page, total),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.accentText,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    if (page == 0 && freezerNames.isNotEmpty()) {
                        DefrostNoticeBanner(freezerNames = freezerNames, modifier = Modifier.fillMaxWidth())
                    }
                    if (page == total - 1) {
                        Text(
                            text = "Dọn ra mâm",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.accentText,
                        )
                        RecipeThumb(
                            recipeId = recipe.id,
                            modifier = Modifier.fillMaxWidth(),
                            size = 160.dp,
                            shape = RoundedCornerShape(20.dp),
                        )
                    }
                    val stepText = recipe.steps[page]
                    Text(
                        text = stepText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 26.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text,
                        modifier = Modifier.semantics {
                            if (page == pagerState.currentPage) {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = state.announcement(stepText)
                            }
                        },
                    )
                    val timerSeconds = stepTimerSeconds(recipe, page)
                    if (timerSeconds != null) {
                        CookTimerCard(
                            totalSeconds = timerSeconds,
                            stepKey = page,
                            dialSize = slideTimerDialDp(fontScale).dp,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(NavButtonGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Giữ chỗ nút "Quay lại" ở bước đầu để nút "Tiếp" không nhảy vị trí.
            if (state.isFirst) {
                Spacer(Modifier.weight(1f))
            } else {
                val backInteractionSource = remember { MutableInteractionSource() }
                Button(
                    onClick = { goToPage(state.previous().stepIndex) },
                    interactionSource = backInteractionSource,
                    modifier = Modifier.weight(1f).heightIn(min = NavButtonHeight).pressScale(backInteractionSource),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceSoft, contentColor = colors.text),
                ) {
                    Text(HandsFreeCopy.BACK, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
            val nextInteractionSource = remember { MutableInteractionSource() }
            Button(
                onClick = { if (state.isLast) onFinished() else goToPage(state.next().stepIndex) },
                interactionSource = nextInteractionSource,
                modifier = Modifier
                    .weight(if (state.isLast) 1.6f else 1f)
                    .heightIn(min = NavButtonHeight)
                    .pressScale(nextInteractionSource),
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
