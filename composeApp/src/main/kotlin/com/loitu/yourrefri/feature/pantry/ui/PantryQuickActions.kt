package com.loitu.yourrefri.feature.pantry.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
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

internal fun toggleBatchSelection(selected: Set<String>, id: String): Set<String> =
    if (id in selected) selected - id else selected + id

internal fun batchClearActions(selectedIds: Set<String>, state: PantryGridState): List<PantryGridAction> =
    state.ingredients.mapNotNull { ingredient ->
        ingredient.id.takeIf { it in selectedIds && state.pantry[it]?.have == true }?.let(PantryGridAction::Tap)
    }

@Composable
internal fun GroceryComboDialog(combos: List<GroceryCombo>, onPick: (GroceryCombo) -> Unit, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text("Combo đi chợ quen thuộc", color = colors.text) },
        text = {
            androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                combos.forEach { combo ->
                    Button(onClick = { onPick(combo) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        androidx.compose.foundation.layout.Column {
                            Text(combo.titleVi)
                            Text(combo.subtitleVi, color = colors.onPrimary.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) { Text("Để sau", color = colors.accentText) } },
    )
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
