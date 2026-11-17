package com.loitu.yourrefri.feature.recipe.ui

// Logic thuần cho chế độ nấu rảnh tay, không phụ thuộc Compose.
// Recipe.required chỉ chứa id nguyên liệu (chưa có tên hiển thị) nên không có trang "Chuẩn bị"; bước 0 là bước nấu đầu tiên.

object HandsFreeCopy {
    const val EMPTY_TITLE = "Món này chưa có các bước"
    const val EMPTY_BACK = "Quay lại công thức"
    const val BACK = "Quay lại"
    const val NEXT = "Tiếp"
    const val DONE = "Xong, mình nấu xong rồi"
    const val CLOSE_DESCRIPTION = "Thoát chế độ nấu rảnh tay"
    const val TIMER_TITLE = "Đồng hồ cho bước này"
    const val TIMER_START = "Bắt đầu đếm"
    const val TIMER_PAUSE = "Tạm dừng"
    const val TIMER_RESUME = "Tiếp tục"
    const val TIMER_RESET = "Đặt lại"
    const val TIMER_DONE = "Đã hết thời gian chờ"
}

private val NUMERIC_DURATION_REGEX = Regex(
    """(\d+)\s*(?:[-–~]\s*\d+\s*)?(giây|phút|giờ|tiếng)""",
    RegexOption.IGNORE_CASE,
)

/**
 * Tách thời gian chờ (tính bằng giây) từ mô tả bước nấu, hoặc null nếu bước không có thời gian chờ.
 * Với khoảng thời gian (vd "10-15 phút"), lấy mốc đầu để người dùng chủ động kiểm tra sớm.
 */
fun parseStepDurationSeconds(stepText: String): Int? {
    val match = NUMERIC_DURATION_REGEX.find(stepText)
    if (match != null) {
        val value = match.groupValues[1].toIntOrNull() ?: return null
        if (value <= 0) return null
        val unitSeconds = when (match.groupValues[2].lowercase()) {
            "giây" -> 1
            "phút" -> 60
            "giờ", "tiếng" -> 3600
            else -> return null
        }
        return value * unitSeconds
    }
    val lower = stepText.lowercase()
    return when {
        "nửa tiếng" in lower || "nửa giờ" in lower -> 30 * 60
        "một phút" in lower -> 60
        "vài phút" in lower || "ít phút" in lower -> 3 * 60
        else -> null
    }
}

/** Định dạng số giây còn lại thành MM:SS (hoặc H:MM:SS nếu từ 1 giờ trở lên). */
fun formatTimerClock(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    val seconds = safe % 60
    return if (hours > 0) {
        "$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}

/** Vị trí bước hiện tại; luôn nằm trong [0, totalSteps - 1] khi tạo bằng [of]. */
data class HandsFreeState(val stepIndex: Int, val totalSteps: Int) {
    val isEmpty: Boolean get() = totalSteps <= 0
    val isFirst: Boolean get() = stepIndex <= 0
    val isLast: Boolean get() = stepIndex >= totalSteps - 1
    val label: String get() = "Bước ${stepIndex + 1}/$totalSteps"
    val progress: Float get() = if (isEmpty) 0f else (stepIndex + 1).toFloat() / totalSteps
    val nextLabel: String get() = if (isLast) HandsFreeCopy.DONE else HandsFreeCopy.NEXT

    fun next(): HandsFreeState = if (isEmpty || isLast) this else copy(stepIndex = stepIndex + 1)

    fun previous(): HandsFreeState = if (isEmpty || isFirst) this else copy(stepIndex = stepIndex - 1)

    /** Câu TalkBack đọc khi đổi bước. */
    fun announcement(stepText: String): String = "Bước ${stepIndex + 1} trên $totalSteps: $stepText"

    companion object {
        fun of(stepIndex: Int, totalSteps: Int): HandsFreeState {
            val total = totalSteps.coerceAtLeast(0)
            return HandsFreeState(stepIndex.coerceIn(0, (total - 1).coerceAtLeast(0)), total)
        }
    }
}
