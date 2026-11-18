package com.loitu.yourrefri.feature.recipe.ui

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** Nhãn ngày theo Figma 05 từ ngày bắt đầu tuần và chỉ số ngày (0 = thứ 2): "Thứ 4 · 18.11". */
internal fun dayDateLabel(weekStart: LocalDate, dayIndex: Int, dayLabel: String): String {
    val date = weekStart.plus(DatePeriod(days = dayIndex))
    return dayChipLabel(dayName = dayLabel, day = date.dayOfMonth, month = date.monthNumber)
}
