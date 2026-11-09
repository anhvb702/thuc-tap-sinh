package com.loitu.yourrefri.feature.pantry.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class PantryGridFiltersTest {
    @Test
    fun initialFilters_showFullCatalogAndAllLocations() {
        assertEquals(PantryGridFilters(StorageTab.ALL, presentOnly = false), PantryGridFilters())
    }

    @Test
    fun showAllItems_resetsLocationAndPresenceFilter() {
        val filtered = PantryGridFilters(StorageTab.FREEZER, presentOnly = true)

        assertEquals(PantryGridFilters(StorageTab.ALL, presentOnly = false), filtered.showAllItems())
    }

    @Test
    fun emptyState_catalogWithoutCells_isCatalogEmpty() {
        assertEquals(
            PantryGridEmptyState.CATALOG_EMPTY,
            pantryGridEmptyState(catalogSize = 0, visibleSize = 0, filters = PantryGridFilters()),
        )
    }

    @Test
    fun emptyState_noPresentItemsAcrossCatalog_isNoPresentItems() {
        assertEquals(
            PantryGridEmptyState.NO_PRESENT_ITEMS,
            pantryGridEmptyState(
                catalogSize = 25,
                visibleSize = 0,
                filters = PantryGridFilters(StorageTab.ALL, presentOnly = true),
            ),
        )
    }

    @Test
    fun emptyState_noPresentItemsInLocation_isLocationSpecific() {
        assertEquals(
            PantryGridEmptyState.NO_PRESENT_IN_LOCATION,
            pantryGridEmptyState(
                catalogSize = 25,
                visibleSize = 0,
                filters = PantryGridFilters(StorageTab.FRIDGE, presentOnly = true),
            ),
        )
    }

    @Test
    fun emptyState_noItemsInLocationWithoutPresenceFilter_isCategoryEmpty() {
        assertEquals(
            PantryGridEmptyState.NO_ITEMS_IN_LOCATION,
            pantryGridEmptyState(
                catalogSize = 25,
                visibleSize = 0,
                filters = PantryGridFilters(StorageTab.FREEZER, presentOnly = false),
            ),
        )
    }

    @Test
    fun emptyState_visibleCells_haveNoEmptyMessage() {
        assertEquals(
            PantryGridEmptyState.NONE,
            pantryGridEmptyState(
                catalogSize = 25,
                visibleSize = 1,
                filters = PantryGridFilters(StorageTab.ALL, presentOnly = true),
            ),
        )
    }
}
