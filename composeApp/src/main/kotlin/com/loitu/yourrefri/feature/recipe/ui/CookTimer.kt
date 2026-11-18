package com.loitu.yourrefri.feature.recipe.ui

/** Tỉ lệ thời gian còn lại cho vòng đếm ngược; không chia cho 0. */
internal fun timerProgress(remainingSec: Int, totalSec: Int): Float {
    if (totalSec <= 0) return 0f
    return (remainingSec.coerceIn(0, totalSec).toFloat() / totalSec)
}

/** Nhãn mm:ss, ví dụ 156 giây → "02:36". */
internal fun timerLabel(remainingSec: Int): String {
    val safe = remainingSec.coerceAtLeast(0)
    val minutes = safe / 60
    val seconds = safe % 60
    return minutes.toString().padStart(2, '0') + ":" + seconds.toString().padStart(2, '0')
}
