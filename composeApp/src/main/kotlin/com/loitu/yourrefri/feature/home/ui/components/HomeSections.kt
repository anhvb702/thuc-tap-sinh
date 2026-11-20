package com.loitu.yourrefri.feature.home.ui

import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loitu.yourrefri.feature.home.logic.homeGreeting
import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import com.loitu.yourrefri.feature.pantry.ui.estimateDaysText
import com.loitu.yourrefri.feature.pantry.ui.isFreezerItem
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.RecipeRecommendation
import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import com.loitu.yourrefri.feature.recipe.ui.WEEKLY_BUTTON_LABEL
import com.loitu.yourrefri.feature.recipe.ui.emptyRecommendationCopy
import com.loitu.yourrefri.feature.recipe.ui.shortReasonLabel
import com.loitu.yourrefri.ui.components.ChunkyButton
import com.loitu.yourrefri.ui.components.FoodThumb
import com.loitu.yourrefri.ui.components.RecipeThumb
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import com.loitu.yourrefri.ui.theme.SageGreenStrong // guard:allow

// Nút hành động chính: nền xanh sage đậm, chữ trắng (WCAG AA >= 4.5).
internal data class ActionStyle(val container: Color, val content: Color)

internal val weeklyPlanActionStyle = ActionStyle(container = SageGreenStrong, content = Color.White) // guard:allow

// Cỡ chữ hệ thống từ 1.2 trở lên thì khối trong thẻ tủ xếp dọc, không đặt cạnh nhau.
internal fun summaryButtonBelowText(fontScale: Float): Boolean = fontScale >= 1.2f

// Lời chào dùng chung từ :shared có kèm ký hiệu trang trí; giao diện mới chỉ hiện chữ.
internal fun greetingText(hour: Int): String = homeGreeting(hour).replace(" 🌿", "") // guard:allow (chuỗi dùng chung ở :shared còn ký hiệu, chỉ cắt khi hiển thị)

// Bản ngắn của statusText cho thẻ hẹp; tiêu đề mục đã ghi "Hạn là ước tính".
internal fun shortEstimateText(daysLeft: Int): String = estimateDaysText(daysLeft)

// Số món đang có theo khu vực bảo quản (ngăn mát / ngăn đông).
internal data class PantryZoneCounts(val fridge: Int, val freezer: Int)

internal fun pantryZoneCounts(haveIds: List<String>): PantryZoneCounts {
    val freezer = haveIds.count { isFreezerItem(it) }
    return PantryZoneCounts(fridge = haveIds.size - freezer, freezer = freezer)
}

// Dòng chữ dưới tên món cần dùng sớm; luôn là ước tính, không khẳng định an toàn.
internal fun urgentCaption(daysLeft: Int): String = estimateDaysText(daysLeft)

// Chữ lớn từ 1.3 thì dải gợi ý món cuộn ngang để tên món không bị cắt.
internal fun recipeStripScrolls(fontScale: Float): Boolean = fontScale >= 1.3f

internal const val homeRecipeLimit = 3

internal fun pantryCardDescription(count: Int, zones: PantryZoneCounts): String =
    if (count == 0) {
        "Tủ đang trống. Chạm để chọn món"
    } else {
        "Trong tủ hiện có $count nguyên liệu, ${zones.fridge} ở ngăn mát, ${zones.freezer} ở ngăn đông. Chạm để xem tủ"
    }

internal enum class PantryOverviewBucket { FRIDGE, FREEZER, URGENT }

@Composable
internal fun PantryOverviewSection(
    totalCount: Int,
    fridgeCount: Int,
    freezerCount: Int,
    urgentCount: Int,
    onOpenPantry: () -> Unit,
    onSelectBucket: (PantryOverviewBucket) -> Unit,
) {
    val colors = AppTheme.colors
    val animatedTotalCount by animateIntAsState(
        targetValue = totalCount,
        animationSpec = tween(350),
        label = "pantryCount",
    )
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onOpenPantry),
        shape = RoundedCornerShape(22.dp),
        color = colors.primary,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Trong tủ hiện có", style = MaterialTheme.typography.bodyMedium, color = colors.onPrimary)
                Text("$animatedTotalCount nguyên liệu", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = colors.onPrimary)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PantryBucketButton("Ngăn mát", fridgeCount, colors.primary, Modifier.weight(1f)) { onSelectBucket(PantryOverviewBucket.FRIDGE) }
                PantryBucketButton("Ngăn đông", freezerCount, colors.primary, Modifier.weight(1f)) { onSelectBucket(PantryOverviewBucket.FREEZER) }
                PantryBucketButton("Dùng ngay", urgentCount, colors.cta, Modifier.weight(1f), isUrgent = true) {
                    onSelectBucket(PantryOverviewBucket.URGENT)
                }
            }
        }
    }
}

