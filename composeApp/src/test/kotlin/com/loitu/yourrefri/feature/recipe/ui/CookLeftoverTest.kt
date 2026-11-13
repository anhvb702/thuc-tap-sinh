package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.data.MealSlot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CookLeftoverTest {
    private val banned = listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên")

    @Test
    fun ingredientsToConsume_removesKept_keepsOrder() {
        assertEquals(listOf("ca", "hanh"), ingredientsToConsume(listOf("ca", "ca-chua", "hanh"), setOf("ca-chua")))
    }

    @Test
    fun ingredientsToConsume_emptyKept_returnsDistinctInOrder() {
        assertEquals(listOf("ca", "hanh"), ingredientsToConsume(listOf("ca", "hanh", "ca"), emptySet()))
    }

    @Test
    fun ingredientsToConsume_keepAll_returnsEmpty() {
        assertEquals(emptyList(), ingredientsToConsume(listOf("ca", "hanh"), setOf("ca", "hanh", "khong-co")))
    }

    @Test
    fun ingredientsToConsume_emptyCandidates_returnsEmpty() {
        assertEquals(emptyList(), ingredientsToConsume(emptyList(), setOf("ca")))
    }

    @Test
    fun leftoverCopy_isExactAndAvoidsBannedWords() {
        assertEquals("Dùng hết hay còn lại?", CookLeftoverCopy.title)
        assertEquals("Dùng hết", CookLeftoverCopy.useAll)
        assertEquals("Còn lại", CookLeftoverCopy.keepSome)
        assertEquals("Xong rồi", CookLeftoverCopy.confirm)
        assertEquals("Để sau", CookLeftoverCopy.later)
        listOf(CookLeftoverCopy.title, CookLeftoverCopy.useAll, CookLeftoverCopy.keepSome, CookLeftoverCopy.confirm, CookLeftoverCopy.later)
            .forEach { t -> banned.forEach { w -> assertFalse(t.contains(w, ignoreCase = true)) } }
    }

    @Test
    fun resultMessage_listsOnlyConsumedNames() {
        assertEquals("Đã cập nhật trong tủ: Cá, Hành.", cookResultMessage(listOf("Cá", "Hành")))
    }

    @Test
    fun resultMessage_whenNothingConsumed_isGentle() {
        assertEquals("Món đã nấu xong. Chưa có nguyên liệu nào cần cập nhật trong tủ.", cookResultMessage(emptyList()))
    }

    @Test
    fun mealHeadingAndLabels_followTheMeal() {
        assertEquals("Trưa nay nhà mình ăn gì nhỉ?", MealCopy.heading(MealSlot.LUNCH))
        assertEquals("Tối nay nhà mình ăn gì nhỉ?", MealCopy.heading(MealSlot.DINNER))
        assertEquals("Bữa trưa", MealCopy.label(MealSlot.LUNCH))
        assertEquals("Bữa tối", MealCopy.label(MealSlot.DINNER))
    }
}
