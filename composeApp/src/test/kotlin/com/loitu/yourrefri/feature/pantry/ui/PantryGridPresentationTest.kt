package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

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
        // P2a (Master Plan đã duyệt): tăng bottom padding 88dp -> 148dp để FAB không che hàng thẻ cuối.
        assertEquals(148.dp, pantryGridContentPadding().calculateBottomPadding())
    }

    @Test
    fun pantryItemMenuCopy_formatsLabelsAndAvoidsBannedWords() {
        val copy = pantryItemMenuCopy("Trứng gà", "quả")
        assertEquals("Trứng gà", copy.title)
        assertEquals("Sửa hạn ước tính", copy.editShelfLabel)
        assertEquals("Đổi đơn vị (quả)", copy.editUnitLabel)
        assertEquals("Đánh dấu hết món", copy.markOutLabel)
        assertEquals("Tùy chọn khác cho Trứng gà", copy.moreButtonDescription)
        assertEquals("Đóng", copy.closeLabel)
        val joined = listOf(copy.editShelfLabel, copy.editUnitLabel, copy.markOutLabel, copy.moreButtonDescription).joinToString(" ")
        for (w in listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", "tồn kho")) {
            kotlin.test.assertFalse(joined.contains(w, ignoreCase = true))
        }
    }

    @Test
    fun pantryItemMenuCopy_omitsParenthesesWhenUnitIsBlank() {
        assertEquals("Đổi đơn vị", pantryItemMenuCopy("Tỏi", "   ").editUnitLabel)
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
    fun fridgeAndFreezerUseExistingClassificationAndKeepOrder() {
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

    private fun cell(id: String, have: Boolean) = PantryCell(
        ingredientId = id,
        nameVi = id,
        have = have,
        quantity = 1,
        unit = "phần",
        daysLeft = null,
        urgency = null,
    )

    private fun List<PantryCell>.ids() = map(PantryCell::ingredientId)
}
