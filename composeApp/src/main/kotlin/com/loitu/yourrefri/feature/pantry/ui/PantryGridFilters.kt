package com.loitu.yourrefri.feature.pantry.ui

internal data class PantryGridFilters(
    val storageTab: StorageTab = StorageTab.ALL,
    val presentOnly: Boolean = false,
) {
    fun showAllItems() = PantryGridFilters()
}

internal enum class PantryGridEmptyState {
    NONE,
    CATALOG_EMPTY,
    NO_PRESENT_ITEMS,
    NO_PRESENT_IN_LOCATION,
    NO_ITEMS_IN_LOCATION,
}

internal fun pantryGridEmptyState(
    catalogSize: Int,
    visibleSize: Int,
    filters: PantryGridFilters,
): PantryGridEmptyState = when {
    catalogSize == 0 -> PantryGridEmptyState.CATALOG_EMPTY
    visibleSize > 0 -> PantryGridEmptyState.NONE
    filters.presentOnly && filters.storageTab == StorageTab.ALL -> PantryGridEmptyState.NO_PRESENT_ITEMS
    filters.presentOnly -> PantryGridEmptyState.NO_PRESENT_IN_LOCATION
    filters.storageTab != StorageTab.ALL -> PantryGridEmptyState.NO_ITEMS_IN_LOCATION
    else -> PantryGridEmptyState.NONE
}
