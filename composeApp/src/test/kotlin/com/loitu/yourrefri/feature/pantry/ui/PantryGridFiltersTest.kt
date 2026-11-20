package com.loitu.yourrefri.feature.pantry.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PantryGridFiltersTest {
    @Test
    fun initialFilters_showFullCatalogAndAllLocations() {
        val filters = PantryGridFilters()
        assertEquals(StorageTab.ALL, filters.storageTab)
        assertEquals(false, filters.presentOnly)
        assertEquals(setOf(PantryCategory.UNPROCESSED, PantryCategory.PROCESSED, PantryCategory.SPICE), filters.selectedCategories)
        assertEquals(listOf("Chưa chế biến", "Đã chế biến", "Gia vị"), PantryCategory.entries.map { it.labelVi })
    }

    @Test
    fun showAllItems_resetsLocationAndPresenceFilter() {
        val filtered = PantryGridFilters(StorageTab.FREEZER, presentOnly = true, selectedCategories = emptySet())

        assertEquals(PantryGridFilters(), filtered.showAllItems())
    }

    @Test
    fun pantryStatusSwitchLabel_dependsOnlyOnPresence() {
        assertEquals("Đang có trong tủ", pantryStatusSwitchLabel(true))
        assertEquals("Tất cả nguyên liệu", pantryStatusSwitchLabel(false))
    }

    @Test
    fun parseDirectQuantityInput_trimsAndKeepsZeroAndPositiveIntegers() {
        assertEquals(0, parseDirectQuantityInput(" 0 "))
        assertEquals(3, parseDirectQuantityInput(" 003 "))
        assertEquals(999, parseDirectQuantityInput("999"))
    }

    @Test
    fun parseDirectQuantityInput_clampsToTheRequestedMaximumEvenForLargeIntegers() {
        assertEquals(999, parseDirectQuantityInput("1000"))
        assertEquals(999, parseDirectQuantityInput("999999999999999999999999"))
        assertEquals(12, parseDirectQuantityInput("27", maxQuantity = 12))
    }

    @Test
    fun parseDirectQuantityInput_rejectsBlankNegativeAndNonIntegerInput() {
        listOf("", "  ", "-1", "1.5", "2e3", "1 2", "3 phần").forEach {
            assertNull(parseDirectQuantityInput(it), it)
        }
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
