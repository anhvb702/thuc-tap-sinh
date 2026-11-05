package com.loitu.yourrefri.feature.notice.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.expiringList
import com.loitu.yourrefri.feature.pantry.logic.statusText
import kotlinx.datetime.LocalDate

data class DailyNotice(
    val day: LocalDate,
    val ingredientIds: List<String>,
    val title: String,
    val body: String,
)

fun planDailyNotice(
    pantry: List<PantryItem>,
    ingredients: List<Ingredient>,
    today: LocalDate,
    lastSent: LocalDate?,
): DailyNotice? {
    // Ngày tương lai cũng chặn để đổi giờ máy lùi không gây gửi lại.
    if (lastSent != null && lastSent >= today) return null
    val items = expiringList(pantry, ingredients, today)
    if (items.isEmpty()) return null
    return DailyNotice(
        day = today,
        ingredientIds = items.map { it.item.ingredientId },
        title = "Gợi ý từ tủ nhà mình",
        body = items.joinToString("\n") { "${it.ingredient.nameVi}: ${statusText(it.daysLeft)}" },
    )
}
