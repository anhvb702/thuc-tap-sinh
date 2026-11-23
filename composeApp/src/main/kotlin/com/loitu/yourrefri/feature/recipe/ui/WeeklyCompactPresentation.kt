package com.loitu.yourrefri.feature.recipe.ui

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

internal fun compactMealOptionLabel(option: WeeklyMealOption): String = when (option) {
    WeeklyMealOption.DINNER -> "Tối"
    WeeklyMealOption.LUNCH -> "Trưa"
    WeeklyMealOption.BOTH -> "Trưa & Tối"
}

internal fun compactDurationLabel(minutes: Int?): String? =
    if (minutes != null && minutes > 0) "${minutes}p" else null

internal fun compactDayTitle(dayLabel: String, weekStart: LocalDate?, dayIndex: Int): String {
    if (weekStart == null) return dayLabel
    val date = weekStart.plus(DatePeriod(days = dayIndex))
    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = date.monthNumber.toString().padStart(2, '0')
    val year = date.year.toString().padStart(4, '0')
    return "$dayLabel ($day/$month/$year)"
}

internal fun compactDishTitles(mainTitle: String, sideTitle: String?): List<String> =
    listOfNotNull(mainTitle, sideTitle).map { it.trim() }.filter { it.isNotEmpty() }
