package com.loitu.yourrefri.feature.home.ui

import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeBucketSortLogicTest {
    @Test
    fun overdueIdsAreEmptyForAnEmptyBucket() {
        assertEquals(emptyList(), overdueBucketIngredientIds(emptyList()))
    }

    @Test
    fun overdueIdsExcludeTodayAndFutureEstimates() {
        assertEquals(emptyList(), overdueBucketIngredientIds(listOf(cell("today", 0), cell("fresh", 3))))
    }

    @Test
    fun overdueIdsExcludeItemsWithoutAnEstimate() {
        assertEquals(emptyList(), overdueBucketIngredientIds(listOf(cell("unknown", null))))
    }

    @Test
    fun overdueIdsPreserveInputOrderInAMixedBucket() {
        val items = listOf(cell("fresh", 4), cell("z", -1), cell("today", 0), cell("a", -3))

        assertEquals(listOf("z", "a"), overdueBucketIngredientIds(items))
    }

    @Test
    fun overdueIdsDeduplicateOnlyAfterFiltering() {
        val items = listOf(cell("a", 2), cell("z", -1), cell("a", -2), cell("z", -3))

        assertEquals(listOf("z", "a"), overdueBucketIngredientIds(items))
    }

    @Test
    fun clearButtonRequiresASupportedBucketShowingAtLeastOneOverdueItem() {
        for (supportsFilter in listOf(false, true)) {
            for (showUsable in listOf(false, true)) {
                for (count in listOf(-1, 0, 1, 3)) {
                    val expected = Triple(supportsFilter, showUsable, count) in
                        setOf(Triple(true, false, 1), Triple(true, false, 3))
                    assertEquals(
                        expected,
                        shouldShowClearOverdueButton(supportsFilter, showUsable, count),
                        "supportsFilter=$supportsFilter, showUsable=$showUsable, count=$count",
                    )
                }
            }
        }
    }

    @Test
    fun clearOverdueCopyUsesTheActualCountAndEstimateWording() {
        for (count in listOf(1, 3)) {
            assertEquals("Dọn hết $count món đã qua mốc", clearOverdueButtonLabel(count))
            assertEquals("Dọn $count món đã qua mốc ước tính?", clearOverdueDialogTitle(count))
            assertEquals("Đã dọn $count món qua mốc ước tính", clearOverdueUndoMessage(count))
        }
    }

    private fun cell(id: String, daysLeft: Int?) = PantryCell(
        ingredientId = id,
        nameVi = id,
        have = true,
        quantity = 1,
        unit = "phần",
        daysLeft = daysLeft,
        urgency = null,
    )
}
