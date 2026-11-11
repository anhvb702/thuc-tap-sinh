package com.loitu.yourrefri.feature.home.logic

fun homeGreeting(hour: Int): String {
    require(hour in 0..23)
    return when (hour) {
        in 5..11 -> "Chào buổi sáng 🌿"
        in 12..17 -> "Chào buổi chiều 🌿"
        else -> "Chào buổi tối 🌿"
    }
}
