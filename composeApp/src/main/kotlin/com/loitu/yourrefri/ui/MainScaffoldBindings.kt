package com.loitu.yourrefri.ui

import com.loitu.yourrefri.feature.cooklog.ui.CookHistoryState
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.settings.presentation.SettingsUiState
import com.loitu.yourrefri.ui.theme.ThemeMode
import kotlinx.datetime.LocalTime

data class MainScaffoldSettingsBinding(
    val uiState: SettingsUiState = SettingsUiState(),
    val appVersionName: String = "",
    val onExportBackup: () -> Unit = {},
    val onImportBackup: () -> Unit = {},
    val onConfirmRestore: () -> Unit = {},
    val onCancelRestore: () -> Unit = {},
    val onReminderChanged: (Boolean) -> Unit = {},
    val onTestReminder: () -> Unit = {},
    val onThemeModeChange: (ThemeMode) -> Unit = {},
    val onDietRestrictionChange: (DietRestriction) -> Unit = {},
    val onSundayTimeChange: (LocalTime) -> Unit = {},
    val onWeekdayTimeChange: (LocalTime) -> Unit = {},
    val onTasteProfileChange: (FamilyTasteProfile) -> Unit = {},
)

data class MainScaffoldCollectionBinding(
    val favoriteIds: Set<String> = emptySet(),
    val onToggleFavorite: (String) -> Unit = {},
    val cookHistoryState: CookHistoryState = CookHistoryState(),
    val onRecordCook: (recipeId: String, savedIngredientIds: List<String>, meal: MealSlot, onResult: (Long?) -> Unit) -> Unit = { _, _, _, done -> done(null) },
    val onDeleteCook: (Long) -> Unit = {},
    val unlockedIds: Set<String> = emptySet(),
    val mysteryRewardsReady: Boolean = true,
    val mysteryUnlockFailedId: String? = null,
    val onUnlockMysteryRecipe: (Recipe) -> Unit = {},
)
