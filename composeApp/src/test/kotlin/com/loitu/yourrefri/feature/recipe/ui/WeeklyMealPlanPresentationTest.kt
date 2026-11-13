package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import kotlin.test.Test
import kotlin.test.assertEquals

class WeeklyMealPlanPresentationTest {
    @Test
    fun weeklyChipSections_haveSeparateShortLabels() {
        assertEquals("Định hướng", WEEKLY_DIET_GOAL_LABEL)
        assertEquals("Khẩu vị", WEEKLY_FAMILY_PREFERENCE_LABEL)
    }

    @Test
    fun lunchPrepFeedbackNamesAffectedIngredientsAndDayLabel() {
        assertEquals("Danh sách đi chợ đã tính thêm phần cơm trưa cho 6 nguyên liệu.", lunchPrepSummaryText(6))
        assertEquals("🍱 + cơm trưa", lunchPrepDayLabel())
    }

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
    @Test fun dayHeaderAndShoppingSubtitleSupportLunchMealPrep() {
        assertEquals("Thứ 2 · Bữa cơm chính", dayMealCardHeader("Thứ 2"))
        assertEquals("Thứ 3 · Bữa trưa & tối", dayMealCardHeader("Thứ 3", "Bữa trưa & tối"))
        assertEquals("Dùng cho 2 bữa", shoppingItemSubtitle(2, 0, 2, isLunchPrep = false))
        assertEquals("Dùng cho 4 phần (có phần cơm trưa mang đi)", shoppingItemSubtitle(4, 0, 4, isLunchPrep = true))
        assertEquals(
            "Cần cho 4 phần (gồm cơm trưa) · trong tủ có 1, mua thêm 3",
            shoppingItemSubtitle(4, 1, 3, isLunchPrep = true),
        )
    }
}
