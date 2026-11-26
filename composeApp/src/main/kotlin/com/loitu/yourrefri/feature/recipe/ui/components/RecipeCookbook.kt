package com.loitu.yourrefri.feature.recipe.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.MysteryUnlockStatus
import com.loitu.yourrefri.feature.recipe.ui.ALL_COOKBOOK_DURATIONS
import com.loitu.yourrefri.feature.recipe.ui.COOKBOOK_SCREEN_TITLE
import com.loitu.yourrefri.feature.recipe.ui.CookbookDurationGroup
import com.loitu.yourrefri.feature.recipe.ui.CookbookEntry
import com.loitu.yourrefri.feature.recipe.ui.CookbookFilters
import com.loitu.yourrefri.feature.recipe.ui.CookbookQuickFilter
import com.loitu.yourrefri.feature.recipe.ui.REWARD_BANNER_TAGLINE
import com.loitu.yourrefri.feature.recipe.ui.activeCookbookQuickFilter
import com.loitu.yourrefri.feature.recipe.ui.applyCookbookQuickFilter
import com.loitu.yourrefri.feature.recipe.ui.cookbookPresenceSwitchLabel
import com.loitu.yourrefri.feature.recipe.ui.filterCookbookEntries
import com.loitu.yourrefri.feature.recipe.ui.isRewardFabReady
import com.loitu.yourrefri.feature.recipe.ui.rewardFabContentDescription
import com.loitu.yourrefri.feature.recipe.ui.rewardFabProgressFraction
import com.loitu.yourrefri.feature.recipe.ui.rewardFabProgressLabel
import com.loitu.yourrefri.feature.recipe.ui.rewardProgressHintMessage
import com.loitu.yourrefri.feature.recipe.ui.shouldShowRewardFab
import com.loitu.yourrefri.feature.recipe.ui.shouldShowRewardRedDot
import com.loitu.yourrefri.feature.recipe.ui.shouldSpinRewardWheel
import com.loitu.yourrefri.feature.recipe.ui.toCookbookDurations
import com.loitu.yourrefri.feature.recipe.ui.toDurationMask
import com.loitu.yourrefri.feature.recipe.ui.toggleCookbookDuration
import com.loitu.yourrefri.ui.components.RecipeThumb
import com.loitu.yourrefri.ui.components.areSystemAnimatorsEnabled
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

