package com.loitu.yourrefri.feature.home.ui

import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import com.loitu.yourrefri.feature.pantry.logic.Urgency
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeBucketSortTest {
    private fun cell(id: String, daysLeft: Int?) = PantryCell(
        ingredientId = id,
        nameVi = id,
        have = true,
        quantity = 1,
        unit = "phần",
        daysLeft = daysLeft,
        urgency = when {
            daysLeft == null -> null
            daysLeft < 0 -> Urgency.EXPIRED
            daysLeft <= 2 -> Urgency.SOON
            else -> Urgency.OK
        },
    )

    @Test
    fun sortBucketItemsByExpiry_ordersOverdueThenSoonThenFreshThenNullLast() {
        val input = listOf(
            cell("c-fresh", 5),
            cell("a-null", null),
            cell("z-overdue", -2),
            cell("m-today", 0),
            cell("b-soon", 2),
            cell("d-overdue-1", -1),
            cell("b-tie", 2),
        )
        val result = sortBucketItemsByExpiry(input)
        assertEquals(
            listOf("z-overdue", "d-overdue-1", "m-today", "b-soon", "b-tie", "c-fresh", "a-null"),
            result.map { it.ingredientId },
        )
    }

    @Test
    fun bucketItemExpiryLabel_handlesNullWithoutSayingTodayOrBannedWords() {
        assertEquals("Chưa đặt hạn ước tính", bucketItemExpiryLabel(null))
        assertFalse(bucketItemExpiryLabel(null).contains("hôm nay", ignoreCase = true))
        assertEquals("Đã qua 2 ngày", bucketItemExpiryLabel(-2))
        assertEquals("Nên dùng hôm nay", bucketItemExpiryLabel(0))
        assertEquals("Còn 3 ngày", bucketItemExpiryLabel(3))
        listOf(null, -2, 0, 3).forEach { d ->
            val text = bucketItemExpiryLabel(d)
            listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên").forEach { banned ->
                assertFalse(text.contains(banned, ignoreCase = true), text)
            }
        }
    }

    @Test
    fun bucketItemIsUrgent_falseForNullAndAboveTwo_trueForOverdueAndUpToTwo() {
        assertTrue(bucketItemIsUrgent(-1))
        assertTrue(bucketItemIsUrgent(0))
        assertTrue(bucketItemIsUrgent(2))
        assertFalse(bucketItemIsUrgent(3))
        assertFalse(bucketItemIsUrgent(null))
    }
}
