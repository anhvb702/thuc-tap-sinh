package com.loitu.yourrefri.feature.cooklog.ui

import com.loitu.yourrefri.feature.cooklog.logic.CookHistoryWeekLabel
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.ui.MealCopy
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber

internal object CookHistoryCopy {
    const val title = "Đã nấu"
    const val homeSubtitle = "Xem lại các món đã nấu"
    const val cookAgain = "Nấu lại"
    const val deleteEntry = "Xóa dòng ghi nhầm"
    const val delete = "Xóa"
    const val deletePrompt = "Xóa dòng này khỏi lịch sử? Nguyên liệu trong tủ sẽ giữ nguyên."
    const val cancel = "Hủy"
    const val eyebrow = "NHỮNG BỮA CƠM NHÀ"
    const val subtitle = "Món quen vẫn ngon, nấu lại lúc nào cũng được."
    const val unknownRecipe = "Món đã lưu"
    const val empty = "Chưa có món nào ở đây. Nấu xong, món sẽ nằm gọn trong này nhé"

    fun weekLabel(label: CookHistoryWeekLabel): String = when (label) {
        CookHistoryWeekLabel.THIS_WEEK -> "Tuần này"
        CookHistoryWeekLabel.LAST_WEEK -> "Tuần trước"
        CookHistoryWeekLabel.EARLIER -> "Trước đó"
    }

    fun moreIngredients(hidden: Int): String = "+$hidden"
}

private val shortWeekdays = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

/** Ví dụ "T5 12/11". */
internal fun cookDateLabel(date: LocalDate): String {
    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = date.monthNumber.toString().padStart(2, '0')
    return "${shortWeekdays[date.dayOfWeek.isoDayNumber - 1]} $day/$month"
}

private val fullWeekdays = listOf("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật")

/** Ví dụ "Thứ 2, 16.11 · Bữa tối"; chưa biết bữa thì bỏ phần bữa. */
internal fun cookDateMealLabel(date: LocalDate, meal: MealSlot?): String {
    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = date.monthNumber.toString().padStart(2, '0')
    val base = "${fullWeekdays[date.dayOfWeek.isoDayNumber - 1]}, $day.$month"
    return if (meal == null) base else "$base · ${MealCopy.label(meal)}"
}
