package com.loitu.yourrefri.feature.pantry.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

@Composable
internal fun PantryGridHeader(
    searchOpen: Boolean,
    query: String,
    onToggleSearch: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    val colors = AppTheme.colors
    val focusRequester = remember { FocusRequester() }
    var focusRequested by rememberSaveable(searchOpen) { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(PantryScreenCopy.eyebrow, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = colors.accentText)
                Text(PantryScreenCopy.title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = colors.text)
            }
            IconButton(onClick = onToggleSearch) {
                Icon(
                    if (searchOpen) AppIcons.Close else AppIcons.Search,
                    if (searchOpen) PantrySearchCopy.closeDescription else PantrySearchCopy.openDescription,
                    tint = colors.text,
                )
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(AppIcons.MoreVert, contentDescription = "Tùy chọn tủ lạnh", tint = colors.text)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Dọn sạch tủ", color = colors.text) },
                        onClick = {
                            menuOpen = false
                            onClear()
                        },
                    )
                }
            }
        }
        if (searchOpen) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(PantrySearchCopy.placeholder) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp).focusRequester(focusRequester),
                trailingIcon = {
                    if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) {
                        Icon(AppIcons.Close, PantrySearchCopy.clearDescription, tint = colors.muted)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.line,
                    focusedTextColor = colors.text,
                    unfocusedTextColor = colors.text,
                    focusedPlaceholderColor = colors.muted,
                    unfocusedPlaceholderColor = colors.muted,
                    cursorColor = colors.primary,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                ),
            )
            LaunchedEffect(focusRequester) {
                if (!focusRequested) {
                    focusRequester.requestFocus()
                    focusRequested = true
                }
            }
        }
    }
}

@Composable
internal fun PantryGridStatusSwitch(presentOnly: Boolean, onChange: (Boolean) -> Unit) {
    val colors = AppTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .toggleable(value = presentOnly, role = Role.Switch, onValueChange = onChange)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(pantryStatusSwitchLabel(presentOnly), color = colors.text, modifier = Modifier.weight(1f))
            Switch(
                checked = presentOnly,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.onPrimary,
                    checkedTrackColor = colors.primary,
                    checkedBorderColor = colors.primary,
                    uncheckedThumbColor = colors.muted,
                    uncheckedTrackColor = colors.surfaceSoft,
                    uncheckedBorderColor = colors.line,
                ),
            )
        }
    }
}

@Composable
internal fun PantryGridContent(
    state: PantryGridState,
    cells: List<PantryCell>,
    selectedIds: Set<String>,
    filters: PantryGridFilters,
    onFilters: (PantryGridFilters) -> Unit,
    onAction: (PantryGridAction) -> Unit,
    onUndo: (PantryCell) -> Unit,
    onEditQuantity: (PantryCell) -> Unit,
    onCookFromSoon: () -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    voiceNote: String?,
    emptyContent: @Composable () -> Unit,
) {
    val colors = AppTheme.colors
    val fullRow: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }
    LazyVerticalGrid(
        columns = GridCells.Fixed(pantryGridColumns(LocalDensity.current.fontScale)),
        modifier = modifier.fillMaxSize(),
        contentPadding = pantryGridContentPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "pantry-header", span = fullRow) { header() }
        item(key = "pantry-status", span = fullRow) {
            PantryGridStatusSwitch(filters.presentOnly) { onFilters(filters.copy(presentOnly = it)) }
        }
        item(key = "pantry-filters", span = fullRow) {
            PantryCategoryChips(filters.selectedCategories) {
                onFilters(filters.copy(selectedCategories = togglePantryCategory(filters.selectedCategories, it)))
            }
        }
        if (voiceNote != null) {
            item(key = "pantry-voice-note", span = fullRow) { Text(voiceNote, color = colors.warnText) }
        }
        if (cells.isEmpty()) {
            item(key = "pantry-empty", span = fullRow) { emptyContent() }
        } else if (filters.storageTab == StorageTab.SOON) {
            foodCards(cells, selectedIds, onAction, onUndo, onEditQuantity)
            item(key = "cook-from-soon", span = fullRow) {
                Button(
                    onClick = onCookFromSoon,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
                ) { Text(PantryScreenCopy.cookFromSoonAction, fontWeight = FontWeight.Bold) }
            }
        } else {
            groupCellsByCategory(cells, state.ingredients).forEach { group ->
                item(key = "header-${group.key}", span = fullRow) {
                    CategoryHeader(group, modifier = Modifier.animateItem())
                }
                foodCards(group.cells, selectedIds, onAction, onUndo, onEditQuantity)
            }
        }
    }
}

private fun LazyGridScope.foodCards(
    cells: List<PantryCell>,
    selectedIds: Set<String>,
    onAction: (PantryGridAction) -> Unit,
    onUndo: (PantryCell) -> Unit,
    onEditQuantity: (PantryCell) -> Unit,
) {
    items(cells, key = { it.ingredientId }) { cell ->
        val id = cell.ingredientId
        WarmFoodCard(
            cell = cell,
            quantity = cell.quantity,
            selected = id in selectedIds,
            onTap = {
                if (tapMarksOut(cell.have)) onUndo(cell)
                onAction(PantryGridAction.Tap(id))
            },
            onEditShelf = { onAction(PantryGridAction.OpenShelfEditor(id)) },
            onEditUnit = { onAction(PantryGridAction.OpenUnitEditor(id)) },
            onEditQuantity = { onEditQuantity(cell) },
            onIncreaseQty = { onAction(PantryGridAction.IncreaseQuantity(id)) },
            onDecreaseQty = {
                if (decreaseMarksOut(cell.quantity)) onUndo(cell)
                onAction(PantryGridAction.DecreaseQuantity(id))
            },
            modifier = Modifier.animateItem(),
        )
    }
}

@Composable
internal fun PantryGridEmptyContent(message: String, resetLabel: String?, onReset: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        Modifier.fillMaxWidth().heightIn(min = 240.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = colors.muted, textAlign = TextAlign.Center)
        if (resetLabel != null) {
            TextButton(onClick = onReset, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(resetLabel, color = colors.accentText)
            }
        }
    }
}

