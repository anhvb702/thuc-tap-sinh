package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanSettings
import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class WeeklyMealPlanStateLogicTest {
    @Test
    fun mondayAndSunday_resolveToSameMonday() {
        assertEquals(
            LocalDate(2026, 11, 9),
            weeklyPlanWeekStart(Instant.parse("2026-11-09T08:00:00Z"), TimeZone.UTC),
        )
        assertEquals(
            LocalDate(2026, 11, 9),
            weeklyPlanWeekStart(Instant.parse("2026-11-15T23:00:00Z"), TimeZone.UTC),
        )
    }

    @Test
    fun timezoneCanMoveInstantIntoPreviousLocalWeek() {
        val now = Instant.parse("2026-11-09T00:30:00Z")

        assertEquals(LocalDate(2026, 11, 9), weeklyPlanWeekStart(now, TimeZone.UTC))
        assertEquals(LocalDate(2026, 11, 2), weeklyPlanWeekStart(now, TimeZone.of("-05:00")))
    }

    @Test
    fun sundayAtYearBoundary_usesCalendarMonday() {
        assertEquals(
            LocalDate(2026, 12, 28),
            weeklyPlanWeekStart(Instant.parse("2027-01-03T23:30:00Z"), TimeZone.UTC),
        )
    }

    @Test
    fun rollover_resetsOnlyWeekScopedFields() {
        val saved = WeeklyMealPlanSettings(
            weekStart = LocalDate(2026, 11, 2),
            goalId = DietGoal.LIGHT.name,
            preferenceIds = setOf(FamilyPreferenceTag.MEAT.name),
            swappedOffsets = mapOf(1 to 2, 4 to 1),
            uncheckedIds = setOf("rice", "egg"),
            includeLunchMealPrep = true,
        )

        assertEquals(
            saved.copy(
                weekStart = LocalDate(2026, 11, 9),
                swappedOffsets = emptyMap(),
                uncheckedIds = emptySet(),
            ),
            rollWeeklyPlanSettings(saved, LocalDate(2026, 11, 9)),
        )
    }

    @Test
    fun sameWeek_keepsEverySetting() {
        val saved = WeeklyMealPlanSettings(
            weekStart = LocalDate(2026, 11, 9),
            goalId = DietGoal.QUICK.name,
            preferenceIds = setOf(FamilyPreferenceTag.SEAFOOD.name),
            swappedOffsets = mapOf(5 to 3),
            uncheckedIds = setOf("tofu"),
            includeLunchMealPrep = true,
        )

        assertEquals(saved, rollWeeklyPlanSettings(saved, LocalDate(2026, 11, 9)))
    }
}
