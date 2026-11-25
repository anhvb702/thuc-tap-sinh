package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.recipe.ui.WeeklyMealPlanIntent.SetGoal as SetGoalIntent
import com.loitu.yourrefri.feature.recipe.ui.WeeklyMealPlanIntent.SetLunchMealPrep as SetLunchMealPrepIntent
import com.loitu.yourrefri.feature.recipe.ui.WeeklyMealPlanIntent.SwapDay as SwapDayIntent
import com.loitu.yourrefri.feature.recipe.ui.WeeklyMealPlanIntent.ToggleIngredient as ToggleIngredientIntent
import com.loitu.yourrefri.feature.recipe.ui.WeeklyMealPlanIntent.TogglePreference as TogglePreferenceIntent
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

data class WeeklyMealPlanState(
    val isLoaded: Boolean,
    val weekStart: LocalDate,
    val goal: DietGoal = DietGoal.BALANCED,
    val preferences: Set<FamilyPreferenceTag> = emptySet(),
    val swappedOffsets: Map<Int, Int> = emptyMap(),
    val uncheckedIds: Set<String> = emptySet(),
    val includeLunchMealPrep: Boolean = false,
)

sealed interface WeeklyMealPlanStateAction : WeeklyMealPlanIntent
sealed interface WeeklyMealPlanIntent {
    data class SetGoal(val value: DietGoal) : WeeklyMealPlanStateAction
    data class TogglePreference(val value: FamilyPreferenceTag) : WeeklyMealPlanStateAction
    data class SwapDay(val dayIndex: Int) : WeeklyMealPlanStateAction
    data class ToggleIngredient(val id: String, val checked: Boolean) : WeeklyMealPlanStateAction
    data class SetLunchMealPrep(val enabled: Boolean) : WeeklyMealPlanStateAction
    data class Refresh(val timeZone: TimeZone) : WeeklyMealPlanIntent
    data object ReloadSettings : WeeklyMealPlanIntent
    data class UpdateTasteProfile(val profile: FamilyTasteProfile) : WeeklyMealPlanIntent

    // Kotlin cannot resolve nested classifiers through a typealias.
    @Suppress("FunctionName")
    companion object {
        fun SetGoal(value: DietGoal): SetGoalIntent = SetGoalIntent(value)
        fun TogglePreference(value: FamilyPreferenceTag): TogglePreferenceIntent = TogglePreferenceIntent(value)
        fun SwapDay(dayIndex: Int): SwapDayIntent = SwapDayIntent(dayIndex)
        fun ToggleIngredient(id: String, checked: Boolean): ToggleIngredientIntent = ToggleIngredientIntent(id, checked)
        fun SetLunchMealPrep(enabled: Boolean): SetLunchMealPrepIntent = SetLunchMealPrepIntent(enabled)
    }
}

typealias WeeklyMealPlanAction = WeeklyMealPlanIntent

sealed interface WeeklyMealPlanEffect {
    data object SettingsSaveFailed : WeeklyMealPlanEffect
    data object SettingsLoadFailed : WeeklyMealPlanEffect
}
