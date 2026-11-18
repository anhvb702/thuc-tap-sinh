package com.loitu.yourrefri.feature.recipe.ui

/** Trạng thái đồng hồ hẹn giờ: giây còn lại và đang chạy hay không. */
internal data class TimerState(val remaining: Int, val running: Boolean)

/** Bấm nút chính: chạy, tạm dừng, hoặc tiếp tục; từ 0 thì bắt đầu lại đủ [total] giây. */
internal fun toggleTimer(state: TimerState, total: Int): TimerState = when {
    state.running -> state.copy(running = false)
    state.remaining == 0 -> TimerState(total, true)
    else -> state.copy(running = true)
}

/** Mỗi giây: chỉ giảm khi đang chạy; về 0 thì dừng. */
internal fun tickTimer(state: TimerState): TimerState {
    if (!state.running) return state
    val next = state.remaining - 1
    return if (next <= 0) TimerState(0, false) else state.copy(remaining = next)
}

/** Đặt lại về 0, không chạy. */
internal fun resetTimer(): TimerState = TimerState(0, false)
