package com.loitu.yourrefri.feature.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.home.logic.homeGreeting
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.feature.pantry.ui.isFreezerItem
import com.loitu.yourrefri.feature.cooklog.ui.CookHistoryCopy
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import com.loitu.yourrefri.feature.recipe.logic.recommendRecipes
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.logic.defaultMealFor
import com.loitu.yourrefri.feature.recipe.logic.recipesForMeal
import com.loitu.yourrefri.feature.recipe.ui.MealCopy
import com.loitu.yourrefri.feature.recipe.ui.PillChip
import com.loitu.yourrefri.feature.recipe.ui.ShortcutCard
import com.loitu.yourrefri.feature.recipe.ui.WEEKLY_BUTTON_LABEL
import com.loitu.yourrefri.feature.recipe.ui.emptyRecommendationCopy
import com.loitu.yourrefri.feature.recipe.ui.shortReasonLabel
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
internal fun shortEstimateText(daysLeft: Int): String = when {
    daysLeft < 0 -> "Kiểm tra lại giúp mình"
    daysLeft == 0 -> "Nên dùng hôm nay"
    else -> "Khoảng còn $daysLeft ngày"
}

// Số món đang có theo khu vực bảo quản (ngăn mát / ngăn đông).
internal data class PantryZoneCounts(val fridge: Int, val freezer: Int)

internal fun pantryZoneCounts(haveIds: List<String>): PantryZoneCounts {
    val freezer = haveIds.count { isFreezerItem(it) }
    return PantryZoneCounts(fridge = haveIds.size - freezer, freezer = freezer)
}

// Dòng chữ dưới tên món cần dùng sớm; luôn là ước tính, không khẳng định an toàn.
internal fun urgentCaption(daysLeft: Int): String = when {
    daysLeft < 0 -> "Kiểm tra lại giúp mình"
    daysLeft == 0 -> "Nên dùng hôm nay (ước tính)"
    else -> "Ước tính còn $daysLeft ngày"
}

// Chữ lớn từ 1.3 thì dải gợi ý món cuộn ngang để tên món không bị cắt.
internal fun recipeStripScrolls(fontScale: Float): Boolean = fontScale >= 1.3f

internal const val homeRecipeLimit = 3

internal fun pantryCardDescription(count: Int, zones: PantryZoneCounts): String =
    if (count == 0) {
        "Tủ đang trống. Chạm để chọn món"
    } else {
        "Trong tủ hiện có $count nguyên liệu, ${zones.fridge} ở ngăn mát, ${zones.freezer} ở ngăn đông. Chạm để xem tủ"
    }

