package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ShelfTest {

    // Ngày luôn cố định trong test: không gọi giờ hệ thống để kết quả lặp lại được.
    private fun d(iso: String): LocalDate = LocalDate.parse(iso)

    private fun ingredient(defaultShelfDays: Int, userShelfDays: Int? = null) = Ingredient(
        id = "egg",
        nameVi = "Trứng gà",
        group = "protein",
        defaultShelfDays = defaultShelfDays,
        userShelfDays = userShelfDays,
    )

    private fun item(markedAt: String, have: Boolean = true) =
        PantryItem(ingredientId = "egg", have = have, markedAt = d(markedAt))

    // --- daysLeft + urgencyOf với mốc 2026-11-01, hạn mặc định 5 ngày (hết hạn 2026-11-06) ---

    @Test
    fun daysLeft_default5_today1103_is3_andOk() {
        val left = daysLeft(item("2026-11-01"), ingredient(5), d("2026-11-03"))
        assertEquals(3, left)
        assertEquals(Urgency.OK, urgencyOf(left))
    }

    @Test
    fun daysLeft_default5_today1106_is0_andSoon() {
        val left = daysLeft(item("2026-11-01"), ingredient(5), d("2026-11-06"))
        assertEquals(0, left)
        assertEquals(Urgency.SOON, urgencyOf(left))
    }

    @Test
    fun daysLeft_default5_today1107_isMinus1_andExpired() {
        val left = daysLeft(item("2026-11-01"), ingredient(5), d("2026-11-07"))
        assertEquals(-1, left)
        assertEquals(Urgency.EXPIRED, urgencyOf(left))
    }

    // --- userShelfDays ghi đè defaultShelfDays ---

    @Test
    fun daysLeft_userShelfDays2_overridesDefault5() {
        val left = daysLeft(item("2026-11-01"), ingredient(defaultShelfDays = 5, userShelfDays = 2), d("2026-11-03"))
        assertEquals(0, left)
        assertEquals(Urgency.SOON, urgencyOf(left))
    }

    @Test
    fun daysLeft_userShelfDaysLongerThanDefault_usesUser() {
        val left = daysLeft(item("2026-11-01"), ingredient(defaultShelfDays = 2, userShelfDays = 10), d("2026-11-03"))
        assertEquals(8, left)
    }

    @Test
    fun daysLeft_userShelfDaysNull_usesDefault() {
        val left = daysLeft(item("2026-11-01"), ingredient(defaultShelfDays = 5, userShelfDays = null), d("2026-11-01"))
        assertEquals(5, left)
    }

    @Test
    fun daysLeft_userShelfDaysZero_isAValidOverride_notTreatedAsMissing() {
        val left = daysLeft(item("2026-11-01"), ingredient(defaultShelfDays = 5, userShelfDays = 0), d("2026-11-01"))
        assertEquals(0, left)
    }

    // --- markedAt = today ---

    @Test
    fun daysLeft_markedToday_equalsShelfDays() {
        assertEquals(7, daysLeft(item("2026-11-05"), ingredient(7), d("2026-11-05")))
    }

    // --- hạn 0 ngày ---

    @Test
    fun daysLeft_shelf0_markedToday_is0_andSoon() {
        val left = daysLeft(item("2026-11-05"), ingredient(0), d("2026-11-05"))
        assertEquals(0, left)
        assertEquals(Urgency.SOON, urgencyOf(left))
    }

    @Test
    fun daysLeft_shelf0_nextDay_isMinus1_andExpired() {
        val left = daysLeft(item("2026-11-05"), ingredient(0), d("2026-11-06"))
        assertEquals(-1, left)
        assertEquals(Urgency.EXPIRED, urgencyOf(left))
    }

    // --- today trước markedAt (ít gặp nhưng không được đổi dấu) ---

    @Test
    fun daysLeft_todayBeforeMarkedAt_isMoreThanShelf() {
        assertEquals(7, daysLeft(item("2026-11-03"), ingredient(5), d("2026-11-01")))
    }

    // --- qua ranh giới tháng và năm ---

    @Test
    fun daysLeft_crossesMonthBoundary_oct30Plus5_today1101_is3() {
        // 2026-10-30 + 5 = 2026-11-04; 2026-11-04 - 2026-11-01 = 3
        assertEquals(3, daysLeft(item("2026-10-30"), ingredient(5), d("2026-11-01")))
    }

    @Test
    fun daysLeft_crossesYearBoundary_dec31Plus1_todayJan1_is0() {
        assertEquals(0, daysLeft(item("2026-12-31"), ingredient(1), d("2027-01-01")))
    }

    @Test
    fun daysLeft_crossesYearBoundary_dec31Plus3_todayDec31_is3_andJan3_is1() {
        assertEquals(3, daysLeft(item("2026-12-31"), ingredient(3), d("2026-12-31")))
        assertEquals(1, daysLeft(item("2026-12-31"), ingredient(3), d("2027-01-02")))
    }

    // --- năm nhuận 2028 và năm không nhuận 2027 làm đối chứng ---

    @Test
    fun daysLeft_leapYear2028_feb28Plus2_expiresMar1() {
        // 2028 nhuận: 28/2 + 2 = 1/3 (đi qua 29/2)
        assertEquals(1, daysLeft(item("2028-02-28"), ingredient(2), d("2028-02-29")))
        assertEquals(0, daysLeft(item("2028-02-28"), ingredient(2), d("2028-03-01")))
        assertEquals(-1, daysLeft(item("2028-02-28"), ingredient(2), d("2028-03-02")))
    }

    @Test
    fun daysLeft_nonLeapYear2027_feb28Plus2_expiresMar2() {
        // 2027 không nhuận: 28/2 + 2 = 2/3
        assertEquals(1, daysLeft(item("2027-02-28"), ingredient(2), d("2027-03-01")))
        assertEquals(0, daysLeft(item("2027-02-28"), ingredient(2), d("2027-03-02")))
    }

    @Test
    fun daysLeft_spansWholeLeapYear_366Days() {
        assertEquals(0, daysLeft(item("2028-01-01"), ingredient(366), d("2029-01-01")))
    }

    // --- cờ have không ảnh hưởng kết quả ---

    @Test
    fun daysLeft_ignoresHaveFlag() {
        val withHave = daysLeft(item("2026-11-01", have = true), ingredient(5), d("2026-11-03"))
        val withoutHave = daysLeft(item("2026-11-01", have = false), ingredient(5), d("2026-11-03"))
        assertEquals(3, withHave)
        assertEquals(3, withoutHave)
    }

    @Test
    fun daysLeft_ignoresHaveFlag_whenExpired() {
        assertEquals(-1, daysLeft(item("2026-11-01", have = false), ingredient(5), d("2026-11-07")))
    }

    // --- biên urgencyOf ---

    @Test
    fun urgencyOf_minus1_isExpired() = assertEquals(Urgency.EXPIRED, urgencyOf(-1))

    @Test
    fun urgencyOf_0_isSoon() = assertEquals(Urgency.SOON, urgencyOf(0))

    @Test
    fun urgencyOf_1_isSoon() = assertEquals(Urgency.SOON, urgencyOf(1))

    @Test
    fun urgencyOf_2_isSoon() = assertEquals(Urgency.SOON, urgencyOf(2))

    @Test
    fun urgencyOf_3_isOk() = assertEquals(Urgency.OK, urgencyOf(3))

    @Test
    fun urgencyOf_intMinValue_isExpired() = assertEquals(Urgency.EXPIRED, urgencyOf(Int.MIN_VALUE))

    @Test
    fun urgencyOf_intMaxValue_isOk() = assertEquals(Urgency.OK, urgencyOf(Int.MAX_VALUE))

    @Test
    fun urgencyOf_largeNegative_isExpired() = assertEquals(Urgency.EXPIRED, urgencyOf(-100_000))

    @Test
    fun urgencyOf_largePositive_isOk() = assertEquals(Urgency.OK, urgencyOf(100_000))
}
