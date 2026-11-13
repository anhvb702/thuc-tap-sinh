package com.loitu.yourrefri.feature.cooklog.logic

import com.loitu.yourrefri.feature.cooklog.data.CookLog
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class CookHistoryTest {
    // 2026-11-13 là Thứ Sáu; tuần này bắt đầu Thứ Hai 2026-11-09.
    private val today = LocalDate(2026, 11, 13)

    private fun log(id: Long, date: LocalDate) = CookLog(id, "r$id", date, null, emptyList())

    private fun CookHistoryWeekLabel.ids(result: List<CookHistoryWeek>) =
        result.firstOrNull { it.label == this }?.entries?.map { it.id }

    @Test
    fun groupsByMondayWeek() {
        val logs = listOf(
            log(1, LocalDate(2026, 11, 13)),
            log(2, LocalDate(2026, 11, 9)),
            log(3, LocalDate(2026, 11, 5)),
            log(4, LocalDate(2026, 11, 2)),
            log(5, LocalDate(2026, 10, 20)),
        )
        val result = groupCookHistory(logs, today)

        assertEquals(listOf(CookHistoryWeekLabel.THIS_WEEK, CookHistoryWeekLabel.LAST_WEEK, CookHistoryWeekLabel.EARLIER), result.map { it.label })
        assertEquals(listOf(1L, 2L), CookHistoryWeekLabel.THIS_WEEK.ids(result))
        assertEquals(listOf(3L, 4L), CookHistoryWeekLabel.LAST_WEEK.ids(result))
        assertEquals(listOf(5L), CookHistoryWeekLabel.EARLIER.ids(result))
    }

    @Test
    fun sundayBelongsToThePreviousMondayWeek() {
        // 8/11 là Chủ Nhật: thuộc tuần trước, không phải tuần này.
        val result = groupCookHistory(listOf(log(1, LocalDate(2026, 11, 8))), today)
        assertEquals(listOf(CookHistoryWeekLabel.LAST_WEEK), result.map { it.label })
    }

    @Test
    fun lastWeekStartsOnMondayAndEarlierStartsTheSundayBefore() {
        val result = groupCookHistory(
            listOf(log(1, LocalDate(2026, 11, 2)), log(2, LocalDate(2026, 11, 1))),
            today,
        )
        assertEquals(listOf(1L), CookHistoryWeekLabel.LAST_WEEK.ids(result))
        assertEquals(listOf(2L), CookHistoryWeekLabel.EARLIER.ids(result))
    }

    @Test
    fun todayOnSunday_thisWeekRunsBackToMonday() {
        val sunday = LocalDate(2026, 11, 15)
        val result = groupCookHistory(listOf(log(1, LocalDate(2026, 11, 9)), log(2, LocalDate(2026, 11, 8))), sunday)
        assertEquals(listOf(1L), CookHistoryWeekLabel.THIS_WEEK.ids(result))
        assertEquals(listOf(2L), CookHistoryWeekLabel.LAST_WEEK.ids(result))
    }

    @Test
    fun omitsEmptyGroups() {
        val result = groupCookHistory(listOf(log(1, LocalDate(2026, 10, 1))), today)
        assertEquals(listOf(CookHistoryWeekLabel.EARLIER), result.map { it.label })
    }

    @Test
    fun keepsInputOrderInsideAGroup() {
        val logs = listOf(log(7, LocalDate(2026, 11, 10)), log(3, LocalDate(2026, 11, 12)), log(9, LocalDate(2026, 11, 11)))
        assertEquals(listOf(7L, 3L, 9L), CookHistoryWeekLabel.THIS_WEEK.ids(groupCookHistory(logs, today)))
    }

    @Test
    fun emptyInputReturnsEmpty() {
        assertEquals(emptyList(), groupCookHistory(emptyList(), today))
    }

    @Test
    fun futureDatedEntryCountsAsThisWeek() {
        val result = groupCookHistory(listOf(log(1, LocalDate(2026, 12, 25))), today)
        assertEquals(listOf(CookHistoryWeekLabel.THIS_WEEK), result.map { it.label })
    }
}
