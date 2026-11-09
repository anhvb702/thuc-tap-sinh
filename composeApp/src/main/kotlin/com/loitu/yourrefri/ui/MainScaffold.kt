package com.loitu.yourrefri.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loitu.yourrefri.feature.home.ui.HomeScreen
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.ui.PantryGridScreen
import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import com.loitu.yourrefri.feature.recipe.ui.RecipeScreen
import com.loitu.yourrefri.feature.recipe.ui.recipeRequestForAllRecipes
import com.loitu.yourrefri.feature.recipe.ui.recipeRequestForHomeCard
import com.loitu.yourrefri.feature.recipe.ui.RecipeScreenMode
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.settings.ui.SettingsScreen
import com.loitu.yourrefri.ui.theme.MutedGray
import com.loitu.yourrefri.ui.theme.SageGreenText
import com.loitu.yourrefri.ui.theme.SoftGreen

enum class NavTab(val label: String, val icon: String) {
    HOME("Trang chủ", "🏠"),
    PANTRY("Tủ lạnh", "🧊"),
    RECIPES("Món ăn", "🍲"),
    SETTINGS("Cá nhân", "⚙️")
}

@Composable
fun MainScaffold(
    state: PantryGridState,
    recipes: List<Recipe>,
    onAction: (PantryGridAction) -> Unit,
    backupStatus: String? = null,
    restoreConfirmationPending: Boolean = false,
    onExportBackup: () -> Unit = {},
    onImportBackup: () -> Unit = {},
    onConfirmRestore: () -> Unit = {},
    onCancelRestore: () -> Unit = {},
    reminderEnabled: Boolean = false,
    onReminderChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var currentTab by remember { mutableStateOf(NavTab.HOME) } // Mặc định mở Trang chủ theo triết lý Warm Minimalism
    var requestedRecipeId by remember { mutableStateOf<String?>(null) }
    var requestedRecipeMode by remember { mutableStateOf(RecipeScreenMode.DAILY) }
    var weeklyGoal by remember { mutableStateOf(DietGoal.BALANCED) }
    var weeklyPreferences by remember { mutableStateOf(emptySet<FamilyPreferenceTag>()) }
    var weeklySwappedOffsets by remember { mutableStateOf(emptyMap<Int, Int>()) }
    var weeklyUncheckedIds by remember { mutableStateOf(emptySet<String>()) }
    var weeklyIncludeLunchMealPrep by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 4.dp
            ) {
                NavTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (tab != currentTab) requestedRecipeId = null
                            currentTab = tab
                        },
                        icon = {
                            Text(
                                text = tab.icon,
                                fontSize = if (isSelected) 22.sp else 18.sp
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 14.sp,
                                maxLines = 1,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SageGreenText else MutedGray
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = SoftGreen,
                            selectedTextColor = SageGreenText,
                            unselectedTextColor = MutedGray,
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
            when (currentTab) {
                NavTab.HOME -> HomeScreen(
                    state = state,
                    recipes = recipes,
                    onNavigateToPantry = { currentTab = NavTab.PANTRY },
                    onNavigateToRecipes = {
                        requestedRecipeId = recipeRequestForAllRecipes()
                        requestedRecipeMode = RecipeScreenMode.DAILY
                        currentTab = NavTab.RECIPES
                    },
                    onNavigateToRecipe = { recipeId ->
                        requestedRecipeId = recipeRequestForHomeCard(recipeId)
                        requestedRecipeMode = RecipeScreenMode.DAILY
                        currentTab = NavTab.RECIPES
                    },
                    onNavigateToWeeklyRecipes = {
                        requestedRecipeId = null
                        requestedRecipeMode = RecipeScreenMode.WEEKLY
                        currentTab = NavTab.RECIPES
                    },
                )
                NavTab.PANTRY -> PantryGridScreen(
                    state = state,
                    onAction = onAction
                )
                NavTab.RECIPES -> RecipeScreen(
                    state = state,
                    recipes = recipes,
                    initialRecipeId = requestedRecipeId,
                    initialMode = requestedRecipeMode,
                    weeklyGoal = weeklyGoal,
                    onWeeklyGoalChange = { weeklyGoal = it },
                    weeklyPreferences = weeklyPreferences,
                    onWeeklyPreferencesChange = { weeklyPreferences = it },
                    weeklySwappedOffsets = weeklySwappedOffsets,
                    onWeeklySwappedOffsetsChange = { weeklySwappedOffsets = it },
                    weeklyUncheckedIds = weeklyUncheckedIds,
                    onWeeklyUncheckedIdsChange = { weeklyUncheckedIds = it },
                    weeklyIncludeLunchMealPrep = weeklyIncludeLunchMealPrep,
                    onWeeklyIncludeLunchMealPrepChange = { weeklyIncludeLunchMealPrep = it },
                    onModeChange = { requestedRecipeMode = it },
                    onCookRecipe = { _, ingredientIds ->
                        ingredientIds.distinct()
                            .filter { state.pantry[it]?.have == true }
                            .forEach { onAction(PantryGridAction.ConsumeIngredient(it)) }
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
                NavTab.SETTINGS -> SettingsScreen(
                    backupStatus = backupStatus,
                    restoreConfirmationPending = restoreConfirmationPending,
                    onExportBackup = onExportBackup,
                    onImportBackup = onImportBackup,
                    onConfirmRestore = onConfirmRestore,
                    onCancelRestore = onCancelRestore,
                    reminderEnabled = reminderEnabled,
                    onReminderChanged = onReminderChanged,
                )
            }
        }
    }
}
