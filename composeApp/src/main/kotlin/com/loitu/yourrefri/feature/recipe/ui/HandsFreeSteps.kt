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
