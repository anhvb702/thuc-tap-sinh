package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.recipe.logic.buildWeeklyMealPlan
import com.loitu.yourrefri.feature.recipe.logic.buildWeeklyShoppingList
import com.loitu.yourrefri.feature.recipe.presentation.RecipeEffect
import com.loitu.yourrefri.feature.recipe.presentation.RecipeIntent
import com.loitu.yourrefri.feature.recipe.presentation.RecipeUiState
import com.loitu.yourrefri.feature.recipe.presentation.RecipeViewModel
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
@OptIn(ExperimentalTime::class)
fun RecipeScreen(
    state: PantryGridState, recipes: List<Recipe>, weekStart: kotlinx.datetime.LocalDate? = null, initialRecipeId: String? = null,
    onCookRecipe: ((Recipe, List<String>) -> Unit)? = null, initialMode: RecipeScreenMode = RecipeScreenMode.DAILY,
    onStockIngredients: ((List<String>) -> Unit)? = null, weeklyGoal: DietGoal? = null, onWeeklyGoalChange: ((DietGoal) -> Unit)? = null,
    weeklyPreferences: Set<FamilyPreferenceTag>? = null, onWeeklyPreferencesChange: ((Set<FamilyPreferenceTag>) -> Unit)? = null,
    weeklySwappedOffsets: Map<Int, Int>? = null, onWeeklySwappedOffsetsChange: ((Map<Int, Int>) -> Unit)? = null,
    weeklyUncheckedIds: Set<String>? = null, onWeeklyUncheckedIdsChange: ((Set<String>) -> Unit)? = null,
    weeklyIncludeLunchMealPrep: Boolean? = null, onWeeklyIncludeLunchMealPrepChange: ((Boolean) -> Unit)? = null,
    onWeeklyAction: ((WeeklyMealPlanAction) -> Unit)? = null, onModeChange: ((RecipeScreenMode) -> Unit)? = null,
    onOpenRecipe: ((String) -> Unit)? = null, modifier: Modifier = Modifier, favoriteIds: Set<String> = emptySet(),
    onToggleFavorite: (String) -> Unit = {}, onOpenHandsFree: ((String) -> Unit)? = null, todayIndexOverride: Int? = null,
    randomPool: List<Recipe>? = null, dietRestriction: DietRestriction = DietRestriction.NONE, onBackHome: () -> Unit = {},
    tasteProfile: FamilyTasteProfile = FamilyTasteProfile(), recipeViewModel: RecipeViewModel = remember { RecipeViewModel() },
) {
    val colors = AppTheme.colors
    val recipeState by recipeViewModel.state.collectAsState()
    var mode by remember(initialMode) { mutableStateOf(initialMode) }
    var goalLocal by remember { mutableStateOf(DietGoal.BALANCED) }
    var preferencesLocal by remember { mutableStateOf(emptySet<FamilyPreferenceTag>()) }
    var swapsLocal by remember { mutableStateOf(emptyMap<Int, Int>()) }
    var uncheckedLocal by remember { mutableStateOf(emptySet<String>()) }
    var pantrySnapshot by remember { mutableStateOf(state) }
    var updatedResult by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showCookConfirmation by remember { mutableStateOf(false) }
    var pendingRecipe by remember { mutableStateOf<Recipe?>(null) }
    val stepChecks = remember { mutableStateMapOf<String, Boolean>() }
    val goal = weeklyGoal ?: goalLocal
    val preferences = weeklyPreferences ?: preferencesLocal
    val swaps = weeklySwappedOffsets ?: swapsLocal
    val unchecked = weeklyUncheckedIds ?: uncheckedLocal
    LaunchedEffect(state, recipes, randomPool, tasteProfile) { recipeViewModel.onIntent(RecipeIntent.SyncInputs(state, recipes, randomPool, tasteProfile)) }
    LaunchedEffect(recipeViewModel) { recipeViewModel.effects.collect { effect -> if (effect is RecipeEffect.OpenRecipeDetail) onOpenRecipe?.invoke(effect.recipeId) } }
    LaunchedEffect(initialRecipeId) { if (shouldClearDurationForRequest(initialRecipeId)) recipeViewModel.onIntent(RecipeIntent.SelectDuration(null)) }
    LaunchedEffect(state.pantry.isNotEmpty()) { if (pantrySnapshot.pantry.isEmpty() && state.pantry.isNotEmpty()) pantrySnapshot = state }
    val weeklyPlan = remember(recipes, pantrySnapshot, goal, preferences, swaps, tasteProfile) { buildWeeklyMealPlan(recipes, pantrySnapshot, goal, preferences, swaps, tasteProfile) }
    val shopping = remember(weeklyPlan, state, preferences, weeklyIncludeLunchMealPrep) { buildWeeklyShoppingList(weeklyPlan, state, preferences, includeLunchMealPrep = weeklyIncludeLunchMealPrep ?: LUNCH_MEAL_PREP_ENABLED) }
    val recommendationIds = recipeState.catalog.recommendations.map { it.recipe.id }
    val resolvedInitialRecipeId = resolveInitialRecipeId(initialRecipeId, recommendationIds)
    val expandInPlace = onOpenRecipe == null
    var selectedRecipeId by remember(recommendationIds, initialRecipeId) { mutableStateOf(resolvedInitialRecipeId.takeIf { initialRecipeId != null }) }
    val pendingPresentation = pendingRecipe?.let { recipeCookPresentation(it, state, onCookRecipe != null) }
    RecipeCookConfirmationHost(showCookConfirmation, pendingRecipe, pendingPresentation, state, onCookRecipe != null, onCookRecipe, { showCookConfirmation = false; pendingRecipe = null }) { id, message -> updatedResult = id to message }
    if (recipeState.showNoCookableDialog) AlertDialog(onDismissRequest = { recipeViewModel.onIntent(RecipeIntent.DismissNoCookableDialog) }, containerColor = colors.surface, title = { Text("Nấu ngẫu nhiên", color = colors.text) }, text = { Text(RandomRecipeCopy.noCookable, color = colors.muted) }, confirmButton = { TextButton(onClick = { recipeViewModel.onIntent(RecipeIntent.DismissNoCookableDialog) }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Đóng", color = colors.accentText) } })
    val todayIndex = todayIndexOverride ?: remember { todayDayIndex(Clock.System.now(), TimeZone.currentSystemDefault()) }
    val pageScroll = rememberScrollState()
    val scrollScope = rememberCoroutineScope()
    Column(modifier.fillMaxSize().background(colors.background).verticalScroll(pageScroll).padding(16.dp)) {
        fun updateMode(next: RecipeScreenMode) {
            mode = next
            onModeChange?.invoke(next)
        }
        RecipeScreenTitle()
        RecipeTabChips(mode, { updateMode(it) }, Modifier.padding(top = 12.dp, bottom = 16.dp))
        when (mode) {
            RecipeScreenMode.WEEKLY -> WeeklyMealPlanSection(
                plan = weeklyPlan,
                goal = goal,
                preferences = preferences,
                onGoal = { next -> goalLocal = next; onWeeklyAction?.invoke(WeeklyMealPlanAction.SetGoal(next)) ?: onWeeklyGoalChange?.invoke(next) },
                onPreference = { tag -> val next = togglePreferenceTag(preferences, tag); preferencesLocal = next; onWeeklyAction?.invoke(WeeklyMealPlanAction.TogglePreference(tag)) ?: onWeeklyPreferencesChange?.invoke(next) },
                onSwap = { day -> val next = bumpDaySwapOffset(swaps, day); swapsLocal = next; onWeeklyAction?.invoke(WeeklyMealPlanAction.SwapDay(day)) ?: onWeeklySwappedOffsetsChange?.invoke(next) },
                onRequestCook = { recipe -> pendingRecipe = recipe; showCookConfirmation = true },
                updatedRecipeResult = updatedResult,
                weekStart = weekStart,
                includeLunchMealPrep = weeklyIncludeLunchMealPrep ?: false,
                onIncludeLunchMealPrepChange = { enabled -> onWeeklyAction?.invoke(WeeklyMealPlanAction.SetLunchMealPrep(enabled)) ?: onWeeklyIncludeLunchMealPrepChange?.invoke(enabled) },
                onNavigate = { next -> updateMode(next); scrollScope.launch { pageScroll.scrollTo(0) } },
            )
            RecipeScreenMode.SHOPPING -> WeeklyShoppingSection(shopping, unchecked, { id, checked -> val next = if (checked) unchecked - id else unchecked + id; uncheckedLocal = next; onWeeklyAction?.invoke(WeeklyMealPlanAction.ToggleIngredient(id, checked)) ?: onWeeklyUncheckedIdsChange?.invoke(next) }, onStockIngredients)
            RecipeScreenMode.DAILY -> DailyRecipeContent(
                state = state,
                randomPool = randomPool,
                recipeState = recipeState,
                initialId = resolvedInitialRecipeId.takeIf { initialRecipeId != null },
                selectedId = selectedRecipeId,
                onSelect = { selectedRecipeId = it; updatedResult = null },
                expandInPlace = expandInPlace,
                stepChecks = stepChecks,
                updatedResult = updatedResult,
                onCooked = { pendingRecipe = it; showCookConfirmation = true },
                weeklyPlan = weeklyPlan,
                todayIndex = todayIndex,
                shopping = shopping,
                onGoTo = { updateMode(it) },
                favoriteIds = favoriteIds,
                onToggleFavorite = onToggleFavorite,
                onOpenHandsFree = onOpenHandsFree,
                onBackHome = onBackHome,
                onOpenRecipe = onOpenRecipe,
                viewModel = recipeViewModel,
            )
        }
    }
}

@Composable
private fun DailyRecipeContent(state: PantryGridState, randomPool: List<Recipe>?, recipeState: RecipeUiState, initialId: String?, selectedId: String?, onSelect: (String) -> Unit, expandInPlace: Boolean, stepChecks: MutableMap<String, Boolean>, updatedResult: Pair<String, String>?, onCooked: (Recipe) -> Unit, weeklyPlan: List<com.loitu.yourrefri.feature.recipe.logic.PlannedDayMeal>, todayIndex: Int, shopping: List<com.loitu.yourrefri.feature.recipe.logic.ShoppingListItem>, onGoTo: (RecipeScreenMode) -> Unit, favoriteIds: Set<String>, onToggleFavorite: (String) -> Unit, onOpenHandsFree: ((String) -> Unit)?, onBackHome: () -> Unit, onOpenRecipe: ((String) -> Unit)?, viewModel: RecipeViewModel) {
    val colors = AppTheme.colors
    if (isPantryEmpty(state)) { EmptyFridgeGate({ onGoTo(RecipeScreenMode.SHOPPING) }, onBackHome, Modifier.padding(top = 12.dp)); return }
    if (randomPool != null) Button({ viewModel.onIntent(RecipeIntent.OpenRandomWheel) }, colors = ButtonDefaults.buttonColors(containerColor = colors.primarySoft, contentColor = colors.accentText), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).heightIn(min = 48.dp)) { Icon(AppIcons.Wheel, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text(RandomRecipeCopy.button, fontWeight = FontWeight.Bold) }
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { recipeDurationFilterOptions().forEach { option -> PillChip(option.label, recipeState.selectedDuration == option.threshold, { viewModel.onIntent(RecipeIntent.SelectDuration(option.threshold)) }) } }
    if (recipeState.showRandomWheel && recipeState.randomPick != null) RandomWheelDialog(recipeState.catalog.cookableRandomPool, recipeState.randomPick, { viewModel.onIntent(RecipeIntent.SpinRandomAgain) }, { viewModel.onIntent(RecipeIntent.SelectRandomRecipe(it.id)) }, { viewModel.onIntent(RecipeIntent.DismissRandomWheel) })
    val visible = recipeState.catalog.visibleRecipes
    val hero = heroRecommendations(visible, initialId)
    Spacer(Modifier.height(12.dp))
    if (recipeState.catalog.recommendations.isEmpty()) HeroEmptyCard() else if (visible.isEmpty()) {
        Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("Chưa có món trong khoảng thời gian này.", color = colors.text); PillChip("Xem tất cả món", false, { viewModel.onIntent(RecipeIntent.SelectDuration(null)) }, Modifier.padding(top = 12.dp)) }
    } else {
        key(hero.map { it.recipe.id }) {
            Text(recipeSectionLabel(hero.first().reason), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = colors.accentText, modifier = Modifier.padding(bottom = 8.dp))
            val pagerState = rememberPagerState(initialPage = 0) { hero.size }
            RecipeHeroPager(
                items = hero,
                pagerState = pagerState,
                favoriteIds = favoriteIds,
                onToggleFavorite = onToggleFavorite,
                onOpen = { id -> if (onOpenRecipe != null) onOpenRecipe(id) else onSelect(id) },
            )
            val current = hero.getOrNull(pagerState.currentPage) ?: hero.first()
            HeroIngredientHints(current.recipe, state)
            if (expandInPlace) hero.firstOrNull { it.recipe.id == selectedId }?.let { selected -> InlineRecipeSteps(selected.recipe, stepChecks, updatedResult?.takeIf { it.first == selected.recipe.id }?.second) { onCooked(selected.recipe) } }
            DailyShortcuts(weeklyPlan, todayIndex, shopping, onGoTo, current.recipe.id.takeIf { onOpenHandsFree != null }, onOpenHandsFree)
        }
    }
    if (recipeState.catalog.recommendations.isEmpty() || visible.isEmpty()) DailyShortcuts(weeklyPlan, todayIndex, shopping, onGoTo, null, null)
}
