package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanSettings
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal fun weeklyPlanWeekStart(instant: Instant, timeZone: TimeZone): LocalDate {
    val today = instant.toLocalDateTime(timeZone).date
    return today.minus(DatePeriod(days = today.dayOfWeek.isoDayNumber - 1))
}

internal fun rollWeeklyPlanSettings(
    settings: WeeklyMealPlanSettings,
    newWeekStart: LocalDate,
): WeeklyMealPlanSettings = if (settings.weekStart == newWeekStart) {
    settings
} else {
    settings.copy(
        weekStart = newWeekStart,
        swappedOffsets = emptyMap(),
        uncheckedIds = emptySet(),
    )
}
