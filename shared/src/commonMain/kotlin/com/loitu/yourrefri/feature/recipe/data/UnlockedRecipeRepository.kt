package com.loitu.yourrefri.feature.recipe.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

interface UnlockedRecipeRepository {
    fun observeUnlockedIds(): Flow<Set<String>>

    /** Mở khóa một món bí mật; gọi lại với cùng id thì giữ nguyên ngày cũ. */
    suspend fun unlock(recipeId: String, unlockedOn: LocalDate)
}

class UnlockedRecipeRepositoryImpl(
    private val db: YourRefriDatabase,
    private val io: CoroutineDispatcher,
) : UnlockedRecipeRepository {
    override fun observeUnlockedIds(): Flow<Set<String>> =
        db.unlockedRecipeEntityQueries.selectAllIds().asFlow().mapToList(io).map { it.toSet() }

    override suspend fun unlock(recipeId: String, unlockedOn: LocalDate) {
        require(recipeId.isNotBlank()) { "recipeId must not be blank" }
        withContext(io) {
            db.unlockedRecipeEntityQueries.insertOrIgnore(recipeId, unlockedOn.toString())
        }
    }
}
