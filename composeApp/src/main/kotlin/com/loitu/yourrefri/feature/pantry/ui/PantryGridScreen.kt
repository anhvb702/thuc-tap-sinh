package com.loitu.yourrefri.feature.pantry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.SHELF_DAY_OPTIONS
import com.loitu.yourrefri.feature.pantry.data.PANTRY_UNITS
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.feature.pantry.logic.statusText
import com.loitu.yourrefri.ui.theme.DeepCharcoal
import com.loitu.yourrefri.ui.theme.MutedGray
import com.loitu.yourrefri.ui.theme.SageGreen
import com.loitu.yourrefri.ui.theme.SageGreenStrong
import com.loitu.yourrefri.ui.theme.SageGreenText
import com.loitu.yourrefri.ui.theme.SoftGreen
import com.loitu.yourrefri.ui.theme.SoftGreenLight
import com.loitu.yourrefri.ui.theme.Terracotta
import com.loitu.yourrefri.ui.theme.TerracottaText
import com.loitu.yourrefri.ui.theme.WarmBeige
import com.loitu.yourrefri.ui.theme.WarmIvory

enum class StorageTab { ALL, FRIDGE, FREEZER }

@Composable
fun PantryGridScreen(
    state: PantryGridState,
    onAction: (PantryGridAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var filters by remember { mutableStateOf(PantryGridFilters()) }
    val minColumnWidth = if (LocalDensity.current.fontScale >= 1.3f) 320.dp else 180.dp
    var showAddDialog by remember { mutableStateOf(false) }
    var newFoodName by remember { mutableStateOf("") }
    var newFoodDays by remember { mutableStateOf<Int?>(null) }
    var addFoodError by remember { mutableStateOf(false) }
    var addFoodAlreadyPresent by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    fun dismissAddFoodDialog() {
        showAddDialog = false
        newFoodName = ""
        newFoodDays = null
        addFoodError = false
        addFoodAlreadyPresent = false
        selectedGroup = null
        selectedIds = emptySet()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
        // Tiêu đề và lời nhắc nhở ấm áp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Trong tủ lạnh",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = DeepCharcoal,
                )
                Text(
                    text = PantryScreenCopy.pantryHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedGray,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                )
            }
        }

        val allCells = cellsOf(state)
        val presentation = pantryGridPresentation(allCells, filters.storageTab, filters.presentOnly)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "Trong tủ: ${presentation.presentCount} món",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = DeepCharcoal,
                )
                Text(
                    text = "Chỉ xem món đang có",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedGray,
                )
            }
            Switch(
                checked = filters.presentOnly,
                onCheckedChange = { filters = filters.copy(presentOnly = it) },
                modifier = Modifier.semantics {
                    contentDescription = "Chỉ xem món đang có, ${presentation.presentCount} món"
                },
            )
        }

        // Bộ lọc phân loại: Tất cả | Ngăn mát | Ngăn đông
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            FilterChip(
                modifier = Modifier.heightIn(min = 48.dp),
                selected = filters.storageTab == StorageTab.ALL,
                onClick = { filters = filters.copy(storageTab = StorageTab.ALL) },
                label = { 
                    Text(
                        "Tất cả món (${state.ingredients.size})",
                        fontWeight = FontWeight.SemiBold,
                        color = if (filters.storageTab == StorageTab.ALL) Color.White else DeepCharcoal
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SageGreenStrong,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = DeepCharcoal
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = filters.storageTab == StorageTab.ALL,
                    borderColor = SoftGreen
                )
            )
            FilterChip(
                modifier = Modifier.heightIn(min = 48.dp),
                selected = filters.storageTab == StorageTab.FRIDGE,
                onClick = { filters = filters.copy(storageTab = StorageTab.FRIDGE) },
                label = { 
                    Text(
                        "Ngăn mát", 
                        fontWeight = FontWeight.SemiBold,
                        color = if (filters.storageTab == StorageTab.FRIDGE) Color.White else DeepCharcoal
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SageGreenStrong,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = DeepCharcoal
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = filters.storageTab == StorageTab.FRIDGE,
                    borderColor = SoftGreen
                )
            )
            FilterChip(
                modifier = Modifier.heightIn(min = 48.dp),
                selected = filters.storageTab == StorageTab.FREEZER,
                onClick = { filters = filters.copy(storageTab = StorageTab.FREEZER) },
                label = { 
                    Text(
                        "Ngăn đông", 
                        fontWeight = FontWeight.SemiBold,
                        color = if (filters.storageTab == StorageTab.FREEZER) Color.White else DeepCharcoal
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SageGreenStrong,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = DeepCharcoal
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = filters.storageTab == StorageTab.FREEZER,
                    borderColor = SoftGreen
                )
            )
        }

        val cells = presentation.visibleCells

        if (cells.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val emptyState = pantryGridEmptyState(state.ingredients.size, cells.size, filters)
                val emptyMessage = when (emptyState) {
                    PantryGridEmptyState.CATALOG_EMPTY -> "Danh mục thực phẩm chưa có món nào."
                    PantryGridEmptyState.NO_PRESENT_ITEMS -> "Chưa có món nào được ghi nhận trong tủ."
                    PantryGridEmptyState.NO_PRESENT_IN_LOCATION -> "Chưa có món nào đang có trong ngăn này."
                    PantryGridEmptyState.NO_ITEMS_IN_LOCATION -> "Danh mục này chưa có món nào."
                    PantryGridEmptyState.NONE -> "Chưa có món phù hợp với lựa chọn này."
                }
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MutedGray,
                )
                if (filters != PantryGridFilters()) {
                    TextButton(
                        onClick = { filters = filters.showAllItems() },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text("Xem tất cả món", color = SageGreenText)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = minColumnWidth),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = pantryGridContentPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(cells, key = { it.ingredientId }) { cell ->
                    WarmFoodCard(
                        cell = cell,
                        quantity = cell.quantity,
                        onTap = { onAction(PantryGridAction.Tap(cell.ingredientId)) },
                        onEditShelf = { onAction(PantryGridAction.OpenShelfEditor(cell.ingredientId)) },
                        onEditUnit = { onAction(PantryGridAction.OpenUnitEditor(cell.ingredientId)) },
                        onIncreaseQty = { onAction(PantryGridAction.IncreaseQuantity(cell.ingredientId)) },
                        onDecreaseQty = { onAction(PantryGridAction.DecreaseQuantity(cell.ingredientId)) },
                    )
                }
            }
        }
    }

    val unitEditor = state.unitEditor
    if (unitEditor != null) {
        val ingredientName = state.ingredients.firstOrNull { it.id == unitEditor.ingredientId }?.nameVi
        AlertDialog(
            onDismissRequest = { onAction(PantryGridAction.CancelUnitEditor) },
            containerColor = Color.White,
            title = {
                Text(
                    "Đơn vị${ingredientName?.let { " · $it" }.orEmpty()}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DeepCharcoal,
                )
            },
            text = {
                Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                    PANTRY_UNITS.forEach { unit ->
                        ShelfDayOption(
                            label = unit,
                            selected = unit == unitEditor.selectedUnit,
                            onClick = { onAction(PantryGridAction.PickUnit(unit)) },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onAction(PantryGridAction.SaveUnitEditor) }) {
                    Text("Lưu", color = SageGreenText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(PantryGridAction.CancelUnitEditor) }) {
                    Text("Hủy", color = MutedGray)
                }
            },
        )
    }

    // Hộp thoại sửa hạn ước tính (Shelf Editor Dialog)
    val editor = state.editor
    if (editor != null) {
        val ingredientName = state.ingredients.firstOrNull { it.id == editor.ingredientId }?.nameVi
        AlertDialog(
            onDismissRequest = { onAction(PantryGridAction.CancelShelfEditor) },
            containerColor = Color.White,
            title = {
                Text(
                    "Hạn ước tính${ingredientName?.let { " · $it" }.orEmpty()}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DeepCharcoal
                )
            },
            text = {
                Column(
                    Modifier
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    if (editor.selectedDays == 0) {
                        Text(
                            "Đang áp dụng: 0 ngày",
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TerracottaText
                        )
                    }
                    ShelfDayOption(
                        label = "Theo gợi ý mặc định",
                        selected = editor.selectedDays == null,
                        onClick = { onAction(PantryGridAction.PickShelfDays(null)) },
                    )
                    SHELF_DAY_OPTIONS.forEach { days ->
                        ShelfDayOption(
                            label = "$days ngày",
                            selected = editor.selectedDays == days,
                            onClick = { onAction(PantryGridAction.PickShelfDays(days)) },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onAction(PantryGridAction.SaveShelfEditor) }) {
                    Text("Lưu", color = SageGreenText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(PantryGridAction.CancelShelfEditor) }) {
                    Text("Hủy", color = MutedGray)
                }
            },
        )
    }

    // Nút FAB Tròn nổi Thêm Món theo W1-07d
    FloatingActionButton(
        onClick = { 
            newFoodName = ""
            newFoodDays = null
            addFoodError = false
            addFoodAlreadyPresent = false
            selectedGroup = null
            selectedIds = emptySet()
            showAddDialog = true 
        },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 20.dp, bottom = 24.dp),
        containerColor = SageGreenStrong,
        contentColor = Color.White,
        shape = CircleShape,
    ) {
        Text(
            text = "+", 
            color = Color.White, 
            fontSize = 32.sp, 
            fontWeight = FontWeight.Bold, 
            modifier = Modifier
                .padding(bottom = 2.dp)
                .semantics {
                    contentDescription = PantryScreenCopy.addFoodDescription
                }
        )
    }

    // Hộp thoại Thêm Món Nhanh (AddFoodDialog)
    if (showAddDialog) {
        val idsToSave = quickAddIdsToSave(state, selectedIds, query = newFoodName)
        AlertDialog(
            onDismissRequest = { dismissAddFoodDialog() },
            containerColor = Color.White,
            title = {
                Text(
                    "Thêm thực phẩm vào tủ 🥦",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = DeepCharcoal
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Chọn món quen thuộc hoặc nhập tên:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedGray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        quickAddGroupOptions().forEach { option ->
                            FilterChip(
                                modifier = Modifier.heightIn(min = 48.dp),
                                selected = selectedGroup == option.groupId,
                                onClick = {
                                    selectedGroup = option.groupId
                                    addFoodError = false
                                    addFoodAlreadyPresent = false
                                },
                                label = { Text(option.label, maxLines = 1) },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = newFoodName,
                        onValueChange = {
                            newFoodName = it
                            if (selectedIds.isEmpty()) newFoodDays = null
                            addFoodError = false
                            addFoodAlreadyPresent = false
                        },
                        placeholder = { Text("Ví dụ: Thịt bò, Cá basa, Trứng gà...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SageGreen,
                            unfocusedBorderColor = SoftGreen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        quickAddSuggestions(
                            state = state,
                            query = newFoodName,
                            selectedGroup = selectedGroup,
                        ).forEach { ingredient ->
                            FilterChip(
                                modifier = Modifier.heightIn(min = 48.dp),
                                selected = ingredient.id in selectedIds,
                                onClick = {
                                    val wasSelected = ingredient.id in selectedIds
                                    selectedIds = toggleQuickAddSelection(selectedIds, ingredient.id)
                                    if (!wasSelected && selectedIds.size == 1) {
                                        newFoodDays = quickAddSuggestionSelection(ingredient).overrideDays
                                    }
                                    addFoodError = false
                                    addFoodAlreadyPresent = false
                                },
                                label = { Text(ingredient.nameVi, maxLines = 1) },
                            )
                        }
                    }
                    val queryMessage = if (addFoodError && addFoodAlreadyPresent) {
                        "Món này đã có trong tủ rồi."
                    } else {
                        quickAddQueryMessage(state, newFoodName, afterAttempt = addFoodError)
                    }
                    if (queryMessage != null) {
                        Text(
                            queryMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = TerracottaText,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Hạn dùng ước tính:",
                        style = MaterialTheme.typography.labelMedium,
                        color = DeepCharcoal
                    )
                    val selectedIngredient = selectedIds.singleOrNull()?.let { selectedId ->
                        state.ingredients.firstOrNull { it.id == selectedId }
                    }
                    val matchedIngredient = selectedIngredient
                        ?: if (selectedIds.isEmpty()) quickAddMatch(state.ingredients, newFoodName) else null
                    val suggestedDays = matchedIngredient?.let {
                        quickAddSuggestionSelection(it).effectiveDays
                    }
                    if (selectedIds.size > 1) {
                        Text(
                            text = newFoodDays?.let { "Áp dụng hạn $it ngày cho các món đã chọn." }
                                ?: "Mỗi món dùng hạn gợi ý riêng.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SageGreenText,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    } else {
                        Text(
                            text = if (suggestedDays != null && newFoodDays != null) "Hạn đã chọn: $newFoodDays ngày"
                            else if (suggestedDays == null) "Chọn món để xem hạn dùng gợi ý."
                            else "Hạn dùng theo gợi ý đang được chọn.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SageGreenText,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        quickAddEstimateOption(matchedIngredient, newFoodDays)?.let { option ->
                            FilterChip(
                                modifier = Modifier.padding(top = 6.dp).heightIn(min = 48.dp),
                                selected = option.selected,
                                onClick = { newFoodDays = option.daysOnSelect },
                                label = { Text(option.label) },
                            )
                        }
                    }
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (selectedIds.size > 1 && newFoodDays != null) {
                            FilterChip(
                                modifier = Modifier.heightIn(min = 48.dp),
                                selected = false,
                                onClick = { newFoodDays = null },
                                label = { Text("Dùng hạn gợi ý riêng cho từng món") },
                            )
                        }
                        quickAddCustomDays(matchedIngredient).forEach { days ->
                            FilterChip(
                                modifier = Modifier.heightIn(min = 48.dp),
                                selected = newFoodDays == days,
                                onClick = { newFoodDays = days },
                                label = { 
                                    Text(
                                        if (selectedIds.size > 1) "$days ngày · áp dụng cho tất cả"
                                        else "$days ngày",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (newFoodDays == days) Color.White else DeepCharcoal
                                    ) 
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreenStrong,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = DeepCharcoal
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (idsToSave.isNotEmpty()) {
                            idsToSave.forEach { ingredientId ->
                                onAction(PantryGridAction.AddIngredient(ingredientId, newFoodDays))
                            }
                            dismissAddFoodDialog()
                        } else {
                            addFoodError = true
                            addFoodAlreadyPresent = if (selectedIds.isNotEmpty()) {
                                selectedIds.any { state.pantry[it]?.have == true }
                            } else {
                                quickAddMatch(state.ingredients, newFoodName)
                                    ?.let { state.pantry[it.id]?.have == true } == true
                            }
                        }
                    }
                ) {
                    Text(quickAddConfirmLabel(idsToSave.size), color = SageGreenText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { dismissAddFoodDialog() }) {
                    Text("Hủy", color = MutedGray)
                }
            }
        )
    }
    }
}

/**
 * Thẻ Thực Phẩm phong cách Warm Minimalism
 */
@Composable
private fun WarmFoodCard(
    cell: PantryCell,
    quantity: Int,
    onTap: () -> Unit,
    onEditShelf: () -> Unit,
    onEditUnit: () -> Unit,
    onIncreaseQty: () -> Unit,
    onDecreaseQty: () -> Unit,
) {
    val cardCopy = pantryCardCopy(cell.have, quantity)
    val cardBackground = if (cell.have) SoftGreenLight else Color.White
    val cardBorderColor = if (cell.have) SoftGreen else Color(0xFFE5E7EB)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (cell.have) 1.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Vùng chạm chính (Đổi trạng thái Có / Hết)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onTap)
                    .semantics {
                        contentDescription = if (cell.have) {
                            "Đánh dấu hết ${cell.nameVi}"
                        } else {
                            "Đánh dấu có ${cell.nameVi}"
                        }
                    }
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Icon Emoji trực quan
                        Text(
                            text = foodEmojiFor(cell.ingredientId),
                            fontSize = 28.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        // Badge trạng thái tinh tế
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (cell.have) SageGreenStrong else Color(0xFFF3F4F6)
                        ) {
                            Text(
                                text = cardCopy.statusLabel,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                ),
                                color = if (cell.have) Color.White else MutedGray,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Tên nguyên liệu
                    Text(
                        text = cell.nameVi,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = DeepCharcoal,
                    )

                    // Trạng thái hạn dùng ước tính
                    if (cardCopy.showEstimatedStatus) {
                        cell.daysLeft?.let { days ->
                            val isUrgent = days <= 2
                            Text(
                                text = statusText(days),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isUrgent) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (isUrgent) TerracottaText else SageGreenText,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = cardCopy.absentHint.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedGray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hàng công cụ bên dưới: Bộ đếm số lượng +/- và nút Sửa hạn
            Column(modifier = Modifier.fillMaxWidth()) {
                if (cell.have) {
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SoftGreen)
                                .clickable(onClick = onDecreaseQty)
                                .semantics {
                                    contentDescription = if (quantity > 1) {
                                        "Giảm số lượng ${cell.nameVi}"
                                    } else {
                                        "Đánh dấu hết ${cell.nameVi}"
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "−",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepCharcoal,
                            )
                        }
                        Text(
                            text = "$quantity ${cell.unit}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = DeepCharcoal,
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SoftGreen)
                                .clickable(onClick = onIncreaseQty)
                                .semantics {
                                    contentDescription = "Tăng số lượng ${cell.nameVi}"
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "+",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = SageGreenText,
                            )
                        }
                    }
                    cardCopy.quantityHint?.let { hint ->
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedGray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Nút sửa hạn ước tính
                if (cell.have) {
                    Text(
                        text = "Sửa hạn ⏱️",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = SageGreenText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable(onClick = onEditShelf)
                            .semantics {
                                contentDescription = "Sửa hạn ước tính cho ${cell.nameVi}"
                            }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    )
                }
            }

            if (cell.have) {
                Text(
                    text = "Đổi đơn vị · ${cell.unit}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = SageGreenText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable(onClick = onEditUnit)
                        .semantics {
                            contentDescription = "Đổi đơn vị số lượng ${cell.nameVi}, hiện là ${cell.unit}"
                        },
                )
            }
        }
    }
}

@Composable
private fun ShelfDayOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = null)
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = DeepCharcoal,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}
