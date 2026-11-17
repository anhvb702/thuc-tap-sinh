package com.loitu.yourrefri.feature.pantry.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

data class LeftoverItem(
    val id: String,
    val title: String,
    val compartment: PantryCompartment = PantryCompartment.CHILL,
    val markedAt: LocalDate,
    val shelfDays: Int = if (compartment == PantryCompartment.FREEZE) 14 else 2,
) {
    init {
        require(id.isNotBlank()) { "Leftover id must not be blank" }
        require(title.isNotBlank()) { "Leftover title must not be blank" }
        require(shelfDays >= 0) { "Leftover shelfDays must be non-negative" }
    }
}

interface LeftoverRepository {
    fun observeAll(): Flow<List<LeftoverItem>>
    suspend fun add(
        title: String,
        compartment: PantryCompartment = PantryCompartment.CHILL,
        markedAt: LocalDate,
        shelfDays: Int = if (compartment == PantryCompartment.FREEZE) 14 else 2,
    ): LeftoverItem
    suspend fun upsert(item: LeftoverItem)
    suspend fun delete(id: String)
}

class LeftoverRepositoryImpl(
    private val db: YourRefriDatabase,
    private val ioContext: CoroutineContext,
) : LeftoverRepository {
    override fun observeAll(): Flow<List<LeftoverItem>> =
        db.leftoverEntityQueries.selectAll().asFlow().mapToList(ioContext).map { rows ->
            rows.map { row ->
                LeftoverItem(
                    id = row.id,
                    title = row.title,
                    compartment = PantryCompartment.valueOf(row.compartment),
                    markedAt = LocalDate.fromEpochDays(row.markedAtEpochDays.toInt()),
                    shelfDays = row.shelfDays.toInt(),
                )
            }
        }

    override suspend fun add(
        title: String,
        compartment: PantryCompartment,
        markedAt: LocalDate,
        shelfDays: Int,
    ): LeftoverItem {
        require(title.isNotBlank()) { "Leftover title must not be blank" }
        require(shelfDays >= 0) { "Leftover shelfDays must be non-negative" }
        val item = LeftoverItem(
            id = "leftover-${Random.nextLong().toULong().toString(16)}",
            title = title.trim(),
            compartment = compartment,
            markedAt = markedAt,
            shelfDays = shelfDays,
        )
        upsert(item)
        return item
    }

    override suspend fun upsert(item: LeftoverItem) {
        withContext(ioContext) {
            db.leftoverEntityQueries.upsert(
                id = item.id,
                title = item.title,
                compartment = item.compartment.name,
                markedAtEpochDays = item.markedAt.toEpochDays().toLong(),
                shelfDays = item.shelfDays.toLong(),
            )
        }
    }

    override suspend fun delete(id: String) {
        require(id.isNotBlank()) { "Leftover id must not be blank" }
        withContext(ioContext) {
            db.leftoverEntityQueries.deleteById(id)
        }
    }
}
