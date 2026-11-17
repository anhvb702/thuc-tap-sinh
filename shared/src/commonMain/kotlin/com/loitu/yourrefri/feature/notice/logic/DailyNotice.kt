package com.loitu.yourrefri.feature.notice.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.LeftoverItem
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.Urgency
import com.loitu.yourrefri.feature.pantry.logic.expiringList
import com.loitu.yourrefri.feature.pantry.logic.urgencyOf
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.filterRecipesByDiet
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

data class DailyNotice(
    val day: LocalDate,
    val ingredientIds: List<String>,
    val title: String,
    val body: String,
    val isContextualNudge: Boolean = false,
)

private data class UrgentEntry(
    val name: String,
    val isExpiredFrozen: Boolean,
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
    leftovers: List<LeftoverItem> = emptyList(),
    defrostNames: List<String> = emptyList(),
): DailyNotice? {
    // Ngày tương lai cũng chặn để đổi giờ máy lùi không gây gửi lại.
    if (lastSent != null && lastSent >= today) return null

    val defrostSummary = formatNamesSummary(defrostNames)

    // Chủ Nhật thay thông báo thường bằng lời nhắc đi chợ (vẫn đúng một thông báo trong ngày).
    if (sundayShopping && today.dayOfWeek == DayOfWeek.SUNDAY) {
        val shoppingBody = "Nay là cuối tuần rồi nè, đã sẵn sàng đi chợ cho 1 tuần mới chưa nào!"
        val body = if (defrostNames.isNotEmpty()) {
            "$shoppingBody Sáng nay nhớ rã đông $defrostSummary cho bữa tối nhé!"
        } else {
            shoppingBody
        }
        return DailyNotice(
            day = today,
            ingredientIds = emptyList(),
            title = "Cuối tuần rồi nè",
            body = body,
        )
    }

    val urgentLeftovers = leftovers.mapNotNull { item ->
        val expiry = item.markedAt.plus(item.shelfDays, DateTimeUnit.DAY)
        val left = (expiry.toEpochDays() - today.toEpochDays()).toInt()
        val isFrozen = item.compartment == PantryCompartment.FREEZE
        val include = if (isFrozen) left < 0 else urgencyOf(left) != Urgency.OK
        if (include) Triple(left, item.title.trim(), isFrozen && left < 0) else null
    }.sortedWith(compareBy<Triple<Int, String, Boolean>> { it.first }.thenBy { it.second })

    val items = expiringList(pantry, ingredients, today)
    // Căn cứ Q18/Q27: Đồ ăn thừa đứng đầu danh sách thông báo ngày.
    val urgentEntries = urgentLeftovers.map { UrgentEntry(it.second, it.third) } +
        items.map { UrgentEntry(it.ingredient.nameVi, it.isExpiredFrozen) }

    val hasExpiredFrozen = urgentEntries.any { it.isExpiredFrozen }
    // Căn cứ quyết định Q13a (2026-11-16): Thông báo liệt kê tối đa 3 món, sau đó là "và vài món khác".
    val summary = formatNamesSummary(urgentEntries.map { it.name })

    if (defrostNames.isNotEmpty()) {
        val defrostLead = "Sáng nay nhớ rã đông $defrostSummary cho bữa tối nhé!"
        val body = if (urgentEntries.isEmpty()) {
            defrostLead
        } else {
            val expiringTail = if (hasExpiredFrozen) {
                "$summary nên dùng trước hoặc kiểm tra lại xem còn dùng được không (hạn ước tính)."
            } else {
                "$summary nên dùng trước (hạn ước tính)."
            }
            "$defrostLead $expiringTail"
        }
        return DailyNotice(
            day = today,
            ingredientIds = items.map { it.item.ingredientId },
            title = "Chuẩn bị rã đông",
            body = body,
        )
    }

    if (urgentEntries.isEmpty()) {
        // SW-09: Thứ 4 nhắc nhẹ giữa tuần khi không có đồ sắp hết hạn hay rã đông.
        if (today.dayOfWeek == DayOfWeek.WEDNESDAY) {
            return DailyNotice(
                day = today,
                ingredientIds = emptyList(),
                title = "Giữa tuần rồi nè",
                body = "Giữa tuần rồi, mình ghé xem lại tủ lạnh và thực đơn cuối tuần nhé!",
            )
        }
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

    val body = if (hasExpiredFrozen) {
        "$summary nên dùng trước hoặc kiểm tra lại xem còn dùng được không (hạn ước tính). Mở xem gợi ý món nhé."
    } else {
        "$summary nên dùng trước (hạn ước tính). Mở xem gợi ý món nhé."
    }
    return DailyNotice(
        day = today,
        ingredientIds = items.map { it.item.ingredientId },
        title = "Gợi ý từ tủ nhà mình",
        body = body,
    )
}

private fun formatNamesSummary(names: List<String>): String {
    if (names.isEmpty()) return ""
    val head = names.take(3).joinToString(", ")
    return if (names.size > 3) "$head và vài món khác" else head
}
