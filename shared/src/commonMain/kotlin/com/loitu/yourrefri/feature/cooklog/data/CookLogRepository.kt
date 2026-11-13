package com.loitu.yourrefri.feature.cooklog.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext

interface CookLogRepository {
    fun observeAll(): Flow<List<CookLog>>
    suspend fun add(recipeId: String, cookedOn: LocalDate, savedIngredientIds: List<String>, meal: MealSlot? = null): Long
    suspend fun delete(id: Long)
}

class CookLogRepositoryImpl(
    private val db: YourRefriDatabase,
    private val ioContext: CoroutineContext,
) : CookLogRepository {
    override fun observeAll(): Flow<List<CookLog>> =
        db.cookLogEntityQueries.selectAllWithMeal().asFlow().mapToList(ioContext).map { rows ->
            rows.map { row ->
                CookLog(
                    id = row.id,
                    recipeId = row.recipeId,
                    cookedOn = LocalDate.parse(row.cookedOn),
                    savedIngredientIds = row.savedIngredientIds,
                    meal = row.meal?.let { stored -> MealSlot.entries.firstOrNull { it.name == stored } },
                )
            }
        }

    override suspend fun add(
        recipeId: String,
        cookedOn: LocalDate,
        savedIngredientIds: List<String>,
        meal: MealSlot?,
    ): Long {
        require(recipeId.isNotBlank())
        return withContext(ioContext) {
            db.transactionWithResult {
                db.cookLogEntityQueries.insert(recipeId, cookedOn.toString(), null, savedIngredientIds)
                val id = db.cookLogEntityQueries.lastInsertRowId().executeAsOne()
                if (meal != null) db.cookLogMealEntityQueries.upsert(id, meal.name)
                id
            }
        }
    }

    override suspend fun delete(id: Long) {
        withContext(ioContext) {
            db.transaction {
                db.cookLogMealEntityQueries.deleteByCookLogId(id)
                db.cookLogEntityQueries.deleteById(id)
            }
        }
    }
}
