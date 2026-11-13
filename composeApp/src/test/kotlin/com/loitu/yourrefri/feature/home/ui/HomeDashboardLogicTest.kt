package com.loitu.yourrefri.feature.home.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Logic thuần của Trang chủ mới: đếm ngăn mát/ngăn đông, chữ ước tính, bố cục dải gợi ý món.
class HomeDashboardLogicTest {
    @Test
    fun zoneCounts_emptyList_isZeroZero() {
        assertEquals(PantryZoneCounts(fridge = 0, freezer = 0), pantryZoneCounts(emptyList()))
    }

    @Test
    fun zoneCounts_splitsFreezerAndFridge() {
        val freezer = "thit-ga"
        val counts = pantryZoneCounts(listOf(freezer, "ca-chua", "trung"))
        assertEquals(1, counts.freezer)
        assertEquals(2, counts.fridge)
    }

    @Test
    fun zoneCounts_sumEqualsInput() {
        val ids = listOf("a", "b", "c", "d")
        val c = pantryZoneCounts(ids)
        assertEquals(ids.size, c.fridge + c.freezer)
    }

    @Test
    fun urgentCaption_containsEstimateWord() {
        assertEquals("Ước tính còn 1 ngày", urgentCaption(1))
        assertEquals("Ước tính còn 2 ngày", urgentCaption(2))
        assertEquals("Nên dùng hôm nay (ước tính)", urgentCaption(0))
        assertEquals("Kiểm tra lại giúp mình", urgentCaption(-1))
    }

    @Test
    fun urgentCaption_avoidsBannedWords() {
        (-2..3).map(::urgentCaption).forEach { text ->
            listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên").forEach { assertFalse(text.contains(it, true), text) }
        }
    }

    @Test
    fun recipeStrip_scrollsOnlyAtLargeFont() {
        assertFalse(recipeStripScrolls(1.0f))
        assertFalse(recipeStripScrolls(1.29f))
        assertTrue(recipeStripScrolls(1.3f))
        assertTrue(recipeStripScrolls(2.0f))
    }

    @Test
    fun recipeStrip_showsAtMostThree() {
        assertEquals(3, homeRecipeLimit)
    }

    @Test
    fun pantryCardDescription_mentionsCountAndZones() {
        val d = pantryCardDescription(5, PantryZoneCounts(3, 2))
        assertTrue(d.contains("5"))
        assertTrue(d.contains("3"))
        assertTrue(d.contains("2"))
        assertTrue(pantryCardDescription(0, PantryZoneCounts(0, 0)).contains("trống"))
    }
}