@Composable
internal fun RecipeCookbook(
    entries: List<CookbookEntry>,
    favoriteIds: Set<String>,
    reward: MysteryUnlockStatus?,
    onSpin: () -> Unit,
    onOpenRecipe: (Recipe) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val animatorsEnabled = areSystemAnimatorsEnabled()
    var canCookOnly by rememberSaveable { mutableStateOf(false) }
    var favoriteOnly by rememberSaveable { mutableStateOf(false) }
    var durationMask by rememberSaveable { mutableStateOf(ALL_COOKBOOK_DURATIONS.toDurationMask()) }
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var showRewardHint by rememberSaveable { mutableStateOf(false) }
    val selectedDurations = remember(durationMask) { durationMask.toCookbookDurations() }
    val filters = CookbookFilters(canCookOnly, favoriteOnly, selectedDurations)
    val visible = remember(entries, favoriteIds, filters, query) {
        filterCookbookEntries(entries, favoriteIds, filters, query)
    }
    Box(modifier.fillMaxSize().background(colors.background)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 164.dp),
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
                CookbookQuickFilterChips(activeCookbookQuickFilter(favoriteOnly, selectedDurations)) { quick ->
                    val updated = applyCookbookQuickFilter(filters, quick)
                    favoriteOnly = updated.favoriteOnly
                    durationMask = updated.selectedDurations.toDurationMask()
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                CookbookPresenceSwitch(canCookOnly) { canCookOnly = it }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                CookbookDurationChips(selectedDurations, animatorsEnabled) { group ->
                    durationMask = toggleCookbookDuration(durationMask.toCookbookDurations(), group).toDurationMask()
                }
            }
            if (visible.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                when {
                    selectedDurations.isEmpty() -> CookbookEmptyState(
                        "Hãy chọn ít nhất 1 nhóm thời gian nấu.",
                        "Chọn lại cả 3 nhóm",
                    ) {
                        durationMask = filters.resetDurations().selectedDurations.toDurationMask()
                    }
                    favoriteOnly -> CookbookEmptyState("Chưa có món yêu thích nào phù hợp.", "Xem tất cả món") {
                        favoriteOnly = false
                    }
                    filters != CookbookFilters() || query.isNotBlank() -> CookbookEmptyState(
                        "Chưa tìm thấy món phù hợp.",
                        "Xem tất cả món",
                    ) {
                        val defaults = filters.resetAll()
                        canCookOnly = defaults.canCookOnly
                        favoriteOnly = defaults.favoriteOnly
                        durationMask = defaults.selectedDurations.toDurationMask()
                        query = ""
                    }
                    else -> CookbookEmptyState("Chưa tìm thấy món phù hợp.")
                }
            }
            items(visible, key = { it.recipe.id }) { entry ->
                CookbookRecipeCard(
                    entry = entry,
                    favorite = entry.recipe.id in favoriteIds,
                    onToggleFavorite = { onToggleFavorite(entry.recipe.id) },
                    onOpen = { onOpenRecipe(entry.recipe) },
                    modifier = if (animatorsEnabled) Modifier.animateItem() else Modifier,
                )
            }
        }
        Column(
            Modifier.align(Alignment.BottomEnd).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End,
        ) {
            if (reward != null && shouldShowRewardFab(reward)) {
                RewardProgressFab(reward, animatorsEnabled) {
                    if (isRewardFabReady(reward)) onSpin() else showRewardHint = true
                }
            }
            FavoriteFilterFab(favoriteOnly, { favoriteOnly = it }, animatorsEnabled)
        }
    }
    if (showRewardHint && reward != null && !isRewardFabReady(reward) && !reward.allUnlocked) {
        AlertDialog(
            onDismissRequest = { showRewardHint = false },
            containerColor = colors.surface,
            title = { Text("Quà nấu tại nhà", color = colors.text, fontWeight = FontWeight.Bold) },
            text = { Text(rewardProgressHintMessage(reward), color = colors.muted) },
            confirmButton = {
                TextButton(onClick = { showRewardHint = false }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Đã hiểu", color = colors.accentText, fontWeight = FontWeight.SemiBold)
                }
            },
        )
    }
}

@Composable
private fun CookbookQuickFilterChips(
    active: CookbookQuickFilter,
    onSelect: (CookbookQuickFilter) -> Unit,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CookbookQuickFilter.entries.forEach { quick ->
            val selected = quick == active
            Box(
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).clip(shape)
                    .background(if (selected) colors.primary else colors.surface)
                    .border(1.dp, if (selected) colors.primary else colors.line, shape)
                    .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(quick) })
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = quick.labelVi,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) colors.onPrimary else colors.text,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun CookbookPresenceSwitch(canCookOnly: Boolean, onChange: (Boolean) -> Unit) {
    val colors = AppTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .toggleable(value = canCookOnly, role = Role.Switch, onValueChange = onChange)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(cookbookPresenceSwitchLabel(canCookOnly), color = colors.text, modifier = Modifier.weight(1f))
            Switch(
                checked = canCookOnly,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.onPrimary,
                    checkedTrackColor = colors.primary,
                    checkedBorderColor = colors.primary,
                    uncheckedThumbColor = colors.muted,
                    uncheckedTrackColor = colors.surfaceSoft,
                    uncheckedBorderColor = colors.line,
                ),
            )
        }
    }
}

