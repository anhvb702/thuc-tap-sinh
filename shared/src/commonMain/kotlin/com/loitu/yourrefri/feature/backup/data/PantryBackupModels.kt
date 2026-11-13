package com.loitu.yourrefri.feature.backup.data

import kotlinx.serialization.Serializable

@Serializable
data class PantryBackupSnapshot(
    val format: String,
    val version: Int,
    val pantry: List<PantryBackupItem>,
    val shelfOverrides: List<ShelfOverrideBackup>,
    val quantities: List<QuantityOverrideBackup>,
    val cookLogs: List<CookLogBackup>,
    val units: List<UnitOverrideBackup> = emptyList(),
    val weeklyMeals: List<WeeklyMealBackup> = emptyList(),
    val weeklySettings: WeeklyMealSettingsBackup? = null,
    val mealToggles: List<MealToggleBackup> = emptyList(),
    val favorites: List<FavoriteRecipeBackup> = emptyList(),
    val cookMeals: List<CookMealBackup> = emptyList(),
)

/** Bữa (LUNCH/DINNER) của một lần nấu trong [PantryBackupSnapshot.cookLogs]; lần nấu không có dòng này là chưa biết bữa. */
@Serializable
data class CookMealBackup(
    val cookLogId: Long,
    val meal: String,
)

@Serializable
data class WeeklyMealSettingsBackup(
    val weekStart: String,
    val goalId: String,
    val preferenceIds: List<String>,
    val swappedOffsets: List<WeeklyMealSwapOffsetBackup>,
    val uncheckedIds: List<String>,
    val includeLunchMealPrep: Boolean,
)

@Serializable
data class WeeklyMealSwapOffsetBackup(
    val dayIndex: Int,
    val offset: Int,
)

@Serializable
data class WeeklyMealBackup(
    val day: String,
    val meal: String,
    val recipeId: String,
    val portions: Int,
)

@Serializable
data class PantryBackupItem(
    val ingredientId: String,
    val have: Boolean,
    val markedAt: String,
)

@Serializable
data class ShelfOverrideBackup(
    val ingredientId: String,
    val shelfDays: Int,
)

@Serializable
data class QuantityOverrideBackup(
    val ingredientId: String,
    val quantity: Int,
)

@Serializable
data class UnitOverrideBackup(
    val ingredientId: String,
    val unit: String,
)

@Serializable
data class CookLogBackup(
    val id: Long,
    val recipeId: String,
    val cookedOn: String,
    val photoUri: String?,
    val savedIngredientIds: List<String>,
)

@Serializable
data class MealToggleBackup(
    val day: String,
    val mealSlot: String,
    val enabled: Boolean,
)


@Serializable
data class FavoriteRecipeBackup(
    val recipeId: String,
    val createdAtEpochMs: Long,
)

