package com.loitu.yourrefri.feature.recipe.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext

data class ExtraShoppingItem(
    val ingredientId: String,
    val addedAt: LocalDate,
) {
    init {
        require(ingredientId.isNotBlank()) { "ExtraShoppingItem ingredientId must not be blank" }
    }
}

interface ExtraShoppingRepository {
    fun observeAll(): Flow<List<ExtraShoppingItem>>
    suspend fun add(ingredientId: String, addedAt: LocalDate)
    suspend fun remove(ingredientId: String)
    suspend fun clear()
}

class ExtraShoppingRepositoryImpl(
    private val db: YourRefriDatabase,
    private val ioContext: CoroutineContext,
) : ExtraShoppingRepository {
    override fun observeAll(): Flow<List<ExtraShoppingItem>> =
        db.extraShoppingItemEntityQueries.selectAll().asFlow().mapToList(ioContext).map { rows ->
            rows.map { row ->
                ExtraShoppingItem(
                    ingredientId = row.ingredientId,
                    addedAt = LocalDate.fromEpochDays(row.addedAtEpochDays.toInt()),
                )
            }
        }

    override suspend fun add(ingredientId: String, addedAt: LocalDate) {
        require(ingredientId.isNotBlank()) { "ingredientId must not be blank" }
        withContext(ioContext) {
            db.extraShoppingItemEntityQueries.upsert(
                ingredientId = ingredientId,
                addedAtEpochDays = addedAt.toEpochDays().toLong(),
            )
        }
    }

    override suspend fun remove(ingredientId: String) {
        require(ingredientId.isNotBlank()) { "ingredientId must not be blank" }
        withContext(ioContext) {
            db.extraShoppingItemEntityQueries.deleteById(ingredientId)
        }
    }

    override suspend fun clear() {
        withContext(ioContext) {
            db.extraShoppingItemEntityQueries.deleteAll()
        }
    }
}
