package com.loitu.yourrefri.feature.backup.data

import com.loitu.yourrefri.shared.db.YourRefriDatabase
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class PantryBackupRepositoryImpl(
    private val db: YourRefriDatabase,
    private val ioContext: CoroutineContext,
) {
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
        BackupCodec.encode(
            PantryBackupSnapshot(
                format = BackupCodec.FORMAT,
                version = BackupCodec.VERSION,
                pantry = pantry,
                shelfOverrides = shelves,
                quantities = quantities,
                cookLogs = cookLogs,
                units = units,
            ),
        )
    }

    suspend fun restoreJson(json: String) {
        val snapshot = BackupCodec.decode(json)
        withContext(ioContext) {
            db.transaction {
                db.cookLogEntityQueries.deleteAll()
                db.pantryQuantityEntityQueries.deleteAll()
                db.pantryUnitEntityQueries.deleteAll()
                db.userShelfEntityQueries.deleteAll()
                db.pantryEntityQueries.deleteAll()

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
            }
        }
    }
}
