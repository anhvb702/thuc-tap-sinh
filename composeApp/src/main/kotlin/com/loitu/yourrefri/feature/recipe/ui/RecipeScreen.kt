package com.loitu.yourrefri.feature.recipe.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.recipe.logic.availableRecipesForPlanning
import com.loitu.yourrefri.feature.recipe.logic.buildWeeklyMealPlan
import com.loitu.yourrefri.feature.recipe.logic.buildWeeklyShoppingList
import com.loitu.yourrefri.feature.recipe.logic.computeMysteryUnlockStatus
import com.loitu.yourrefri.feature.recipe.logic.filterRecipesByDiet
import com.loitu.yourrefri.feature.recipe.presentation.RecipeViewModel
import com.loitu.yourrefri.feature.recipe.ui.components.RecipeCookbook
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@Suppress("UNUSED_PARAMETER")
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
    randomPool: List<Recipe>? = null, dietRestriction: DietRestriction = DietRestriction.NONE, onBackHome: (() -> Unit)? = null,
    tasteProfile: FamilyTasteProfile = FamilyTasteProfile(), recipeViewModel: RecipeViewModel = remember { RecipeViewModel() },
    unlockedIds: Set<String> = emptySet(),
    totalCookCount: Int = 0,
    onUnlockMysteryRecipe: (Recipe) -> Unit = {},
    mysteryRewardsReady: Boolean = true,
    mysteryUnlockFailedId: String? = null,
) {
    val colors = AppTheme.colors
    var mode by remember(initialMode) { mutableStateOf(initialMode) }
    var goalLocal by remember { mutableStateOf(DietGoal.BALANCED) }
    var preferencesLocal by remember { mutableStateOf(emptySet<FamilyPreferenceTag>()) }
    var swapsLocal by remember { mutableStateOf(emptyMap<Int, Int>()) }
    var uncheckedLocal by remember { mutableStateOf(emptySet<String>()) }
    var pantrySnapshot by remember { mutableStateOf(state) }
    var updatedResult by remember { mutableStateOf<Pair<String, String>?>(null) }
    var pendingRecipe by remember { mutableStateOf<Recipe?>(null) }
    var selectedRecipeId by rememberSaveable(initialRecipeId) { mutableStateOf(initialRecipeId) }
    var showMysteryWheel by remember { mutableStateOf(false) }

    val available = remember(recipes, unlockedIds) { availableRecipesForPlanning(recipes, unlockedIds) }
    val selectedRecipe = available.firstOrNull { it.id == selectedRecipeId }

    fun updateMode(next: RecipeScreenMode) {
        selectedRecipeId = null
        mode = next
        onModeChange?.invoke(next)
    }
    fun openRecipe(recipe: Recipe) {
        if (onOpenRecipe != null) onOpenRecipe(recipe.id)
        else selectedRecipeId = recipe.id
    }
    fun finishCook(recipe: Recipe) {
        val plan = buildDirectCookPlan(recipe, state, callbackAvailable = onCookRecipe != null)
        if (plan != null) {
            onCookRecipe?.invoke(recipe, plan.ingredientIds)
            updatedResult = recipe.id to plan.resultMessage
        } else {
            pendingRecipe = recipe
        }
    }

    if (onCookRecipe == null) {
        RecipeCookConfirmationHost(
            showConfirmation = pendingRecipe != null,
            pendingRecipe = pendingRecipe,
            presentation = pendingRecipe?.let { recipeCookPresentation(it, state, callbackAvailable = false) },
            onDismiss = { pendingRecipe = null },
            onResult = { id, message -> updatedResult = id to message },
        )
    }

    BackHandler(selectedRecipe != null && onOpenRecipe == null) { selectedRecipeId = null }
    if (selectedRecipe != null && onOpenRecipe == null && mode == RecipeScreenMode.DAILY) {
        RecipeDetailScreen(
            recipe = selectedRecipe,
            state = state,
            onBack = { selectedRecipeId = null },
            onCook = { finishCook(selectedRecipe) },
            modifier = modifier,
            isFavorite = selectedRecipe.id in favoriteIds,
            onToggleFavorite = { onToggleFavorite(selectedRecipe.id) },
            onStartHandsFree = onOpenHandsFree?.let { start -> { start(selectedRecipe.id) } },
            showDoneSnackbar = onCookRecipe != null,
        )
        return
    }

    LaunchedEffect(state.pantry.isNotEmpty()) {
        if (pantrySnapshot.pantry.isEmpty() && state.pantry.isNotEmpty()) pantrySnapshot = state
    }

    if (mode == RecipeScreenMode.DAILY) {
        val entries = remember(recipes, state, unlockedIds) { buildCookbookEntries(recipes, state, unlockedIds) }
        val reward = remember(totalCookCount, recipes, unlockedIds, mysteryRewardsReady) {
            if (mysteryRewardsReady && recipes.any { it.isMystery }) {
                computeMysteryUnlockStatus(totalCookCount, recipes, unlockedIds)
            } else null
        }
        val canSpin = (reward?.availableSpins ?: 0) > 0
        RecipeCookbook(
            entries = entries,
            favoriteIds = favoriteIds,
            reward = reward,
            onSpin = { showMysteryWheel = true },
            onOpenRecipe = ::openRecipe,
            onToggleFavorite = onToggleFavorite,
            modifier = modifier,
        )
        if (showMysteryWheel) {
            MysteryUnlockWheel(
                recipes = recipes,
                unlockedIds = unlockedIds,
                saveFailedId = mysteryUnlockFailedId,
                canSpin = canSpin,
                onUnlocked = onUnlockMysteryRecipe,
                onOpenRecipe = { recipe ->
                    showMysteryWheel = false
                    openRecipe(recipe)
                },
                onDismiss = { showMysteryWheel = false },
            )
        }
        return
    }

    BackHandler { updateMode(RecipeScreenMode.DAILY) }
    val goal = weeklyGoal ?: goalLocal
    val preferences = weeklyPreferences ?: preferencesLocal
    val swaps = weeklySwappedOffsets ?: swapsLocal
    val unchecked = weeklyUncheckedIds ?: uncheckedLocal
    val planningRecipes = remember(available, dietRestriction, state.ingredients) {
        filterRecipesByDiet(available, dietRestriction, state.ingredients).ifEmpty { available }
    }
    val weeklyPlan = remember(planningRecipes, pantrySnapshot, goal, preferences, swaps, tasteProfile, weeklyIncludeLunchMealPrep) {
        buildWeeklyMealPlan(
            planningRecipes, pantrySnapshot, goal, preferences, swaps, tasteProfile,
            separateLunchAndDinner = weeklyIncludeLunchMealPrep ?: false,
        )
    }
    val shopping = remember(weeklyPlan, state, preferences, weeklyIncludeLunchMealPrep) {
        buildWeeklyShoppingList(weeklyPlan, state, preferences, includeLunchMealPrep = weeklyIncludeLunchMealPrep ?: LUNCH_MEAL_PREP_ENABLED)
    }
    val pageScroll = rememberScrollState()
    val scrollScope = rememberCoroutineScope()
    Column(modifier.fillMaxSize().background(colors.background).verticalScroll(pageScroll).padding(16.dp)) {
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBackHome ?: { updateMode(RecipeScreenMode.DAILY) },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(AppIcons.Back, contentDescription = "Quay lại", tint = colors.text)
            }
            Text(
                text = recipeHeadline(mode),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineSmall,
                color = colors.text,
                fontWeight = FontWeight.Bold,
            )
        }
        when (mode) {
            RecipeScreenMode.WEEKLY -> WeeklyMealPlanSection(
                plan = weeklyPlan,
                goal = goal,
                preferences = preferences,
                onGoal = { next ->
                    goalLocal = next
                    onWeeklyAction?.invoke(WeeklyMealPlanAction.SetGoal(next)) ?: onWeeklyGoalChange?.invoke(next)
                },
                onPreference = { tag ->
                    val next = togglePreferenceTag(preferences, tag)
                    preferencesLocal = next
                    onWeeklyAction?.invoke(WeeklyMealPlanAction.TogglePreference(tag)) ?: onWeeklyPreferencesChange?.invoke(next)
                },
                onSwap = { day ->
                    val next = bumpDaySwapOffset(swaps, day)
                    swapsLocal = next
                    onWeeklyAction?.invoke(WeeklyMealPlanAction.SwapDay(day)) ?: onWeeklySwappedOffsetsChange?.invoke(next)
                },
                onRequestCook = ::finishCook,
                updatedRecipeResult = updatedResult,
                weekStart = weekStart,
                includeLunchMealPrep = weeklyIncludeLunchMealPrep ?: false,
                onIncludeLunchMealPrepChange = { enabled ->
                    onWeeklyAction?.invoke(WeeklyMealPlanAction.SetLunchMealPrep(enabled))
                        ?: onWeeklyIncludeLunchMealPrepChange?.invoke(enabled)
                },
                onNavigate = { next -> updateMode(next); scrollScope.launch { pageScroll.scrollTo(0) } },
            )
            RecipeScreenMode.SHOPPING -> WeeklyShoppingSection(
                shopping = shopping,
                uncheckedIds = unchecked,
                onToggleChecked = { id, checked ->
                    val next = if (checked) unchecked - id else unchecked + id
                    uncheckedLocal = next
                    onWeeklyAction?.invoke(WeeklyMealPlanAction.ToggleIngredient(id, checked)) ?: onWeeklyUncheckedIdsChange?.invoke(next)
                },
                onStock = onStockIngredients,
            )
            RecipeScreenMode.DAILY -> Unit
        }
    }
}
