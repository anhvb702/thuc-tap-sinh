package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import kotlinx.datetime.LocalDate

data class ExpiringItem(
    val item: PantryItem,
    val ingredient: Ingredient,
    val daysLeft: Int,
    val urgency: Urgency,
)

fun expiringList(
    pantry: List<PantryItem>,
    ingredients: List<Ingredient>,
    today: LocalDate,
): List<ExpiringItem> {
    val byId = ingredients.associateBy { it.id }
    return pantry.filter { it.have }.mapNotNull { item ->
        val ingredient = byId[item.ingredientId] ?: return@mapNotNull null
        val left = daysLeft(item, ingredient, today)
        val urgency = urgencyOf(left)
        if (urgency == Urgency.OK) null else ExpiringItem(item, ingredient, left, urgency)
    }.sortedWith(compareBy<ExpiringItem> { it.daysLeft }.thenBy { it.item.ingredientId })
}
