package com.loitu.yourrefri.reminder

import android.content.Context
import com.loitu.yourrefri.AppGraph
import com.loitu.yourrefri.feature.notice.logic.ReminderSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

fun loadReminderSnapshot(context: Context): ReminderSnapshot = runBlocking {
    val dependencies = AppGraph.get(context.applicationContext)
    val overrides = dependencies.pantryRepository.observeShelfOverrides().first()
    val pantry = dependencies.pantryRepository.observe().first()
    val ingredients = dependencies.ingredients.map { it.copy(userShelfDays = overrides[it.id]) }
    val customRecipes = dependencies.customRecipeRepository.observeAll().first()
    val weeklyPlan = dependencies.weeklyMealPlanRepository.observeAll().first()
    val disabledMeals = dependencies.weeklyMealPlanRepository.observeDisabledMeals().first()
    val leftovers = dependencies.leftoverRepository.observeAll().first()

    ReminderSnapshot(
        weeklyPlan = weeklyPlan,
        recipes = dependencies.recipes + customRecipes,
        ingredients = ingredients,
        pantry = pantry,
        leftovers = leftovers,
        disabledMeals = disabledMeals,
    )
}