@Composable
private fun CookbookDurationChips(
    selectedDurations: Set<CookbookDurationGroup>,
    animatorsEnabled: Boolean,
    onToggle: (CookbookDurationGroup) -> Unit,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        CookbookDurationGroup.entries.forEach { group ->
            val selected = group in selectedDurations
            val containerColor by animateColorAsState(
                targetValue = if (selected) colors.primary else colors.surface,
                animationSpec = if (animatorsEnabled) tween(180) else snap(),
                label = "cookbook chip background",
            )
            val borderColor by animateColorAsState(
                targetValue = if (selected) colors.primary else colors.line,
                animationSpec = if (animatorsEnabled) tween(180) else snap(),
                label = "cookbook chip border",
            )
            val textColor by animateColorAsState(
                targetValue = if (selected) colors.onPrimary else colors.text,
                animationSpec = if (animatorsEnabled) tween(180) else snap(),
                label = "cookbook chip text",
            )
            Row(
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).clip(shape)
                    .background(containerColor)
                    .border(1.dp, borderColor, shape)
                    .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onToggle(group) })
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                AnimatedVisibility(
                    visible = selected,
                    enter = fadeIn(if (animatorsEnabled) tween(150) else snap()) +
                        expandHorizontally(if (animatorsEnabled) tween(150) else snap()),
                    exit = fadeOut(if (animatorsEnabled) tween(120) else snap()) +
                        shrinkHorizontally(if (animatorsEnabled) tween(120) else snap()),
                ) {
                    Icon(
                        imageVector = AppIcons.Check,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.padding(end = 3.dp).size(14.dp),
                    )
                }
                Text(
                    text = group.labelVi,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun RewardProgressFab(status: MysteryUnlockStatus, animatorsEnabled: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val ready = isRewardFabReady(status)
    val foreground = if (ready) colors.onCta else colors.ctaText
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val scale by animateFloatAsState(
        targetValue = if (animatorsEnabled && (!entered || pressed)) 0.90f else 1f,
        animationSpec = if (animatorsEnabled) spring(Spring.DampingRatioMediumBouncy) else snap(),
        label = "reward FAB scale",
    )
    val fraction by animateFloatAsState(
        targetValue = rewardFabProgressFraction(status),
        animationSpec = if (animatorsEnabled) tween(300) else snap(),
        label = "reward progress",
    )
    val description = rewardFabContentDescription(status)
    val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    val badgeLabelSp = minOf(11f, 13.5f / fontScale).sp
    Box(Modifier.size(56.dp).scale(scale)) {
        Surface(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxSize().semantics {
                contentDescription = description
            },
            shape = CircleShape,
            color = if (ready) colors.cta else colors.surface,
            border = if (ready) BorderStroke(1.dp, colors.cta) else null,
            shadowElevation = if (ready) 8.dp else 4.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (!ready) {
                    Canvas(Modifier.matchParentSize()) {
                        val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        inset(1.25.dp.toPx()) {
                            drawCircle(colors.line, style = stroke)
                            drawArc(colors.cta, -90f, 360f * fraction, useCenter = false, style = stroke)
                        }
                    }
                }
                val rotation = if (animatorsEnabled && shouldSpinRewardWheel(status)) {
                    val transition = rememberInfiniteTransition(label = "reward wheel")
                    val angle by transition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing)),
                        label = "reward wheel rotation",
                    )
                    angle
                } else 0f
                Column(Modifier.clearAndSetSemantics {}, horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        AppIcons.Wheel,
                        contentDescription = null,
                        tint = foreground,
                        modifier = Modifier.size(16.dp).graphicsLayer { rotationZ = rotation },
                    )
                    Text(
                        rewardFabProgressLabel(status),
                        color = foreground,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = badgeLabelSp,
                        lineHeight = badgeLabelSp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
        }
        if (shouldShowRewardRedDot(status)) {
            val pulse = if (animatorsEnabled) {
                val transition = rememberInfiniteTransition(label = "reward badge")
                val badgeScale by transition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(tween(950), RepeatMode.Reverse),
                    label = "reward badge pulse",
                )
                badgeScale
            } else 1f
            Box(
                Modifier.align(Alignment.TopEnd).size(13.dp).scale(pulse)
                    .background(colors.badgeRed, CircleShape).border(1.5.dp, colors.background, CircleShape),
            )
        }
    }
}

