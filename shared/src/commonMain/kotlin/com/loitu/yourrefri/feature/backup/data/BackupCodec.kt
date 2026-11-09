package com.loitu.yourrefri.feature.backup.data

import com.loitu.yourrefri.feature.pantry.data.PANTRY_UNITS
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object BackupCodec {
    const val FORMAT = "yourrefri-backup"
    const val VERSION = 1

    private val json = Json {
        ignoreUnknownKeys = false
    }

    fun encode(snapshot: PantryBackupSnapshot): String {
        validate(snapshot)
        return try {
            json.encodeToString(snapshot)
        } catch (error: SerializationException) {
            throw IllegalArgumentException("Invalid backup snapshot", error)
        }
    }

    fun decode(json: String): PantryBackupSnapshot {
        val snapshot = try {
            this.json.decodeFromString<PantryBackupSnapshot>(json)
        } catch (error: Exception) {
            if (error is IllegalArgumentException) throw error
            throw IllegalArgumentException("Invalid backup JSON", error)
        }
        validate(snapshot)
        return snapshot
    }

    private fun validate(snapshot: PantryBackupSnapshot) {
        require(snapshot.format == FORMAT) { "Unsupported backup format" }
        require(snapshot.version == VERSION) { "Unsupported backup version" }

        validateUniqueIds("pantry", snapshot.pantry.map { it.ingredientId })
        snapshot.pantry.forEach { LocalDate.parse(it.markedAt) }

        validateUniqueIds("shelfOverrides", snapshot.shelfOverrides.map { it.ingredientId })
        snapshot.shelfOverrides.forEach { require(it.shelfDays >= 0) { "shelfDays must be non-negative" } }

        validateUniqueIds("quantities", snapshot.quantities.map { it.ingredientId })
        snapshot.quantities.forEach { require(it.quantity > 1) { "quantity must be greater than one" } }

        snapshot.units.forEach { require(it.ingredientId.isNotBlank()) { "units id must not be blank" } }
        validateUniqueIds("units", snapshot.units.map { it.ingredientId })
        snapshot.units.forEach { require(it.unit in PANTRY_UNITS) { "Unsupported pantry unit" } }

        validateUniqueIds("cookLogs", snapshot.cookLogs.map { it.id.toString() })
        snapshot.cookLogs.forEach { log ->
            require(log.id > 0) { "Cook log id must be positive" }
            require(log.recipeId.isNotEmpty()) { "recipeId must not be empty" }
            LocalDate.parse(log.cookedOn)
            require(log.savedIngredientIds.all { it.isNotEmpty() && ',' !in it }) {
                "Saved ingredient ids must be non-empty and contain no commas"
            }
        }
    }

    private fun validateUniqueIds(table: String, ids: List<String>) {
        val seen = HashSet<String>()
        ids.forEach { id ->
            require(id.isNotEmpty()) { "$table id must not be empty" }
            require(seen.add(id)) { "$table id must be unique" }
        }
    }
}