@Composable
private fun PantryBucketButton(
    label: String,
    count: Int,
    circleColor: Color,
    modifier: Modifier = Modifier,
    isUrgent: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = tween(350),
        label = "pantryCount",
    )
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.94f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "bucketPress",
    )
    val animatorsEnabled = remember { Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled() }
    val circleModifier = if (isUrgent && count > 0 && animatorsEnabled) {
        val pulse = rememberInfiniteTransition(label = "urgentPulse")
        val urgentScale by pulse.animateFloat(
            1f,
            1.05f,
            infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "urgentScale",
        )
        Modifier.graphicsLayer {
            scaleX = urgentScale
            scaleY = urgentScale
        }
    } else {
        Modifier
    }
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier
            .semantics { role = Role.Button }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(16.dp), color = colors.surface,
        shadowElevation = 4.dp,
    ) {
        Column(Modifier.padding(vertical = 10.dp, horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(shape = CircleShape, color = circleColor, shadowElevation = 3.dp, modifier = circleModifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(animatedCount.toString(), color = if (circleColor == colors.cta) colors.onCta else colors.onPrimary, fontWeight = FontWeight.ExtraBold)
                }
            }
            Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.text, maxLines = 1)
        }
    }
}

@Composable
internal fun PantryBucketPopupDialog(bucket: PantryOverviewBucket, items: List<PantryCell>, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    val title = when (bucket) {
        PantryOverviewBucket.FRIDGE -> "Ngăn mát"
        PantryOverviewBucket.FREEZER -> "Ngăn đông"
        PantryOverviewBucket.URGENT -> "Cần dùng ngay"
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth(0.92f).heightIn(max = 560.dp), shape = RoundedCornerShape(24.dp), color = colors.surface) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.text)
                        Text("${items.size} thực phẩm · Hạn là ước tính", style = MaterialTheme.typography.bodySmall, color = colors.muted)
                    }
                    IconButton(onClick = onDismiss) { Icon(AppIcons.Close, contentDescription = "Đóng", tint = colors.text) }
                }
                if (items.isEmpty()) {
                    Text("Chưa có thực phẩm nào trong mục này.", style = MaterialTheme.typography.bodyMedium, color = colors.muted)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(items, key = { it.ingredientId }) { item ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                FoodThumb(item.ingredientId, size = 44.dp, shape = RoundedCornerShape(12.dp), urgent = bucket == PantryOverviewBucket.URGENT)
                                Column(Modifier.weight(1f)) {
                                    Text(item.nameVi, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.text)
                                    Text(estimateDaysText(item.daysLeft ?: 0), style = MaterialTheme.typography.bodySmall, color = if ((item.daysLeft ?: 0) <= 2) colors.warnText else colors.muted)
                                }
                                if (item.quantity > 0) Text("${item.quantity} ${item.unit}", style = MaterialTheme.typography.bodySmall, color = colors.text)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Mục "Nên dùng trước": một thẻ cam nhạt gom các món sắp hết hạn ước tính.
@Composable
internal fun UseSoonSection(items: List<PantryCell>, onOpenPantry: () -> Unit) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Nên dùng trước",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = colors.text,
            )
            TextButton(onClick = onOpenPantry, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Xem tủ", color = colors.accentText, fontWeight = FontWeight.SemiBold)
                Icon(AppIcons.Chevron, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(18.dp))
            }
        }
        if (items.isEmpty()) {
            Text(
                text = "Chưa có món nào cần ưu tiên dùng sớm",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.muted,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(role = Role.Button, onClick = onOpenPantry),
                shape = RoundedCornerShape(20.dp),
                color = colors.warnSoft,
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Hạn là ước tính",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = colors.warnText,
                    )
                    items.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FoodThumb(ingredientId = item.ingredientId, size = 36.dp, shape = CircleShape, urgent = true)
                            Column {
                                Text(
                                    text = item.nameVi,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.text,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = urgentCaption(item.daysLeft ?: 0),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.warnText,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Gợi ý bữa cơm: danh sách hàng (ảnh, tên, thời gian, nhãn), đúng thứ tự Figma.
@Composable
internal fun RecommendationSection(
    recommendations: List<RecipeRecommendation>,
    onNavigateToRecipe: (String) -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToPantry: () -> Unit,
) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Gợi ý bữa cơm hôm nay",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = colors.text,
            )
            TextButton(onClick = onNavigateToRecipes, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Xem thêm", color = colors.accentText, fontWeight = FontWeight.SemiBold)
            }
        }
        if (recommendations.isEmpty()) {
            Text(
                text = emptyRecommendationCopy.message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.muted,
            )
            TextButton(onClick = onNavigateToPantry, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(emptyRecommendationCopy.actionLabel)
            }
        } else {
            recommendations.forEach { r ->
                HomeRecipeRow(r.recipe, r.reason) { onNavigateToRecipe(r.recipe.id) }
            }
        }
    }
}