@Composable
private fun FavoriteFilterFab(
    favoriteOnly: Boolean,
    onChange: (Boolean) -> Unit,
    animatorsEnabled: Boolean,
) {
    val colors = AppTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val containerColor by animateColorAsState(
        targetValue = if (favoriteOnly) colors.cta else colors.surface,
        animationSpec = if (animatorsEnabled) tween(180) else snap(),
        label = "favorite filter background",
    )
    val borderColor by animateColorAsState(
        targetValue = if (favoriteOnly) colors.cta else colors.line,
        animationSpec = if (animatorsEnabled) tween(180) else snap(),
        label = "favorite filter border",
    )
    val iconColor by animateColorAsState(
        targetValue = if (favoriteOnly) colors.onCta else colors.ctaText,
        animationSpec = if (animatorsEnabled) tween(180) else snap(),
        label = "favorite filter icon",
    )
    val scale by animateFloatAsState(
        targetValue = when {
            !animatorsEnabled -> 1f
            pressed -> 0.90f
            favoriteOnly -> 1.06f
            else -> 1f
        },
        animationSpec = if (animatorsEnabled) spring(Spring.DampingRatioMediumBouncy) else snap(),
        label = "favorite filter scale",
    )
    val heartScale by animateFloatAsState(
        targetValue = if (animatorsEnabled && favoriteOnly) 1.14f else 1f,
        animationSpec = if (animatorsEnabled) spring(Spring.DampingRatioHighBouncy) else snap(),
        label = "favorite heart pop",
    )
    Surface(
        modifier = Modifier.size(56.dp).scale(scale),
        shape = CircleShape,
        shadowElevation = if (favoriteOnly) 8.dp else 4.dp,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .toggleable(
                    value = favoriteOnly,
                    interactionSource = interactionSource,
                    indication = androidx.compose.foundation.LocalIndication.current,
                    role = Role.Switch,
                    onValueChange = onChange,
                )
                .semantics {
                    contentDescription = if (favoriteOnly) {
                        "Đang chỉ xem món yêu thích, chạm để tắt"
                    } else {
                        "Chỉ xem món yêu thích"
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (favoriteOnly) AppIcons.HeartFilled else AppIcons.Heart,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(26.dp).scale(heartScale),
            )
        }
    }
}

@Composable
private fun CookbookEmptyState(message: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
    val colors = AppTheme.colors
    Column(Modifier.fillMaxWidth().padding(vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, color = colors.muted, textAlign = TextAlign.Center)
        if (actionLabel != null) {
            TextButton(onClick = onAction, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(actionLabel, color = colors.accentText)
            }
        }
    }
}

@Composable
private fun CookbookHeader(
    unlockedTotal: Int,
    total: Int,
    searchVisible: Boolean,
    onToggleSearch: () -> Unit,
) {
    val colors = AppTheme.colors
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("SỔ TAY MÓN ĂN", color = colors.accentText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(
                COOKBOOK_SCREEN_TITLE,
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
    val ready = isRewardFabReady(status)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (ready) colors.ctaSoft else colors.beigeSoft,
        border = BorderStroke(1.dp, lerp(colors.line, colors.cta, if (ready) 0.4f else 0.18f)),
    ) {
        val tap = if (ready) Modifier.clickable(role = Role.Button, onClick = onSpin) else Modifier
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp).then(tap).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                if (status.allUnlocked) AppIcons.CheckCircle else AppIcons.Wheel,
                contentDescription = null,
                tint = colors.ctaText,
                modifier = Modifier.size(20.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    if (status.allUnlocked) "Đã mở trọn bộ $recipeCount món nhà làm!" else REWARD_BANNER_TAGLINE,
                    color = colors.text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (ready) {
                    Text(
                        "Bạn có ${status.availableSpins} lượt quay mở món mới",
                        color = colors.ctaText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun CookbookRecipeCard(
    entry: CookbookEntry,
    favorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val recipe = entry.recipe
    Surface(
        onClick = onOpen,
        modifier = modifier,
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
                            if (favorite) AppIcons.HeartFilled else AppIcons.Heart,
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
                    if (entry.canCookNow) "Đủ đồ" else "Thiếu ${entry.missingCount} nguyên liệu",
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
