package com.loitu.yourrefri.feature.settings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.recipe.logic.DishStyle
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.recipe.logic.TasteFlavor
import com.loitu.yourrefri.ui.theme.AppIcons
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
    "ca-basa" to "Cá",
    "thit-bo" to "Thịt bò",
    "dau-phu" to "Đậu phụ",
    "nam" to "Nấm",
    "sua-tuoi" to "Sữa",
)

private fun shortDishLabel(style: DishStyle): String = when (style) {
    DishStyle.BRAISED -> "Kho"
    DishStyle.SOUP -> "Canh"
    DishStyle.STIR_FRY -> "Xào"
    DishStyle.FRIED -> "Chiên"
    DishStyle.STEAMED_BOILED -> "Hấp/luộc"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FamilyTasteSettingsCard(
    profile: FamilyTasteProfile,
    onProfileChange: (FamilyTasteProfile) -> Unit,
) {
    val colors = AppTheme.colors
    var expanded by remember { mutableStateOf(false) }
    var draft by remember(profile) { mutableStateOf(profile) }
    var savedNotice by remember { mutableStateOf(false) }
    val hasChanges = draft != profile

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Khẩu vị gia đình",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.text,
                    )
                    Text(
                        text = tasteProfileSummaryVi(profile),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    imageVector = if (expanded) AppIcons.ChevronUp else AppIcons.ChevronDown,
                    contentDescription = null,
                    tint = colors.muted,
                    modifier = Modifier.size(20.dp),
                )
            }

            if (expanded) {
                HorizontalDivider(color = colors.line)
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TasteChipGroup("Vị thích") {
                        TasteFlavor.entries.forEach { flavor ->
                            SelectChip(
                                text = flavor.labelVi,
                                selected = flavor in draft.flavors,
                                onClick = { draft = toggleFlavor(draft, flavor); savedNotice = false },
                            )
                        }
                    }
                    TasteChipGroup("Món thích") {
                        DishStyle.entries.forEach { style ->
                            SelectChip(
                                text = shortDishLabel(style),
                                selected = style in draft.dishStyles,
                                onClick = { draft = toggleDishStyle(draft, style); savedNotice = false },
                            )
                        }
                    }
                    TasteChipGroup("Không ăn") {
                        avoidIngredients.forEach { (id, label) ->
                            SelectChip(
                                text = label,
                                selected = id in draft.avoidIngredientIds,
                                onClick = { draft = toggleAvoidIngredient(draft, id); savedNotice = false },
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Nhà có trẻ nhỏ", modifier = Modifier.weight(1f), color = colors.text)
                        Switch(
                            checked = draft.hasYoungKids,
                            onCheckedChange = { draft = draft.copy(hasYoungKids = it); savedNotice = false },
                            colors = SwitchDefaults.colors(checkedThumbColor = colors.onPrimary, checkedTrackColor = colors.primary),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = {
                                onProfileChange(draft)
                                savedNotice = true
                            },
                            enabled = hasChanges,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary,
                            ),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        ) {
                            Text(if (hasChanges) "Lưu khẩu vị" else "Đã lưu", fontWeight = FontWeight.Bold)
                        }
                        if (!draft.isEmpty) {
                            TextButton(
                                onClick = { draft = FamilyTasteProfile(); savedNotice = false },
                                modifier = Modifier.heightIn(min = 48.dp),
                            ) {
                                Text("Đặt lại", color = colors.muted)
                            }
                        }
                    }
                    if (savedNotice && !hasChanges) {
                        Text(
                            text = "Đã lưu khẩu vị gia đình.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.accentText,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TasteChipGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = AppTheme.colors.muted)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}
