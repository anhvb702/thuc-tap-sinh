package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import kotlin.test.Test
import kotlin.test.assertEquals

class WeeklyMealPlanPresentationTest {
    @Test fun preferenceToggleAddsAndRemoves() {
        assertEquals(setOf(FamilyPreferenceTag.SEAFOOD), togglePreferenceTag(emptySet(), FamilyPreferenceTag.SEAFOOD))
        assertEquals(emptySet(), togglePreferenceTag(setOf(FamilyPreferenceTag.SEAFOOD), FamilyPreferenceTag.SEAFOOD))
    }
    @Test fun swapOffsetIncrementsOnlyRequestedDay() {
        assertEquals(mapOf(1 to 4, 3 to 1), bumpDaySwapOffset(mapOf(1 to 4), 3))
        assertEquals(mapOf(1 to 5), bumpDaySwapOffset(mapOf(1 to 4), 1))
    }
    @Test fun copyShowsCountsAndStockResult() {
        assertEquals("2 món cần mua · 3 món đã có trong tủ", shoppingSummaryText(2, 3))
        assertEquals("Đã mua xong (2 món) · Cất vào tủ lạnh 🧊", stockActionButtonLabel(2))
        assertEquals("Đã cất 2 món vào tủ lạnh.", stockResultMessage(2))
    }
    @Test fun zeroStockResultIsGentle() {
        assertEquals("Đã mua xong (0 món) · Cất vào tủ lạnh 🧊", stockActionButtonLabel(0))
        assertEquals("Danh sách đã được cập nhật.", stockResultMessage(0))
    }
}
