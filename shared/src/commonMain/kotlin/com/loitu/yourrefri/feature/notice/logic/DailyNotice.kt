package com.loitu.yourrefri.feature.notice.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.expiringList
import kotlinx.datetime.DayOfWeek
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
    sundayShopping: Boolean = true,
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
    if (items.isEmpty()) return null
    val names = items.take(2).joinToString(", ") { it.ingredient.nameVi }
    val summary = if (items.size > 2) "$names và ${items.size - 2} món khác" else names
    return DailyNotice(
        day = today,
        ingredientIds = items.map { it.item.ingredientId },
        title = "Gợi ý từ tủ nhà mình",
        body = "$summary nên dùng trước (hạn ước tính). Mở xem gợi ý món nhé.",
    )
}
