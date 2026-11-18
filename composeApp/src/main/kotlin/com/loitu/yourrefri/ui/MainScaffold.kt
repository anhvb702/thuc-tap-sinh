package com.loitu.yourrefri.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loitu.yourrefri.feature.home.ui.HomeScreen
import com.loitu.yourrefri.feature.pantry.data.LeftoverItem
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.ui.PantryGridScreen
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.rememberSaveable
import com.loitu.yourrefri.feature.recipe.ui.RecipeDetailScreen
import com.loitu.yourrefri.feature.recipe.ui.RecipeScreen
import com.loitu.yourrefri.feature.recipe.ui.HandsFreeCookScreen
import com.loitu.yourrefri.feature.recipe.logic.defaultMealFor
import com.loitu.yourrefri.feature.recipe.ui.CookLeftoverDialog
import com.loitu.yourrefri.feature.recipe.ui.findRecipeById
import com.loitu.yourrefri.feature.recipe.ui.ingredientsToConsume
import com.loitu.yourrefri.feature.recipe.ui.CookDone
import com.loitu.yourrefri.feature.recipe.ui.CookDoneScreen
import com.loitu.yourrefri.feature.recipe.ui.cookDoneNames
import com.loitu.yourrefri.feature.recipe.ui.cookDoneSummary
import com.loitu.yourrefri.feature.recipe.ui.cookUndoActions
import com.loitu.yourrefri.feature.recipe.ui.leftoverShelfDaysFor
import com.loitu.yourrefri.feature.recipe.ui.recipeCookPresentation
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.filterRecipesByDiet
import com.loitu.yourrefri.feature.recipe.logic.isDietFilterTooStrict
import com.loitu.yourrefri.feature.settings.ui.SettingsPresentation
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.loitu.yourrefri.feature.recipe.ui.WeeklyMealPlanAction
import com.loitu.yourrefri.feature.recipe.ui.WeeklyMealPlanState
import com.loitu.yourrefri.feature.recipe.ui.recipeRequestForAllRecipes
import com.loitu.yourrefri.feature.recipe.ui.notificationDetailRequest
import com.loitu.yourrefri.feature.recipe.ui.recipeRequestForHomeCard
import com.loitu.yourrefri.feature.recipe.ui.RecipeScreenMode
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.settings.ui.SettingsScreen
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import com.loitu.yourrefri.ui.theme.ThemeMode

// Nhãn tab cho phép xuống dòng khi cỡ chữ hệ thống lớn, để không cắt còn một từ.
internal const val NAV_LABEL_MAX_LINES = 2

// Chữ nhãn tab không vượt 18sp hiển thị, kể cả khi cỡ chữ hệ thống lớn.
internal const val NAV_LABEL_MAX_DISPLAY_SP = 18f

// Cỡ chữ đặt cho Text (sp). Compose nhân với fontScale hệ thống, nên chia lại để chữ hiển thị bị chặn.
internal fun navLabelFontSizeSp(fontScale: Float): Float =
    if (fontScale <= 0f) 14f else minOf(14f, NAV_LABEL_MAX_DISPLAY_SP / fontScale)

internal fun dietAdjustmentTab(): NavTab = NavTab.SETTINGS

enum class NavTab(val label: String, val icon: ImageVector) {
    HOME("Trang chủ", AppIcons.Home),
    PANTRY("Tủ lạnh", AppIcons.Fridge),
    RECIPES("Món ăn", AppIcons.Pot),
    SETTINGS("Cài đặt", AppIcons.User),
}

