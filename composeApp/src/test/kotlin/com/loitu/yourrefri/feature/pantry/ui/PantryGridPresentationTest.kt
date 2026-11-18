package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.IngredientKind
import com.loitu.yourrefri.feature.pantry.data.LeftoverItem
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class PantryGridPresentationTest {
    private val today = LocalDate.parse("2026-11-07")

    // Deliberately not alphabetical; the selector must keep catalog order.
    private val catalog = listOf(
        cell("cai-ngot", have = true),
        cell("rau-muong", have = false),
        cell("ca-basa", have = true),
        cell("hanh-la", have = false),
    )

    @Test
    fun gridContentPaddingLeavesRoomForTheFloatingAddButton() {
        assertEquals(88.dp, pantryGridContentPadding().calculateBottomPadding())
    }

    @Test
    fun compartmentFeedbackShowsOldAndNewEstimatedDays() {
        assertEquals("Đã chuyển sang Ngăn đông (hạn ước tính: 2 ngày → 20 ngày)", compartmentChangeMessage("Ngăn đông", 2, 20))
    }

    @Test
    fun compartmentPreviewResetsMarkedDateAndUserShelfOverrideLikeReducer() {
        val item = PantryItem("egg", have = true, markedAt = LocalDate.parse("2026-10-01"), compartment = PantryCompartment.CHILL)
        val ingredient = Ingredient("egg", "Trứng", "sua-trung", 5, userShelfDays = 1, freezerShelfDays = 20)
        assertEquals(20, compartmentChangeDays(item, ingredient, PantryCompartment.FREEZE, today))
    }

    @Test
    fun cardCopyMakesTapActionExplicitForBothPantryStates() {
        assertEquals("Đang có · Chạm để đánh dấu hết", pantryCardCopy(have = true, quantity = 1).tapHint)
        assertEquals("Đã hết · Chạm để thêm vào tủ", pantryCardCopy(have = false, quantity = 1).tapHint)
    }

    @Test
    fun presentCount_countsOnlyPresentCatalogCells_andDoesNotDependOnFilters() {
        assertEquals(2, pantryGridPresentation(catalog, StorageTab.ALL, presentOnly = false).presentCount)
        assertEquals(2, pantryGridPresentation(catalog, StorageTab.FRIDGE, presentOnly = true).presentCount)
        assertEquals(2, pantryGridPresentation(catalog, StorageTab.FREEZER, presentOnly = true).presentCount)
    }

    @Test
    fun allLocation_keepsWholeCatalogAndOriginalOrder() {
        val result = pantryGridPresentation(catalog, StorageTab.ALL, presentOnly = false)

        assertEquals(listOf("cai-ngot", "rau-muong", "ca-basa", "hanh-la"), result.visibleCells.ids())
    }

    @Test
    fun fridgeAndFreezerUseSelectedCompartmentAndKeepOrder() {
        val fridge = pantryGridPresentation(catalog, StorageTab.FRIDGE, presentOnly = false)
        val freezer = pantryGridPresentation(catalog, StorageTab.FREEZER, presentOnly = false)

        assertEquals(listOf("cai-ngot", "rau-muong", "hanh-la"), fridge.visibleCells.ids())
        assertEquals(listOf("ca-basa"), freezer.visibleCells.ids())
    }

    @Test
    fun presentOnly_filtersCurrentItemsAfterLocation_andKeepsCatalogOrder() {
        val allPresent = pantryGridPresentation(catalog, StorageTab.ALL, presentOnly = true)
        val fridgePresent = pantryGridPresentation(catalog, StorageTab.FRIDGE, presentOnly = true)
        val freezerPresent = pantryGridPresentation(catalog, StorageTab.FREEZER, presentOnly = true)

        assertEquals(listOf("cai-ngot", "ca-basa"), allPresent.visibleCells.ids())
        assertEquals(listOf("cai-ngot"), fridgePresent.visibleCells.ids())
        assertEquals(listOf("ca-basa"), freezerPresent.visibleCells.ids())
    }

    @Test
    fun emptyAndNoPresentCatalog_returnExpectedListsAndCount() {
        val noItems = pantryGridPresentation(emptyList(), StorageTab.ALL, presentOnly = true)
        val nonePresent = pantryGridPresentation(catalog.map { it.copy(have = false) }, StorageTab.ALL, presentOnly = true)

        assertEquals(0, noItems.presentCount)
        assertEquals(emptyList(), noItems.visibleCells)
        assertEquals(0, nonePresent.presentCount)
        assertEquals(emptyList(), nonePresent.visibleCells)
        assertEquals(4, pantryGridPresentation(catalog.map { it.copy(have = false) }, StorageTab.ALL, presentOnly = false).visibleCells.size)
    }

    @Test
    fun unknownPantryRecord_isNotCountedOutsideCatalogCells() {
        val knownIngredient = Ingredient("rau-muong", "Rau muống", "rau-la", 2)
        val unknownRecord = PantryItem("not-in-catalog", have = true, markedAt = today)
        val state = PantryGridState(
            today = today,
            ingredients = listOf(knownIngredient),
            pantry = mapOf(unknownRecord.ingredientId to unknownRecord),
        )
        val cells = cellsOf(state)

        assertEquals(listOf("rau-muong"), cells.ids())
        assertEquals(0, pantryGridPresentation(cells, StorageTab.ALL, presentOnly = false).presentCount)
    }

    @Test
    fun groupTab_filtersByFreshReadySeasoningAndLeftovers() {
        val ingredients = listOf(
            Ingredient("rau-muong", "Rau muống", "rau-la", 2, kind = IngredientKind.FRESH),
            Ingredient("sua-tuoi", "Sữa tươi", "sua-trung", 7, kind = IngredientKind.READY),
            Ingredient("nuoc-mam", "Nước mắm", "khac", 180, kind = IngredientKind.SEASONING),
        )
        val cells = ingredients.map { cell(it.id, have = true) }

        assertEquals(
            listOf("rau-muong"),
            pantryGridPresentation(cells, StorageTab.ALL, false, PantryGroupTab.FRESH, ingredients).visibleCells.ids(),
        )
        assertEquals(
            listOf("sua-tuoi"),
            pantryGridPresentation(cells, StorageTab.ALL, false, PantryGroupTab.READY, ingredients).visibleCells.ids(),
        )
        assertEquals(
            listOf("nuoc-mam"),
            pantryGridPresentation(cells, StorageTab.ALL, false, PantryGroupTab.SEASONING, ingredients).visibleCells.ids(),
        )
        assertEquals(
            emptyList(),
            pantryGridPresentation(cells, StorageTab.ALL, false, PantryGroupTab.LEFTOVERS, ingredients).visibleCells.ids(),
        )
    }

    @Test
    fun q28_leftoverReminderBanner_showsForChillOrUrgentLeftovers() {
        assertNull(leftoverReminderBanner(emptyList(), today))

        val calmFrozen = listOf(
            LeftoverItem("l1", "Thịt kho", PantryCompartment.FREEZE, markedAt = today, shelfDays = 14),
        )
        assertNull(leftoverReminderBanner(calmFrozen, today))

        val chillLeftover = listOf(
            LeftoverItem("l2", "Canh chua", PantryCompartment.CHILL, markedAt = today, shelfDays = 2),
        )
        assertEquals(LEFTOVER_REMINDER_BANNER, leftoverReminderBanner(chillLeftover, today))

        val urgentFrozen = listOf(
            LeftoverItem("l3", "Cá kho", PantryCompartment.FREEZE, markedAt = LocalDate.parse("2026-10-23"), shelfDays = 14),
        )
        assertEquals(LEFTOVER_REMINDER_BANNER, leftoverReminderBanner(urgentFrozen, today))
    }

    @Test
    fun leftoverUrgencyDependsOnCompartmentAndUsesExactBoundary() {
        assertTrue(isLeftoverUrgent(LeftoverItem("chill-2", "Canh", PantryCompartment.CHILL, today, 2), today))
        assertFalse(isLeftoverUrgent(LeftoverItem("chill-3", "Canh", PantryCompartment.CHILL, today, 3), today))
        assertTrue(isLeftoverUrgent(LeftoverItem("freeze-expired", "Cá", PantryCompartment.FREEZE, LocalDate.parse("2026-11-06"), 0), today))
        assertFalse(isLeftoverUrgent(LeftoverItem("freeze-today", "Cá", PantryCompartment.FREEZE, today, 0), today))
    }

    @Test
    fun q25_frozenExpiredStatus_returnsGentleRecheckLabel() {
        assertEquals("Đã qua 1 ngày (ước tính)", pantryRowStatus(-1, PantryCompartment.FREEZE))
        assertEquals("Đã qua 1 ngày (ước tính)", pantryRowStatus(-1, PantryCompartment.CHILL))
    }

    private fun cell(id: String, have: Boolean) = PantryCell(
        ingredientId = id,
        nameVi = id,
        have = have,
        quantity = 1,
        unit = "phần",
        daysLeft = null,
        urgency = null,
        compartment = if (id == "ca-basa") PantryCompartment.FREEZE else PantryCompartment.CHILL,
    )

    private fun List<PantryCell>.ids() = map(PantryCell::ingredientId)
}
