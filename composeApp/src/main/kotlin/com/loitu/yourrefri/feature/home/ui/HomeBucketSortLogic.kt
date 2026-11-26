package com.loitu.yourrefri.feature.home.ui

import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import com.loitu.yourrefri.feature.pantry.ui.URGENT_DAYS
import com.loitu.yourrefri.feature.pantry.ui.estimateDaysText

internal fun sortBucketItemsByExpiry(items: List<PantryCell>): List<PantryCell> =
    items.sortedWith(compareBy<PantryCell> { it.daysLeft ?: Int.MAX_VALUE }.thenBy { it.ingredientId })

internal fun bucketItemExpiryLabel(daysLeft: Int?): String =
    if (daysLeft == null) "Chưa đặt hạn ước tính" else estimateDaysText(daysLeft)

internal fun bucketItemIsUrgent(daysLeft: Int?): Boolean =
    daysLeft != null && daysLeft <= URGENT_DAYS

internal enum class BucketFreshnessFilter(val labelVi: String) {
    PAST_ESTIMATE("Đã qua mốc"),
    USABLE("Còn dùng được"),
}

internal const val BUCKET_FILTER_BUTTON_OFF = "Bật lọc dùng được"
internal const val BUCKET_FILTER_BUTTON_ON = "Đang bật lọc"
internal const val BUCKET_FILTER_RESET_LABEL = "Tắt bộ lọc"
internal const val BUCKET_FILTER_EMPTY_MESSAGE = "Chưa có thực phẩm nào ở nhóm lọc này."

internal fun supportsBucketFreshnessFilter(bucket: PantryOverviewBucket): Boolean =
    bucket == PantryOverviewBucket.FRIDGE || bucket == PantryOverviewBucket.FREEZER

internal fun filterBucketItems(
    items: List<PantryCell>,
    enabled: Boolean,
    filter: BucketFreshnessFilter,
): List<PantryCell> {
    if (!enabled) return items
    return when (filter) {
        BucketFreshnessFilter.PAST_ESTIMATE -> items.filter { (it.daysLeft ?: 0) < 0 }
        BucketFreshnessFilter.USABLE -> items.filter {
            val daysLeft = it.daysLeft
            daysLeft == null || daysLeft >= 0
        }
    }
}

internal fun defaultBucketFreshnessFilter(items: List<PantryCell>): BucketFreshnessFilter =
    if (items.any { (it.daysLeft ?: 0) < 0 }) BucketFreshnessFilter.PAST_ESTIMATE else BucketFreshnessFilter.USABLE

internal const val BUCKET_SWITCH_USABLE_LABEL = "Đồ dùng được"
internal const val BUCKET_SWITCH_PAST_LABEL = "Đã qua mốc"
internal const val BUCKET_SWITCH_EMPTY_PAST_MESSAGE = "Không có món nào cần kiểm tra lại."
internal const val BUCKET_SWITCH_EMPTY_USABLE_MESSAGE = "Chưa có món dùng được trong ngăn này."

internal fun bucketSwitchFilterOf(showUsable: Boolean): BucketFreshnessFilter =
    if (showUsable) BucketFreshnessFilter.USABLE else BucketFreshnessFilter.PAST_ESTIMATE

internal fun filterBucketBySwitch(items: List<PantryCell>, showUsable: Boolean): List<PantryCell> =
    filterBucketItems(items, enabled = true, filter = bucketSwitchFilterOf(showUsable))

internal fun bucketSwitchStatusLabel(showUsable: Boolean, pastCount: Int): String =
    if (showUsable) BUCKET_SWITCH_USABLE_LABEL else if (pastCount > 0) "$BUCKET_SWITCH_PAST_LABEL · $pastCount" else BUCKET_SWITCH_PAST_LABEL
