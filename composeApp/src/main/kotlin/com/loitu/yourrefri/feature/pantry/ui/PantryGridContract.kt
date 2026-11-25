package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import kotlinx.datetime.TimeZone

sealed interface PantryGridIntent {
    data class Action(val action: PantryGridAction) : PantryGridIntent
    data class Refresh(val timeZone: TimeZone? = null) : PantryGridIntent
}

sealed interface PantryGridUiEffect {
    data class SaveFailed(val ingredientId: String) : PantryGridUiEffect
    data object LoadFailed : PantryGridUiEffect
}
