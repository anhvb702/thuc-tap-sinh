package com.loitu.yourrefri.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.home.ui.HomeScreen
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.ui.PantryGridScreen
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.loitu.yourrefri.feature.recipe.ui.RecipeDetailScreen
import com.loitu.yourrefri.feature.recipe.ui.RecipeScreen
import com.loitu.yourrefri.feature.recipe.ui.HandsFreeCookScreen
import com.loitu.yourrefri.feature.recipe.logic.defaultMealFor
import com.loitu.yourrefri.feature.recipe.ui.findRecipeById
import com.loitu.yourrefri.feature.recipe.ui.CookDone
import com.loitu.yourrefri.feature.recipe.ui.CookDoneScreen
import com.loitu.yourrefri.feature.recipe.ui.cookDoneNames
import com.loitu.yourrefri.feature.recipe.ui.cookDoneSummary
import com.loitu.yourrefri.feature.recipe.ui.buildDirectCookPlan
import com.loitu.yourrefri.feature.recipe.ui.freezerIngredientNames
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.filterRecipesByDiet
import com.loitu.yourrefri.feature.recipe.logic.availableRecipesForPlanning
import com.loitu.yourrefri.feature.recipe.logic.isDietFilterTooStrict
import com.loitu.yourrefri.feature.settings.ui.SettingsPresentation
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.loitu.yourrefri.reminder.ReminderOpenRequest
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.loitu.yourrefri.feature.recipe.ui.WeeklyMealPlanAction
import com.loitu.yourrefri.feature.recipe.ui.WeeklyMealPlanState
import com.loitu.yourrefri.feature.recipe.ui.recipeRequestForAllRecipes
import com.loitu.yourrefri.feature.recipe.ui.recipeRequestForHomeCard
import com.loitu.yourrefri.feature.recipe.ui.RecipeScreenMode
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.buildWeeklyMealPlan
import com.loitu.yourrefri.feature.recipe.logic.buildWeeklyShoppingList
import com.loitu.yourrefri.feature.recipe.logic.cookableRecipes
import com.loitu.yourrefri.feature.recipe.logic.pickRandomRecipe
import com.loitu.yourrefri.feature.recipe.ui.RandomWheelDialog
import com.loitu.yourrefri.feature.recipe.ui.RandomRecipeCopy
import com.loitu.yourrefri.feature.recipe.ui.WeeklyMealPlanSection
import com.loitu.yourrefri.feature.recipe.ui.WeeklyShoppingSection
import com.loitu.yourrefri.feature.settings.ui.SettingsScreen
import com.loitu.yourrefri.ui.components.CircleTabBar
import com.loitu.yourrefri.ui.components.areSystemAnimatorsEnabled
import com.loitu.yourrefri.ui.components.tabContentDurationMs
import com.loitu.yourrefri.ui.components.tabSlideDirection
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import com.loitu.yourrefri.reminder.ReminderPopupBoxDialog
import com.loitu.yourrefri.reminder.buildReminderPopupData

// Nhãn tab cho phép xuống dòng khi cỡ chữ hệ thống lớn, để không cắt còn một từ.
internal const val NAV_LABEL_MAX_LINES = 2

// Chữ nhãn tab không vượt 18sp hiển thị, kể cả khi cỡ chữ hệ thống lớn.
internal const val NAV_LABEL_MAX_DISPLAY_SP = 18f

// Cỡ chữ đặt cho Text (sp). Compose nhân với fontScale hệ thống, nên chia lại để chữ hiển thị bị chặn.
internal fun navLabelFontSizeSp(fontScale: Float): Float =
    if (fontScale <= 0f) 14f else minOf(14f, NAV_LABEL_MAX_DISPLAY_SP / fontScale)

enum class NavTab(val label: String, val icon: ImageVector) {
    HOME("Trang chủ", AppIcons.Home),
    PANTRY("Tủ lạnh", AppIcons.Fridge),
    RECIPES("Món ăn", AppIcons.Pot),
    SETTINGS("Cài đặt", AppIcons.User),
}

internal fun tabStateKey(tab: NavTab): String = when (tab) {
    NavTab.HOME -> "tab-home"
    NavTab.PANTRY -> "tab-pantry"
    NavTab.RECIPES -> "tab-recipes"
    NavTab.SETTINGS -> "tab-settings"
}

