package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

// Chữ và quy tắc trình bày thuần của tab "Món ăn" (theo khung Figma "Gợi ý hôm nay").
internal const val RECIPE_DAILY_SUBTITLE = "Ba gợi ý nhỏ, bữa cơm bớt đắn đo."
internal const val ALTERNATIVE_EYEBROW = "HAY LÀ MÓN NÀY?"
internal const val RECIPE_FOOTER_NOTE = "Gợi ý ưu tiên món nấu được ngay từ tủ."
internal const val VIEW_COOK_ACTION = "Xem cách nấu"

internal fun recipeEyebrow(count: Int): String = if (count > 0) "MÓN ĂN · $count CÔNG THỨC" else "MÓN ĂN"

internal fun recipeHeadline(mode: RecipeScreenMode): String = when (mode) {
    RecipeScreenMode.DAILY -> "Hôm nay ăn gì?"
    RecipeScreenMode.WEEKLY -> "Thực đơn tuần này"
    RecipeScreenMode.SHOPPING -> "Giỏ đi chợ lẻ"
}

internal fun heroEyebrow(index: Int, reason: RecommendationReason): String {
    val label = when (reason) {
        RecommendationReason.USE_SOON -> "Dùng đồ cần dùng sớm"
        RecommendationReason.READY -> "Nấu được ngay"
        RecommendationReason.QUICK -> "Một món nhanh"
        RecommendationReason.OTHER -> "Gợi ý cho gia đình"
    }
    return "${(index + 1).toString().padStart(2, '0')} · ${label.uppercase()}"
}

internal fun recipeMetaLine(recipe: Recipe): String =
    "${heroMinutesLabel(recipe.minutes)} · ${difficultyLabel(recipe)}"

internal fun ingredientCoverageLabel(have: Int, total: Int): String = when {
    total <= 0 -> ""
    have >= total -> "Có đủ $total/$total nguyên liệu chính"
    else -> "Có $have/$total nguyên liệu chính"
}

internal fun dietChipOptions(): List<DietRestriction> = DietRestriction.entries.filter { it != DietRestriction.NONE }

internal fun nextDietRestriction(current: DietRestriction, tapped: DietRestriction): DietRestriction =
    if (current == tapped) DietRestriction.NONE else tapped

internal fun dietSelectionNote(restriction: DietRestriction): String? =
    if (restriction == DietRestriction.NONE) null else "Đang chọn: ${restriction.labelVi.lowercase()}"

internal fun alternativeRecipe(recipes: List<Recipe>, excludeIds: Set<String>, offset: Int): Recipe? {
    val candidates = recipes.filter { it.id !in excludeIds }.distinctBy { it.id }
    if (candidates.isEmpty()) return null
    return candidates[Math.floorMod(offset, candidates.size)]
}

internal fun alternativeStatusLabel(canCookNow: Boolean): String =
    if (canCookNow) "Có đủ nguyên liệu" else "Cần thêm vài thứ nữa"

/** Mục "Thực đơn tuần này" + nút chữ "Xem chi tiết" và dải 7 ngày T2..CN. */
@Composable
internal fun WeekStripSection(
    chips: List<WeekDayChip>,
    onOpenWeek: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = WEEK_SECTION_TITLE,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onOpenWeek, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(WEEK_SECTION_ACTION, color = colors.accentText, fontWeight = FontWeight.SemiBold)
                Icon(AppIcons.Chevron, contentDescription = null, tint = colors.accentText, modifier = Modifier.padding(start = 2.dp).size(16.dp))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            chips.forEach { chip -> WeekDayPill(chip, onClick = onOpenWeek, modifier = Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun WeekDayPill(chip: WeekDayChip, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(24.dp)
    val bg = if (chip.isToday) colors.cta else colors.surface
    val fg = if (chip.isToday) colors.onCta else colors.text
    Column(
        modifier = modifier
            .heightIn(min = 72.dp)
            .clip(shape)
            .background(bg)
            .then(if (chip.isToday) Modifier else Modifier.border(1.dp, colors.line, shape))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = weekDayChipDescription(chip) }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        Text(chip.label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = fg)
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (chip.hasMeal) (if (chip.isToday) colors.onCta else colors.primarySoft) else colors.surfaceSoft),
            contentAlignment = Alignment.Center,
        ) {
            if (chip.hasMeal) {
                Icon(
                    AppIcons.Check,
                    contentDescription = null,
                    tint = if (chip.isToday) colors.cta else colors.accentText,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

/** Thẻ lối tắt: icon trong vòng tròn, tiêu đề, phụ đề (tối đa 2 dòng phụ) và chevron. */
@Composable
internal fun ShortcutCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    extraLine: String? = null,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.line, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.primarySoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.text)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.muted)
            if (extraLine != null) {
                Text(extraLine, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = colors.accentText)
            }
        }
        Icon(AppIcons.Chevron, contentDescription = null, tint = colors.muted, modifier = Modifier.size(20.dp))
    }
}
