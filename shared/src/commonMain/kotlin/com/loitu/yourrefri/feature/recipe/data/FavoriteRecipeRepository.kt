package com.loitu.yourrefri.feature.recipe.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Hàm thuần: thêm id nếu chưa có, bỏ id nếu đã có. */
fun toggleFavorite(favorites: Set<String>, recipeId: String): Set<String> =
    if (recipeId in favorites) favorites - recipeId else favorites + recipeId

interface FavoriteRecipeRepository {
    fun observeIds(): Flow<Set<String>>
    suspend fun setFavorite(recipeId: String, favorite: Boolean)
    /** Đảo trạng thái yêu thích; trả về trạng thái mới. */
    suspend fun toggle(recipeId: String): Boolean
}

@OptIn(ExperimentalTime::class)
class FavoriteRecipeRepositoryImpl(
    private val db: YourRefriDatabase,
    private val ioContext: CoroutineContext,
    private val clock: Clock,
) : FavoriteRecipeRepository {
    override fun observeIds(): Flow<Set<String>> =
        db.favoriteRecipeEntityQueries.selectAll().asFlow().mapToList(ioContext).map { rows ->
            rows.map { it.recipeId }.toSet()
        }

    override suspend fun setFavorite(recipeId: String, favorite: Boolean) {
        require(recipeId.isNotBlank()) { "recipeId must not be blank" }
        withContext(ioContext) {
            if (favorite) {
                db.favoriteRecipeEntityQueries.insertIfAbsent(recipeId, clock.now().toEpochMilliseconds())
            } else {
                db.favoriteRecipeEntityQueries.deleteById(recipeId)
            }
        }
    }

    override suspend fun toggle(recipeId: String): Boolean {
        require(recipeId.isNotBlank()) { "recipeId must not be blank" }
        return withContext(ioContext) {
            db.transactionWithResult {
                val queries = db.favoriteRecipeEntityQueries
                val wasFavorite = queries.exists(recipeId).executeAsOne() > 0L
                if (wasFavorite) queries.deleteById(recipeId)
                else queries.insertIfAbsent(recipeId, clock.now().toEpochMilliseconds())
                !wasFavorite
            }
        }
    }
}