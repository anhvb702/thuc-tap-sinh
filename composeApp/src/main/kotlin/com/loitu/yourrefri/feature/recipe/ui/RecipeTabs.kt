package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

/** Tiêu đề màn: icon nồi + "Món ăn & Thực đơn". */
@Composable
internal fun RecipeScreenTitle(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(AppIcons.Pot, contentDescription = null, tint = colors.brand, modifier = Modifier.size(28.dp))
        Text(
            text = RECIPE_SCREEN_TITLE,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
        )
    }
}

/** Chip viên thuốc dùng chung cho tab và bộ lọc thời gian. Chọn = nền primary; chưa chọn = surface viền line. */
@Composable
internal fun PillChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val colors = AppTheme.colors
    Surface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) colors.primary else colors.surface,
        border = BorderStroke(1.dp, if (selected) colors.primary else colors.line),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = if (selected) colors.onPrimary else colors.muted, modifier = Modifier.size(20.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (selected) colors.onPrimary else colors.text,
            )
        }
    }
}

private fun tabIcon(mode: RecipeScreenMode): ImageVector = when (mode) {
    RecipeScreenMode.DAILY -> AppIcons.ChefHat
    RecipeScreenMode.WEEKLY -> AppIcons.Calendar
    RecipeScreenMode.SHOPPING -> AppIcons.Cart
}

/** Hàng chip "Gợi ý hôm nay | Thực đơn tuần | Đi chợ"; cuộn ngang được khi chữ lớn. */
@Composable
internal fun RecipeTabChips(selected: RecipeScreenMode, onSelect: (RecipeScreenMode) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        recipeTabSpecs().forEach { tab ->
            PillChip(
                label = tab.label,
                selected = tab.mode == selected,
                onClick = { onSelect(tab.mode) },
                icon = tabIcon(tab.mode),
            )
        }
    }
}
