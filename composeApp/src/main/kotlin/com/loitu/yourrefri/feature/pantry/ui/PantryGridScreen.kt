package com.loitu.yourrefri.feature.pantry.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.core.content.ContextCompat
import com.loitu.yourrefri.ui.components.FoodThumb
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

enum class StorageTab { ALL, FRIDGE, FREEZER, SOON }

@Composable
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
fun PantryGridScreen(
    state: PantryGridState,
    onAction: (PantryGridAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    voiceInputEnabled: Boolean = true,
    onCookFromSoon: () -> Unit = {},
) {
    val colors = AppTheme.colors
    val scope = rememberCoroutineScope()

    // Sau khi đánh dấu hết, hiện thông báo ngắn có "Hoàn tác" (chạm lại đưa món về trạng thái cũ).
    fun offerUndo(cell: PantryCell) {
        val host = snackbarHostState ?: return
        scope.launch {
            host.currentSnackbarData?.dismiss()
            val result = host.showSnackbar(
                message = markedOutMessage(cell.nameVi),
                actionLabel = UNDO_LABEL,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                onAction(PantryGridAction.Tap(cell.ingredientId))
            }
        }
    }

    val context = LocalContext.current
    var filters by remember { mutableStateOf(PantryGridFilters()) }
    var searchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchFocus = remember { FocusRequester() }
    var showAddDialog by remember { mutableStateOf(false) }
    var newFoodName by remember { mutableStateOf("") }
    var newFoodDays by remember { mutableStateOf<Int?>(null) }
    var addFoodError by remember { mutableStateOf(false) }
    var addFoodAlreadyPresent by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    // "Mình nghe được: ..." chỉ hiện trong bảng thêm món khi mở từ giọng nói.
    var voiceHeard by remember { mutableStateOf<String?>(null) }
    var voicePhase by remember { mutableStateOf(VoicePhase.IDLE) }
    var voicePartial by remember { mutableStateOf("") }
    var showMicExplain by remember { mutableStateOf(false) }
    var voiceNote by remember { mutableStateOf<String?>(null) }
    // Nút giọng nói tự ẩn khi máy không có bộ nhận dạng on-device.
    val voiceAvailable = voiceInputEnabled && remember { isVoiceInputAvailable(context) }
    val sessionHolder = remember { arrayOfNulls<VoiceSession>(1) }

    // Rời màn hoặc hủy thì dọn bộ nhận dạng, không để micro chạy ngầm.
    DisposableEffect(Unit) {
        onDispose {
            sessionHolder[0]?.destroy()
            sessionHolder[0] = null
        }
    }
    LaunchedEffect(searchOpen) { if (searchOpen) searchFocus.requestFocus() }

    fun dismissAddFoodDialog() {
        showAddDialog = false
        newFoodName = ""
        newFoodDays = null
        addFoodError = false
        addFoodAlreadyPresent = false
        selectedGroup = null
        selectedIds = emptySet()
        voiceHeard = null
    }

    fun openAddSheet(prefill: String = "", preselected: Set<String> = emptySet(), heard: String? = null) {
        newFoodName = prefill
        newFoodDays = null
        addFoodError = false
        addFoodAlreadyPresent = false
        selectedGroup = null
        selectedIds = preselected
        voiceHeard = heard
        showAddDialog = true
    }

    fun showNote(message: String) {
        val host = snackbarHostState
        if (host != null) {
            scope.launch {
                host.currentSnackbarData?.dismiss()
                host.showSnackbar(message = message, duration = SnackbarDuration.Short)
            }
        } else {
            voiceNote = message
        }
    }

    fun endVoiceSession() {
        sessionHolder[0]?.destroy()
        sessionHolder[0] = null
    }

    fun handleVoiceText(text: String) {
        endVoiceSession()
        val parsed = parseVoiceItems(text, state.ingredients)
        val addable = parsed.filter { state.pantry[it]?.have != true }
        when {
            parsed.isEmpty() -> voicePhase = VoicePhase.NOT_HEARD
            addable.isEmpty() -> {
                voicePhase = VoicePhase.IDLE
                showNote(VOICE_ALREADY_HAVE)
            }
            else -> {
                voicePhase = VoicePhase.IDLE
                // Chỉ chọn sẵn; người dùng xác nhận bằng nút "Thêm N món vào tủ".
                openAddSheet(preselected = addable.toSet(), heard = voiceHeardText(text))
            }
        }
    }

    fun startListening() {
        endVoiceSession()
        voicePartial = ""
        voicePhase = VoicePhase.LISTENING
        val session = startVoiceSession(
            context = context,
            onPartial = { voicePartial = it },
            onResult = { handleVoiceText(it) },
            onFailure = { failure ->
                endVoiceSession()
                voicePhase = if (failure == VoiceFailure.NOTHING_HEARD) VoicePhase.NOT_HEARD else VoicePhase.ERROR
            },
        )
        if (session == null) voicePhase = VoicePhase.ERROR else sessionHolder[0] = session
    }

    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startListening() else showNote(VOICE_MIC_DENIED)
    }

    fun onVoiceClick() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) startListening() else showMicExplain = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(AppIcons.Fridge, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(28.dp))
            Text(
                text = "Tủ lạnh",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.text,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
            )
            IconButton(
                onClick = {
                    searchOpen = !searchOpen
                    if (!searchOpen) searchQuery = ""
                },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = if (searchOpen) AppIcons.Close else AppIcons.Search,
                    contentDescription = if (searchOpen) PantrySearchCopy.closeDescription else PantrySearchCopy.openDescription,
                    tint = colors.text,
                )
            }
        }
        if (searchOpen) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(PantrySearchCopy.placeholder) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(48.dp)) {
                            Icon(
                                AppIcons.Close,
                                contentDescription = PantrySearchCopy.clearDescription,
                                tint = colors.muted,
                            )
                        }
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp).focusRequester(searchFocus),
            )
        }
        Text(
            text = PantryScreenCopy.pantryHint,
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
        )

        val allCells = cellsOf(state)
        val presentation = pantryGridPresentation(allCells, filters.storageTab, filters.presentOnly)

        // Chip lọc ngăn tủ có icon.
        StorageSegmentedControl(
            selected = filters.storageTab,
            allCount = state.ingredients.size,
            soonCount = soonCount(allCells),
            onSelect = { filters = filters.copy(storageTab = it) },
        )

        Surface(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.line),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .toggleable(
                        value = filters.presentOnly,
                        role = Role.Switch,
                        onValueChange = { filters = filters.copy(presentOnly = it) },
                    )
                    .semantics {
                        contentDescription = "Chỉ xem món đang có, ${presentation.presentCount} món"
                    }
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Chỉ xem món đang có (${presentation.presentCount})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.text,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                )
                Switch(
                    checked = filters.presentOnly,
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

        val filteredByLocation = presentation.visibleCells
        val cells = if (searchOpen) filterCellsBySearch(filteredByLocation, searchQuery) else filteredByLocation
        val searching = searchOpen && searchQuery.isNotBlank()

        voiceNote?.let { note ->
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.warnText,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )
        }

        if (cells.isEmpty() && searching) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = PantrySearchCopy.notFound,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.muted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Button(
                    onClick = { openAddSheet(prefill = searchQuery.trim()) },
                    modifier = Modifier.padding(top = 12.dp).heightIn(min = 52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
                ) {
                    Text(PantrySearchCopy.addAction, fontWeight = FontWeight.Bold)
                }
            }
        } else if (cells.isEmpty()) {
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
                    PantryGridEmptyState.NO_SOON_ITEMS -> PantryScreenCopy.soonEmpty
                    PantryGridEmptyState.NONE -> "Chưa có món phù hợp với lựa chọn này."
                }
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.muted,
                )
                if (filters != PantryGridFilters()) {
                    TextButton(
                        onClick = { filters = filters.showAllItems() },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text("Xem tất cả món", color = colors.accentText)
                    }
                }
            }
        } else {
            // Tab "Nên dùng sớm" giữ thứ tự theo độ gấp, không gom theo nhóm thực phẩm.
            val isSoonTab = filters.storageTab == StorageTab.SOON
            val groups = if (isSoonTab) emptyList() else groupCellsByCategory(cells, state.ingredients)
            @Composable
            fun FoodCard(cell: PantryCell) = WarmFoodCard(
                cell = cell,
                quantity = cell.quantity,
                onTap = {
                    if (tapMarksOut(cell.have)) offerUndo(cell)
                    onAction(PantryGridAction.Tap(cell.ingredientId))
                },
                onEditShelf = { onAction(PantryGridAction.OpenShelfEditor(cell.ingredientId)) },
                onEditUnit = { onAction(PantryGridAction.OpenUnitEditor(cell.ingredientId)) },
                onIncreaseQty = { onAction(PantryGridAction.IncreaseQuantity(cell.ingredientId)) },
                onDecreaseQty = {
                    if (decreaseMarksOut(cell.quantity)) offerUndo(cell)
                    onAction(PantryGridAction.DecreaseQuantity(cell.ingredientId))
                },
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = pantryGridContentPadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isSoonTab) {
                    items(cells, key = { it.ingredientId }) { cell -> FoodCard(cell) }
                    item(key = "cook-from-soon") {
                        Button(
                            onClick = onCookFromSoon,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
                        ) {
                            Text(PantryScreenCopy.cookFromSoonAction, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                groups.forEach { group ->
                    item(key = "header-${group.key}") { CategoryHeader(group) }
                    items(group.cells, key = { it.ingredientId }) { cell -> FoodCard(cell) }
                }
            }
        }
    }

    val unitEditor = state.unitEditor
    if (unitEditor != null) {
        val ingredientName = state.ingredients.firstOrNull { it.id == unitEditor.ingredientId }?.nameVi
        AlertDialog(
            onDismissRequest = { onAction(PantryGridAction.CancelUnitEditor) },
            containerColor = colors.surface,
            titleContentColor = colors.text,
            textContentColor = colors.muted,
            title = {
                Text(
                    "Đơn vị${ingredientName?.let { " · $it" }.orEmpty()}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.text,
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
                    Text("Lưu", color = colors.accentText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(PantryGridAction.CancelUnitEditor) }) {
                    Text("Hủy", color = colors.muted)
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
            containerColor = colors.surface,
            titleContentColor = colors.text,
            textContentColor = colors.muted,
            title = {
                Text(
                    "Hạn ước tính${ingredientName?.let { " · $it" }.orEmpty()}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.text
                )
            },
            text = {
                Column(
                    Modifier
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    shelfEditorStatusText(editor.selectedDays)?.let { statusText ->
                        Text(
                            statusText,
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.warnText
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
                    Text("Lưu", color = colors.accentText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(PantryGridAction.CancelShelfEditor) }) {
                    Text("Hủy", color = colors.muted)
                }
            },
        )
    }

    // Thanh hành động nổi ở đáy: "Thêm thủ công" và (nếu máy hỗ trợ) "Thêm bằng giọng nói".
    PantryActionBar(
        voiceAvailable = voiceAvailable,
        onManual = { openAddSheet() },
        onVoice = { onVoiceClick() },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    )

    if (showMicExplain) {
        AlertDialog(
            onDismissRequest = { showMicExplain = false },
            containerColor = colors.surface,
            titleContentColor = colors.text,
            textContentColor = colors.muted,
            title = { Text(VOICE_MIC_TITLE, style = MaterialTheme.typography.headlineSmall, color = colors.text) },
            text = { Text(VOICE_MIC_EXPLAIN, style = MaterialTheme.typography.bodyMedium, color = colors.muted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMicExplain = false
                        micPermission.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text("Tiếp tục", color = colors.accentText, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showMicExplain = false }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Để sau", color = colors.muted)
                }
            },
        )
    }

    if (voicePhase != VoicePhase.IDLE) {
        VoiceSheet(
            phase = voicePhase,
            partial = voicePartial,
            onStop = { sessionHolder[0]?.stop() },
            onCancel = {
                endVoiceSession()
                voicePhase = VoicePhase.IDLE
            },
            onRetry = { startListening() },
            onManual = {
                endVoiceSession()
                voicePhase = VoicePhase.IDLE
                openAddSheet()
            },
        )
    }

    if (showAddDialog) {
        val confirmActions = quickAddConfirmActions(state, selectedIds, newFoodName, newFoodDays)
        val increasesExisting = confirmActions.singleOrNull() is PantryGridAction.IncreaseQuantity
        val columns = quickAddGridColumns(LocalDensity.current.fontScale)
        ModalBottomSheet(
            onDismissRequest = { dismissAddFoodDialog() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = colors.background,
            contentColor = colors.text,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                ) {
                    Text(
                        AddFoodSheetCopy.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.text,
                    )
                    Text(
                        AddFoodSheetCopy.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.muted,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                    )
                    voiceHeard?.let { heard ->
                        Text(
                            text = voiceHeardLine(heard),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.ctaText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.ctaSoft)
                                .padding(12.dp),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
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
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = colors.surface,
                                    labelColor = colors.text,
                                    selectedContainerColor = colors.primary,
                                    selectedLabelColor = colors.onPrimary,
                                    disabledContainerColor = colors.surface,
                                    disabledLabelColor = colors.muted,
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedGroup == option.groupId,
                                    borderColor = colors.line,
                                    selectedBorderColor = colors.primary,
                                ),
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
                        placeholder = { Text(AddFoodSheetCopy.searchPlaceholder) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
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
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                    val queryMessage = if (increasesExisting) {
                        AddFoodSheetCopy.alreadyPresentWithIncrease
                    } else if (addFoodError && addFoodAlreadyPresent) {
                        AddFoodSheetCopy.alreadyPresent
                    } else {
                        quickAddQueryMessage(state, newFoodName, afterAttempt = addFoodError)
                    }
                    if (queryMessage != null) {
                        Text(
                            queryMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.warnText,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    val suggestions = quickAddSuggestions(
                        state = state,
                        query = newFoodName,
                        limit = Int.MAX_VALUE,
                        selectedGroup = selectedGroup,
                    )
                    suggestions.chunked(columns).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowItems.forEach { ingredient ->
                                val selected = ingredient.id in selectedIds
                                QuickAddTile(
                                    name = ingredient.nameVi,
                                    iconId = ingredient.id,
                                    selected = selected,
                                    onClick = {
                                        selectedIds = toggleQuickAddSelection(selectedIds, ingredient.id)
                                        newFoodName = quickAddQueryAfterChipTap(newFoodName, selectedIds, ingredient)
                                        if (!selected && selectedIds.size == 1) {
                                            newFoodDays = quickAddSuggestionSelection(ingredient).overrideDays
                                        }
                                        addFoodError = false
                                        addFoodAlreadyPresent = false
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    AddFoodEstimateSection(
                        state = state,
                        query = newFoodName,
                        selectedIds = selectedIds,
                        days = newFoodDays,
                        onDays = { newFoodDays = it },
                    )
                    Spacer(Modifier.height(12.dp))
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Button(
                        onClick = {
                            if (confirmActions.isNotEmpty()) {
                                confirmActions.forEach(onAction)
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
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                        ),
                    ) {
                        Text(quickAddConfirmLabelFor(confirmActions.size, increasesExisting), fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = { dismissAddFoodDialog() },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Text(AddFoodSheetCopy.closeLabel, color = colors.muted)
                    }
                }
            }
        }
    }
    }
}

/** Chip lọc Tất cả | Ngăn mát | Ngăn đông có icon; chữ xuống dòng khi cỡ chữ lớn thay vì bị cắt. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StorageSegmentedControl(
    selected: StorageTab,
    allCount: Int,
    soonCount: Int,
    onSelect: (StorageTab) -> Unit,
) {
    val colors = AppTheme.colors
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StorageTab.entries.forEach { tab ->
            val isSelected = tab == selected
            val label = when (tab) {
                StorageTab.ALL -> "Tất cả"
                StorageTab.FRIDGE -> "Ngăn mát"
                StorageTab.FREEZER -> "Ngăn đông"
                StorageTab.SOON -> PantryScreenCopy.soonChipLabel(soonCount)
            }
            val icon = when (tab) {
                StorageTab.ALL -> null
                StorageTab.FRIDGE -> AppIcons.Fridge
                StorageTab.FREEZER -> AppIcons.Snowflake
                StorageTab.SOON -> AppIcons.Clock
            }
            val contentColor = if (isSelected) colors.onPrimary else colors.text
            Row(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) colors.primary else colors.surface)
                    .border(1.dp, if (isSelected) colors.primary else colors.line, RoundedCornerShape(16.dp))
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(tab) })
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = contentColor,
                )
            }
        }
    }
}

/** Tiêu đề nhóm: icon + "Rau củ (6)". */
@Composable
private fun CategoryHeader(group: PantryCategoryGroup) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(categoryIconFor(group.key), contentDescription = null, tint = colors.accentText, modifier = Modifier.size(22.dp))
        Text(
            text = group.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
        )
    }
}

private fun categoryIconFor(key: String) = when (key) {
    "rau-cu" -> AppIcons.Leaf
    "thit" -> AppIcons.Meat
    "trai-cay" -> AppIcons.Tomato
    "sua-trung" -> AppIcons.Milk
    "khac-dau-nam" -> AppIcons.Tofu
    else -> AppIcons.Utensils
}

/** Thanh hành động nổi ở đáy: hai nút dạng viên thuốc cao >= 52dp. */
@Composable
private fun PantryActionBar(
    voiceAvailable: Boolean,
    onManual: () -> Unit,
    onVoice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ActionPill(
            label = PantryActionCopy.manual,
            icon = AppIcons.Keyboard,
            container = colors.surfaceSoft,
            content = colors.text,
            borderColor = colors.line,
            description = PantryScreenCopy.addFoodDescription,
            onClick = onManual,
            modifier = Modifier.weight(1f),
        )
        if (voiceAvailable) {
            ActionPill(
                label = PantryActionCopy.voice,
                icon = AppIcons.Mic,
                container = colors.cta,
                content = colors.onCta,
                borderColor = colors.cta,
                description = PantryActionCopy.voiceDescription,
                onClick = onVoice,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ActionPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    container: Color,
    content: Color,
    borderColor: Color,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(50))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        shape = RoundedCornerShape(50),
        color = container,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = content,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
    }
}

/** Bảng nhỏ "Đang nghe...": chữ nghe được hiện dần, có Dừng / Hủy. Không tự thêm món nào. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoiceSheet(
    phase: VoicePhase,
    partial: String,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onManual: () -> Unit,
) {
    val colors = AppTheme.colors
    ModalBottomSheet(
        onDismissRequest = onCancel,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = colors.background,
        contentColor = colors.text,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(AppIcons.Mic, contentDescription = null, tint = colors.ctaText, modifier = Modifier.size(28.dp))
                Text(
                    text = when (phase) {
                        VoicePhase.LISTENING -> VOICE_LISTENING_TITLE
                        VoicePhase.NOT_HEARD -> VOICE_NOT_HEARD
                        else -> VOICE_UNAVAILABLE_NOW
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
            }
            if (phase == VoicePhase.LISTENING) {
                Text(
                    text = partial.ifBlank { VOICE_LISTENING_HINT },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (partial.isBlank()) colors.muted else colors.text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.line, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
                Button(
                    onClick = onStop,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta),
                ) { Text("Dừng", fontWeight = FontWeight.Bold) }
                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("Hủy", color = colors.muted)
                }
            } else {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta),
                ) { Text("Nói lại", fontWeight = FontWeight.Bold) }
                TextButton(onClick = onManual, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("Chọn thủ công", color = colors.accentText)
                }
                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("Đóng", color = colors.muted)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/**
 * Hàng thực phẩm: khung ảnh 48dp, tên, hạn ước tính và bộ đếm − / + tròn 48dp.
 * Chạm vào tên để đổi trạng thái có / hết; "Sửa hạn" và "Đổi đơn vị" giữ vùng chạm 48dp.
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
    val colors = AppTheme.colors
    val cardCopy = pantryCardCopy(cell.have, quantity)
    val urgent = pantryRowUrgent(cell.have, cell.daysLeft)
    // Chữ lớn: bộ đếm xuống dòng riêng để tên món không bị bóp hẹp.
    val stacked = LocalDensity.current.fontScale >= 1.15f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (cell.have) colors.surface else Color.Transparent,
        border = BorderStroke(1.dp, if (cell.have) colors.line else colors.primarySoft),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FoodThumb(
                    ingredientId = cell.ingredientId,
                    size = 48.dp,
                    shape = RoundedCornerShape(12.dp),
                    urgent = urgent,
                    modifier = Modifier.alpha(if (cell.have) 1f else 0.55f),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clickable(onClick = onTap)
                        .semantics {
                            contentDescription = if (cell.have) {
                                "Đánh dấu hết ${cell.nameVi}"
                            } else {
                                "Đánh dấu có ${cell.nameVi}"
                            }
                        },
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = cell.nameVi,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (cell.have) colors.text else colors.muted,
                    )
                    if (cardCopy.showEstimatedStatus) {
                        cell.daysLeft?.let { days ->
                            Text(
                                text = pantryRowStatus(days),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (urgent) FontWeight.SemiBold else FontWeight.Normal,
                                ),
                                color = if (urgent) colors.ctaText else colors.accentText,
                            )
                        }
                    } else {
                        Text(
                            text = listOfNotNull(cardCopy.statusLabel, cardCopy.absentHint).joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.muted,
                        )
                    }
                }
                if (cell.have && !stacked) {
                    QuantityStepper(cell.nameVi, quantity, cell.unit, onDecreaseQty, onIncreaseQty)
                }
            }

            if (cell.have) {
                if (stacked) {
                    QuantityStepper(
                        name = cell.nameVi,
                        quantity = quantity,
                        unit = cell.unit,
                        onDecrease = onDecreaseQty,
                        onIncrease = onIncreaseQty,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }
                cardCopy.quantityHint?.let { hint ->
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    )
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    TextButton(
                        onClick = onEditShelf,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .semantics { contentDescription = "Sửa hạn ước tính cho ${cell.nameVi}" },
                    ) {
                        Text("Sửa hạn", color = colors.accentText, fontWeight = FontWeight.Medium)
                    }
                    TextButton(
                        onClick = onEditUnit,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .semantics {
                                contentDescription = "Đổi đơn vị số lượng ${cell.nameVi}, hiện là ${cell.unit}"
                            },
                    ) {
                        Text("Đổi đơn vị · ${cell.unit}", color = colors.accentText, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuantityStepper(
    name: String,
    quantity: Int,
    unit: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.primarySoft)
                .clickable(role = Role.Button, onClick = onDecrease)
                .semantics {
                    contentDescription = if (quantity > 1) "Giảm số lượng $name" else "Đánh dấu hết $name"
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcons.Minus, contentDescription = null, tint = colors.text, modifier = Modifier.size(22.dp))
        }
        Text(
            text = "$quantity $unit",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.text,
            modifier = Modifier.widthIn(min = 56.dp).padding(horizontal = 4.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.primarySoft)
                .clickable(role = Role.Button, onClick = onIncrease)
                .semantics { contentDescription = "Tăng số lượng $name" },
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcons.Plus, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun ShelfDayOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = colors.primary,
                    unselectedColor = colors.muted,
                ),
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.text,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

/** Ô chạm trong lưới chọn món: icon nét trong vòng tròn 36dp và tên món (không cắt). */
@Composable
private fun QuickAddTile(
    name: String,
    iconId: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Surface(
        modifier = modifier
            .heightIn(min = 88.dp)
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() })
            .semantics { contentDescription = quickAddTileDescription(name, selected) },
        shape = RoundedCornerShape(16.dp),
        color = if (selected) colors.primarySoft else colors.surfaceSoft,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.line),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(colors.surface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (selected) AppIcons.Check else foodIconFor(iconId),
                    contentDescription = null,
                    tint = colors.accentText,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.text,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

/** Phần "Hạn dùng ước tính": mặc định theo gợi ý, hoặc số ngày áp dụng cho các món đã chọn. */
@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun AddFoodEstimateSection(
    state: PantryGridState,
    query: String,
    selectedIds: Set<String>,
    days: Int?,
    onDays: (Int?) -> Unit,
) {
    val colors = AppTheme.colors
    val selectedIngredient = selectedIds.singleOrNull()?.let { id -> state.ingredients.firstOrNull { it.id == id } }
    val matched = selectedIngredient
        ?: if (selectedIds.isEmpty()) quickAddMatch(state.ingredients, query) else null
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = colors.surface,
        labelColor = colors.text,
        selectedContainerColor = colors.primary,
        selectedLabelColor = colors.onPrimary,
        disabledContainerColor = colors.surface,
        disabledLabelColor = colors.muted,
    )
    Text(
        AddFoodSheetCopy.estimateHeader,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = colors.text,
    )
    Text(
        text = when {
            selectedIds.size > 1 -> days?.let { "Áp dụng khoảng $it ngày cho các món đã chọn." }
                ?: "Mỗi món dùng hạn gợi ý riêng."
            matched == null -> "Chọn món để xem hạn dùng gợi ý."
            days != null -> "Hạn đã chọn: khoảng $days ngày"
            else -> "Hạn dùng theo gợi ý đang được chọn."
        },
        style = MaterialTheme.typography.bodySmall,
        color = colors.accentText,
        modifier = Modifier.padding(top = 2.dp),
    )
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (selectedIds.size <= 1) {
            quickAddEstimateOption(matched, days)?.let { option ->
                FilterChip(
                    modifier = Modifier.heightIn(min = 48.dp),
                    selected = option.selected,
                    onClick = { onDays(option.daysOnSelect) },
                    label = { Text(option.label) },
                    shape = RoundedCornerShape(16.dp),
                    colors = chipColors,
                )
            }
        } else if (days != null) {
            FilterChip(
                modifier = Modifier.heightIn(min = 48.dp),
                selected = false,
                onClick = { onDays(null) },
                label = { Text("Theo gợi ý riêng từng món") },
                shape = RoundedCornerShape(16.dp),
                colors = chipColors,
            )
        }
        quickAddCustomDays(matched).forEach { d ->
            FilterChip(
                modifier = Modifier.heightIn(min = 48.dp),
                selected = days == d,
                onClick = { onDays(d) },
                label = { Text(if (selectedIds.size > 1) "$d ngày · cho tất cả" else "$d ngày") },
                shape = RoundedCornerShape(16.dp),
                colors = chipColors,
            )
        }
    }
}

internal enum class VoicePhase { IDLE, LISTENING, NOT_HEARD, ERROR }

/** Chữ của ô tìm trên màn Tủ lạnh. */
internal object PantrySearchCopy {
    const val openDescription = "Tìm món trong tủ"
    const val closeDescription = "Đóng ô tìm"
    const val clearDescription = "Xóa chữ đã gõ"
    const val placeholder = "Tìm món, ví dụ: cà chua"
    const val notFound = "Chưa thấy món này, mình thêm vào tủ nhé?"
    const val addAction = "Thêm vào tủ"
}

internal object PantryActionCopy {
    const val manual = "Thêm thủ công"
    const val voice = "Thêm bằng giọng nói"
    const val voiceDescription = "Thêm thực phẩm bằng giọng nói"
}

internal const val VOICE_MIC_TITLE = "Bật micro nhé?"
internal const val VOICE_MIC_EXPLAIN = "Mình cần micro để nghe tên món, âm thanh không rời khỏi máy."
internal const val VOICE_MIC_DENIED = "Chưa bật micro, mình vẫn thêm thủ công được nhé"
internal const val VOICE_LISTENING_TITLE = "Đang nghe..."
internal const val VOICE_LISTENING_HINT = "Bạn nói tên món, ví dụ: cà chua, trứng gà và hành lá"
internal const val VOICE_UNAVAILABLE_NOW = "Máy chưa nghe được lúc này, mình vẫn thêm thủ công được nhé"
internal const val VOICE_ALREADY_HAVE = "Những món này đã có trong tủ rồi nhé"

internal fun voiceHeardLine(heard: String): String = "Mình nghe được: $heard"
