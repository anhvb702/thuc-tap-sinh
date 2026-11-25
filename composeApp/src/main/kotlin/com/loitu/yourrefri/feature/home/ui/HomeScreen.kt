package com.loitu.yourrefri.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.cooklog.ui.CookHistoryCopy
import com.loitu.yourrefri.feature.home.presentation.HomeEffect
import com.loitu.yourrefri.feature.home.presentation.HomeIntent
import com.loitu.yourrefri.feature.home.presentation.HomeViewModel
import com.loitu.yourrefri.feature.home.domain.model.formatHomeWeatherLine
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.ui.isFreezerItem
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.recipe.logic.RecipeRecommendation
import com.loitu.yourrefri.feature.recipe.logic.defaultMealFor
import com.loitu.yourrefri.feature.recipe.logic.filterRecipesByDiet
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
    userName: String = DEFAULT_USER_NAME,
    dietRestriction: DietRestriction = DietRestriction.NONE,
) {
    val colors = AppTheme.colors
    val homeState by homeViewModel.state.collectAsState()
    val dashboard = homeState.dashboard
    val zones = pantryZoneCounts(dashboard.haveItems.map { it.ingredientId })
    var activeBucket by remember { mutableStateOf<PantryOverviewBucket?>(null) }
    var dismissedRecipeIds by remember { mutableStateOf(emptySet<String>()) }
    val activeSuggestion: RecipeRecommendation? = activeSpotlightSuggestion(dashboard.recommendations, dismissedRecipeIds)

    val suggestions = remember(recipes, dietRestriction, state.ingredients) {
        filterRecipesByDiet(recipes, dietRestriction, state.ingredients)
    }
    LaunchedEffect(state, suggestions, meal, tasteProfile) {
        homeViewModel.onIntent(HomeIntent.SyncInputs(state, suggestions, meal, tasteProfile))
    }
    LaunchedEffect(homeViewModel) {
        homeViewModel.onIntent(HomeIntent.LoadWeather)
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
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 136.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HomeHeader(
            greeting = userGreetingText(userName),
            weatherLine = formatHomeWeatherLine(homeState.weatherCity, homeState.weather, homeState.weatherLoading),
            onWeatherClick = { homeViewModel.onIntent(HomeIntent.ToggleWeatherCity) },
        )
        PantryOverviewSection(
            totalCount = dashboard.haveItems.size,
            fridgeCount = zones.fridge,
            freezerCount = zones.freezer,
            urgentCount = dashboard.urgentItems.size,
            onOpenPantry = onNavigateToPantry,
            onSelectBucket = { activeBucket = it },
        )
        DailySpotlightRecommendationSection(
            hasRecommendations = dashboard.recommendations.isNotEmpty(),
            activeSuggestion = activeSuggestion,
            onDismissSuggestion = { dismissedRecipeIds = dismissedRecipeIds + it },
            onResetDismissedSuggestions = { dismissedRecipeIds = resetDismissedSuggestions() },
            onNavigateToRecipe = onNavigateToRecipe,
            onNavigateToRecipes = onNavigateToRecipes,
            onNavigateToPantry = onNavigateToPantry,
        )
        if (onNavigateToHistory != null) {
            ShortcutCard(
                icon = AppIcons.Clock,
                title = CookHistoryCopy.title,
                subtitle = CookHistoryCopy.homeSubtitle,
                onClick = onNavigateToHistory,
            )
        }
    }
    activeBucket?.let { bucket ->
        val items = when (bucket) {
            PantryOverviewBucket.FRIDGE -> dashboard.haveItems.filter { !isFreezerItem(it.ingredientId) }
            PantryOverviewBucket.FREEZER -> dashboard.haveItems.filter { isFreezerItem(it.ingredientId) }
            PantryOverviewBucket.URGENT -> dashboard.urgentItems
        }
        PantryBucketPopupDialog(bucket, sortBucketItemsByExpiry(items), onDismiss = { activeBucket = null })
    }
}
@Composable
private fun HomeHeader(greeting: String, weatherLine: String, onWeatherClick: () -> Unit) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = greeting, style = MaterialTheme.typography.headlineMedium, color = colors.text)
        Text(
            text = weatherLine,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
            modifier = Modifier
                .heightIn(min = 36.dp)
                .clickable(onClick = onWeatherClick),
        )
    }
}
