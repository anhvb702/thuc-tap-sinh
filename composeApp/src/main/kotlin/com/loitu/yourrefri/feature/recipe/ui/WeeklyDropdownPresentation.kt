package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag

internal fun dietGoalFieldText(goal: DietGoal): String = goal.labelVi

internal fun preferenceFieldText(selected: Set<FamilyPreferenceTag>): String =
    if (selected.isEmpty()) "Chưa chọn, món nào cũng được"
    else FamilyPreferenceTag.entries.filter { it in selected }.joinToString(", ") { it.labelVi }

internal fun preferenceOptionDescription(label: String, selected: Boolean): String =
    "$label, ${if (selected) "đã chọn" else "chưa chọn"}"
