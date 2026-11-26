@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.loitu.yourrefri.feature.notice.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant

class SundayNoticeTest {
    private val eight = LocalTime(8, 0)
    private val sunday = LocalDate(2026, 11, 15)
    private val friday = LocalDate(2026, 11, 13)
    private val shoppingBody = "Nay là cuối tuần rồi nè, đã sẵn sàng đi chợ cho 1 tuần mới chưa nào!"

    @Test
    fun reminderTimeFor_sundayUsesSundayTime_otherDaysUse1600() {
        assertEquals(eight, reminderTimeFor(sunday, eight))
        assertEquals(LocalTime(7, 5), reminderTimeFor(sunday, LocalTime(7, 5)))
        assertEquals(LocalTime(16, 0), reminderTimeFor(friday, eight))
        assertEquals(LocalTime(16, 0), reminderTimeFor(LocalDate(2026, 11, 14), eight))
    }

    @Test
    fun saturdayEvening_nextIsSunday0800() {
        assertEquals(
            Instant.parse("2026-11-15T08:00:00Z"),
            nextReminderTrigger(true, Instant.parse("2026-11-14T18:00:00Z"), TimeZone.UTC, eight),
        )
    }

    @Test
    fun sundayAfterEight_nextIsMonday1600() {
        assertEquals(
            Instant.parse("2026-11-16T16:00:00Z"),
            nextReminderTrigger(true, Instant.parse("2026-11-15T09:00:00Z"), TimeZone.UTC, eight),
        )
    }

    @Test
    fun sundayExactlyAtEight_isNotAgainTheSameInstant() {
        assertEquals(
            Instant.parse("2026-11-16T16:00:00Z"),
            nextReminderTrigger(true, Instant.parse("2026-11-15T08:00:00Z"), TimeZone.UTC, eight),
        )
    }

    @Test
    fun weekdayBeforeEveningStillUses1600() {
        assertEquals(
            Instant.parse("2026-11-13T16:00:00Z"),
            nextReminderTrigger(true, Instant.parse("2026-11-13T15:00:00Z"), TimeZone.UTC, eight),
        )
    }

    @Test
    fun weekdayAtExactly1600_movesToNextDay1600() {
        assertEquals(
            Instant.parse("2026-11-14T16:00:00Z"),
            nextReminderTrigger(true, Instant.parse("2026-11-13T16:00:00Z"), TimeZone.UTC, eight),
        )
    }

    @Test
    fun disabledReturnsNull() {
        assertNull(nextReminderTrigger(false, Instant.parse("2026-11-14T18:00:00Z"), TimeZone.UTC, eight))
    }

    @Test
    fun dstChangeSunday_keepsLocalWallClock() {
        // 8/3/2026 là Chủ Nhật New York chuyển giờ mùa hè: 08:00 EDT = 12:00Z.
        assertEquals(
            Instant.parse("2026-03-08T12:00:00Z"),
            nextReminderTrigger(true, Instant.parse("2026-03-07T23:00:00Z"), TimeZone.of("America/New_York"), eight),
        )
    }

    @Test
    fun withoutSundayTime_behavesLikeBefore() {
        assertEquals(
            Instant.parse("2026-11-15T16:00:00Z"),
            nextReminderTrigger(true, Instant.parse("2026-11-14T18:00:00Z"), TimeZone.UTC),
        )
    }

    @Test
    fun sundayNotice_sentEvenWhenNothingIsExpiring() {
        val notice = planDailyNotice(emptyList(), emptyList(), sunday, lastSent = null)
        assertEquals(shoppingBody, notice?.body)
        assertEquals("Cuối tuần rồi nè", notice?.title)
        assertEquals(emptyList(), notice?.ingredientIds)
        assertEquals(sunday, notice?.day)
    }

