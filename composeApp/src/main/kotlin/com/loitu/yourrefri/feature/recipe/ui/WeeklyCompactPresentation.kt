package com.loitu.yourrefri.feature.recipe.ui

import kotlinx.datetime.LocalDate

internal fun compactMealOptionLabel(option: WeeklyMealOption): String = when (option) {
    WeeklyMealOption.DINNER -> "Tối"
    WeeklyMealOption.LUNCH -> "Trưa"
    WeeklyMealOption.BOTH -> "Trưa & Tối"
}

internal fun compactDurationLabel(minutes: Int?): String? =
    if (minutes != null && minutes > 0) "${minutes}p" else null

internal fun compactDayTitle(dayLabel: String, weekStart: LocalDate?, dayIndex: Int): String =
    if (weekStart != null) dayDateLabel(weekStart, dayIndex, dayLabel) else dayLabel

internal fun compactDishTitles(mainTitle: String, sideTitle: String?): List<String> =
    listOfNotNull(mainTitle, sideTitle).map { it.trim() }.filter { it.isNotEmpty() }
