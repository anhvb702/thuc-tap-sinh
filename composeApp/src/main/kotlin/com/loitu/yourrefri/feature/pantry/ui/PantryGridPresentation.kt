package com.loitu.yourrefri.feature.pantry.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.IngredientKind
import com.loitu.yourrefri.feature.pantry.data.LeftoverItem
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import com.loitu.yourrefri.feature.pantry.logic.Urgency
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

internal fun pantryGridContentPadding(): PaddingValues = PaddingValues(bottom = 88.dp)

internal data class PantryGridPresentation(
    val presentCount: Int,
    val visibleCells: List<PantryCell>,
)

internal fun pantryGridPresentation(
    catalogCells: List<PantryCell>,
    storageTab: StorageTab,
    presentOnly: Boolean,
    groupTab: PantryGroupTab = PantryGroupTab.FRESH,
    ingredients: List<Ingredient> = emptyList(),
): PantryGridPresentation {
    val kindById = ingredients.associate { it.id to it.kind }
    val groupCells = when (groupTab) {
        PantryGroupTab.FRESH -> catalogCells.filter { (kindById[it.ingredientId] ?: IngredientKind.FRESH) == IngredientKind.FRESH }
        PantryGroupTab.READY -> catalogCells.filter { kindById[it.ingredientId] == IngredientKind.READY }
        PantryGroupTab.SEASONING -> catalogCells.filter { kindById[it.ingredientId] == IngredientKind.SEASONING }
        PantryGroupTab.LEFTOVERS -> emptyList()
    }
    val presentCount = groupCells.count { it.have }
    // Tab "Nên dùng sớm" tự chỉ gồm món đang có nên không cần cờ presentOnly.
    if (storageTab == StorageTab.SOON) {
        return PantryGridPresentation(presentCount, soonCells(groupCells, kindById))
    }
    val locationCells = when (storageTab) {
        StorageTab.ALL, StorageTab.SOON -> groupCells
        StorageTab.FRIDGE -> groupCells.filter { it.compartment == PantryCompartment.CHILL }
        StorageTab.FREEZER -> groupCells.filter { it.compartment == PantryCompartment.FREEZE }
    }
    val visibleCells = if (presentOnly) locationCells.filter { it.have } else locationCells

    return PantryGridPresentation(
        presentCount = presentCount,
        visibleCells = visibleCells,
    )
}

// Cùng quy tắc với expiringList(): món đang có, không phải gia vị và urgency khác OK; quá hạn ước tính đứng đầu.
private fun soonCells(
    cells: List<PantryCell>,
    kindById: Map<String, IngredientKind> = emptyMap(),
): List<PantryCell> =
    cells.filter {
        it.have &&
            kindById[it.ingredientId] != IngredientKind.SEASONING &&
            it.compartment != PantryCompartment.FREEZE &&
            it.urgency != null && it.urgency != Urgency.OK && it.daysLeft != null
    }
        .sortedWith(compareBy<PantryCell> { it.daysLeft }.thenBy { it.ingredientId })

internal fun soonCount(
    cells: List<PantryCell>,
    ingredients: List<Ingredient> = emptyList(),
): Int {
    val kindById = ingredients.associate { it.id to it.kind }
    return soonCells(cells, kindById).size
}

internal fun compartmentChangeMessage(target: String, oldDays: Int, newDays: Int): String =
    "Đã chuyển sang $target (hạn ước tính: $oldDays ngày → $newDays ngày)"

internal const val LEFTOVER_REMINDER_BANNER = "Có đồ ăn thừa cần dùng sớm — chạm để xem"

internal fun leftoverDaysLeft(item: LeftoverItem, today: LocalDate): Int {
    val expiry = item.markedAt.plus(item.shelfDays, DateTimeUnit.DAY)
    return (expiry.toEpochDays() - today.toEpochDays()).toInt()
}

internal fun hasUrgentLeftovers(leftovers: List<LeftoverItem>, today: LocalDate): Boolean =
    leftovers.any { item ->
        item.compartment == PantryCompartment.CHILL || leftoverDaysLeft(item, today) <= URGENT_DAYS
    }

internal fun leftoverReminderBanner(leftovers: List<LeftoverItem>, today: LocalDate): String? =
    if (hasUrgentLeftovers(leftovers, today)) LEFTOVER_REMINDER_BANNER else null
