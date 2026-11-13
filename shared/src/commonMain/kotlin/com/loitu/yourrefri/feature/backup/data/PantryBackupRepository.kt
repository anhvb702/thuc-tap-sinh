package com.loitu.yourrefri.feature.backup.data

import com.loitu.yourrefri.shared.db.YourRefriDatabase
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

class PantryBackupRepositoryImpl(
    private val db: YourRefriDatabase,
    private val ioContext: CoroutineContext,
) {
    private val settingsJson = Json

    suspend fun exportJson(): String = withContext(ioContext) {
        val pantry = db.pantryEntityQueries.selectAll().executeAsList().map {
            PantryBackupItem(it.ingredientId, it.have, it.markedAt)
        }
        val shelves = db.userShelfEntityQueries.selectAll().executeAsList().map {
            ShelfOverrideBackup(it.ingredientId, it.userShelfDays.toInt())
        }
        val quantities = db.pantryQuantityEntityQueries.selectAll().executeAsList().map {
            QuantityOverrideBackup(it.ingredientId, it.quantity.toInt())
        }
        val units = db.pantryUnitEntityQueries.selectAll().executeAsList().map {
            UnitOverrideBackup(it.ingredientId, it.unit)
        }
        val cookLogs = db.cookLogEntityQueries.selectAll().executeAsList().map {
            CookLogBackup(it.id, it.recipeId, it.cookedOn, it.photoUri, it.savedIngredientIds)
        }
        val cookMeals = db.cookLogMealEntityQueries.selectAll().executeAsList().map {
            CookMealBackup(it.cookLogId, it.meal)
        }
        val mealToggles = db.mealSlotToggleEntityQueries.selectAll().executeAsList().map {
            MealToggleBackup(it.day, it.mealSlot, it.enabled == 1L)
        }
        val weeklyMeals = db.weeklyMealPlanEntityQueries.selectAll().executeAsList().map {
            WeeklyMealBackup(it.day, it.mealSlot, it.recipeId, it.portions.toInt())
        }
        val weeklySettings = db.weeklyMealPlanSettingsEntityQueries.selectAll().executeAsList().firstOrNull()?.let { row ->
            WeeklyMealSettingsBackup(
                weekStart = row.weekStart,
                goalId = row.goalId,
                preferenceIds = settingsJson.decodeFromString<List<String>>(row.preferenceIdsJson),
                swappedOffsets = settingsJson.decodeFromString<List<WeeklyMealSwapOffsetBackup>>(row.swappedOffsetsJson),
                uncheckedIds = settingsJson.decodeFromString<List<String>>(row.uncheckedIdsJson),
                includeLunchMealPrep = row.includeLunchMealPrep == 1L,
            )
        }
        val favorites = db.favoriteRecipeEntityQueries.selectAll().executeAsList().map {
            FavoriteRecipeBackup(it.recipeId, it.createdAtEpochMs)
        }
        BackupCodec.encode(
            PantryBackupSnapshot(
                format = BackupCodec.FORMAT,
                version = BackupCodec.VERSION,
                pantry = pantry,
                shelfOverrides = shelves,
                quantities = quantities,
                cookLogs = cookLogs,
                units = units,
                weeklyMeals = weeklyMeals,
                mealToggles = mealToggles,
                weeklySettings = weeklySettings,
                favorites = favorites,
                cookMeals = cookMeals,
            ),
        )
    }

    suspend fun restoreJson(json: String) {
        val snapshot = BackupCodec.decode(json)
        withContext(ioContext) {
            db.transaction {
                db.cookLogMealEntityQueries.deleteAll()
                db.cookLogEntityQueries.deleteAll()
                db.pantryQuantityEntityQueries.deleteAll()
                db.pantryUnitEntityQueries.deleteAll()
                db.userShelfEntityQueries.deleteAll()
                db.pantryEntityQueries.deleteAll()
                db.mealSlotToggleEntityQueries.deleteAll()
                db.weeklyMealPlanEntityQueries.deleteAll()
                db.weeklyMealPlanSettingsEntityQueries.deleteAll()
                db.favoriteRecipeEntityQueries.deleteAll()

                snapshot.pantry.forEach {
                    db.pantryEntityQueries.upsert(it.ingredientId, it.have, it.markedAt)
                }
                snapshot.shelfOverrides.forEach {
                    db.userShelfEntityQueries.upsert(it.ingredientId, it.shelfDays.toLong())
                }
                snapshot.quantities.forEach {
                    db.pantryQuantityEntityQueries.upsert(it.ingredientId, it.quantity.toLong())
                }
                snapshot.units.forEach {
                    db.pantryUnitEntityQueries.upsert(it.ingredientId, it.unit)
                }
                snapshot.cookLogs.forEach {
                    db.cookLogEntityQueries.insertWithId(
                        id = it.id,
                        recipeId = it.recipeId,
                        cookedOn = it.cookedOn,
                        photoUri = it.photoUri,
                        savedIngredientIds = it.savedIngredientIds,
                    )
                }
                snapshot.cookMeals.forEach {
                    db.cookLogMealEntityQueries.upsert(it.cookLogId, it.meal)
                }
                snapshot.mealToggles.forEach {
                    db.mealSlotToggleEntityQueries.upsert(it.day, it.mealSlot, if (it.enabled) 1L else 0L)
                }
                snapshot.weeklyMeals.forEach {
                    db.weeklyMealPlanEntityQueries.upsert(it.day, it.meal, it.recipeId, it.portions.toLong())
                }
                snapshot.favorites.forEach {
                    db.favoriteRecipeEntityQueries.upsert(it.recipeId, it.createdAtEpochMs)
                }
                snapshot.weeklySettings?.let { settings ->
                    db.weeklyMealPlanSettingsEntityQueries.upsert(
                        settingsId = 1L,
                        weekStart = settings.weekStart,
                        goalId = settings.goalId,
                        preferenceIdsJson = settingsJson.encodeToString(settings.preferenceIds),
                        swappedOffsetsJson = settingsJson.encodeToString(settings.swappedOffsets),
                        uncheckedIdsJson = settingsJson.encodeToString(settings.uncheckedIds),
                        includeLunchMealPrep = if (settings.includeLunchMealPrep) 1L else 0L,
                    )
                }
            }
        }
    }
}