internal fun shouldComputeGlobalOverlays(showWeeklyOverlay: Boolean, showShoppingOverlay: Boolean): Boolean =
    showWeeklyOverlay || showShoppingOverlay

@Composable
fun MainScaffold(
    state: PantryGridState,
    weeklyMealPlanState: WeeklyMealPlanState,
    onWeeklyMealPlanAction: (WeeklyMealPlanAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    recipes: List<Recipe>,
    onAction: (PantryGridAction) -> Unit,
    settings: MainScaffoldSettingsBinding = MainScaffoldSettingsBinding(),
    collections: MainScaffoldCollectionBinding = MainScaffoldCollectionBinding(),
    currentHour: (() -> Int)? = null,
    modifier: Modifier = Modifier,
    homeViewModel: com.loitu.yourrefri.feature.home.presentation.HomeViewModel? = null,
) {
    val dietRestriction = settings.uiState.dietRestriction
    val tasteProfile = settings.uiState.tasteProfile
    val favoriteIds = collections.favoriteIds
    val onToggleFavorite = collections.onToggleFavorite
    val cookHistoryState = collections.cookHistoryState
    val onRecordCook = collections.onRecordCook
    val onDeleteCook = collections.onDeleteCook
    val unlockedIds = collections.unlockedIds
    val mysteryRewardsReady = collections.mysteryRewardsReady
    val mysteryUnlockFailedId = collections.mysteryUnlockFailedId
    val onUnlockMysteryRecipe = collections.onUnlockMysteryRecipe
    var currentTab by remember { mutableStateOf(NavTab.HOME) } // Mặc định mở Trang chủ theo triết lý Warm Minimalism
    var showWeeklyOverlay by remember { mutableStateOf(false) }
    var showShoppingOverlay by remember { mutableStateOf(false) }
    var showGlobalRandomWheel by remember { mutableStateOf(false) }
    var showGlobalNoCookable by remember { mutableStateOf(false) }
    var globalRandomPick by remember { mutableStateOf<com.loitu.yourrefri.feature.recipe.logic.RandomRecipePick?>(null) }
    var requestedRecipeId by remember { mutableStateOf<String?>(null) }
    var requestedRecipeMode by remember { mutableStateOf(RecipeScreenMode.DAILY) }
    // Công thức đang mở ở màn chi tiết. Tạm dùng state cục bộ; khi có Navigation Compose sẽ thành route RecipeDetail.
    var openedRecipeId by rememberSaveable { mutableStateOf<String?>(null) }
    var showReminderPopup by rememberSaveable { mutableStateOf(false) }
    // Công thức đang nấu ở chế độ rảnh tay (màn riêng, tạm dùng state cục bộ như màn chi tiết).
    var handsFreeRecipeId by rememberSaveable { mutableStateOf<String?>(null) }
    // Bấm popup nhắc: mở thẳng nấu rảnh tay của món đó (id do MainActivity đặt vào ReminderOpenRequest).
    val openRequest by ReminderOpenRequest.recipeId.collectAsState()
    LaunchedEffect(openRequest) {
        openRequest?.let {
            handsFreeRecipeId = it
            ReminderOpenRequest.consume()
        }
    }
    // Màn "Đã nấu" mở từ Trang chủ, đóng bằng nút quay lại hoặc khi đổi tab.
    var showHistory by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showHistory && handsFreeRecipeId == null) { showHistory = false }
    BackHandler(enabled = handsFreeRecipeId != null) { handsFreeRecipeId = null }
    BackHandler(enabled = handsFreeRecipeId == null && openedRecipeId != null) { openedRecipeId = null }
    val scope = rememberCoroutineScope()
    val tabStateHolder = rememberSaveableStateHolder()
    val resolvedHomeViewModel = homeViewModel ?: remember { com.loitu.yourrefri.feature.home.presentation.HomeViewModel() }

    // Bữa người dùng chọn ở Trang chủ; null = chưa chọn thì theo giờ. Giữ ở đây để không mất khi đổi tab.
    var chosenMeal by rememberSaveable { mutableStateOf<com.loitu.yourrefri.feature.recipe.data.MealSlot?>(null) }
    val currentMeal = chosenMeal ?: defaultMealFor(state.hourOfDay)

    // Nấu xong thì hiện màn chúc mừng rồi về Trang chủ (thay cho thông báo nhỏ dễ bỏ qua).
    var cookDone by remember { mutableStateOf<CookDone?>(null) }
    fun goHomeAfterCook() {
        cookDone = null
        handsFreeRecipeId = null
        openedRecipeId = null
        showHistory = false
        requestedRecipeId = null
        currentTab = NavTab.HOME
    }
    BackHandler(enabled = cookDone != null) { goHomeAfterCook() }

    // Chế độ ăn lọc danh sách gợi ý một lần ở đây, mọi màn dùng chung. Lọc hết món thì nới ra và nhắn nhẹ.
    val availableRecipes = remember(recipes, unlockedIds) { availableRecipesForPlanning(recipes, unlockedIds) }
    val dietTooStrict = remember(availableRecipes, dietRestriction, state.ingredients) {
        isDietFilterTooStrict(availableRecipes, dietRestriction, state.ingredients)
    }
    val visibleRecipes = remember(availableRecipes, dietRestriction, dietTooStrict, state.ingredients) {
        if (dietTooStrict) availableRecipes else filterRecipesByDiet(availableRecipes, dietRestriction, state.ingredients)
    }
    val globalWeeklyPlan = if (shouldComputeGlobalOverlays(showWeeklyOverlay, showShoppingOverlay)) {
        remember(
            visibleRecipes, state, weeklyMealPlanState.goal, weeklyMealPlanState.preferences,
            weeklyMealPlanState.swappedOffsets, tasteProfile, weeklyMealPlanState.includeLunchMealPrep,
        ) {
            buildWeeklyMealPlan(
                visibleRecipes, state, weeklyMealPlanState.goal, weeklyMealPlanState.preferences,
                weeklyMealPlanState.swappedOffsets, tasteProfile,
                separateLunchAndDinner = weeklyMealPlanState.includeLunchMealPrep,
            )
        }
    } else null
    val globalShopping = if (globalWeeklyPlan != null) {
        remember(globalWeeklyPlan, state, weeklyMealPlanState.preferences, weeklyMealPlanState.includeLunchMealPrep) {
            buildWeeklyShoppingList(
                globalWeeklyPlan, state, weeklyMealPlanState.preferences,
                includeLunchMealPrep = weeklyMealPlanState.includeLunchMealPrep,
            )
        }
    } else null
    fun stockIngredients(ids: List<String>) {
        ids.groupingBy { it }.eachCount().forEach { (id, needed) ->
            if (state.pantry[id]?.have != true) {
                onAction(PantryGridAction.AddIngredient(id, null))
                repeat((needed - 1).coerceAtLeast(0)) { onAction(PantryGridAction.IncreaseQuantity(id)) }
            } else repeat(needed.coerceAtLeast(0)) { onAction(PantryGridAction.IncreaseQuantity(id)) }
        }
    }
    LaunchedEffect(dietTooStrict, dietRestriction) {
        if (dietTooStrict) snackbarHostState.showSnackbar(SettingsPresentation.dietTooStrictNotice)
    }
    val handsFreeRecipe = findRecipeById(handsFreeRecipeId, visibleRecipes, availableRecipes)

    // Một chỗ duy nhất khi nấu xong (cả ba luồng): trừ nguyên liệu đang có, ghi một dòng vào "Đã nấu", hiện màn chúc mừng.
    fun completeCook(recipe: Recipe, consumedIds: List<String>) {
        val before = state
        val ids = consumedIds.distinct().filter { before.pantry[it]?.have == true }
        ids.forEach { onAction(PantryGridAction.ConsumeIngredient(it)) }
        // Ghi đúng bữa người dùng đang chọn ở Trang chủ; chưa chọn thì ViewModel tính theo giờ lúc ghi.
        // Chưa chọn bữa thì tính theo giờ lúc bấm nấu xong (không dùng giá trị cũ của state).
        val namesById = before.ingredients.associate { it.id to it.nameVi }
        cookDone = CookDone(
            recipeTitle = recipe.title,
            summary = cookDoneSummary(cookDoneNames(ids, namesById)),
            undo = com.loitu.yourrefri.feature.recipe.ui.cookUndoActions(before, ids),
            recipeId = recipe.id,
        )
        // Ghi lịch sử sau khi đặt cookDone để nhận id dòng vừa ghi; Hoàn tác (Q10) sẽ xóa dòng này.
        onRecordCook(recipe.id, ids, chosenMeal ?: defaultMealFor(currentHour?.invoke() ?: state.hourOfDay)) { id ->
            cookDone = cookDone?.copy(logId = id)
        }
        handsFreeRecipeId = null
        openedRecipeId = null
        showHistory = false
    }

    // Nấu rảnh tay xong: trừ nguyên liệu như nút "Nấu xong" ở màn công thức, rồi hiện màn chúc mừng (có Hoàn tác).
    fun finishHandsFree(recipe: Recipe) {
        val plan = buildDirectCookPlan(recipe, state, callbackAvailable = true) ?: return
        completeCook(recipe, plan.ingredientIds)
    }
    if (showReminderPopup) {
        val popupData = remember(state, state.today) { buildReminderPopupData(state, state.today.dayOfYear) }
        ReminderPopupBoxDialog(
            data = popupData,
            onOpenRecipe = { id -> showReminderPopup = false; openedRecipeId = id },
            onDismiss = { showReminderPopup = false },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                val colors = AppTheme.colors
                Snackbar(
                    containerColor = colors.snackbar,
                    contentColor = colors.onSnackbar,
                    action = data.visuals.actionLabel?.let { label ->
                        {
                            TextButton(
                                onClick = { data.performAction() },
                                modifier = Modifier.heightIn(min = 48.dp),
                            ) {
                                Text(label, color = colors.snackbarAction, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                ) {
                    Text(data.visuals.message, color = colors.onSnackbar)
                }
            }
        },
        bottomBar = {
            // Đang nấu rảnh tay thì ẩn thanh tab để màn hình tập trung vào bước nấu.
            if (handsFreeRecipe == null) {
                CircleTabBar(currentTab, onSelectTab = { tab ->
                    if (tab != currentTab) requestedRecipeId = null
                    if (tab == NavTab.RECIPES) requestedRecipeMode = RecipeScreenMode.DAILY
                    cookDone = null
                    showHistory = false
                    openedRecipeId = null
                    currentTab = tab
                })
            }
        }
    ) { innerPadding ->
        val animatorsEnabled = areSystemAnimatorsEnabled()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val doneNow = cookDone
            if (doneNow != null) {
                CookDoneScreen(
                    done = doneNow,
                    onHome = { goHomeAfterCook() },
                    onUndo = { actions ->
                        // Q10: trả lại tủ đúng trạng thái trước khi nấu và xóa dòng "Đã nấu" vừa ghi.
                        actions.forEach { onAction(it) }
                        doneNow.logId?.let { onDeleteCook(it) }
                        cookDone = null
                    },
                )
            } else if (handsFreeRecipe != null) {
                HandsFreeCookScreen(
                    recipe = handsFreeRecipe,
                    onClose = { handsFreeRecipeId = null },
                    onFinished = { finishHandsFree(handsFreeRecipe) },
                    freezerNames = freezerIngredientNames(handsFreeRecipe, state),
                )
            } else if (showHistory && currentTab == NavTab.HOME) {
                com.loitu.yourrefri.feature.cooklog.ui.CookHistoryScreen(
                    state = cookHistoryState,
                    recipes = availableRecipes,
                    onDelete = onDeleteCook,
                    onOpenRecipe = { recipeId ->
                        // Mở lại công thức ở tab Món ăn; dùng danh sách chưa lọc nên món cũ ngoài chế độ ăn vẫn mở được.
                        requestedRecipeId = recipeRequestForHomeCard(recipeId)
                        requestedRecipeMode = RecipeScreenMode.DAILY
                        openedRecipeId = recipeId
                        showHistory = false
                        currentTab = NavTab.RECIPES
                    },
                    onBack = { showHistory = false },
                )
            } else {
                val duration = tabContentDurationMs(animatorsEnabled)
                AnimatedContent(
                    targetState = currentTab,
                    modifier = Modifier.clipToBounds(),
                    transitionSpec = {
                        val dir = tabSlideDirection(initialState.ordinal, targetState.ordinal)
                        val transition = if (duration > 0) {
                            (slideInHorizontally(tween(duration)) { dir * (it / 12) } + fadeIn(tween(duration)))
                                .togetherWith(
                                    slideOutHorizontally(tween(duration)) { -dir * (it / 12) } + fadeOut(tween(duration)),
                                )
                        } else {
                            fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                        }
                        transition.using(null)
                    },
                    label = "mainTabTransition",
                ) { tab ->
                    when (tab) {
                        NavTab.HOME -> tabStateHolder.SaveableStateProvider(tabStateKey(tab)) {
                            HomeScreen(
                                homeViewModel = resolvedHomeViewModel,
                                state = state,
                                recipes = availableRecipes,
                                dietRestriction = if (dietTooStrict) DietRestriction.NONE else dietRestriction,
                                onNavigateToPantry = { currentTab = NavTab.PANTRY },
                                onOpenShopping = {
                                    requestedRecipeMode = RecipeScreenMode.SHOPPING
                                    currentTab = NavTab.RECIPES
                                },
                                onNavigateToRecipes = {
                                    requestedRecipeId = recipeRequestForAllRecipes()
                                    requestedRecipeMode = RecipeScreenMode.DAILY
                                    currentTab = NavTab.RECIPES
                                },
                                onNavigateToRecipe = { recipeId ->
                                    requestedRecipeId = recipeRequestForHomeCard(recipeId)
                                    requestedRecipeMode = RecipeScreenMode.DAILY
                                    openedRecipeId = recipeId
                                    currentTab = NavTab.RECIPES
                                },
                                onNavigateToWeeklyRecipes = {
                                    requestedRecipeId = null
                                    requestedRecipeMode = RecipeScreenMode.WEEKLY
                                    currentTab = NavTab.RECIPES
                                },
                                onNavigateToHistory = { showHistory = true },
                                meal = currentMeal,
                                onMealChange = { chosenMeal = it },
                                tasteProfile = tasteProfile,
                            )
                        }
                        NavTab.PANTRY -> tabStateHolder.SaveableStateProvider(tabStateKey(tab)) {
                            PantryGridScreen(
                                state = state,
                                onAction = onAction,
                                snackbarHostState = snackbarHostState,
                                onCookFromSoon = {
                                    requestedRecipeId = recipeRequestForAllRecipes()
                                    requestedRecipeMode = RecipeScreenMode.DAILY
                                    currentTab = NavTab.RECIPES
                                },
                            )
                        }
                        NavTab.RECIPES -> {
                            val lastOpenedRecipeId = remember { mutableStateOf(openedRecipeId) }
                            val isCurrentTab = currentTab == tab
                            val displayedRecipeId = if (isCurrentTab) openedRecipeId else lastOpenedRecipeId.value
                            // Giữ màn chi tiết trong lúc tab Món ăn mờ dần.
                            SideEffect {
                                if (isCurrentTab) lastOpenedRecipeId.value = displayedRecipeId
                            }
                            // Nút "Nấu ngẫu nhiên" bỏ qua chế độ ăn nên món mở ra có thể không nằm trong danh sách đã lọc.
                            val opened = findRecipeById(displayedRecipeId, visibleRecipes, availableRecipes)
                            if (opened != null) {
                                RecipeDetailScreen(
                                    recipe = opened,
                                    state = state,
                                    isFavorite = opened.id in favoriteIds,
                                    onToggleFavorite = { onToggleFavorite(opened.id) },
                                    onStartHandsFree = { handsFreeRecipeId = opened.id },
                                    onBack = { openedRecipeId = null },
                                    onCook = { ids -> completeCook(opened, ids) },
                                    snackbarHostState = snackbarHostState,
                                    showDoneSnackbar = false,
                                )
                            } else {
                                tabStateHolder.SaveableStateProvider(tabStateKey(tab)) {
                                    RecipeScreen(
                                        state = state,
                                        recipes = recipes,
                                        unlockedIds = unlockedIds,
                                        totalCookCount = cookHistoryState.weeks.sumOf { it.entries.size },
                                        mysteryRewardsReady = mysteryRewardsReady,
                                        mysteryUnlockFailedId = mysteryUnlockFailedId,
                                        onUnlockMysteryRecipe = onUnlockMysteryRecipe,
                                        weekStart = weeklyMealPlanState.weekStart,
                                        favoriteIds = favoriteIds,
                                        onToggleFavorite = onToggleFavorite,
                                        onOpenHandsFree = { handsFreeRecipeId = it },
                                        dietRestriction = dietRestriction,
                                        initialRecipeId = requestedRecipeId,
                                        initialMode = requestedRecipeMode,
                                        onWeeklyAction = onWeeklyMealPlanAction,
                                        weeklyGoal = weeklyMealPlanState.goal,
                                        weeklyPreferences = weeklyMealPlanState.preferences,
                                        weeklySwappedOffsets = weeklyMealPlanState.swappedOffsets,
                                        weeklyUncheckedIds = weeklyMealPlanState.uncheckedIds,
                                        weeklyIncludeLunchMealPrep = weeklyMealPlanState.includeLunchMealPrep,
                                        onWeeklyIncludeLunchMealPrepChange = { enabled ->
                                            onWeeklyMealPlanAction(WeeklyMealPlanAction.SetLunchMealPrep(enabled))
                                        },
                                        onModeChange = { requestedRecipeMode = it },
                                        onBackHome = { currentTab = NavTab.HOME },
                                        onOpenRecipe = { openedRecipeId = it },
                                        onCookRecipe = { cooked, ingredientIds -> completeCook(cooked, ingredientIds) },
                                        tasteProfile = tasteProfile,
                                        onStockIngredients = ::stockIngredients,
                                    )
                                }
                            }
                        }
                        NavTab.SETTINGS -> tabStateHolder.SaveableStateProvider(tabStateKey(tab)) {
                            SettingsScreen(
                                backupStatus = settings.uiState.backupStatus,
                                restoreConfirmationPending = settings.uiState.restoreConfirmationPending,
                                onExportBackup = settings.onExportBackup,
                                onImportBackup = settings.onImportBackup,
                                onConfirmRestore = settings.onConfirmRestore,
                                onCancelRestore = settings.onCancelRestore,
                                reminderEnabled = settings.uiState.reminderEnabled,
                                onReminderChanged = settings.onReminderChanged,
                                onTestReminder = {
                                    settings.onTestReminder()
                                    showReminderPopup = true
                                },
                                themeMode = settings.uiState.themeMode,
                                onThemeModeChange = settings.onThemeModeChange,
                                weeklyGoal = weeklyMealPlanState.goal,
                                onWeeklyGoalChange = { onWeeklyMealPlanAction(WeeklyMealPlanAction.SetGoal(it)) },
                                weeklyPreferences = weeklyMealPlanState.preferences,
                                onWeeklyPreferencesChange = { wanted ->
                                    // Màn cài đặt trả về cả tập khẩu vị; ViewModel chỉ có "đổi một khẩu vị", nên đổi từng cái khác nhau.
                                    val current = weeklyMealPlanState.preferences
                                    (wanted - current).forEach { onWeeklyMealPlanAction(WeeklyMealPlanAction.TogglePreference(it)) }
                                    (current - wanted).forEach { onWeeklyMealPlanAction(WeeklyMealPlanAction.TogglePreference(it)) }
                                },
                                dietRestriction = settings.uiState.dietRestriction,
                                onDietRestrictionChange = settings.onDietRestrictionChange,
                                appVersionName = settings.appVersionName,
                                weekdayTime = settings.uiState.weekdayTime,
                                onWeekdayTimeChange = settings.onWeekdayTimeChange,
                                sundayTime = settings.uiState.sundayTime,
                                onSundayTimeChange = settings.onSundayTimeChange,
                                weekendDay = settings.uiState.weekendDay,
                                onWeekendDayChange = settings.onWeekendDayChange,
                                weekdayEnabled = settings.uiState.weekdayEnabled,
                                onWeekdayEnabledChange = settings.onWeekdayEnabledChange,
                                tasteProfile = settings.uiState.tasteProfile,
                                onTasteProfileChange = settings.onTasteProfileChange,
                            )
                        }
                    }
                }
            }
            val showHomeFabs = shouldShowGlobalFloatingActions(currentTab, cookDone != null, handsFreeRecipe != null, showHistory)
            AnimatedVisibility(
                visible = showHomeFabs,
                enter = fadeIn(tween(if (animatorsEnabled) 180 else 0)),
                exit = fadeOut(tween(if (animatorsEnabled) 140 else 0)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
            ) {
                GlobalFloatingActionButtons(
                    onOpenWeeklyPlan = { showWeeklyOverlay = true },
                    onOpenRandomWheel = {
                        val cookable = cookableRecipes(availableRecipes, state)
                        if (cookable.isEmpty()) showGlobalNoCookable = true
                        else {
                            globalRandomPick = pickRandomRecipe(cookable, state, globalRandomPick?.recipe?.id, kotlin.random.Random.Default)
                            showGlobalRandomWheel = globalRandomPick != null
                        }
                    },
                    modifier = Modifier,
                )
            }
            if (showWeeklyOverlay && globalWeeklyPlan != null) FloatingOverlayDialog("Thực đơn cả tuần", { showWeeklyOverlay = false }) {
                WeeklyMealPlanSection(
                    plan = globalWeeklyPlan,
                    goal = weeklyMealPlanState.goal,
                    preferences = weeklyMealPlanState.preferences,
                    onGoal = { onWeeklyMealPlanAction(WeeklyMealPlanAction.SetGoal(it)) },
                    onPreference = { onWeeklyMealPlanAction(WeeklyMealPlanAction.TogglePreference(it)) },
                    onSwap = { onWeeklyMealPlanAction(WeeklyMealPlanAction.SwapDay(it)) },
                    onRequestCook = { cooked -> showWeeklyOverlay = false; openedRecipeId = cooked.id; currentTab = NavTab.RECIPES },
                    updatedRecipeResult = null,
                    weekStart = weeklyMealPlanState.weekStart,
                    onNavigate = { target -> if (target == RecipeScreenMode.SHOPPING) showShoppingOverlay = true },
                    includeLunchMealPrep = weeklyMealPlanState.includeLunchMealPrep,
                    onIncludeLunchMealPrepChange = { onWeeklyMealPlanAction(WeeklyMealPlanAction.SetLunchMealPrep(it)) },
                )
            }
            if (showShoppingOverlay && globalShopping != null) FloatingOverlayDialog("Danh sách đi chợ", { showShoppingOverlay = false }) {
                WeeklyShoppingSection(
                    shopping = globalShopping,
                    uncheckedIds = weeklyMealPlanState.uncheckedIds,
                    onToggleChecked = { id, checked -> onWeeklyMealPlanAction(WeeklyMealPlanAction.ToggleIngredient(id, checked)) },
                    onStock = { ids -> stockIngredients(ids); showShoppingOverlay = false; showWeeklyOverlay = false },
                )
            }
            val globalPick = globalRandomPick
            if (showGlobalRandomWheel && globalPick != null && availableRecipes.any { it.id == globalPick.recipe.id }) RandomWheelDialog(
                pool = cookableRecipes(availableRecipes, state), pick = globalPick,
                onSpinAgain = { globalRandomPick = pickRandomRecipe(availableRecipes, state, globalPick.recipe.id, kotlin.random.Random.Default) },
                onOpenRecipe = { recipe -> showGlobalRandomWheel = false; showHistory = false; cookDone = null; handsFreeRecipeId = null; openedRecipeId = recipe.id; currentTab = NavTab.RECIPES },
                onDismiss = { showGlobalRandomWheel = false },
            )
        }
    }
    if (showGlobalNoCookable) AlertDialog(
        onDismissRequest = { showGlobalNoCookable = false }, containerColor = AppTheme.colors.surface,
        title = {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Nấu ngẫu nhiên", Modifier.weight(1f), color = AppTheme.colors.text)
                IconButton(onClick = { showGlobalNoCookable = false }) { Icon(AppIcons.Close, contentDescription = "Đóng", tint = AppTheme.colors.text) }
            }
        }, text = { Text(RandomRecipeCopy.noCookable, color = AppTheme.colors.muted) }, confirmButton = {},
    )
}
