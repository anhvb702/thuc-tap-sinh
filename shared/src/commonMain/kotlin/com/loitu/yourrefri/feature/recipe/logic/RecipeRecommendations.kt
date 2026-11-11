package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.feature.recipe.data.Recipe

enum class RecommendationReason { USE_SOON, READY, QUICK, OTHER }

data class RecipeRecommendation(
    val recipe: Recipe,
    val reason: RecommendationReason,
)

fun recommendRecipes(recipes: List<Recipe>, state: PantryGridState): List<RecipeRecommendation> {
    val uniqueRecipes = recipes.distinctBy { it.id }
    if (uniqueRecipes.isEmpty()) return emptyList()

    val cells = cellsOf(state).associateBy { it.ingredientId }
    val availableIds = cells.values.filter { it.have }.mapTo(HashSet()) { it.ingredientId }
    val urgentIds = cells.values
        .filter { it.have && it.daysLeft in 0..2 }
        .mapTo(HashSet()) { it.ingredientId }
    val recommendations = mutableListOf<RecipeRecommendation>()
    val selectedIds = HashSet<String>()

    fun addFirst(candidates: List<Recipe>, reason: RecommendationReason) {
        val candidate = candidates.firstOrNull { it.id !in selectedIds } ?: return
        selectedIds += candidate.id
        recommendations += RecipeRecommendation(candidate, reason)
    }

    val urgentRanked = uniqueRecipes.mapIndexedNotNull { index, recipe ->
        val score = recipe.required.count { slot -> slot.anyOf.any { it in urgentIds } }
        if (score > 0) Triple(recipe, score, index) else null
    }.sortedWith(compareByDescending<Triple<Recipe, Int, Int>> { it.second }.thenBy { it.third })
    addFirst(urgentRanked.map { it.first }, RecommendationReason.USE_SOON)

    val readyRecipes = uniqueRecipes.filter { recipe ->
        recipe.required.all { slot -> slot.anyOf.any { it in availableIds } }
    }
    addFirst(readyRecipes, RecommendationReason.READY)

    val quickRanked = uniqueRecipes.mapIndexedNotNull { index, recipe ->
        val minutes = recipe.minutes
        if (minutes != null && minutes < 20) Triple(recipe, minutes, index) else null
    }.sortedWith(compareBy<Triple<Recipe, Int, Int>> { it.second }.thenBy { it.third })
    addFirst(quickRanked.map { it.first }, RecommendationReason.QUICK)

    uniqueRecipes.forEach { recipe ->
        if (recommendations.size < 3 && selectedIds.add(recipe.id)) {
            recommendations += RecipeRecommendation(recipe, RecommendationReason.OTHER)
        }
    }
    return recommendations
}
