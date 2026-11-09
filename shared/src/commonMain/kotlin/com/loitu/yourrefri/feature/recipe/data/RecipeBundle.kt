package com.loitu.yourrefri.feature.recipe.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object RecipeBundle {
    @Serializable
    private data class SlotRow(val anyOf: List<String>)

    @Serializable
    private data class RecipeRow(
        val id: String,
        val title: String,
        val steps: List<String>,
        val required: List<SlotRow>,
        val optional: List<SlotRow> = emptyList(),
        val minutes: Int? = null,
    )

    fun parse(json: String): List<Recipe> {
        val rows = Json.decodeFromString<List<RecipeRow>>(json)
        val seenIds = HashSet<String>()
        return rows.map { row ->
            require(seenIds.add(row.id)) { "id công thức trùng: ${row.id}" }
            Recipe(
                id = row.id,
                title = row.title,
                steps = row.steps,
                required = row.required.map { Slot(it.anyOf) },
                optional = row.optional.map { Slot(it.anyOf) },
                minutes = row.minutes,
            )
        }
    }
}
