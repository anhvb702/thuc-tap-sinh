package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.IngredientKind
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import kotlinx.datetime.LocalDate

data class ExpiringItem(
    val item: PantryItem,
    val ingredient: Ingredient,
    val daysLeft: Int,
    val urgency: Urgency,
) {
    val isExpiredFrozen: Boolean
        get() = item.compartment == PantryCompartment.FREEZE && daysLeft < 0
}

fun expiringList(
    pantry: List<PantryItem>,
    ingredients: List<Ingredient>,
    today: LocalDate,
): List<ExpiringItem> {
    val byId = ingredients.associateBy { it.id }
    return pantry.filter { it.have }.mapNotNull { item ->
        val ingredient = byId[item.ingredientId] ?: return@mapNotNull null
        if (ingredient.kind == IngredientKind.SEASONING) return@mapNotNull null
        val left = daysLeft(item, ingredient, today)
        val urgency = urgencyOf(left)
        val include = when (item.compartment) {
            PantryCompartment.FREEZE -> left < 0
            PantryCompartment.CHILL -> urgency != Urgency.OK
        }
        if (include) ExpiringItem(item, ingredient, left, urgency) else null
    }.sortedWith(compareBy<ExpiringItem> { it.daysLeft }.thenBy { it.item.ingredientId })
}
