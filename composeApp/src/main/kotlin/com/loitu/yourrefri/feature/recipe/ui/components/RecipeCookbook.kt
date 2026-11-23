package com.loitu.yourrefri.feature.recipe.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.MysteryUnlockStatus
import com.loitu.yourrefri.feature.recipe.ui.CookbookEntry
import com.loitu.yourrefri.feature.recipe.ui.MYSTERY_RECIPE_LABEL
import com.loitu.yourrefri.feature.recipe.ui.MysteryQuestionMark
import com.loitu.yourrefri.feature.recipe.ui.MysteryRewardButton
import com.loitu.yourrefri.feature.recipe.ui.PillChip
import com.loitu.yourrefri.feature.recipe.ui.RecipeCookbookFilter
import com.loitu.yourrefri.feature.recipe.ui.filterCookbookEntries
import com.loitu.yourrefri.ui.components.RecipeThumb
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

@Composable
internal fun RecipeCookbook(
    entries: List<CookbookEntry>,
    favoriteIds: Set<String>,
    reward: MysteryUnlockStatus?,
    onSpin: () -> Unit,
    onLockedRecipe: () -> Unit,
    onOpenRecipe: (Recipe) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    var filter by rememberSaveable { mutableStateOf(RecipeCookbookFilter.ALL) }
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val visible = remember(entries, favoriteIds, filter, query) {
        filterCookbookEntries(entries, favoriteIds, filter, query)
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize().background(colors.background),
        contentPadding = PaddingValues(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            CookbookHeader(
                unlockedTotal = entries.count { it.unlocked },
                total = entries.size,
                searchVisible = searchVisible,
                onToggleSearch = {
                    searchVisible = !searchVisible
                    if (!searchVisible) query = ""
                },
            )
        }
        if (searchVisible) item(span = { GridItemSpan(maxLineSpan) }) {
            val focusRequester = remember { FocusRequester() }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                label = { Text("Tìm món trong sổ tay") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        }
        if (reward != null) item(span = { GridItemSpan(maxLineSpan) }) {
            MysteryRewardBanner(reward, entries.size, onSpin)
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RecipeCookbookFilter.entries.forEach { choice ->
                    PillChip(choice.labelVi, filter == choice, { filter = choice })
                }
            }
        }
        if (visible.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                "Chưa tìm thấy món phù hợp.",
                Modifier.fillMaxWidth().padding(vertical = 28.dp),
                textAlign = TextAlign.Center,
                color = colors.muted,
            )
        }
        items(visible, key = { it.recipe.id }) { entry ->
            if (entry.unlocked) {
                CookbookRecipeCard(
                    entry = entry,
                    favorite = entry.recipe.id in favoriteIds,
                    onToggleFavorite = { onToggleFavorite(entry.recipe.id) },
                    onOpen = { onOpenRecipe(entry.recipe) },
                )
            } else {
                MysteryLockedGridCard(onLockedRecipe, enabled = reward != null)
            }
        }
    }
}

@Composable
private fun CookbookHeader(unlockedTotal: Int, total: Int, searchVisible: Boolean, onToggleSearch: () -> Unit) {
    val colors = AppTheme.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("SỔ TAY MÓN ĂN", color = colors.accentText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(
                "Kho món nhà mình",
                Modifier.semantics { heading() },
                color = colors.text,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text("Đã mở $unlockedTotal / $total món", color = colors.muted, style = MaterialTheme.typography.bodyMedium)
        }
        IconButton(onClick = onToggleSearch, modifier = Modifier.size(48.dp).background(colors.primarySoft, CircleShape)) {
            Icon(
                if (searchVisible) AppIcons.Close else AppIcons.Search,
                contentDescription = if (searchVisible) "Đóng tìm kiếm" else "Tìm món",
                tint = colors.accentText,
            )
        }
    }
}

@Composable
private fun MysteryRewardBanner(status: MysteryUnlockStatus, recipeCount: Int, onSpin: () -> Unit) {
    val colors = AppTheme.colors
    val ready = status.availableSpins > 0
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (ready) colors.ctaSoft else colors.beigeSoft,
        border = BorderStroke(1.dp, lerp(colors.line, colors.cta, if (ready) 0.4f else 0.18f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    if (status.allUnlocked) AppIcons.CheckCircle else AppIcons.Wheel,
                    contentDescription = null,
                    tint = colors.ctaText,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    if (status.allUnlocked) "Đã mở trọn bộ $recipeCount món nhà làm!" else "Quà nấu tại nhà",
                    color = colors.text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (ready) {
                Text("Bạn có ${status.availableSpins} lượt quay mở món mới!", color = colors.ctaText)
                MysteryRewardButton("Quay mở món mới", onSpin)
            } else if (!status.allUnlocked) {
                val progress by animateFloatAsState(
                    targetValue = status.cooksIntoCurrentCycle.toFloat() / status.cooksPerUnlock,
                    animationSpec = tween(300),
                    label = "mysteryCookProgress",
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                    color = colors.cta,
                    trackColor = colors.surface,
                )
                Text(
                    "Đã nấu ${status.cooksIntoCurrentCycle}/${status.cooksPerUnlock} bữa tại nhà · " +
                        "Thêm ${status.cooksNeededForNext} bữa để quay món mới",
                    color = colors.beigeText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun CookbookRecipeCard(entry: CookbookEntry, favorite: Boolean, onToggleFavorite: () -> Unit, onOpen: () -> Unit) {
    val colors = AppTheme.colors
    val recipe = entry.recipe
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
        shadowElevation = 1.dp,
    ) {
        Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box {
                RecipeThumb(recipe.id, Modifier.fillMaxWidth(), size = 104.dp, shape = RoundedCornerShape(12.dp))
                IconToggleButton(
                    checked = favorite,
                    onCheckedChange = { onToggleFavorite() },
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(48.dp),
                ) {
                    Box(
                        Modifier.size(34.dp).background(if (favorite) colors.ctaSoft else colors.surface, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            AppIcons.Heart,
                            contentDescription = if (favorite) "Bỏ yêu thích ${recipe.title}" else "Yêu thích ${recipe.title}",
                            tint = if (favorite) colors.ctaText else colors.muted,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
            Column(Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(recipe.title, color = colors.text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, minLines = 2)
                recipe.minutes?.let { Text("$it phút", color = colors.muted, style = MaterialTheme.typography.bodySmall) }
                Text(
                    if (entry.canCookNow) "Đủ đồ" else "Thiếu ${entry.missingCount} món",
                    Modifier
                        .background(if (entry.canCookNow) colors.primarySoft else colors.beigeSoft, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = if (entry.canCookNow) colors.accentText else colors.beigeText,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun MysteryLockedGridCard(onClick: () -> Unit, enabled: Boolean) {
    val colors = AppTheme.colors
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 208.dp),
        shape = RoundedCornerShape(16.dp),
        color = lerp(colors.surfaceSoft, colors.ctaSoft, 0.48f),
        border = BorderStroke(1.dp, lerp(colors.line, colors.cta, 0.22f)),
    ) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MysteryQuestionMark(Modifier.size(96.dp))
            Text(MYSTERY_RECIPE_LABEL, color = colors.text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("Nấu tại nhà để mở", color = colors.muted, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}
