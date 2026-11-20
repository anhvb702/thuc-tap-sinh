package com.loitu.yourrefri.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.cooklog.ui.CookHistoryCopy
import com.loitu.yourrefri.feature.home.presentation.HomeEffect
import com.loitu.yourrefri.feature.home.presentation.HomeIntent
import com.loitu.yourrefri.feature.home.presentation.HomeViewModel
import com.loitu.yourrefri.feature.home.logic.homeGreeting
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.ui.estimateDaysText
import com.loitu.yourrefri.feature.pantry.ui.isFreezerItem
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.recipe.logic.defaultMealFor
import com.loitu.yourrefri.feature.recipe.ui.MealCopy
import com.loitu.yourrefri.feature.recipe.ui.PillChip
import com.loitu.yourrefri.feature.recipe.ui.RandomRecipeCopy
import com.loitu.yourrefri.feature.recipe.ui.RandomWheelDialog
import com.loitu.yourrefri.feature.recipe.ui.ShortcutCard
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import kotlinx.coroutines.flow.collect



@Composable
fun HomeScreen(
    state: PantryGridState,
    recipes: List<Recipe>,
    onNavigateToPantry: () -> Unit,
    onOpenShopping: () -> Unit = onNavigateToPantry,
    onNavigateToRecipes: () -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    onNavigateToWeeklyRecipes: () -> Unit = onNavigateToRecipes,
    onNavigateToHistory: (() -> Unit)? = null,
    meal: MealSlot = defaultMealFor(state.hourOfDay),
    onMealChange: (MealSlot) -> Unit = {},
    modifier: Modifier = Modifier,
    tasteProfile: FamilyTasteProfile = FamilyTasteProfile(),
    homeViewModel: HomeViewModel = remember { HomeViewModel() },
) {
    val colors = AppTheme.colors
    val homeState by homeViewModel.state.collectAsState()
    val dashboard = homeState.dashboard
    val zones = pantryZoneCounts(dashboard.haveItems.map { it.ingredientId })

    LaunchedEffect(state, recipes, meal, tasteProfile) {
        homeViewModel.onIntent(HomeIntent.SyncInputs(state, recipes, meal, tasteProfile))
    }
    LaunchedEffect(homeViewModel) {
        homeViewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.OpenRecipeDetail -> onNavigateToRecipe(effect.recipeId)
            }
        }
    }

    if (homeState.showNoCookableDialog) {
        AlertDialog(
            onDismissRequest = { homeViewModel.onIntent(HomeIntent.DismissNoCookableDialog) },
            containerColor = colors.surface,
            title = { Text("Nấu ngẫu nhiên", color = colors.text) },
            text = { Text(RandomRecipeCopy.noCookable, color = colors.muted) },
            confirmButton = {
                TextButton(onClick = { homeViewModel.onIntent(HomeIntent.DismissNoCookableDialog) }) {
                    Text("Đóng", color = colors.accentText)
                }
            },
        )
    }

    val randomPick = homeState.randomPick
    if (homeState.showRandomWheel && randomPick != null) {
        RandomWheelDialog(
            pool = dashboard.cookableRecipes,
            pick = randomPick,
            onSpinAgain = { homeViewModel.onIntent(HomeIntent.SpinRandomAgain) },
            onOpenRecipe = { recipe -> homeViewModel.onIntent(HomeIntent.SelectRandomRecipe(recipe.id)) },
            onDismiss = { homeViewModel.onIntent(HomeIntent.DismissRandomWheel) },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HomeHeader(greeting = greetingText(state.hourOfDay), meal = meal)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MealSlot.entries.forEach { option ->
                PillChip(label = MealCopy.label(option), selected = option == meal, onClick = { onMealChange(option) })
            }
        }
        PantrySummaryCard(
            count = dashboard.haveItems.size,
            zones = zones,
            onOpenPantry = onNavigateToPantry,
            onOpenShopping = onOpenShopping,
        )
        UseSoonSection(items = dashboard.urgentItems, onOpenPantry = onNavigateToPantry)
        RecommendationSection(
            recommendations = dashboard.recommendations,
            onNavigateToRecipe = onNavigateToRecipe,
            onNavigateToRecipes = onNavigateToRecipes,
            onNavigateToPantry = onNavigateToPantry,
        )
        WeeklyPlanButton(onClick = onNavigateToWeeklyRecipes)
        if (dashboard.haveItems.isNotEmpty() && recipes.isNotEmpty()) {
            ShortcutCard(
                icon = AppIcons.Wheel,
                title = RandomRecipeCopy.button,
                subtitle = "Quay vòng chọn nhanh một món cho bữa tối",
                onClick = { homeViewModel.onIntent(HomeIntent.OpenRandomWheel) },
            )
        }
        if (onNavigateToHistory != null) {
            ShortcutCard(
                icon = AppIcons.Clock,
                title = CookHistoryCopy.title,
                subtitle = CookHistoryCopy.homeSubtitle,
                onClick = onNavigateToHistory,
            )
        }
        Text(
            text = "Chỉ trên máy bạn · Không cần tài khoản",
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
        )
    }
}
@Composable
private fun HomeHeader(greeting: String, meal: MealSlot) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = "BẾP NHÀ", style = MaterialTheme.typography.labelMedium, color = colors.accentText)
        Text(text = greeting, style = MaterialTheme.typography.bodyMedium, color = colors.muted)
        Text(text = MealCopy.heading(meal), style = MaterialTheme.typography.headlineMedium, color = colors.text)
    }
}