    @Test
    fun sundayNotice_replacesExpiringNoticeOnSunday() {
        val ingredients = listOf(Ingredient("rau", "Rau muống", "rau-la", 1))
        val pantry = listOf(PantryItem("rau", true, LocalDate(2026, 11, 10)))
        val notice = planDailyNotice(pantry, ingredients, sunday, lastSent = null)
        assertEquals(shoppingBody, notice?.body)
    }

    @Test
    fun sundayNotice_notSentTwice() {
        assertNull(planDailyNotice(emptyList(), emptyList(), sunday, lastSent = sunday))
        assertNull(planDailyNotice(emptyList(), emptyList(), sunday, lastSent = LocalDate(2026, 11, 16)))
    }

    @Test
    fun sundayNotice_sentWhenLastSentWasYesterday() {
        assertEquals(shoppingBody, planDailyNotice(emptyList(), emptyList(), sunday, lastSent = LocalDate(2026, 11, 14))?.body)
    }

    @Test
    fun sundayShoppingOff_fallsBackToExpiringOnlyBehavior() {
        assertNull(planDailyNotice(emptyList(), emptyList(), sunday, lastSent = null, sundayShopping = false))
    }

    @Test
    fun weekday_noExpiring_stillNoNotice() {
        assertNull(planDailyNotice(emptyList(), emptyList(), friday, lastSent = null))
    }

    @Test
    fun weekendDaySaturday_sendsShoppingNoticeOnSaturday() {
        val saturday = LocalDate(2026, 11, 14)
        val notice = planDailyNotice(emptyList(), emptyList(), saturday, lastSent = null, weekendDay = WeekendReminderDay.SATURDAY)
        assertEquals(shoppingBody, notice?.body)
        assertEquals("Cuối tuần rồi nè", notice?.title)
        assertEquals(saturday, notice?.day)
    }

    @Test
    fun weekendDaySaturday_sundayIsOrdinaryDay() {
        assertNull(planDailyNotice(emptyList(), emptyList(), sunday, lastSent = null, weekendDay = WeekendReminderDay.SATURDAY))
    }

    @Test
    fun weekendDaySaturday_sunday_withExpiringItem_weekdayEnabled_returnsExpiringNotice() {
        val ingredients = listOf(Ingredient("rau", "Rau muống", "rau-la", 1))
        val pantry = listOf(PantryItem("rau", true, LocalDate(2026, 11, 10)))
        val notice = planDailyNotice(
            pantry, ingredients, sunday, lastSent = null,
            weekendDay = WeekendReminderDay.SATURDAY, weekdayEnabled = true,
        )
        assertNotNull(notice)
        assertEquals(listOf("rau"), notice.ingredientIds)
        assertEquals("Gợi ý từ tủ nhà mình", notice.title)
    }

    @Test
    fun weekendDaySaturday_sunday_withExpiringItem_weekdayDisabled_returnsNull() {
        val ingredients = listOf(Ingredient("rau", "Rau muống", "rau-la", 1))
        val pantry = listOf(PantryItem("rau", true, LocalDate(2026, 11, 10)))
        assertNull(
            planDailyNotice(
                pantry, ingredients, sunday, lastSent = null,
                weekendDay = WeekendReminderDay.SATURDAY, weekdayEnabled = false,
            ),
        )
    }

    @Test
    fun weekdayDisabled_hidesExpiringNoticeOnWeekdays() {
        val ingredients = listOf(Ingredient("rau", "Rau muống", "rau-la", 1))
        val pantry = listOf(PantryItem("rau", true, LocalDate(2026, 11, 10)))
        // Đối chứng: cùng dữ liệu, khi bật ngày thường thì có thông báo.
        assertNotNull(planDailyNotice(pantry, ingredients, friday, lastSent = null))
        assertNull(planDailyNotice(pantry, ingredients, friday, lastSent = null, weekdayEnabled = false))
    }

    @Test
    fun weekdayDisabled_stillSendsShoppingNoticeOnWeekendDay() {
        val notice = planDailyNotice(emptyList(), emptyList(), sunday, lastSent = null, weekdayEnabled = false)
        assertEquals(shoppingBody, notice?.body)
    }
}
