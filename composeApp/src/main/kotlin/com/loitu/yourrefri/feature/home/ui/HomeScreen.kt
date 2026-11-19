package com.loitu.yourrefri.feature.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.loitu.yourrefri.feature.pantry.ui.estimateDaysText
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
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Nhãn nhỏ rồi đến câu hỏi chính, giống bố cục Figma "01 · Trang chủ".
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "BẾP NHÀ",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.accentText,
            )
            Text(
                text = greetingText(state.hourOfDay),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.muted,
            )
            Text(
                text = MealCopy.heading(meal),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.text,
            )
        }

        // Chọn bữa: bữa này cũng là bữa ghi vào "Đã nấu".
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MealSlot.entries.forEach { option ->
                PillChip(label = MealCopy.label(option), selected = option == meal, onClick = { onMealChange(option) })
            }
        }

        PantrySummaryCard(count = haveItems.size, zones = zones, onOpenPantry = onNavigateToPantry)

        UseSoonSection(items = urgentItems, onOpenPantry = onNavigateToPantry)

        RecommendationSection(
            recommendations = recommendations,
            onNavigateToRecipe = onNavigateToRecipe,
            onNavigateToRecipes = onNavigateToRecipes,
            onNavigateToPantry = onNavigateToPantry,
        )

        WeeklyPlanButton(onClick = onNavigateToWeeklyRecipes)

        if (onNavigateToHistory != null) {
            ShortcutCard(
                icon = AppIcons.Clock,
                title = CookHistoryCopy.title,
                subtitle = CookHistoryCopy.homeSubtitle,
                onClick = onNavigateToHistory,
            )
        }

        Text(
            text = "Chỉ trên máy bạn · Không cần tài khoản",
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
        )
    }
}

// Mục "Nên dùng trước": một thẻ cam nhạt gom các món sắp hết hạn ước tính.
@Composable
private fun UseSoonSection(items: List<com.loitu.yourrefri.feature.pantry.logic.PantryCell>, onOpenPantry: () -> Unit) {
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
private fun RecommendationSection(
    recommendations: List<com.loitu.yourrefri.feature.recipe.logic.RecipeRecommendation>,
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
private fun PantrySummaryCard(count: Int, zones: PantryZoneCounts, onOpenPantry: () -> Unit) {
    val colors = AppTheme.colors
    val stacked = summaryButtonBelowText(LocalDensity.current.fontScale)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .semantics(mergeDescendants = true) { contentDescription = pantryCardDescription(count, zones) }
            .clickable(role = Role.Button, onClick = onOpenPantry),
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
                    onClick = onOpenPantry,
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
private fun WeeklyPlanButton(onClick: () -> Unit) {
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
