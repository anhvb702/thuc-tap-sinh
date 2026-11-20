package com.loitu.yourrefri.feature.recipe.ui

internal fun toggleManualCookStep(checkedSteps: Set<Int>, index: Int): Set<Int> =
    if (index in checkedSteps) checkedSteps - index else checkedSteps + index

internal fun manualCookProgress(checkedSteps: Set<Int>, totalSteps: Int): Float =
    if (totalSteps <= 0) 1f else (checkedSteps.size.toFloat() / totalSteps).coerceIn(0f, 1f)

internal fun canFinishManualCook(checkedSteps: Set<Int>, totalSteps: Int): Boolean =
    totalSteps <= 0 || checkedSteps.size >= totalSteps
