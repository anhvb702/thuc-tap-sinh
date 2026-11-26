package com.loitu.yourrefri.feature.recipe.ui

import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.pickMysteryRecipeToUnlock
import com.loitu.yourrefri.ui.components.RecipeThumb
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
internal fun MysteryUnlockWheel(
    recipes: List<Recipe>,
    unlockedIds: Set<String>,
    saveFailedId: String?,
    canSpin: Boolean,
    onUnlocked: (Recipe) -> Unit,
    onOpenRecipe: (Recipe) -> Unit,
    onDismiss: () -> Unit,
    userName: String = "Andy",
) {
    val colors = AppTheme.colors
    val animatorsEnabled = Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled()
    val listState = rememberLazyListState()
    val flip = remember { Animatable(0f) }
    val latestUnlock by rememberUpdatedState(onUnlocked)
    var selected by remember { mutableStateOf<Recipe?>(null) }
    var landed by remember { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(false) }
    val cardWidth = 138.dp
    val cardSpacing = 10.dp
    val stride = with(LocalDensity.current) { cardWidth.roundToPx() + cardSpacing.roundToPx() }
    val landingIndex = MYSTERY_WHEEL_GACHA_LANDING_INDEX
    val winner = selected
    val saved = landed && winner != null && winner.id in unlockedIds
    val saveFailed = landed && winner != null && !saved && winner.id == saveFailedId
    val busy = winner != null && !revealed && !saveFailed

    LaunchedEffect(winner) {
        if (winner != null) {
            if (animatorsEnabled) {
                listState.animateScrollBy(
                    value = landingIndex * stride.toFloat(),
                    animationSpec = tween(durationMillis = MYSTERY_WHEEL_GACHA_DURATION_MS, easing = GachaSpinEasing),
                )
            }
            listState.scrollToItem(landingIndex)
            landed = true
            latestUnlock(winner)
        }
    }
    LaunchedEffect(saved) {
        if (saved) {
            if (animatorsEnabled) flip.animateTo(180f, tween(480, easing = FastOutSlowInEasing))
            else flip.snapTo(180f)
            revealed = true
        }
    }
    val scale by animateFloatAsState(
        if (revealed && animatorsEnabled) 1.08f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "mysteryCardScale",
    )

    Dialog(onDismissRequest = { if (!busy) onDismiss() }) {
        Surface(shape = RoundedCornerShape(24.dp), color = colors.surface, contentColor = colors.text) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 18.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val displayName = userName.trim().ifEmpty { "nhà mình" }
                    Text(
                        if (revealed) "Chúc mừng $displayName!" else "Quà nấu tại nhà",
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onDismiss, enabled = !busy) {
                        Icon(AppIcons.Close, contentDescription = "Đóng")
                    }
                }
                BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val sidePadding = ((maxWidth - cardWidth) / 2).coerceAtLeast(0.dp)
                    if (animatorsEnabled && saved) {
                        WarmCelebrationConfetti(Modifier.matchParentSize())
                    }
                    LazyRow(
                        state = listState,
                        userScrollEnabled = false,
                        contentPadding = PaddingValues(horizontal = sidePadding),
                        horizontalArrangement = Arrangement.spacedBy(cardSpacing),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 184.dp),
                    ) {
                        items(landingIndex + 4) { index ->
                            val isLandingCard = index == landingIndex && landed
                            val angle = if (isLandingCard) flip.value else 0f
                            val cardModifier = Modifier.width(cardWidth).padding(vertical = 10.dp).graphicsLayer {
                                rotationY = if (angle < 90f) angle else angle - 180f
                                cameraDistance = 12f * density
                                scaleX = if (isLandingCard) scale else 1f
                                scaleY = scaleX
                                alpha = if (saved && !isLandingCard) 1f - flip.value / 225f else 1f
                            }
                            if (angle >= 90f && winner != null) {
                                MysteryWinnerCard(winner, cardModifier)
                            } else {
                                MysteryQuestionCard(cardModifier)
                            }
                        }
                    }
                    if (!saved) Icon(
                        AppIcons.ChevronDown,
                        contentDescription = null,
                        tint = colors.ctaText,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
                if (revealed && winner != null) {
                    Column(
                        Modifier.padding(horizontal = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Bạn vừa mở khóa món mới: ${winner.title}", textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Món này đã có trong Sổ tay và Thực đơn nhà mình rồi nhé!",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = colors.muted,
                        )
                        MysteryRewardButton("Xem công thức ngay", { onOpenRecipe(winner) })
                        TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text("Để vào sổ tay", color = colors.accentText)
                        }
                    }
                } else {
                    if (saveFailed) Text(
                        "Chưa lưu được món mới. Mình thử lại nhé?",
                        Modifier.padding(horizontal = 18.dp),
                        color = colors.warnText,
                        textAlign = TextAlign.Center,
                    )
                    val label = when {
                        saveFailed -> "Thử lưu lại"
                        landed -> "Đang lưu món mới…"
                        busy -> "Đang quay…"
                        else -> "Quay mở món mới"
                    }
                    MysteryRewardButton(
                        label = label,
                        onClick = {
                            if (saveFailed) onUnlocked(winner)
                            else if (selected == null && canSpin) {
                                selected = pickMysteryRecipeToUnlock(recipes, unlockedIds, Random.Default::nextInt)
                            }
                        },
                        enabled = saveFailed || (canSpin && !busy),
                    )
                }
            }
        }
    }
}

