package com.loitu.yourrefri.feature.pantry.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.ui.components.FoodThumb
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

data class GroceryCombo(val id: String, val titleVi: String, val subtitleVi: String, val ingredientIds: List<String>)

internal val GROCERY_COMBOS = listOf(
    GroceryCombo("weekly", "Đi chợ tuần cơ bản", "Mâm cơm gọn cho ngày thường", listOf("thit-heo", "trung-ga", "rau-muong", "ca-chua", "hanh-la")),
    GroceryCombo("spice", "Bổ sung gia vị bếp nhà", "Những món hay dùng", listOf("nuoc-mam", "dau-an", "toi")),
    GroceryCombo("weekend", "Cuối tuần đổi vị", "Thêm chút mới cho bữa nhà", listOf("suon-heo", "muc", "dua-leo")),
)

internal val LEFTOVER_PRESETS = listOf("Thịt kho trứng (hộp dư)", "Canh rau (tô dư)", "Cơm nguội (hộp mát)")

internal fun comboActionsFor(combo: GroceryCombo, state: PantryGridState): List<PantryGridAction> = combo.ingredientIds.mapNotNull { id ->
    if (state.ingredients.none { it.id == id }) null
    else if (state.pantry[id]?.have == true) PantryGridAction.IncreaseQuantity(id)
    else PantryGridAction.AddIngredient(id, null)
}

internal fun clearAllPantryActions(state: PantryGridState): List<PantryGridAction.Tap> =
    state.ingredients.mapNotNull { ingredient ->
        ingredient.id.takeIf { state.pantry[it]?.have == true }?.let(PantryGridAction::Tap)
    }

internal data class ComboIngredientItem(val id: String, val nameVi: String, val alreadyHave: Boolean)

internal fun comboItemsFor(combo: GroceryCombo, state: PantryGridState): List<ComboIngredientItem> =
    combo.ingredientIds.mapNotNull { id ->
        state.ingredients.firstOrNull { it.id == id }?.let { ingredient ->
            ComboIngredientItem(id, ingredient.nameVi, state.pantry[id]?.have == true)
        }
    }

internal fun comboActionsForSelected(
    combo: GroceryCombo,
    selectedIds: Set<String>,
    state: PantryGridState,
): List<PantryGridAction> =
    comboActionsFor(combo.copy(ingredientIds = combo.ingredientIds.filter { it in selectedIds }), state)

internal fun comboChipLabel(item: ComboIngredientItem): String =
    if (item.alreadyHave) "${item.nameVi} +1" else item.nameVi

internal fun comboConfirmLabel(selectedCount: Int): String =
    if (selectedCount > 0) "Thêm $selectedCount món vào tủ" else "Chọn ít nhất 1 món"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun GroceryComboSheet(
    combos: List<GroceryCombo>,
    state: PantryGridState,
    onConfirm: (GroceryCombo, Set<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    val firstCombo = combos.firstOrNull() ?: return
    val colors = AppTheme.colors
    var activeCombo by remember(combos) { mutableStateOf(firstCombo) }
    var deselectedIds by remember(activeCombo) { mutableStateOf(emptySet<String>()) }
    val items = comboItemsFor(activeCombo, state)
    val selectedIds = items.map { it.id }.toSet() - deselectedIds
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = colors.surface,
        labelColor = colors.text,
        selectedContainerColor = colors.primary,
        selectedLabelColor = colors.onPrimary,
        selectedTrailingIconColor = colors.onPrimary,
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = colors.background,
        contentColor = colors.text,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Thêm nhanh gói đi chợ", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(AppIcons.Close, contentDescription = "Đóng", tint = colors.text)
                }
            }
            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FlowRow(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    combos.forEach { combo ->
                        FilterChip(
                            selected = activeCombo == combo,
                            onClick = { activeCombo = combo },
                            label = { Text(combo.titleVi) },
                            modifier = Modifier.heightIn(min = 48.dp),
                            colors = chipColors,
                        )
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.forEach { item ->
                        val selected = item.id in selectedIds
                        FilterChip(
                            selected = selected,
                            onClick = { deselectedIds = toggleQuickAddSelection(deselectedIds, item.id) },
                            label = { Text(comboChipLabel(item)) },
                            leadingIcon = { FoodThumb(item.id, size = 24.dp, shape = CircleShape) },
                            trailingIcon = if (selected) {
                                { Icon(AppIcons.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.heightIn(min = 48.dp),
                            colors = chipColors,
                        )
                    }
                }
            }
            Button(
                onClick = { onConfirm(activeCombo, selectedIds) },
                enabled = selectedIds.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
            ) {
                Text(comboConfirmLabel(selectedIds.size))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun QuickLeftoverDialog(onSave: (String) -> Unit, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text("Cất món thừa", color = colors.text) },
        text = {
            androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Mình sẽ ghi hạn dùng ước tính khoảng 2 ngày.", color = colors.muted)
                OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text("Tên món") }, modifier = Modifier.fillMaxWidth())
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LEFTOVER_PRESETS.forEach { preset ->
                        FilterChip(selected = name == preset, onClick = { name = preset }, label = { Text(preset) }, modifier = Modifier.heightIn(min = 48.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name.trim()) }, enabled = name.trim().isNotEmpty(), modifier = Modifier.heightIn(min = 48.dp)) { Text("Cất vào Đồ ăn thừa") }
        },
        dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) { Text("Để sau", color = colors.muted) } },
    )
}
