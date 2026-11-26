package com.loitu.yourrefri.feature.home.ui

import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import com.loitu.yourrefri.feature.pantry.logic.Urgency
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
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
            listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên").forEach { banned -> // lint-copy: rule
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

    @Test
    fun supportsBucketFreshnessFilter_enabledForFridgeAndFreezerOnly() {
        assertEquals(
            listOf(PantryOverviewBucket.FRIDGE, PantryOverviewBucket.FREEZER),
            PantryOverviewBucket.entries.filter(::supportsBucketFreshnessFilter),
        )
    }

    @Test
    fun filterBucketItems_disabledReturnsAllItems() {
        val input = listOf(cell("past", -1), cell("today", 0), cell("unknown", null))
        BucketFreshnessFilter.entries.forEach { filter ->
            assertSame(input, filterBucketItems(input, enabled = false, filter = filter))
        }
    }

    @Test
    fun filterBucketItems_pastEstimateReturnsOnlyNegativeDaysLeft() {
        val past = cell("past", -1)
        val earlier = cell("earlier", -4)
        val input = listOf(cell("today", 0), past, cell("unknown", null), earlier, cell("later", 3))
        assertEquals(listOf(past, earlier), filterBucketItems(input, true, BucketFreshnessFilter.PAST_ESTIMATE))
    }

    @Test
    fun filterBucketItems_usableReturnsZeroAndPositiveDaysLeft() {
        val today = cell("today", 0)
        val unknown = cell("unknown", null)
        val later = cell("later", 3)
        val input = listOf(later, cell("past", -1), unknown, today)
        assertEquals(listOf(later, unknown, today), filterBucketItems(input, true, BucketFreshnessFilter.USABLE))
    }

    @Test
    fun defaultBucketFreshnessFilter_picksPastEstimateWhenOverdueExistsElseUsable() {
        val usable = listOf(cell("unknown", null), cell("today", 0), cell("later", 3))
        assertEquals(BucketFreshnessFilter.PAST_ESTIMATE, defaultBucketFreshnessFilter(usable + cell("past", -1)))
        assertEquals(BucketFreshnessFilter.USABLE, defaultBucketFreshnessFilter(usable))
        assertEquals(BucketFreshnessFilter.USABLE, defaultBucketFreshnessFilter(emptyList()))
    }

    @Test
    fun bucketFreshnessFilterLabels_containNoBannedCopy() {
        val labels = BucketFreshnessFilter.entries.map { it.labelVi } + listOf(
            BUCKET_FILTER_BUTTON_OFF,
            BUCKET_FILTER_BUTTON_ON,
            BUCKET_FILTER_RESET_LABEL,
            BUCKET_FILTER_EMPTY_MESSAGE,
        )
        val bannedCopy = listOf("hết hạn", "bạn quên", "lãng phí", "bỏ lỡ") // lint-copy: rule
        labels.forEach { label ->
            bannedCopy.forEach { banned ->
                assertFalse(label.contains(banned, ignoreCase = true), label)
            }
        }
    }

    @Test
    fun bucketSwitchFilterOf_trueIsUsableAndFalseIsPastEstimate() {
        assertEquals(BucketFreshnessFilter.USABLE, bucketSwitchFilterOf(true))
        assertEquals(BucketFreshnessFilter.PAST_ESTIMATE, bucketSwitchFilterOf(false))
    }

    @Test
    fun filterBucketBySwitch_defaultTrueReturnsUsableOnly() {
        val usable = listOf(cell("later", 3), cell("unknown", null), cell("today", 0))
        assertEquals(usable, filterBucketBySwitch(listOf(cell("past", -1)) + usable, showUsable = true))
    }

    @Test
    fun filterBucketBySwitch_falseReturnsPastEstimateOnly() {
        val past = listOf(cell("past", -1), cell("earlier", -4))
        val input = past + listOf(cell("today", 0), cell("unknown", null), cell("later", 3))
        assertEquals(past, filterBucketBySwitch(input, showUsable = false))
    }

    @Test
    fun bucketSwitchStatusLabel_formatsUsableAndPastWithCount() {
        assertEquals("Đồ dùng được", bucketSwitchStatusLabel(showUsable = true, pastCount = 2))
        assertEquals("Đã qua mốc · 2", bucketSwitchStatusLabel(showUsable = false, pastCount = 2))
    }

    @Test
    fun bucketSwitchStatusLabel_omitsNonPositivePastCount() {
        assertEquals("Đã qua mốc", bucketSwitchStatusLabel(showUsable = false, pastCount = 0))
        assertEquals("Đã qua mốc", bucketSwitchStatusLabel(showUsable = false, pastCount = -1))
    }

    @Test
    fun bucketSwitchLabels_containNoBannedCopy() {
        val labels = listOf(BUCKET_SWITCH_USABLE_LABEL, BUCKET_SWITCH_PAST_LABEL,
            BUCKET_SWITCH_EMPTY_USABLE_MESSAGE, BUCKET_SWITCH_EMPTY_PAST_MESSAGE)
        val bannedCopy = listOf("hết hạn", "bạn quên", "lãng phí", "bỏ lỡ") // lint-copy: rule
        labels.forEach { label ->
            bannedCopy.forEach { banned -> assertFalse(label.contains(banned, ignoreCase = true), label) }
        }
    }
}