@Composable
fun MainScaffold(
    state: PantryGridState,
    weeklyMealPlanState: WeeklyMealPlanState,
    onWeeklyMealPlanAction: (WeeklyMealPlanAction) -> Unit,
    leftovers: List<LeftoverItem> = emptyList(),
    defrostItems: List<String> = emptyList(),
    onAddLeftover: (title: String, compartment: PantryCompartment, shelfDays: Int) -> Unit = { _, _, _ -> },
    onToggleLeftoverCompartment: (LeftoverItem) -> Unit = {},
    onUpdateLeftoverShelfDays: (LeftoverItem, Int) -> Unit = { _, _ -> },
    onDeleteLeftover: (String) -> Unit = {},
    onAddToExtraShopping: (String) -> Unit = {},
    onRemoveExtraShoppingItem: (String) -> Unit = {},
    onClearExtraShoppingItems: () -> Unit = {},
    snackbarHostState: SnackbarHostState,
    recipes: List<Recipe>,
    customRecipeIds: Set<String> = emptySet(),
    onAddCustomRecipe: (String, List<String>) -> Unit = { _, _ -> },
    onEditCustomRecipe: (Recipe) -> Unit = {},
    onDeleteCustomRecipe: (String) -> Unit = {},
    onAddDailyShopping: (List<String>) -> Unit = {},
    onAction: (PantryGridAction) -> Unit,
    backupStatus: String? = null,
    restoreConfirmationPending: Boolean = false,
    onExportBackup: () -> Unit = {},
    onImportBackup: () -> Unit = {},
    onConfirmRestore: () -> Unit = {},
    onCancelRestore: () -> Unit = {},
    reminderEnabled: Boolean = false,
    onReminderChanged: (Boolean) -> Unit = {},
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    favoriteIds: Set<String> = emptySet(),
    onToggleFavorite: (String) -> Unit = {},
    dietRestriction: DietRestriction = DietRestriction.NONE,
    onDietRestrictionChange: (DietRestriction) -> Unit = {},
    appVersionName: String = "",
    sundayTime: kotlinx.datetime.LocalTime = kotlinx.datetime.LocalTime(8, 0),
    onSundayTimeChange: (kotlinx.datetime.LocalTime) -> Unit = {},
    weekdayTime: kotlinx.datetime.LocalTime = kotlinx.datetime.LocalTime(16, 0),
    onWeekdayTimeChange: (kotlinx.datetime.LocalTime) -> Unit = {},
    // Ghi "Đã nấu": nhận id bản ghi (hoặc null nếu không ghi được) để Hoàn tác xóa đúng bản ghi đó.
    onRecordCook: (recipeId: String, savedIngredientIds: List<String>, meal: com.loitu.yourrefri.feature.recipe.data.MealSlot, onResult: (Long?) -> Unit) -> Unit = { _, _, _, done -> done(null) },
    currentHour: (() -> Int)? = null,
    onDeleteCookLog: (Long) -> Unit = {},
    cookHistoryState: com.loitu.yourrefri.feature.cooklog.ui.CookHistoryState = com.loitu.yourrefri.feature.cooklog.ui.CookHistoryState(),
    openRecipe: Pair<String, Long>? = null,
    modifier: Modifier = Modifier,
) {
    var currentTab by remember { mutableStateOf(NavTab.HOME) } // Mặc định mở Trang chủ theo triết lý Warm Minimalism
    var requestedRecipeId by remember { mutableStateOf<String?>(null) }
    var requestedRecipeMode by remember { mutableStateOf(RecipeScreenMode.DAILY) }
    // Công thức đang mở ở màn chi tiết. Tạm dùng state cục bộ; khi có Navigation Compose sẽ thành route RecipeDetail.
    var openedRecipeId by rememberSaveable { mutableStateOf<String?>(null) }
    // Công thức đang nấu ở chế độ rảnh tay (màn riêng, tạm dùng state cục bộ như màn chi tiết).
    var handsFreeRecipeId by rememberSaveable { mutableStateOf<String?>(null) }
    // Màn "Đã nấu" mở từ Trang chủ, đóng bằng nút quay lại hoặc khi đổi tab.
    var showHistory by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showHistory && handsFreeRecipeId == null) { showHistory = false }
    BackHandler(enabled = handsFreeRecipeId != null) { handsFreeRecipeId = null }
    BackHandler(enabled = handsFreeRecipeId == null && openedRecipeId != null) { openedRecipeId = null }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val weekKey = weeklyMealPlanState.weekStart.toString()
    val confirmationPrefs = remember(context) { context.getSharedPreferences("weekly_confirmation", Context.MODE_PRIVATE) }
    var weeklyConfirmed by remember(weekKey) { mutableStateOf(confirmationPrefs.getBoolean(weekKey, false)) }
    val weeklyCookedRecipeIdsByDay = remember(cookHistoryState) {
        cookHistoryState.weeks
            .filter { it.label == com.loitu.yourrefri.feature.cooklog.logic.CookHistoryWeekLabel.THIS_WEEK }
            .flatMap { it.entries }
            .groupBy { it.cookedOn.dayOfWeek.ordinal }
            .mapValues { (_, logs) -> logs.mapTo(linkedSetOf()) { it.recipeId } }
    }

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
    // Bấm thông báo nhắc có món: mở thẳng màn chi tiết của món đó, giống đường Trang chủ.
    // Đóng các màn phụ đang mở để không che mất món (id đã được kiểm tra ở App).
    LaunchedEffect(openRecipe) {
        openRecipe?.let { (recipeId, _) ->
            cookDone = null
            handsFreeRecipeId = null
            showHistory = false
            requestedRecipeId = recipeRequestForHomeCard(recipeId)
            requestedRecipeMode = RecipeScreenMode.DAILY
            openedRecipeId = notificationDetailRequest(recipeId)
            currentTab = NavTab.RECIPES
        }
    }

    // Căn cứ Q2: Chế độ ăn là bộ lọc cứng (không nới); lọc hết món thì để rỗng và màn tự gợi ý điều chỉnh chế độ ăn.
    val dietTooStrict = remember(recipes, dietRestriction, state.ingredients) {
        isDietFilterTooStrict(recipes, dietRestriction, state.ingredients)
    }
    val visibleRecipes = remember(recipes, dietRestriction, state.ingredients) {
        filterRecipesByDiet(recipes, dietRestriction, state.ingredients)
    }
    LaunchedEffect(dietTooStrict, dietRestriction) {
        if (dietTooStrict) snackbarHostState.showSnackbar(SettingsPresentation.dietTooStrictNotice)
    }
    LaunchedEffect(currentTab) {
        snackbarHostState.currentSnackbarData?.dismiss()
    }
    val handsFreeRecipe = findRecipeById(handsFreeRecipeId, visibleRecipes, recipes)

    // Một chỗ duy nhất khi nấu xong (cả ba luồng): trừ nguyên liệu đang có, ghi một dòng vào "Đã nấu", hiện màn chúc mừng.
    fun completeCook(recipe: Recipe, consumedIds: List<String>) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val before = state
        val ids = consumedIds.distinct().filter { before.pantry[it]?.have == true }
        ids.forEach { onAction(PantryGridAction.ConsumeIngredient(it)) }
        // Ghi đúng bữa người dùng đang chọn ở Trang chủ (chưa chọn thì theo giờ lúc bấm nấu xong).
        val actualMeal = chosenMeal ?: defaultMealFor(currentHour?.invoke() ?: state.hourOfDay)
        onRecordCook(recipe.id, ids, actualMeal) { logId ->
            if (ids.isNotEmpty()) scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val snackbarJob = launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Đã cập nhật trong tủ",
                        actionLabel = "Hoàn tác",
                        withDismissAction = true,
                        duration = SnackbarDuration.Indefinite,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        cookUndoActions(before, ids).forEach(onAction)
                        logId?.let(onDeleteCookLog)
                    }
                }
                delay(5_000)
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarJob.join()
            }
        }
        val namesById = before.ingredients.associate { it.id to it.nameVi }
        cookDone = CookDone(
            recipeTitle = recipe.title,
            summary = cookDoneSummary(cookDoneNames(ids, namesById)),
        )
        handsFreeRecipeId = null
        openedRecipeId = null
        showHistory = false
    }

    // Nấu rảnh tay xong: trừ nguyên liệu như nút "Nấu xong" ở màn công thức, rồi hiện màn chúc mừng (có Hoàn tác).
    // Phần người dùng đánh dấu "còn lại" ở lại trong tủ và không bị hoàn tác.
    fun finishHandsFree(recipe: Recipe, kept: Set<String>) {
        val ids = ingredientsToConsume(recipeCookPresentation(recipe, state, callbackAvailable = true).ingredientIds, kept)
        completeCook(recipe, ids)
    }

    // Kết thúc rảnh tay mặc định trừ nguyên liệu; lựa chọn giữ phần dư mở hộp riêng.
    var leftoverRecipe by remember { mutableStateOf<Recipe?>(null) }
    fun requestFinishHandsFree(recipe: Recipe) {
        finishHandsFree(recipe, emptySet())
    }
    leftoverRecipe?.let { pending ->
        val names = state.ingredients.associate { it.id to it.nameVi }
        val candidates = recipeCookPresentation(pending, state, callbackAvailable = true).ingredientIds.distinct()
        CookLeftoverDialog(
            ingredients = candidates.map { it to (names[it] ?: it) },
            onConfirm = { kept ->
                leftoverRecipe = null
                finishHandsFree(pending, kept)
            },
            onDismiss = { leftoverRecipe = null },
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
            val colors = AppTheme.colors
            // Đang nấu rảnh tay thì ẩn thanh tab để màn hình tập trung vào bước nấu.
            if (handsFreeRecipe == null) NavigationBar(
                containerColor = colors.surface,
                tonalElevation = 0.dp,
            ) {
                NavTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (tab != currentTab) requestedRecipeId = null
                            cookDone = null
                            showHistory = false
                            openedRecipeId = null
                            currentTab = tab
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = navLabelFontSizeSp(LocalDensity.current.fontScale).sp,
                                maxLines = NAV_LABEL_MAX_LINES,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) colors.ctaText else colors.muted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = colors.ctaText,
                            unselectedIconColor = colors.muted,
                            selectedTextColor = colors.ctaText,
                            unselectedTextColor = colors.muted,
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
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
                    onSaveLeftover = { title, compartment ->
                        onAddLeftover(title, compartment, leftoverShelfDaysFor(compartment))
                    },
                )
            } else if (handsFreeRecipe != null) {
                HandsFreeCookScreen(
                    recipe = handsFreeRecipe,
                    deductionPreview = recipeCookPresentation(handsFreeRecipe, state, callbackAvailable = true).deductionPreview,
                    onClose = { handsFreeRecipeId = null },
                    onFinished = { requestFinishHandsFree(handsFreeRecipe) },
                    onKeepLeftovers = { leftoverRecipe = handsFreeRecipe },
                )
            } else if (showHistory && currentTab == NavTab.HOME) {
                com.loitu.yourrefri.feature.cooklog.ui.CookHistoryScreen(
                    state = cookHistoryState,
                    recipes = recipes,
                    onOpenRecipe = { recipeId ->
                        // Mở lại công thức ở tab Món ăn; dùng danh sách chưa lọc nên món cũ ngoài chế độ ăn vẫn mở được.
                        requestedRecipeId = recipeRequestForHomeCard(recipeId)
                        requestedRecipeMode = RecipeScreenMode.DAILY
                        openedRecipeId = recipeId
                        showHistory = false
                        currentTab = NavTab.RECIPES
                    },
                    onBack = { showHistory = false },
                    onDelete = onDeleteCookLog,
                )
            } else when (currentTab) {
                NavTab.HOME -> HomeScreen(
                    state = state,
                    recipes = visibleRecipes,
                    onNavigateToPantry = { currentTab = NavTab.PANTRY },
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
                    onAddDailyShopping = onAddDailyShopping,
                    onAdjustDiet = { currentTab = dietAdjustmentTab() },
                    meal = currentMeal,
                    onMealChange = { chosenMeal = it },
                    leftovers = leftovers,
                    defrostItems = defrostItems,
                    onOpenLeftovers = { currentTab = NavTab.PANTRY },
                )
                NavTab.PANTRY -> PantryGridScreen(
                    state = state,
                    onAction = onAction,
                    snackbarHostState = snackbarHostState,
                    onCookFromSoon = {
                        requestedRecipeId = recipeRequestForAllRecipes()
                        requestedRecipeMode = RecipeScreenMode.DAILY
                        currentTab = NavTab.RECIPES
                    },
                    leftovers = leftovers,
                    onAddLeftover = onAddLeftover,
                    onToggleLeftoverCompartment = onToggleLeftoverCompartment,
                    onUpdateLeftoverShelfDays = onUpdateLeftoverShelfDays,
                    onDeleteLeftover = onDeleteLeftover,
                    onAddToExtraShopping = onAddToExtraShopping,
                )
                NavTab.RECIPES -> {
                    // Món mở từ danh sách hoặc kết quả ngẫu nhiên được tra cứu trong danh sách đã lọc trước.
                    val opened = findRecipeById(openedRecipeId, visibleRecipes, recipes)
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
                    } else RecipeScreen(
                    state = state,
                    recipes = visibleRecipes,
                    customRecipes = recipes.filter { it.id in customRecipeIds },
                    onAddCustomRecipe = onAddCustomRecipe,
                    onEditCustomRecipe = onEditCustomRecipe,
                    onDeleteCustomRecipe = onDeleteCustomRecipe,
                    favoriteIds = favoriteIds,
                    onToggleFavorite = onToggleFavorite,
                    onOpenHandsFree = { handsFreeRecipeId = it },
                    dietRestriction = dietRestriction,
                    dietFilteredEmpty = dietTooStrict,
                    onAdjustDiet = { currentTab = dietAdjustmentTab() },
                    initialRecipeId = requestedRecipeId,
                    initialMode = requestedRecipeMode,
                    onWeeklyAction = onWeeklyMealPlanAction,
                    extraShoppingItems = weeklyMealPlanState.extraShoppingItems,
                    onRemoveExtraShoppingItem = onRemoveExtraShoppingItem,
                    onClearExtraShoppingItems = onClearExtraShoppingItems,
                    weeklyGoal = weeklyMealPlanState.goal,
                    weeklyPreferences = weeklyMealPlanState.preferences,
                    weeklySwappedOffsets = weeklyMealPlanState.swappedOffsets,
                    weeklyUncheckedIds = weeklyMealPlanState.uncheckedIds,
                    weeklyDisabledMeals = weeklyMealPlanState.disabledMeals,
                    weeklyIncludeLunchMealPrep = weeklyMealPlanState.includeLunchMealPrep,
                    onModeChange = { requestedRecipeMode = it },
                    weeklyCookedRecipeIdsByDay = weeklyCookedRecipeIdsByDay,
                    weeklyConfirmed = weeklyConfirmed,
                    onConfirmWeek = {
                        confirmationPrefs.edit().putBoolean(weekKey, true).apply()
                        weeklyConfirmed = true
                    },
                    onOpenRecipe = { openedRecipeId = it },
                    onCookRecipe = { cooked, ingredientIds -> completeCook(cooked, ingredientIds) },
                    onStockShopping = { items ->
                        items.forEach { item ->
                            val current = state.pantry[item.id]
                            if (current?.have == true) onAction(PantryGridAction.IncreaseQuantity(item.id))
                            else onAction(PantryGridAction.AddIngredient(item.id, null))
                            if ((current?.compartment ?: com.loitu.yourrefri.feature.pantry.data.PantryCompartment.CHILL) != item.compartment) {
                                onAction(PantryGridAction.ToggleCompartment(item.id))
                            }
                        }
                    },
                    onStockWeekend = { items ->
                        items.groupBy { it.first }.forEach { (id, rows) ->
                            val current = state.pantry[id]
                            if (current?.have == true) repeat(rows.size) { onAction(PantryGridAction.IncreaseQuantity(id)) }
                            else {
                                onAction(PantryGridAction.AddIngredient(id, null))
                                repeat(rows.size - 1) { onAction(PantryGridAction.IncreaseQuantity(id)) }
                            }
                            if ((current?.compartment ?: com.loitu.yourrefri.feature.pantry.data.PantryCompartment.CHILL) != rows.first().second) {
                                onAction(PantryGridAction.ToggleCompartment(id))
                            }
                        }
                    },
                    onStockIngredients = { ids ->
                        ids.groupingBy { it }.eachCount().forEach { (id, needed) ->
                            val alreadyHave = state.pantry[id]?.have == true
                            if (!alreadyHave) {
                                onAction(PantryGridAction.AddIngredient(id, null))
                                repeat((needed - 1).coerceAtLeast(0)) {
                                    onAction(PantryGridAction.IncreaseQuantity(id))
                                }
                            } else {
                                repeat(needed.coerceAtLeast(0)) {
                                    onAction(PantryGridAction.IncreaseQuantity(id))
                                }
                            }
                        }
                    },
                    )
                }
                NavTab.SETTINGS -> SettingsScreen(
                    backupStatus = backupStatus,
                    restoreConfirmationPending = restoreConfirmationPending,
                    onExportBackup = onExportBackup,
                    onImportBackup = onImportBackup,
                    onConfirmRestore = onConfirmRestore,
                    onCancelRestore = onCancelRestore,
                    reminderEnabled = reminderEnabled,
                    onReminderChanged = onReminderChanged,
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    weeklyGoal = weeklyMealPlanState.goal,
                    onWeeklyGoalChange = { onWeeklyMealPlanAction(WeeklyMealPlanAction.SetGoal(it)) },
                    weeklyPreferences = weeklyMealPlanState.preferences,
                    onWeeklyPreferencesChange = { wanted ->
                        // Màn cài đặt trả về cả tập khẩu vị; ViewModel chỉ có "đổi một khẩu vị", nên đổi từng cái khác nhau.
                        val current = weeklyMealPlanState.preferences
                        (wanted - current).forEach { onWeeklyMealPlanAction(WeeklyMealPlanAction.TogglePreference(it)) }
                        (current - wanted).forEach { onWeeklyMealPlanAction(WeeklyMealPlanAction.TogglePreference(it)) }
                    },
                    dietRestriction = dietRestriction,
                    onDietRestrictionChange = onDietRestrictionChange,
                    appVersionName = appVersionName,
                    sundayTime = sundayTime,
                    onSundayTimeChange = onSundayTimeChange,
                    weekdayTime = weekdayTime,
                    onWeekdayTimeChange = onWeekdayTimeChange,
                )
            }
        }
    }
}
