package com.loitu.yourrefri.feature.pantry.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class StorageTab { ALL, FRIDGE, FREEZER, SOON }

@Composable
fun PantryGridScreen(
    state: PantryGridState,
    onAction: (PantryGridAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    voiceInputEnabled: Boolean = true,
    onCookFromSoon: () -> Unit = {},
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var filters by remember { mutableStateOf(PantryGridFilters()) }
    var searchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchFocus = remember { FocusRequester() }
    var showAdd by remember { mutableStateOf(false) }
    var addChoice by remember { mutableStateOf(AddChoice.ADD_INGREDIENT) }
    var newFoodName by remember { mutableStateOf("") }
    var newFoodDays by remember { mutableStateOf<Int?>(null) }
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    var selectedIds by remember { mutableStateOf(emptySet<String>()) }
    var addFoodError by remember { mutableStateOf(false) }
    var addFoodAlreadyPresent by remember { mutableStateOf(false) }
    var voiceHeard by remember { mutableStateOf<String?>(null) }
    var showClear by remember { mutableStateOf(false) }
    var showCombo by remember { mutableStateOf(false) }
    var showLeftover by remember { mutableStateOf(false) }
    var showMicExplain by remember { mutableStateOf(false) }
    var voicePhase by remember { mutableStateOf(VoicePhase.IDLE) }
    var voicePartial by remember { mutableStateOf("") }
    var voiceNote by remember { mutableStateOf<String?>(null) }
    val voiceAvailable = voiceInputEnabled && remember { isVoiceInputAvailable(context) }
    val sessionHolder = remember { arrayOfNulls<VoiceSession>(1) }

    fun endVoiceSession() { sessionHolder[0]?.destroy(); sessionHolder[0] = null }
    fun dismissAdd() { showAdd = false; newFoodName = ""; newFoodDays = null; selectedGroup = null; selectedIds = emptySet(); voiceHeard = null; addFoodError = false; addFoodAlreadyPresent = false; addChoice = AddChoice.ADD_INGREDIENT }
    fun openAdd(prefill: String = "", preselected: Set<String> = emptySet(), heard: String? = null, choice: AddChoice = AddChoice.ADD_INGREDIENT) { addChoice = choice; newFoodName = prefill; selectedIds = preselected; voiceHeard = heard; newFoodDays = null; selectedGroup = null; addFoodError = false; addFoodAlreadyPresent = false; showAdd = true }
    fun note(message: String) { snackbarHostState?.let { host -> scope.launch { host.currentSnackbarData?.dismiss(); host.showSnackbar(message, duration = SnackbarDuration.Short) } } ?: run { voiceNote = message } }
    fun offerUndo(cell: PantryCell) { val host = snackbarHostState ?: return; scope.launch { host.currentSnackbarData?.dismiss(); if (host.showSnackbar(markedOutMessage(cell.nameVi), actionLabel = UNDO_LABEL, duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed) onAction(PantryGridAction.Tap(cell.ingredientId)) } }
    fun clearAll() { val actions = clearAllPantryActions(state); actions.forEach(onAction); snackbarHostState?.let { host -> scope.launch { host.currentSnackbarData?.dismiss(); val job = launch { if (host.showSnackbar("Đã dọn hết ${actions.size} món khỏi tủ", actionLabel = "Hoàn tác", duration = SnackbarDuration.Indefinite) == SnackbarResult.ActionPerformed) actions.forEach(onAction) }; delay(5_000); if (job.isActive) host.currentSnackbarData?.dismiss() } } }
    fun startListening() { endVoiceSession(); voicePartial = ""; voicePhase = VoicePhase.LISTENING; val session = startVoiceSession(context, { voicePartial = it }, { text -> endVoiceSession(); val parsed = parseVoiceItems(text, state.ingredients); val addable = parsed.filter { state.pantry[it]?.have != true }; when { parsed.isEmpty() -> voicePhase = VoicePhase.NOT_HEARD; addable.isEmpty() -> { voicePhase = VoicePhase.IDLE; note(VOICE_ALREADY_HAVE) }; else -> { voicePhase = VoicePhase.IDLE; openAdd(preselected = addable.toSet(), heard = voiceHeardText(text)) } } }, { failure -> endVoiceSession(); voicePhase = if (failure == VoiceFailure.NOTHING_HEARD) VoicePhase.NOT_HEARD else VoicePhase.ERROR }); if (session == null) voicePhase = VoicePhase.ERROR else sessionHolder[0] = session }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) startListening() else note(VOICE_MIC_DENIED) }
    DisposableEffect(Unit) { onDispose { endVoiceSession() } }
    LaunchedEffect(searchOpen) { if (searchOpen) searchFocus.requestFocus() }
    LaunchedEffect(voicePhase) { if (voicePhase == VoicePhase.LISTENING) { delay(10_000); if (voicePhase == VoicePhase.LISTENING) { endVoiceSession(); voicePhase = VoicePhase.NOT_HEARD } } }

    val allCells = cellsOf(state)
    val presentation = pantryGridPresentation(allCells, filters.storageTab, filters.presentOnly)
    val categoryCells = filterByCategory(presentation.visibleCells, filters.pantryCategory)
    val cells = if (searchOpen) filterCellsBySearch(categoryCells, searchQuery) else categoryCells
    val emptyMessage = when (pantryGridEmptyState(state.ingredients.size, cells.size, filters)) {
        PantryGridEmptyState.CATALOG_EMPTY -> "Danh mục thực phẩm chưa có món nào."
        PantryGridEmptyState.NO_PRESENT_ITEMS -> "Chưa có món nào được ghi nhận trong tủ."
        PantryGridEmptyState.NO_PRESENT_IN_LOCATION -> "Chưa có món nào đang có trong ngăn này."
        PantryGridEmptyState.NO_ITEMS_IN_LOCATION -> "Danh mục này chưa có món nào."
        PantryGridEmptyState.NO_SOON_ITEMS -> PantryScreenCopy.soonEmpty
        PantryGridEmptyState.NONE -> "Chưa có món phù hợp với lựa chọn này."
    }
    Box(modifier.fillMaxSize().background(colors.background)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
            PantryGridHeader(searchOpen, searchQuery, searchFocus, { searchOpen = !searchOpen; if (!searchOpen) searchQuery = "" }, { searchQuery = it })
            PantryGridAddActions({ openAdd() }, { openAdd(choice = AddChoice.ADD_OTHER) }, voiceAvailable) { if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startListening() else showMicExplain = true }
            PantryGridFiltersContent(filters, state, { filters = it }, { if (clearAllPantryActions(state).isEmpty()) note("Tủ nhà mình đang trống rồi.") else showClear = true }, { showCombo = true }, { showLeftover = true })
            voiceNote?.let { Text(it, color = colors.warnText, modifier = Modifier.padding(bottom = 8.dp)) }
            if (cells.isEmpty()) PantryGridEmptyContent(if (searchOpen && searchQuery.isNotBlank()) PantrySearchCopy.notFound else emptyMessage, filters != PantryGridFilters(), { filters = filters.showAllItems() })
            else PantryGridItems(state, cells, selectedIds, filters.storageTab == StorageTab.SOON, onAction, ::offerUndo, onCookFromSoon)
        }
    }
    PantryGridDialogs(state, onAction, showClear, { showClear = false }, ::clearAll, showLeftover, { showLeftover = false }, { name -> if (name.isNotBlank()) { onAction(PantryGridAction.AddCustomFood(name, 2, "khac", "leftover", true)); note("Đã cất $name vào Đồ ăn thừa") }; showLeftover = false }, showMicExplain, { showMicExplain = false }, { micPermission.launch(Manifest.permission.RECORD_AUDIO) }, voicePhase, voicePartial, { sessionHolder[0]?.stop() }, { endVoiceSession(); voicePhase = VoicePhase.IDLE }, ::startListening, { endVoiceSession(); voicePhase = VoicePhase.IDLE; openAdd() }, showAdd, addChoice, newFoodName, newFoodDays, selectedGroup, selectedIds, addFoodError, addFoodAlreadyPresent, voiceHeard, { newFoodName = it; addFoodError = false; addFoodAlreadyPresent = false }, { newFoodDays = it }, { selectedGroup = it }, { selectedIds = toggleQuickAddSelection(selectedIds, it) }, { val existing = state.ingredients.map { it.nameVi }.toSet(); val actions = if (addChoice == AddChoice.ADD_OTHER) listOfNotNull(if (canAddOther(newFoodName, existing)) PantryGridAction.AddCustomFood(newFoodName.trim(), newFoodDays ?: 3, "khac", "fresh", false) else null) else ingredientOnlyConfirmActions(state, selectedIds, newFoodName, newFoodDays); if (actions.isNotEmpty()) { actions.forEach(onAction); dismissAdd() } else { addFoodError = true; addFoodAlreadyPresent = selectedIds.any { state.pantry[it]?.have == true } } }, ::dismissAdd, { combo -> val actions = comboActionsFor(combo, state); actions.forEach(onAction); showCombo = false; note("Đã thêm gói ${combo.titleVi} (${actions.size} món) vào tủ") }, showCombo, { showCombo = false })
}