@Composable
internal fun MysteryRewardButton(label: String, onClick: () -> Unit, enabled: Boolean = true) {
    val colors = AppTheme.colors
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta),
    ) { Text(label, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun MysteryQuestionCard(modifier: Modifier) {
    val colors = AppTheme.colors
    Surface(
        modifier.heightIn(min = 158.dp),
        shape = RoundedCornerShape(16.dp),
        color = lerp(colors.surfaceSoft, colors.ctaSoft, 0.62f),
        border = BorderStroke(1.dp, lerp(colors.line, colors.cta, 0.30f)),
        shadowElevation = 2.dp,
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            MysteryQuestionMark(Modifier.size(90.dp))
            Text(MYSTERY_RECIPE_LABEL, color = colors.ctaText, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
internal fun MysteryQuestionMark(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Canvas(modifier) {
        val unit = size.minDimension / 100f
        val badge = center
        fun point(x: Float, y: Float) = badge + Offset(x * unit, y * unit)
        drawCircle(colors.beigeSoft, 32 * unit)
        drawCircle(colors.cta.copy(alpha = 0.34f), 32 * unit, style = Stroke(1.2f * unit))
        val hook = Path().apply {
            val start = point(-10f, -9f)
            moveTo(start.x, start.y)
            val topLeft = point(-10f, -23f)
            val topRight = point(12f, -23f)
            val bend = point(12f, -10f)
            cubicTo(topLeft.x, topLeft.y, topRight.x, topRight.y, bend.x, bend.y)
            val right = point(12f, -2f)
            val stem = point(0f, -2f)
            val end = point(0f, 7f)
            cubicTo(right.x, right.y, stem.x, stem.y, end.x, end.y)
        }
        drawPath(hook, colors.ctaText, style = Stroke(4.6f * unit, cap = StrokeCap.Round))
        drawCircle(colors.ctaText, 2.6f * unit, point(0f, 17f))
        drawCircle(colors.cta, 2.3f * unit, point(-37f, -17f))
        drawCircle(colors.brand, 2.1f * unit, point(32f, 27f))
        drawLine(colors.cta, point(32f, -28f), point(43f, -28f), 1.8f * unit, StrokeCap.Round)
        drawLine(colors.cta, point(37.5f, -33.5f), point(37.5f, -22.5f), 1.8f * unit, StrokeCap.Round)
    }
}

@Composable
private fun MysteryWinnerCard(recipe: Recipe, modifier: Modifier) {
    val colors = AppTheme.colors
    Surface(
        modifier.heightIn(min = 158.dp),
        shape = RoundedCornerShape(16.dp),
        color = colors.primarySoft,
        border = BorderStroke(1.dp, colors.brand),
        shadowElevation = 4.dp,
    ) {
        Column(
            Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RecipeThumb(recipe.id, Modifier.fillMaxWidth(), size = 84.dp)
            Text(recipe.title, style = MaterialTheme.typography.titleSmall, color = colors.text, textAlign = TextAlign.Center)
            recipe.minutes?.let { Text("$it phút", style = MaterialTheme.typography.labelMedium, color = colors.muted) }
        }
    }
}

@Composable
private fun WarmCelebrationConfetti(modifier: Modifier) {
    val colors = AppTheme.colors
    val progress = remember { Animatable(0f) }
    val palette = listOf(colors.cta, colors.warn, colors.brand, colors.beigeText)
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(1250, easing = FastOutSlowInEasing)) }
    Canvas(modifier) {
        val fraction = progress.value
        repeat(24) { index ->
            val angle = index * (2.0 * Math.PI / 24)
            val travel = (0.65f + index % 5 * 0.08f) * fraction
            val burst = Offset(
                center.x + cos(angle).toFloat() * size.width * 0.52f * travel,
                center.y + sin(angle).toFloat() * size.height * 0.50f * travel + fraction * fraction * 20.dp.toPx(),
            )
            val color = palette[index % palette.size].copy(alpha = (1f - fraction) * 0.86f)
            if (index % 3 == 0) drawCircle(color, 2.4.dp.toPx(), burst)
            else drawLine(color, burst, burst + Offset(6.dp.toPx(), 3.dp.toPx()), 2.2.dp.toPx(), StrokeCap.Round)
        }
    }
}