@Composable
fun HomeScreen(
    state: PantryGridState,
    recipes: List<Recipe>,
    onNavigateToPantry: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    onNavigateToWeeklyRecipes: () -> Unit = onNavigateToRecipes,
    // Lối vào "Đã nấu"; null thì ẩn.
    onNavigateToHistory: (() -> Unit)? = null,
    // Bữa đang chọn (do MainScaffold giữ để lịch sử "Đã nấu" ghi đúng bữa này).
    meal: MealSlot = defaultMealFor(state.hourOfDay),
    onMealChange: (MealSlot) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val haveItems = cellsOf(state).filter { it.have }
    val zones = pantryZoneCounts(haveItems.map { it.ingredientId })
    val urgentItems = haveItems.filter { it.daysLeft in 0..2 }.sortedBy { it.daysLeft }
    // Bữa trưa ưu tiên món nhanh; bữa tối giữ nguyên danh sách.
    val recommendations = recommendRecipes(recipesForMeal(recipes, meal), state).take(homeRecipeLimit)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = greetingText(state.hourOfDay),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
        )
        Text(
            text = MealCopy.heading(meal),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
        )
        // Chọn bữa: bữa trưa ưu tiên món nhanh, bữa tối như cũ; bữa này cũng là bữa ghi vào "Đã nấu".
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 14.dp)) {
            MealSlot.entries.forEach { option ->
                PillChip(label = MealCopy.label(option), selected = option == meal, onClick = { onMealChange(option) })
            }
        }

        WeeklyPlanButton(onClick = onNavigateToWeeklyRecipes)
        Spacer(modifier = Modifier.height(16.dp))

        PantrySummaryCard(count = haveItems.size, zones = zones, onOpenPantry = onNavigateToPantry)

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(title = "Cần dùng sớm", action = "Xem tất cả", onAction = onNavigateToPantry)
        if (urgentItems.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.line),
            ) {
                Text(
                    text = "Chưa có món nào cần ưu tiên dùng sớm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.accentText,
                    modifier = Modifier.padding(16.dp),
                )
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(urgentItems, key = { it.ingredientId }) { item ->
                    UrgentCard(
                        ingredientId = item.ingredientId,
                        name = item.nameVi,
                        caption = urgentCaption(item.daysLeft ?: 0),
                        onClick = onNavigateToPantry,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(title = "Gợi ý bữa cơm hôm nay", action = "Xem thêm", onAction = onNavigateToRecipes)
        if (recommendations.isEmpty()) {
            Text(
                text = emptyRecommendationCopy.message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.muted,
            )
            TextButton(onClick = onNavigateToPantry, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(emptyRecommendationCopy.actionLabel)
            }
        } else if (recipeStripScrolls(LocalDensity.current.fontScale)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(recommendations, key = { it.recipe.id }) { r ->
                    HomeRecipeCard(r.recipe, r.reason, Modifier.width(176.dp)) { onNavigateToRecipe(r.recipe.id) }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                recommendations.forEach { r ->
                    HomeRecipeCard(r.recipe, r.reason, Modifier.weight(1f).fillMaxHeight()) { onNavigateToRecipe(r.recipe.id) }
                }
            }
        }

        if (onNavigateToHistory != null) {
            Spacer(modifier = Modifier.height(16.dp))
            ShortcutCard(
                icon = AppIcons.Clock,
                title = CookHistoryCopy.title,
                subtitle = CookHistoryCopy.homeSubtitle,
                onClick = onNavigateToHistory,
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String, onAction: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onAction, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(action, color = colors.ctaText, fontWeight = FontWeight.SemiBold)
            Icon(AppIcons.Chevron, contentDescription = null, tint = colors.ctaText, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun IconBadge(icon: ImageVector, container: Color, tint: Color, size: Int = 40) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size((size * 0.55f).dp))
    }
}

// Thẻ "Trong tủ hiện có": cả thẻ là một nút mở tủ.
@Composable
private fun PantrySummaryCard(count: Int, zones: PantryZoneCounts, onOpenPantry: () -> Unit) {
    val colors = AppTheme.colors
    val stacked = summaryButtonBelowText(LocalDensity.current.fontScale)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .semantics(mergeDescendants = true) { contentDescription = pantryCardDescription(count, zones) }
            .clickable(role = Role.Button, onClick = onOpenPantry),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceSoft,
        border = BorderStroke(1.dp, colors.primarySoft),
    ) {
        if (count == 0) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconBadge(AppIcons.Fridge, colors.primarySoft, colors.accentText)
                    Column {
                        Text(
                            "Tủ đang trống",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.text,
                        )
                        Text(
                            "Mình thêm vài món quen thuộc nhé",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.accentText,
                        )
                    }
                }
                Button(
                    onClick = onOpenPantry,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Chọn món", fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            val countBlock: @Composable () -> Unit = {
                Column {
                    Text("Trong tủ hiện có", style = MaterialTheme.typography.bodySmall, color = colors.muted)
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "$count",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.text,
                        )
                        Text(
                            "nguyên liệu",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.muted,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                }
            }
            val stats: @Composable () -> Unit = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ZoneStat(AppIcons.Fridge, zones.fridge, "Ngăn mát")
                    ZoneStat(AppIcons.Snowflake, zones.freezer, "Ngăn đông")
                }
            }
            if (stacked) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    countBlock()
                    stats()
                }
            } else {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(modifier = Modifier.weight(1f)) { countBlock() }
                    stats()
                }
            }
        }
    }
}

@Composable
private fun ZoneStat(icon: ImageVector, value: Int, label: String) {
    val colors = AppTheme.colors
    Surface(shape = RoundedCornerShape(14.dp), color = colors.surface, border = BorderStroke(1.dp, colors.line)) {
        Column(
            modifier = Modifier
                .widthIn(min = 76.dp)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(icon, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(18.dp))
                Text(
                    "$value",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
            }
            Text(label, style = MaterialTheme.typography.bodySmall, color = colors.muted)
        }
    }
}

@Composable
private fun UrgentCard(ingredientId: String, name: String, caption: String, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Surface(
        modifier = Modifier
            .width(112.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FoodThumb(ingredientId = ingredientId, size = 64.dp, shape = RoundedCornerShape(14.dp), urgent = true)
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                caption,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = colors.warnText,
            )
        }
    }
}

@Composable
private fun HomeRecipeCard(recipe: Recipe, reason: RecommendationReason, modifier: Modifier, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val (tagBg, tagFg) = reasonColors(reason)
    val time = recipe.minutes?.let { "$it phút" } ?: "Thời gian chưa rõ"
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            RecipeThumb(recipeId = recipe.id, size = 64.dp)
            Text(
                recipe.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(AppIcons.Clock, contentDescription = null, tint = colors.muted, modifier = Modifier.size(14.dp))
                Text(time, style = MaterialTheme.typography.bodySmall, color = colors.muted)
            }
            val tag = shortReasonLabel(reason)
            if (tag.isNotEmpty()) {
                Surface(shape = RoundedCornerShape(999.dp), color = tagBg) {
                    Text(
                        tag,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = tagFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
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

// Nút lớn vào thực đơn cả tuần, đặt ngay dưới phần chào để dễ thấy.
@Composable
private fun WeeklyPlanButton(onClick: () -> Unit) {
    val colors = AppTheme.colors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = colors.primary,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(AppIcons.Calendar, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(28.dp))
            Text(
                text = WEEKLY_BUTTON_LABEL,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.onPrimary,
                modifier = Modifier.weight(1f),
            )
            Icon(AppIcons.Chevron, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(20.dp))
        }
    }
}
