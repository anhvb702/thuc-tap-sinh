package com.loitu.yourrefri.feature.pantry.ui

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import com.loitu.yourrefri.feature.pantry.data.PANTRY_UNITS
import com.loitu.yourrefri.feature.pantry.logic.MAX_PANTRY_QUANTITY
import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.SHELF_DAY_OPTIONS
import com.loitu.yourrefri.ui.theme.AppTheme

@Composable
internal fun QuantityInputDialog(cell: PantryCell, onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    val colors = AppTheme.colors
    val focusRequester = remember { FocusRequester() }
    var input by remember(cell.ingredientId) {
        val initial = cell.quantity.toString()
        mutableStateOf(TextFieldValue(initial, selection = TextRange(0, initial.length)))
    }
    val quantity = parseDirectQuantityInput(input.text)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text(cell.nameVi, color = colors.text) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Số lượng (${cell.unit})") },
                supportingText = { Text("Từ 0 đến $MAX_PANTRY_QUANTITY") },
                isError = input.text.isNotBlank() && quantity == null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.line,
                    focusedTextColor = colors.text,
                    unfocusedTextColor = colors.text,
                ),
            )
            LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
        },
        confirmButton = {
            Button(
                onClick = { quantity?.let(onSave) },
                enabled = quantity != null,
                modifier = Modifier.heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
            ) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Hủy", color = colors.muted)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PantryGridDialogs(
    state: PantryGridState,
    onAction: (PantryGridAction) -> Unit,
    showClear: Boolean,
    onDismissClear: () -> Unit,
    onClearAll: () -> Unit,
    showLeftover: Boolean,
    onDismissLeftover: () -> Unit,
    onSaveLeftover: (String) -> Unit,
    showMicExplain: Boolean,
    onDismissMicExplain: () -> Unit,
    onRequestMic: () -> Unit,
    voicePhase: VoicePhase,
    voicePartial: String,
    onVoiceStop: () -> Unit,
    onVoiceCancel: () -> Unit,
    onVoiceRetry: () -> Unit,
    onVoiceManual: () -> Unit,
    showAdd: Boolean,
    addChoice: AddChoice,
    newFoodName: String,
    newFoodDays: Int?,
    selectedGroup: String?,
    selectedIds: Set<String>,
    addFoodError: Boolean,
    addFoodAlreadyPresent: Boolean,
    voiceHeard: String?,
    onName: (String) -> Unit,
    onDays: (Int?) -> Unit,
    onGroup: (String?) -> Unit,
    onToggleId: (String) -> Unit,
    onConfirmAdd: () -> Unit,
    onDismissAdd: () -> Unit,
    onCombo: (GroceryCombo) -> Unit,
    showCombo: Boolean,
    onDismissCombo: () -> Unit,
    voiceLevel: Float = 0f,
) {
    val colors = AppTheme.colors
    if (showCombo) GroceryComboDialog(GROCERY_COMBOS, onPick = onCombo, onDismiss = onDismissCombo)
    if (showClear) AlertDialog(onDismissRequest = onDismissClear, containerColor = colors.surface, title = { Text("Dọn hết tủ lạnh?", color = colors.text) }, text = { Text("Đánh dấu hết toàn bộ món đang có trong tủ? Bạn vẫn có thể Hoàn tác ngay sau đó.", color = colors.muted) }, confirmButton = { Button(onClick = { onDismissClear(); onClearAll() }, modifier = Modifier.heightIn(min = 48.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)) { Text("Dọn hết tủ") } }, dismissButton = { TextButton(onClick = onDismissClear, modifier = Modifier.heightIn(min = 48.dp)) { Text("Hủy", color = colors.muted) } })
    if (showLeftover) QuickLeftoverDialog(onSave = onSaveLeftover, onDismiss = onDismissLeftover)
    state.unitEditor?.let { editor ->
        AlertDialog(onDismissRequest = { onAction(PantryGridAction.CancelUnitEditor) }, containerColor = colors.surface, title = { Text("Đơn vị", color = colors.text) }, text = { Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) { PANTRY_UNITS.forEach { unit -> ShelfDayOption(unit, unit == editor.selectedUnit) { onAction(PantryGridAction.PickUnit(unit)) } } } }, confirmButton = { TextButton(onClick = { onAction(PantryGridAction.SaveUnitEditor) }) { Text("Lưu", color = colors.accentText) } }, dismissButton = { TextButton(onClick = { onAction(PantryGridAction.CancelUnitEditor) }) { Text("Hủy", color = colors.muted) } })
    }
    state.editor?.let { editor ->
        AlertDialog(onDismissRequest = { onAction(PantryGridAction.CancelShelfEditor) }, containerColor = colors.surface, title = { Text("Hạn ước tính", color = colors.text) }, text = { Column(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) { ShelfDayOption("Theo gợi ý mặc định", editor.selectedDays == null) { onAction(PantryGridAction.PickShelfDays(null)) }; SHELF_DAY_OPTIONS.forEach { d -> ShelfDayOption("$d ngày", editor.selectedDays == d) { onAction(PantryGridAction.PickShelfDays(d)) } } } }, confirmButton = { TextButton(onClick = { onAction(PantryGridAction.SaveShelfEditor) }) { Text("Lưu", color = colors.accentText) } }, dismissButton = { TextButton(onClick = { onAction(PantryGridAction.CancelShelfEditor) }) { Text("Hủy", color = colors.muted) } })
    }
    if (showMicExplain) AlertDialog(onDismissRequest = onDismissMicExplain, containerColor = colors.surface, title = { Text(VOICE_MIC_TITLE, color = colors.text) }, text = { Text(VOICE_MIC_EXPLAIN, color = colors.muted) }, confirmButton = { TextButton(onClick = { onDismissMicExplain(); onRequestMic() }) { Text("Tiếp tục", color = colors.accentText) } }, dismissButton = { TextButton(onClick = onDismissMicExplain) { Text("Để sau", color = colors.muted) } })
    if (voicePhase != VoicePhase.IDLE) {
        VoiceSheet(voicePhase, voicePartial, onVoiceStop, onVoiceCancel, onVoiceRetry, onVoiceManual, voiceLevel = voiceLevel)
    }
    if (showAdd) PantryAddFoodSheet(state, addChoice, newFoodName, newFoodDays, selectedGroup, selectedIds, addFoodError, addFoodAlreadyPresent, voiceHeard, onName, onDays, onGroup, onToggleId, onConfirmAdd, onDismissAdd)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PantryAddFoodSheet(state: PantryGridState, choice: AddChoice, name: String, days: Int?, group: String?, selectedIds: Set<String>, error: Boolean, alreadyPresent: Boolean, heard: String?, onName: (String) -> Unit, onDays: (Int?) -> Unit, onGroup: (String?) -> Unit, onToggleId: (String) -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    val isOther = choice == AddChoice.ADD_OTHER
    val suggestions = ingredientOnlySuggestions(quickAddSuggestions(state, name, Int.MAX_VALUE, group)).takeUnless { isOther }.orEmpty()
    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), containerColor = colors.background) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (isOther) addChoiceLabel(choice) else AddFoodSheetCopy.title, style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.text)
            Text(AddFoodSheetCopy.subtitle, color = colors.muted)
            heard?.let { Text(voiceHeardLine(it), color = colors.ctaText, modifier = Modifier.fillMaxWidth().padding(8.dp)) }
            if (!isOther) Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { quickAddGroupOptions().forEach { option -> FilterChip(selected = group == option.groupId, onClick = { onGroup(option.groupId) }, label = { Text(option.label) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = colors.primary, selectedLabelColor = colors.onPrimary)) } }
            OutlinedTextField(name, onName, placeholder = { Text(AddFoodSheetCopy.searchPlaceholder) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary, unfocusedBorderColor = colors.line, focusedTextColor = colors.text, unfocusedTextColor = colors.text))
            if (error) Text(if (alreadyPresent) AddFoodSheetCopy.alreadyPresent else PantrySearchCopy.notFound, color = colors.warnText)
            suggestions.chunked(quickAddGridColumns(LocalDensity.current.fontScale)).forEach { row -> FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { item -> QuickAddTile(item.nameVi, item.id, item.id in selectedIds, { onToggleId(item.id) }, Modifier.weight(1f)) } } }
            AddFoodEstimateSection(state, name, selectedIds, days, onDays)
            Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)) { Text(if (isOther) "Thêm vào tủ" else addSelectionLabel(selectedIds.size), fontWeight = FontWeight.Bold) }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(AddFoodSheetCopy.closeLabel, color = colors.muted) }
        }
    }
}

