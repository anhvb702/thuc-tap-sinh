package com.loitu.yourrefri.feature.recipe.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object RecipeBundle {
    @Serializable
    private data class MeasureRow(val amount: Double, val unit: String)

    @Serializable
    private data class SeasoningRow(val name: String, val amountText: String)

    @Serializable
    private data class SlotRow(val anyOf: List<String>, val measures: Map<String, MeasureRow> = emptyMap())

    @Serializable
    private data class RecipeRow(
        val id: String,
        val title: String,
        val steps: List<String>,
        val required: List<SlotRow>,
        val optional: List<SlotRow> = emptyList(),
        val minutes: Int? = null,
        val stepMinutes: List<Int?> = emptyList(),
        val mystery: Boolean = false,
        val servings: Int = 2,
        val seasonings: List<SeasoningRow> = emptyList(),
    )

    private fun SlotRow.toSlot(): Slot =
        Slot(anyOf, measures.mapValues { (_, m) -> Measure(m.amount, m.unit) })

    fun parse(json: String): List<Recipe> {
        val rows = Json.decodeFromString<List<RecipeRow>>(json)
        val seenIds = HashSet<String>()
        return rows.map { row ->
            require(seenIds.add(row.id)) { "id công thức trùng: ${row.id}" }
            Recipe(
                id = row.id,
                title = row.title,
                steps = row.steps,
                required = row.required.map { it.toSlot() },
                optional = row.optional.map { it.toSlot() },
                minutes = row.minutes,
                stepMinutes = row.stepMinutes,
                isMystery = row.mystery,
                servings = row.servings,
                seasonings = row.seasonings.map { Seasoning(it.name, it.amountText) },
            )
        }
    }
}
