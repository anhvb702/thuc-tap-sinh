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
