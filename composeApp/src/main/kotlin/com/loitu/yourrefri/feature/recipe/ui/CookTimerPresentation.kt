package com.loitu.yourrefri.feature.recipe.ui

internal enum class CookTimerPhase { READY, RUNNING, RUNNING_LOW, PAUSED, DONE }

internal fun cookTimerPhase(state: TimerState, totalSeconds: Int): CookTimerPhase = when {
    state.remaining <= 0 -> CookTimerPhase.DONE
    state.running && totalSeconds > 0 && timerProgress(state.remaining, totalSeconds) <= 0.2f ->
        CookTimerPhase.RUNNING_LOW
    state.running -> CookTimerPhase.RUNNING
    totalSeconds > 0 && state.remaining < totalSeconds -> CookTimerPhase.PAUSED
    else -> CookTimerPhase.READY
}

internal fun cookTimerTapHint(state: TimerState, totalSeconds: Int): String =
    when (cookTimerPhase(state, totalSeconds)) {
        CookTimerPhase.DONE -> "Chạm đồng hồ để hẹn lại"
        CookTimerPhase.RUNNING, CookTimerPhase.RUNNING_LOW -> "Chạm đồng hồ để tạm dừng"
        CookTimerPhase.PAUSED -> "Chạm đồng hồ để tiếp tục"
        CookTimerPhase.READY -> "Chạm đồng hồ để bắt đầu"
    }

internal fun cookTimerStatusLabel(state: TimerState, totalSeconds: Int): String =
    when (cookTimerPhase(state, totalSeconds)) {
        CookTimerPhase.DONE -> "Hết giờ"
        CookTimerPhase.RUNNING, CookTimerPhase.RUNNING_LOW -> "Đang đếm"
        CookTimerPhase.PAUSED -> "Tạm dừng"
        CookTimerPhase.READY -> "Sẵn sàng"
    }

internal fun canResetCookTimer(state: TimerState, totalSeconds: Int): Boolean =
    state.running || state.remaining != totalSeconds

internal fun resetCookTimerToFull(totalSeconds: Int): TimerState =
    TimerState(remaining = totalSeconds.coerceAtLeast(0), running = false)

internal fun timerKnobAngleDegrees(progress: Float): Float =
    progress.coerceIn(0f, 1f) * 360f - 90f