@Composable
internal fun DailySpotlightRecommendationSection(
    hasRecommendations: Boolean,
    activeSuggestion: RecipeRecommendation?,
    onDismissSuggestion: (String) -> Unit,
    onResetDismissedSuggestions: () -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToPantry: () -> Unit,
) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Gợi ý bữa cơm hôm nay", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = colors.text)
            TextButton(onClick = onNavigateToRecipes) { Text("Xem thêm", color = colors.accentText) }
        }
        if (!hasRecommendations) {
            Text(emptyRecommendationCopy.message, style = MaterialTheme.typography.bodyMedium, color = colors.muted)
            TextButton(onClick = onNavigateToPantry) { Text("Xem tủ lạnh", color = colors.accentText) }
        } else {
            AnimatedContent(
                targetState = activeSuggestion?.recipe?.id,
                transitionSpec = {
                    (slideInHorizontally(tween(260)) { it / 3 } + fadeIn(tween(240))) togetherWith
                        (slideOutHorizontally(tween(220)) { -it / 3 } + fadeOut(tween(180)))
                },
                label = "dailySpotlight",
            ) { suggestionId ->
                val isCurrentSuggestion = activeSuggestion?.recipe?.id == suggestionId
                val currentSuggestion = activeSuggestion?.takeIf { isCurrentSuggestion }
                val lastSuggestion = remember(suggestionId) { mutableStateOf(currentSuggestion) }
                // Giữ nội dung của thẻ trong lúc thẻ trượt ra.
                SideEffect {
                    if (currentSuggestion != null) lastSuggestion.value = currentSuggestion
                }
                val suggestion = currentSuggestion ?: lastSuggestion.value
                if (suggestionId == null) {
                    Surface(shape = RoundedCornerShape(20.dp), color = colors.surface, border = BorderStroke(1.dp, colors.line)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Bạn đã ẩn các gợi ý vừa rồi", Modifier.weight(1f), color = colors.muted)
                            TextButton(onClick = onResetDismissedSuggestions, enabled = isCurrentSuggestion) {
                                Text("Xem lại", color = colors.accentText)
                            }
                        }
                    }
                } else if (suggestion != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable(enabled = isCurrentSuggestion, role = Role.Button) {
                            onNavigateToRecipe(suggestion.recipe.id)
                        },
                        shape = RoundedCornerShape(22.dp), color = colors.surface, shadowElevation = 6.dp, border = BorderStroke(1.dp, colors.line),
                    ) {
                        Column {
                            Box(Modifier.fillMaxWidth().height(176.dp)) {
                                RecipeThumb(suggestion.recipe.id, Modifier.fillMaxSize(), 176.dp, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                                Text(shortReasonLabel(suggestion.reason), Modifier.align(Alignment.TopStart).padding(12.dp).clip(RoundedCornerShape(999.dp)).background(colors.surface.copy(alpha = 0.92f)).padding(horizontal = 10.dp, vertical = 6.dp), color = colors.text, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                                Surface(Modifier.align(Alignment.TopEnd).padding(8.dp), shape = CircleShape, color = colors.surface.copy(alpha = 0.90f), shadowElevation = 2.dp) {
                                    IconButton(
                                        onClick = { onDismissSuggestion(suggestion.recipe.id) },
                                        enabled = isCurrentSuggestion,
                                        modifier = Modifier.size(40.dp),
                                    ) {
                                        Icon(AppIcons.Close, contentDescription = "Bỏ qua món này", modifier = Modifier.size(18.dp), tint = colors.text)
                                    }
                                }
                            }
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(suggestion.recipe.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(spotlightSubtitle(suggestion), style = MaterialTheme.typography.bodyMedium, color = colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun PantrySummaryCard(
    count: Int,
    zones: PantryZoneCounts,
    onOpenPantry: () -> Unit,
    onOpenShopping: () -> Unit,
) {
    val colors = AppTheme.colors
    val stacked = summaryButtonBelowText(LocalDensity.current.fontScale)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .semantics(mergeDescendants = true) { contentDescription = pantryCardDescription(count, zones) }
            .clickable(role = Role.Button, onClick = if (count == 0) onOpenShopping else onOpenPantry),
        shape = RoundedCornerShape(20.dp),
        color = colors.primary,
    ) {
        if (count == 0) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    HomeEmptyFridgeCopy.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.onPrimary,
                )
                Text(
                    HomeEmptyFridgeCopy.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onPrimary,
                )
                ChunkyButton(
                    onClick = onOpenShopping,
                    container = colors.surface,
                    edge = colors.primaryEdge,
                    minHeight = 52.dp,
                ) {
                    Text(HomeEmptyFridgeCopy.chooseAction, color = colors.primary, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            val countBlock: @Composable () -> Unit = {
                Column {
                    Text("Trong tủ hiện có", style = MaterialTheme.typography.bodySmall, color = colors.onPrimary)
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "$count",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.onPrimary,
                        )
                        Text(
                            "nguyên liệu",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onPrimary,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                }
            }
            val stats: @Composable () -> Unit = {
                Text(
                    "${zones.fridge} Ngăn mát · ${zones.freezer} Ngăn đông",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.onPrimary,
                )
            }
            if (stacked) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    countBlock()
                    stats()
                }
            } else {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    countBlock()
                    stats()
                }
            }
        }
    }
}

// Hàng gợi ý: ảnh món, tên, thời gian và nhãn; cả hàng là một nút mở công thức.
@Composable
private fun HomeRecipeRow(recipe: Recipe, reason: RecommendationReason, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val (tagBg, tagFg) = reasonColors(reason)
    val time = recipe.minutes?.let { "$it phút" } ?: "Thời gian chưa rõ"
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            RecipeThumb(recipeId = recipe.id, size = 58.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    recipe.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(time, style = MaterialTheme.typography.bodySmall, color = colors.muted)
                val tag = shortReasonLabel(reason)
                if (tag.isNotEmpty()) {
                    Text(
                        tag,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = tagFg,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(tagBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
            Icon(AppIcons.Chevron, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun reasonColors(reason: RecommendationReason): Pair<Color, Color> {
    val colors = AppTheme.colors
    return when (reason) {
        RecommendationReason.USE_SOON -> colors.warnSoft to colors.warnText
        RecommendationReason.QUICK -> colors.beigeSoft to colors.beigeText
        RecommendationReason.READY, RecommendationReason.OTHER -> colors.primarySoft to colors.text
    }
}

// Nút lớn vào thực đơn cả tuần, đặt cuối phần chính như Figma.
@Composable
internal fun WeeklyPlanButton(onClick: () -> Unit) {
    val colors = AppTheme.colors
    ChunkyButton(
        onClick = onClick,
        container = colors.primary,
        edge = colors.primaryEdge,
        modifier = Modifier.fillMaxWidth(),
        minHeight = 60.dp,
    ) {
        Icon(AppIcons.Calendar, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(24.dp))
        Text(
            text = WEEKLY_BUTTON_LABEL,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.onPrimary,
            modifier = Modifier.weight(1f),
        )
        Icon(AppIcons.Chevron, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(20.dp))
    }
}
