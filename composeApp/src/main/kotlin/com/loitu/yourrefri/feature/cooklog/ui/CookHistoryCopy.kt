package com.loitu.yourrefri.feature.cooklog.ui

import com.loitu.yourrefri.feature.cooklog.logic.CookHistoryWeekLabel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber

internal object CookHistoryCopy {
    const val title = "Đã nấu"
    const val homeSubtitle = "Xem lại các món đã nấu"
    const val cookAgain = "Nấu lại"
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
