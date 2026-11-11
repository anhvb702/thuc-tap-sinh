package com.loitu.yourrefri.reminder

internal fun completeReminderWork(
    closeDriver: () -> Unit,
    scheduleNext: () -> Unit,
    finish: () -> Unit,
) {
    try {
        closeDriver()
    } finally {
        try {
            scheduleNext()
        } finally {
            finish()
        }
    }
}
