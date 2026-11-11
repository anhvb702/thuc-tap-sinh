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
