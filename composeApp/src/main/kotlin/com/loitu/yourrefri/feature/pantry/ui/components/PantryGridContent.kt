package com.loitu.yourrefri.feature.pantry.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.ui.components.ChunkyButton
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

@Composable
internal fun PantryGridHeader(
    searchOpen: Boolean,
    query: String,
    focusRequester: FocusRequester,
    onToggleSearch: () -> Unit,
    onQueryChange: (String) -> Unit,
) {
    val colors = AppTheme.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(PantryScreenCopy.eyebrow, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = colors.accentText)
            Text(PantryScreenCopy.title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = colors.text)
        }
        IconButton(onClick = onToggleSearch, modifier = Modifier.heightIn(min = 48.dp)) {
            Icon(if (searchOpen) AppIcons.Close else AppIcons.Search, if (searchOpen) PantrySearchCopy.closeDescription else PantrySearchCopy.openDescription, tint = colors.text)
        }
    }
    if (searchOpen) {
        OutlinedTextField(
            value = query, onValueChange = onQueryChange, placeholder = { Text(PantrySearchCopy.placeholder) }, singleLine = true,
            shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp).focusRequester(focusRequester),
            trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }, modifier = Modifier.heightIn(min = 48.dp)) { Icon(AppIcons.Close, PantrySearchCopy.clearDescription, tint = colors.muted) } },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary, unfocusedBorderColor = colors.line, focusedTextColor = colors.text, unfocusedTextColor = colors.text, focusedPlaceholderColor = colors.muted, unfocusedPlaceholderColor = colors.muted, cursorColor = colors.primary, focusedContainerColor = colors.surface, unfocusedContainerColor = colors.surface),
        )
    }
    Text(PantryScreenCopy.pantryHint, style = MaterialTheme.typography.bodySmall, color = colors.muted, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PantryGridAddActions(onAdd: () -> Unit, onAddOther: () -> Unit, voiceAvailable: Boolean, onVoice: () -> Unit) {
    val colors = AppTheme.colors
    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ChunkyButton(onClick = onAdd, container = colors.primary, edge = colors.primaryEdge, minHeight = 52.dp, modifier = Modifier.weight(1f)) { Text(addChoiceLabel(AddChoice.ADD_INGREDIENT), color = colors.onPrimary, fontWeight = FontWeight.Bold) }
        ChunkyButton(onClick = onAddOther, container = colors.surface, edge = colors.line, minHeight = 52.dp, modifier = Modifier.weight(1f)) { Text(addChoiceLabel(AddChoice.ADD_OTHER), color = colors.primary, fontWeight = FontWeight.Bold) }
        if (voiceAvailable) ChunkyButton(onClick = onVoice, container = colors.surface, edge = colors.line, minHeight = 52.dp, modifier = Modifier.weight(1f)) { Text(PantryScreenCopy.voiceAction, color = colors.primary, fontWeight = FontWeight.Bold) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PantryGridFiltersContent(filters: PantryGridFilters, state: PantryGridState, onFilters: (PantryGridFilters) -> Unit, onClear: () -> Unit, onCombo: () -> Unit, onLeftover: () -> Unit) {
    val colors = AppTheme.colors
    val presentation = pantryGridPresentation(cellsOf(state), filters.storageTab, filters.presentOnly)
    StorageSegmentedControl(filters.storageTab, state.ingredients.size, soonCount(cellsOf(state))) { onFilters(filters.copy(storageTab = it)) }
    Surface(Modifier.fillMaxWidth().padding(bottom = 8.dp), RoundedCornerShape(16.dp), color = colors.surface, border = BorderStroke(1.dp, colors.line)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(value = filters.presentOnly, role = Role.Switch, onValueChange = { onFilters(filters.copy(presentOnly = it)) }).semantics { contentDescription = "Chỉ xem món đang có, ${presentation.presentCount} món" }.padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Chỉ xem món đang có (${presentation.presentCount})", color = colors.text, modifier = Modifier.weight(1f).padding(end = 8.dp))
            Switch(checked = filters.presentOnly, onCheckedChange = null, colors = SwitchDefaults.colors(checkedThumbColor = colors.onPrimary, checkedTrackColor = colors.primary, checkedBorderColor = colors.primary, uncheckedThumbColor = colors.muted, uncheckedTrackColor = colors.surfaceSoft, uncheckedBorderColor = colors.line))
        }
    }
    PantryCategoryDropbar(filters.pantryCategory) { onFilters(filters.copy(pantryCategory = it)) }
    FlowRow(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        QuickActionButton("Dọn tủ", onClear); QuickActionButton("+ Combo", onCombo); QuickActionButton("+ Món thừa", onLeftover)
    }
}

@Composable
internal fun PantryGridItems(state: PantryGridState, cells: List<PantryCell>, selectedIds: Set<String>, isSoon: Boolean, onAction: (PantryGridAction) -> Unit, onUndo: (PantryCell) -> Unit, onCookFromSoon: () -> Unit) {
    val colors = AppTheme.colors
    val groups = groupCellsByCategory(cells, state.ingredients)
    val fullRow: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }
    LazyVerticalGrid(columns = GridCells.Fixed(pantryGridColumns(LocalDensity.current.fontScale)), modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp), contentPadding = pantryGridContentPadding(), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isSoon) {
            items(cells, key = { it.ingredientId }) { cell -> WarmFoodCard(cell, cell.quantity, cell.ingredientId in selectedIds, { if (tapMarksOut(cell.have)) onUndo(cell); onAction(PantryGridAction.Tap(cell.ingredientId)) }, { onAction(PantryGridAction.OpenShelfEditor(cell.ingredientId)) }, { onAction(PantryGridAction.OpenUnitEditor(cell.ingredientId)) }, { onAction(PantryGridAction.IncreaseQuantity(cell.ingredientId)) }, { if (decreaseMarksOut(cell.quantity)) onUndo(cell); onAction(PantryGridAction.DecreaseQuantity(cell.ingredientId)) }) }
            item(key = "cook-from-soon", span = fullRow) { Button(onClick = onCookFromSoon, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)) { Text(PantryScreenCopy.cookFromSoonAction, fontWeight = FontWeight.Bold) } }
        } else groups.forEach { group -> item(key = "header-${group.key}", span = fullRow) { CategoryHeader(group) }; items(group.cells, key = { it.ingredientId }) { cell -> WarmFoodCard(cell, cell.quantity, cell.ingredientId in selectedIds, { if (tapMarksOut(cell.have)) onUndo(cell); onAction(PantryGridAction.Tap(cell.ingredientId)) }, { onAction(PantryGridAction.OpenShelfEditor(cell.ingredientId)) }, { onAction(PantryGridAction.OpenUnitEditor(cell.ingredientId)) }, { onAction(PantryGridAction.IncreaseQuantity(cell.ingredientId)) }, { if (decreaseMarksOut(cell.quantity)) onUndo(cell); onAction(PantryGridAction.DecreaseQuantity(cell.ingredientId)) }) } }
    }
}

@Composable
internal fun PantryGridEmptyContent(message: String, canReset: Boolean, onReset: () -> Unit) {
    val colors = AppTheme.colors
    Column(Modifier.fillMaxWidth().heightIn(min = 240.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = colors.muted, textAlign = TextAlign.Center)
        if (canReset) TextButton(onClick = onReset, modifier = Modifier.heightIn(min = 48.dp)) { Text("Xem tất cả món", color = colors.accentText) }
    }
}

