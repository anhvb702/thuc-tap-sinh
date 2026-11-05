package com.loitu.yourrefri.feature.pantry.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class PantryRepositoryImpl(
    private val db: YourRefriDatabase,
    private val clock: Clock,
    private val timeZone: TimeZone,
    private val ioContext: CoroutineContext,
) : PantryRepository {
    override fun observe(): Flow<List<PantryItem>> =
        db.pantryEntityQueries.selectAll().asFlow().mapToList(ioContext).map { rows ->
            rows.map { PantryItem(it.ingredientId, it.have, LocalDate.parse(it.markedAt)) }
        }

    override fun observeShelfOverrides(): Flow<Map<String, Int>> =
        db.userShelfEntityQueries.selectAll().asFlow().mapToList(ioContext).map { rows ->
            rows.associate { it.ingredientId to it.userShelfDays.toInt() }
        }

    override suspend fun setHave(ingredientId: String, have: Boolean, markedAt: LocalDate?) {
        withContext(ioContext) {
            db.transaction {
                val pantry = db.pantryEntityQueries
                if (have) {
                    val date = markedAt ?: clock.now().toLocalDateTime(timeZone).date
                    pantry.upsert(ingredientId, true, date.toString())
                } else if (markedAt == null) {
                    pantry.updateHave(false, ingredientId)
                } else if (pantry.selectById(ingredientId).executeAsOneOrNull() != null) {
                    pantry.upsert(ingredientId, false, markedAt.toString())
                }
            }
        }
    }

    override suspend fun setShelfDays(ingredientId: String, days: Int?) {
        require(days == null || days >= 0)
        withContext(ioContext) {
            if (days == null) {
                db.userShelfEntityQueries.deleteById(ingredientId)
            } else {
                db.userShelfEntityQueries.upsert(ingredientId, days.toLong())
            }
        }
    }
}
