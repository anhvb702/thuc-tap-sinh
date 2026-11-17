package com.loitu.yourrefri.feature.notice.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.expiringList
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.filterRecipesByDiet
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

data class DailyNotice(
    val day: LocalDate,
    val ingredientIds: List<String>,
    val title: String,
    val body: String,
    val isContextualNudge: Boolean = false,
)

fun planDailyNotice(
    pantry: List<PantryItem>,
    ingredients: List<Ingredient>,
    today: LocalDate,
    lastSent: LocalDate?,
    sundayShopping: Boolean = true,
    recipes: List<Recipe> = emptyList(),
    dietRestriction: DietRestriction = DietRestriction.NONE,
    lastNudgeSent: LocalDate? = null,
): DailyNotice? {
    // Ngày tương lai cũng chặn để đổi giờ máy lùi không gây gửi lại.
    if (lastSent != null && lastSent >= today) return null
    // Chủ Nhật thay thông báo thường bằng lời nhắc đi chợ (vẫn đúng một thông báo trong ngày).
    if (sundayShopping && today.dayOfWeek == DayOfWeek.SUNDAY) {
        return DailyNotice(
            day = today,
            ingredientIds = emptyList(),
            title = "Cuối tuần rồi nè",
            body = "Nay là cuối tuần rồi nè, đã sẵn sàng đi chợ cho 1 tuần mới chưa nào!",
        )
    }
    val items = expiringList(pantry, ingredients, today)
    if (items.isEmpty()) {
        val daysSinceNudge = lastNudgeSent?.let { today.toEpochDays() - it.toEpochDays() }
        if (daysSinceNudge != null && daysSinceNudge < 3) return null
        val suggestion = filterRecipesByDiet(recipes, dietRestriction, ingredients).firstOrNull() ?: return null
        return DailyNotice(
            day = today,
            ingredientIds = emptyList(),
            title = "Gợi ý món ngon hôm nay",
            body = "Nay trời dịu nhẹ, tối nay làm món ${suggestion.title} là hết ý luôn nè!",
            isContextualNudge = true,
        )
    }
    // Căn cứ quyết định Q13a (2026-11-16): Thông báo liệt kê tối đa 3 món, sau đó là "và vài món khác".
    val names = items.take(3).joinToString(", ") { it.ingredient.nameVi }
    val summary = if (items.size > 3) "$names và vài món khác" else names
    return DailyNotice(
        day = today,
        ingredientIds = items.map { it.item.ingredientId },
        title = "Gợi ý từ tủ nhà mình",
        body = "$summary nên dùng trước (hạn ước tính). Mở xem gợi ý món nhé.",
    )
}
