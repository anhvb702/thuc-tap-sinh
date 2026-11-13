package com.loitu.yourrefri.feature.recipe.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext

enum class MealSlot {
    LUNCH,
    DINNER,
}

data class MealSlotKey(val day: LocalDate, val slot: MealSlot)

data class WeeklyMealPlanEntry(
    val day: LocalDate,
    val meal: MealSlot,
    val recipeId: String,
    val portions: Int,
)

data class WeeklyMealPlanSettings(
    val weekStart: LocalDate,
    val goalId: String,
    val preferenceIds: Set<String>,
    val swappedOffsets: Map<Int, Int>,
    val uncheckedIds: Set<String>,
    val includeLunchMealPrep: Boolean,
)

interface WeeklyMealPlanRepository {
    fun observeDisabledMeals(): Flow<List<MealSlotKey>> = kotlinx.coroutines.flow.flowOf(emptyList())
    suspend fun setMealEnabled(day: LocalDate, slot: MealSlot, enabled: Boolean) {
        throw UnsupportedOperationException("Meal toggles are not supported")
    }
    fun observeAll(): Flow<List<WeeklyMealPlanEntry>>
    suspend fun save(entry: WeeklyMealPlanEntry)
    fun observeSettings(): Flow<WeeklyMealPlanSettings?>
    suspend fun saveSettings(settings: WeeklyMealPlanSettings)
}

class WeeklyMealPlanRepositoryImpl(
    private val db: YourRefriDatabase,
    private val ioContext: CoroutineContext,
) : WeeklyMealPlanRepository {
    private val settingsJson = Json

    override fun observeDisabledMeals(): Flow<List<MealSlotKey>> =
        db.mealSlotToggleEntityQueries.selectAll().asFlow().mapToList(ioContext).map { rows ->
            rows.filter { it.enabled == 0L }.map { MealSlotKey(LocalDate.parse(it.day), MealSlot.valueOf(it.mealSlot)) }
        }

    override suspend fun setMealEnabled(day: LocalDate, slot: MealSlot, enabled: Boolean) {
        withContext(ioContext) {
            db.mealSlotToggleEntityQueries.upsert(day.toString(), slot.name, if (enabled) 1L else 0L)
        }
    }

    override fun observeAll(): Flow<List<WeeklyMealPlanEntry>> =
        db.weeklyMealPlanEntityQueries.selectAll().asFlow().mapToList(ioContext).map { rows ->
            rows.map { row ->
                WeeklyMealPlanEntry(
                    day = LocalDate.parse(row.day),
                    meal = MealSlot.valueOf(row.mealSlot),
                    recipeId = row.recipeId,
                    portions = row.portions.toInt(),
                )
            }
        }

    override suspend fun save(entry: WeeklyMealPlanEntry) {
        require(entry.recipeId.isNotBlank())
        require(entry.portions >= 1)
        withContext(ioContext) {
            db.weeklyMealPlanEntityQueries.upsert(
                day = entry.day.toString(),
                mealSlot = entry.meal.name,
                recipeId = entry.recipeId,
                portions = entry.portions.toLong(),
            )
        }
    }

    override fun observeSettings(): Flow<WeeklyMealPlanSettings?> =
        db.weeklyMealPlanSettingsEntityQueries.selectAll().asFlow().mapToList(ioContext).map { rows ->
            rows.firstOrNull()?.let { row ->
                WeeklyMealPlanSettings(
                    weekStart = LocalDate.parse(row.weekStart),
                    goalId = row.goalId,
                    preferenceIds = settingsJson.decodeFromString<List<String>>(row.preferenceIdsJson).toSet(),
                    swappedOffsets = settingsJson.decodeFromString<List<SwapOffset>>(row.swappedOffsetsJson)
                        .associate { it.dayIndex to it.offset },
                    uncheckedIds = settingsJson.decodeFromString<List<String>>(row.uncheckedIdsJson).toSet(),
                    includeLunchMealPrep = row.includeLunchMealPrep == 1L,
                )
            }
        }

    override suspend fun saveSettings(settings: WeeklyMealPlanSettings) {
        require(settings.goalId.isNotBlank())
        require(settings.preferenceIds.all(String::isNotBlank))
        require(settings.uncheckedIds.all(String::isNotBlank))
        require(settings.swappedOffsets.all { (dayIndex, offset) -> dayIndex in 0..6 && offset >= 0 })
        withContext(ioContext) {
            db.weeklyMealPlanSettingsEntityQueries.upsert(
                settingsId = 1L,
                weekStart = settings.weekStart.toString(),
                goalId = settings.goalId,
                preferenceIdsJson = settingsJson.encodeToString(settings.preferenceIds.sorted()),
                swappedOffsetsJson = settingsJson.encodeToString(
                    settings.swappedOffsets.toSortedMap().map { (dayIndex, offset) -> SwapOffset(dayIndex, offset) },
                ),
                uncheckedIdsJson = settingsJson.encodeToString(settings.uncheckedIds.sorted()),
                includeLunchMealPrep = if (settings.includeLunchMealPrep) 1L else 0L,
            )
        }
    }
}

@Serializable
private data class SwapOffset(val dayIndex: Int, val offset: Int)
