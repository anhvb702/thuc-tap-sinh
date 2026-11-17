package com.loitu.yourrefri.feature.recipe.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

interface CustomRecipeRepository {
    fun observeAll(): Flow<List<Recipe>>
    suspend fun addLite(title: String, ingredientIds: List<String>): Recipe
    suspend fun upsert(recipe: Recipe)
    suspend fun delete(recipeId: String)
}

class CustomRecipeRepositoryImpl(
    private val db: YourRefriDatabase,
    private val ioContext: CoroutineContext,
) : CustomRecipeRepository {
    private val json = Json

    override fun observeAll(): Flow<List<Recipe>> =
        db.customRecipeEntityQueries.selectAll().asFlow().mapToList(ioContext).map { rows ->
            rows.map { row ->
                Recipe(
                    id = row.recipeId,
                    title = row.title,
                    steps = json.decodeFromString(row.stepsJson),
                    required = json.decodeFromString<List<List<String>>>(row.requiredJson).map(::Slot),
                    optional = json.decodeFromString<List<List<String>>>(row.optionalJson).map(::Slot),
                    minutes = row.minutes?.toInt(),
                )
            }
        }

    override suspend fun addLite(title: String, ingredientIds: List<String>): Recipe {
        require(title.isNotBlank()) { "Recipe title must not be blank" }
        val ids = ingredientIds.distinct()
        require(ids.isNotEmpty() && ids.all(String::isNotBlank)) { "At least one ingredient is required" }
        val recipe = Recipe(
            id = "custom-${Random.nextLong().toULong().toString(16)}",
            title = title.trim(),
            steps = emptyList(),
            required = ids.map { Slot(listOf(it)) },
        )
        upsert(recipe)
        return recipe
    }

    override suspend fun upsert(recipe: Recipe) {
        require(recipe.id.startsWith("custom-")) { "Custom recipe id must use the custom- prefix" }
        withContext(ioContext) {
            db.customRecipeEntityQueries.upsert(
                recipeId = recipe.id,
                title = recipe.title,
                stepsJson = json.encodeToString(recipe.steps),
                requiredJson = json.encodeToString(recipe.required.map { it.anyOf }),
                optionalJson = json.encodeToString(recipe.optional.map { it.anyOf }),
                minutes = recipe.minutes?.toLong(),
            )
        }
    }

    override suspend fun delete(recipeId: String) {
        require(recipeId.startsWith("custom-")) { "Custom recipe id must use the custom- prefix" }
        withContext(ioContext) { db.customRecipeEntityQueries.deleteById(recipeId) }
    }
}
