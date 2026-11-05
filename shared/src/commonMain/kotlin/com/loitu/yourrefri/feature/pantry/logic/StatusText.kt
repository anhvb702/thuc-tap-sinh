package com.loitu.yourrefri.feature.pantry.logic

fun statusText(daysLeft: Int): String = when {
    daysLeft < 0 -> "Ước tính đã qua ngày nên dùng trước; kiểm tra lại xem còn dùng được không"
    daysLeft == 0 -> "Ước tính nên dùng trước hôm nay"
    else -> "Ước tính nên dùng trước khoảng $daysLeft ngày nữa"
}
