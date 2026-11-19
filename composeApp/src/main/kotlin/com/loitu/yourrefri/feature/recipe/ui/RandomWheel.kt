package com.loitu.yourrefri.feature.recipe.ui

import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.RandomRecipePick
import com.loitu.yourrefri.ui.components.RecipeThumb
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

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
) {
    val colors = AppTheme.colors
    val (reel, winnerIndex) = remember(pool, pick.recipe.id) { wheelReel(pool, pick.recipe) }
    val listState = rememberLazyListState()

    LaunchedEffect(reel, winnerIndex) {
        if (reel.isNotEmpty()) {
            if (Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled()) {
                listState.animateScrollToItem(winnerIndex)
            } else {
                listState.scrollToItem(winnerIndex)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hôm nay mình nấu gì nhỉ?", color = colors.text) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Món được chọn", style = MaterialTheme.typography.bodyMedium, color = colors.muted)
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val cardWidth = 132.dp
                    val sidePadding = ((maxWidth - cardWidth) / 2).coerceAtLeast(0.dp)
                    Box {
                        LazyRow(
                            state = listState,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = sidePadding),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                        ) {
                            itemsIndexed(reel) { index, item ->
                                val selected = index == winnerIndex
                                Card(
                                    modifier = Modifier
                                        .width(cardWidth)
                                        .height(138.dp)
                                        .then(if (selected) Modifier.border(BorderStroke(2.dp, colors.primary), RoundedCornerShape(16.dp)) else Modifier),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (selected) colors.primarySoft else colors.surface),
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        RecipeThumb(recipeId = item.id, size = 72.dp)
                                        Text(
                                            item.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
                                            color = colors.text,
                                            maxLines = 2,
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
                if (!pick.canCookNow) {
                    Text(RandomRecipeCopy.needMore, style = MaterialTheme.typography.bodySmall, color = colors.muted)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onSpinAgain, modifier = Modifier.heightIn(min = 48.dp)) { Text("Quay lại") }
                TextButton(onClick = { onOpenRecipe(pick.recipe) }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Xem cách nấu") }
                TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) { Text("Đóng") }
            }
        },
    )
}
