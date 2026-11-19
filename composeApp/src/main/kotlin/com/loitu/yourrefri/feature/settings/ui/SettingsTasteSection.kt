package com.loitu.yourrefri.feature.settings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.recipe.logic.DishStyle
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.recipe.logic.TasteFlavor
import com.loitu.yourrefri.ui.theme.AppTheme

internal fun toggleFlavor(profile: FamilyTasteProfile, flavor: TasteFlavor): FamilyTasteProfile =
    profile.copy(flavors = profile.flavors.toggle(flavor))

internal fun toggleDishStyle(profile: FamilyTasteProfile, style: DishStyle): FamilyTasteProfile =
    profile.copy(dishStyles = profile.dishStyles.toggle(style))

internal fun toggleAvoidIngredient(profile: FamilyTasteProfile, id: String): FamilyTasteProfile =
    if (id.isBlank()) profile else profile.copy(avoidIngredientIds = profile.avoidIngredientIds.toggle(id))

internal fun tasteProfileSummaryVi(profile: FamilyTasteProfile): String {
    if (profile.isEmpty) return "Chưa chọn — gợi ý cân bằng mọi món nhà"
    val parts = buildList {
        if (profile.flavors.isNotEmpty()) add("Vị ${profile.flavors.joinToString(" / ") { it.labelVi.lowercase() }}")
        if (profile.dishStyles.isNotEmpty()) add(profile.dishStyles.joinToString(" / ") { it.labelVi })
        if (profile.avoidIngredientIds.isNotEmpty()) add("Không ăn ${profile.avoidIngredientIds.joinToString(", ") { ingredientLabel(it) }}")
        if (profile.hasYoungKids) add("Nhà có trẻ nhỏ")
    }
    return parts.joinToString(" · ")
}

private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value

private fun ingredientLabel(id: String): String = when (id) {
    "tom" -> "Tôm"
    "ca-basa" -> "Cá basa"
    "thit-bo" -> "Thịt bò"
    "dau-phu" -> "Đậu phụ"
    "nam" -> "Nấm"
    "sua-tuoi" -> "Sữa tươi"
    else -> id
}

private val avoidIngredients = listOf(
    "tom" to "Tôm",
    "ca-basa" to "Cá basa",
    "thit-bo" to "Thịt bò",
    "dau-phu" to "Đậu phụ",
    "nam" to "Nấm",
    "sua-tuoi" to "Sữa tươi",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FamilyTasteSettingsCard(
    profile: FamilyTasteProfile,
    onProfileChange: (FamilyTasteProfile) -> Unit,
) {
    val colors = AppTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Khẩu vị gia đình", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = colors.text)
            Text(tasteProfileSummaryVi(profile), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = colors.muted)
            TasteChipGroup("Vị ưa thích") {
                TasteFlavor.entries.forEach { flavor ->
                    SelectChip(text = flavor.labelVi, selected = flavor in profile.flavors, onClick = { onProfileChange(toggleFlavor(profile, flavor)) })
                }
            }
            TasteChipGroup("Kiểu món thích ăn") {
                DishStyle.entries.forEach { style ->
                    SelectChip(text = style.labelVi, selected = style in profile.dishStyles, onClick = { onProfileChange(toggleDishStyle(profile, style)) })
                }
            }
            TasteChipGroup("Nguyên liệu nhà không ăn") {
                avoidIngredients.forEach { (id, label) ->
                    SelectChip(text = label, selected = id in profile.avoidIngredientIds, onClick = { onProfileChange(toggleAvoidIngredient(profile, id)) })
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Nhà có trẻ nhỏ (tránh món cay)", modifier = Modifier.weight(1f), color = colors.text)
                Switch(
                    checked = profile.hasYoungKids,
                    onCheckedChange = { onProfileChange(profile.copy(hasYoungKids = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = colors.onPrimary, checkedTrackColor = colors.primary),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TasteChipGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = androidx.compose.material3.MaterialTheme.typography.labelLarge, color = AppTheme.colors.text)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}
