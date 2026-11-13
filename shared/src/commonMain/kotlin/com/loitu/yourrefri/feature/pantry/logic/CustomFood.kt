package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem

const val CUSTOM_FOOD_PREFIX = "custom:"
const val CUSTOM_FOOD_GROUP = "tu-them"
const val CUSTOM_FOOD_DEFAULT_SHELF_DAYS = 3

fun customFoodId(name: String): String? {
    val normalized = name.trim().split(Regex("\\s+")).joinToString(" ")
    if (normalized.length !in 1..40 || ',' in normalized) return null
    return CUSTOM_FOOD_PREFIX + normalized
}

fun customFoodName(id: String): String? {
    if (!id.startsWith(CUSTOM_FOOD_PREFIX)) return null
    val name = id.removePrefix(CUSTOM_FOOD_PREFIX)
    return name.takeIf { customFoodId(it) == id }
}

fun customIngredientsOf(pantry: List<PantryItem>, catalogIds: Set<String>): List<Ingredient> =
    pantry.mapNotNull { item ->
        if (item.ingredientId in catalogIds) return@mapNotNull null
        val name = customFoodName(item.ingredientId) ?: return@mapNotNull null
        Ingredient(item.ingredientId, name, CUSTOM_FOOD_GROUP, CUSTOM_FOOD_DEFAULT_SHELF_DAYS)
    }
