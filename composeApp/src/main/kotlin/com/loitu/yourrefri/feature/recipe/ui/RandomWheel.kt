package com.loitu.yourrefri.feature.recipe.ui

import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.RandomRecipePick
import com.loitu.yourrefri.ui.components.RecipeThumb
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

internal const val RANDOM_WHEEL_GACHA_LOOPS = 5
internal const val RANDOM_WHEEL_GACHA_DURATION_MS = 5400
internal const val MYSTERY_WHEEL_GACHA_LANDING_INDEX = 24
internal const val MYSTERY_WHEEL_GACHA_DURATION_MS = 4800

internal fun gachaSpinProgress(fraction: Float): Float {
    val t = fraction.coerceIn(0f, 1f)
    val inv = 1f - t
    return 1f - inv * inv * inv * inv * inv
}

internal val GachaSpinEasing = Easing(::gachaSpinProgress)

/** Creates a circular reel whose landing card always has a visible tail. */
internal fun wheelReel(pool: List<Recipe>, winner: Recipe, loops: Int = 3, tail: Int = 3): Pair<List<Recipe>, Int> {
    if (pool.isEmpty()) return emptyList<Recipe>() to 0
    val winnerOffset = pool.indexOfFirst { it.id == winner.id }.takeIf { it >= 0 } ?: 0
    val safeLoops = loops.coerceAtLeast(0)
    val safeTail = tail.coerceAtLeast(3)
    val prefix = List(safeLoops) { pool }.flatten()
    val reel = buildList {
        addAll(prefix)
        add(winner)
        repeat(safeTail) { offset -> add(pool[(winnerOffset + offset + 1) % pool.size]) }
    }
    return reel to (prefix.size)
}

@Composable
internal fun RandomWheelDialog(
    pool: List<Recipe>,
    pick: RandomRecipePick,
    onSpinAgain: () -> Unit,
    onOpenRecipe: (Recipe) -> Unit,
    onDismiss: () -> Unit,
    onlyReadyIngredients: Boolean = true,
    onToggleOnlyReady: ((Boolean) -> Unit)? = null,
) {
    val colors = AppTheme.colors
    var spinTrigger by remember { mutableIntStateOf(0) }
    var landed by remember(pool, pick.recipe.id, spinTrigger) { mutableStateOf(false) }
    var showResult by remember(pool, pick.recipe.id, spinTrigger) { mutableStateOf(false) }
    val (reel, winnerIndex) = remember(pool, pick.recipe.id, spinTrigger) {
        wheelReel(pool, pick.recipe, loops = RANDOM_WHEEL_GACHA_LOOPS, tail = 3)
    }
    val listState = rememberLazyListState()
    val cardWidth = 132.dp
    val cardSpacing = 10.dp
    val itemStridePx = with(LocalDensity.current) { (cardWidth + cardSpacing).toPx() }

    LaunchedEffect(reel, winnerIndex, spinTrigger) {
        if (reel.isNotEmpty()) {
            landed = false
            listState.scrollToItem(0)
            if (Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled()) {
                listState.animateScrollBy(
                    value = winnerIndex * itemStridePx,
                    animationSpec = tween(durationMillis = RANDOM_WHEEL_GACHA_DURATION_MS, easing = GachaSpinEasing),
                )
            }
            listState.scrollToItem(winnerIndex)
            landed = true
            delay(650)
            showResult = true
        }
    }

    if (!showResult) AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Hôm nay mình nấu gì nhỉ?", color = colors.text, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(AppIcons.Close, contentDescription = "Đóng", tint = colors.muted) }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onToggleOnlyReady != null) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (onlyReadyIngredients) colors.primarySoft else colors.surfaceSoft,
                        border = BorderStroke(1.dp, if (onlyReadyIngredients) colors.primary else colors.line),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .toggleable(
                                value = onlyReadyIngredients,
                                role = Role.Switch,
                                onValueChange = onToggleOnlyReady,
                            ),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            if (onlyReadyIngredients) {
                                Icon(AppIcons.Check, contentDescription = null, tint = colors.accentText, modifier = Modifier.width(16.dp).height(16.dp))
                            }
                            Text(
                                text = RANDOM_WHEEL_READY_ONLY_LABEL,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (onlyReadyIngredients) colors.accentText else colors.muted,
                            )
                        }
                    }
                }
                Text(
                    if (landed) "Món được chọn" else "Đang quay chọn món...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted,
                )
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val sidePadding = ((maxWidth - cardWidth) / 2).coerceAtLeast(0.dp)
                    Box {
                        LazyRow(
                            state = listState,
                            userScrollEnabled = false,
                            verticalAlignment = Alignment.CenterVertically,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = sidePadding),
                            horizontalArrangement = Arrangement.spacedBy(cardSpacing),
                            modifier = Modifier.fillMaxWidth().height(172.dp),
                        ) {
                            itemsIndexed(reel, key = { idx, item -> "$idx-${item.id}" }) { index, item ->
                                val isWinnerLit = landed && index == winnerIndex
                                val cardScale by animateFloatAsState(
                                    targetValue = if (isWinnerLit) 1.08f else 0.96f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium,
                                    ),
                                    label = "wheelCardScale",
                                )
                                val borderColor by animateColorAsState(
                                    targetValue = if (isWinnerLit) colors.primary else colors.line,
                                    animationSpec = tween(durationMillis = 260),
                                    label = "wheelBorderColor",
                                )
                                val cardBg by animateColorAsState(
                                    targetValue = if (isWinnerLit) colors.primarySoft else colors.surfaceSoft,
                                    animationSpec = tween(durationMillis = 260),
                                    label = "wheelCardBg",
                                )
                                Card(
                                    modifier = Modifier
                                        .width(cardWidth)
                                        .height(142.dp)
                                        .graphicsLayer {
                                            scaleX = cardScale
                                            scaleY = cardScale
                                        }
                                        .border(
                                            BorderStroke(if (isWinnerLit) 3.dp else 1.dp, borderColor),
                                            RoundedCornerShape(16.dp),
                                        ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = if (isWinnerLit) 8.dp else 0.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        RecipeThumb(recipeId = item.id, size = 72.dp)
                                        Text(
                                            item.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isWinnerLit) FontWeight.Bold else FontWeight.Medium),
                                            color = colors.text,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        }
                        Icon(
                            imageVector = AppIcons.ChevronDown,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.align(Alignment.TopCenter),
                        )
                    }
                }
            }
        },
        confirmButton = {},
    ) else AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(RandomRecipeCopy.resultTitle, color = colors.text, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(AppIcons.Close, contentDescription = "Đóng", tint = colors.muted) }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RecipeThumb(
                    recipeId = pick.recipe.id,
                    modifier = Modifier.fillMaxWidth(),
                    size = 160.dp,
                    shape = RoundedCornerShape(20.dp),
                )
                Text(
                    pick.recipe.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
                pick.recipe.minutes?.let { minutes ->
                    Text("Khoảng $minutes phút", color = colors.muted)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = { showResult = false; spinTrigger++; onSpinAgain() },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text("Quay lại", color = colors.muted) }
                TextButton(
                    onClick = { onOpenRecipe(pick.recipe) },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text("Xem cách nấu", color = colors.accentText, fontWeight = FontWeight.Bold) }
            }
        },
    )
}
