package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.PlannedDayMeal
import com.loitu.yourrefri.feature.recipe.logic.RecipeRecommendation
import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// Logic thuần cho màn "Món ăn & Thực đơn": nhãn tab, thẻ nổi bật, dải 7 ngày.

internal const val RECIPE_SCREEN_TITLE = "Món ăn & Thực đơn"
internal const val HERO_TOP_LABEL = "Gợi ý phù hợp nhất"
internal const val HERO_EMPTY_MESSAGE = "Thêm vài món vào tủ, mình sẽ gợi ý ngay"
internal const val SHOPPING_CARD_TITLE = "Danh sách đi chợ"
internal const val SHOPPING_CARD_SUBTITLE = "Đã trừ phần đang có trong tủ"
internal const val HANDS_FREE_CARD_TITLE = "Chế độ Nấu rảnh tay"
internal const val HANDS_FREE_CARD_SUBTITLE = "Từng bước rõ ràng, màn hình luôn sáng"
internal const val WEEK_SECTION_TITLE = "Thực đơn tuần này"
internal const val WEEK_SECTION_ACTION = "Xem chi tiết"

internal const val HERO_MAX_CARDS = 3

internal data class RecipeTabSpec(val mode: RecipeScreenMode, val label: String)

internal fun recipeTabSpecs(): List<RecipeTabSpec> = listOf(
    RecipeTabSpec(RecipeScreenMode.DAILY, "Gợi ý hôm nay"),
    RecipeTabSpec(RecipeScreenMode.WEEKLY, "Thực đơn tuần"),
    RecipeTabSpec(RecipeScreenMode.SHOPPING, "Đi chợ"),
)

/** "Dễ nấu" khi ít bước và không quá lâu; còn lại "Vừa sức". Thiếu thời gian thì chỉ xét số bước. */
internal fun difficultyLabel(recipe: Recipe): String {
    val shortSteps = recipe.steps.size <= 4
    val shortTime = recipe.minutes?.let { it <= 30 } ?: true
    return if (shortSteps && shortTime) "Dễ nấu" else "Vừa sức"
}

internal fun heroBadgeLabel(index: Int, reason: RecommendationReason): String =
    if (index == 0) HERO_TOP_LABEL else recommendationLabel(reason)

internal fun heroMinutesLabel(minutes: Int?): String = minutes?.let { "$it phút" } ?: "Thời gian chưa rõ"

/** Tối đa [HERO_MAX_CARDS] thẻ; món được yêu cầu mở (nếu có trong danh sách) luôn nằm ở thẻ đầu. */
internal fun heroRecommendations(
    items: List<RecipeRecommendation>,
    preferredId: String?,
): List<RecipeRecommendation> {
    val preferred = items.firstOrNull { it.recipe.id == preferredId }
    if (preferred == null) return items.take(HERO_MAX_CARDS)
    return listOf(preferred) + items.filter { it !== preferred }.take(HERO_MAX_CARDS - 1)
}

internal fun favoriteToggleDescription(title: String, isFavorite: Boolean): String =
    if (isFavorite) "Bỏ $title khỏi yêu thích" else "Thêm $title vào yêu thích"

internal data class WeekDayChip(val dayIndex: Int, val label: String, val hasMeal: Boolean, val isToday: Boolean)

private val shortDayLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
private val fullDayLabels = listOf("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật")

internal fun weekDayChips(plan: List<PlannedDayMeal>, todayIndex: Int?): List<WeekDayChip> {
    val planned = plan.mapTo(HashSet()) { it.dayIndex }
    return shortDayLabels.mapIndexed { index, label ->
        WeekDayChip(index, label, hasMeal = index in planned, isToday = index == todayIndex)
    }
}

internal fun weekDayChipDescription(chip: WeekDayChip): String {
    val parts = buildList {
        add(fullDayLabels.getOrElse(chip.dayIndex) { chip.label })
        if (chip.isToday) add("hôm nay")
        add(if (chip.hasMeal) "đã có món" else "chưa xếp món")
    }
    return parts.joinToString(", ")
}

/** Thứ Hai = 0 ... Chủ nhật = 6, tính theo múi giờ được tiêm vào. */
@OptIn(ExperimentalTime::class)
internal fun todayDayIndex(instant: Instant, timeZone: TimeZone): Int =
    instant.toLocalDateTime(timeZone).date.dayOfWeek.isoDayNumber - 1

internal fun shoppingCardCountText(toBuy: Int): String =
    if (toBuy <= 0) "Tuần này nhà mình đã đủ nguyên liệu" else "$toBuy món cần mua"
