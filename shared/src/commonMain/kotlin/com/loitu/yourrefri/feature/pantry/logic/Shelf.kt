package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

enum class Urgency { EXPIRED, SOON, OK }

// Đếm theo epoch day để hiệu số ngày lịch đúng qua tháng, năm và năm nhuận.
fun daysLeft(item: PantryItem, ingredient: Ingredient, today: LocalDate): Int {
    val shelfDays = ingredient.userShelfDays ?: ingredient.defaultShelfDays
    val expiry = item.markedAt.plus(shelfDays, DateTimeUnit.DAY)
    return (expiry.toEpochDays() - today.toEpochDays()).toInt()
}

// Ngưỡng SOON 0..2 khớp với cách người dùng đọc "còn vài ngày": hôm nay vẫn dùng được nhưng sắp hết.
fun urgencyOf(daysLeft: Int): Urgency = when {
    daysLeft < 0 -> Urgency.EXPIRED
    daysLeft <= 2 -> Urgency.SOON
    else -> Urgency.OK
}
