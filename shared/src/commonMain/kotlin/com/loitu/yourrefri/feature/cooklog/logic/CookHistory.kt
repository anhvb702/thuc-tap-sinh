package com.loitu.yourrefri.feature.cooklog.logic

import com.loitu.yourrefri.feature.cooklog.data.CookLog
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus

enum class CookHistoryWeekLabel { THIS_WEEK, LAST_WEEK, EARLIER }

data class CookHistoryWeek(val label: CookHistoryWeekLabel, val entries: List<CookLog>)

private fun mondayOf(date: LocalDate): LocalDate = date.minus(date.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)

/**
 * Gom lịch sử nấu theo tuần (tuần bắt đầu Thứ Hai). Giữ thứ tự đầu vào trong từng nhóm, bỏ nhóm rỗng.
 * Ngày ở tương lai (đổi giờ máy lùi) tính vào tuần này để món không biến mất.
 */
fun groupCookHistory(logs: List<CookLog>, today: LocalDate): List<CookHistoryWeek> {
    val thisMonday = mondayOf(today)
    val lastMonday = thisMonday.minus(7, DateTimeUnit.DAY)
    fun labelOf(date: LocalDate): CookHistoryWeekLabel = when {
        date >= thisMonday -> CookHistoryWeekLabel.THIS_WEEK
        date >= lastMonday -> CookHistoryWeekLabel.LAST_WEEK
        else -> CookHistoryWeekLabel.EARLIER
    }
    val byLabel = logs.groupBy { labelOf(it.cookedOn) }
    return CookHistoryWeekLabel.entries.mapNotNull { label ->
        byLabel[label]?.takeIf { it.isNotEmpty() }?.let { CookHistoryWeek(label, it) }
    }
}
