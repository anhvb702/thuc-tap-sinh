package com.loitu.yourrefri.feature.notice.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DailyNoticeTest {
    private val today = LocalDate(2026, 11, 5)
    private fun ingredient(id: String, days: Int = 2) = Ingredient(id, "Tên $id", "khac", days)
    private fun item(id: String, have: Boolean = true, date: LocalDate = today) = PantryItem(id, have, date)

    @Test
    fun noRelevantItems_returnsNull() {
        assertNull(planDailyNotice(emptyList(), listOf(ingredient("egg")), today, null))
        assertNull(planDailyNotice(listOf(item("egg", false)), listOf(ingredient("egg")), today, null))
        assertNull(planDailyNotice(listOf(item("egg")), listOf(ingredient("egg", 3)), today, null))
        assertNull(planDailyNotice(listOf(item("unknown")), listOf(ingredient("egg")), today, null))
    }

    @Test
    fun sentToday_returnsNull() {
        assertNull(planDailyNotice(listOf(item("egg")), listOf(ingredient("egg")), today, today))
    }

    @Test
    fun futureLastSent_returnsNull() {
        assertNull(planDailyNotice(listOf(item("egg")), listOf(ingredient("egg")), today, today.plus(1, DateTimeUnit.DAY)))
    }

    @Test
    fun yesterdayAndNeverSent_allowOneCombinedNotice() {
        val pantry = listOf(item("two"), item("old", date = LocalDate(2026, 11, 1)), item("zero", date = LocalDate(2026, 11, 3)))
        val ingredients = listOf(ingredient("zero"), ingredient("two"), ingredient("old"))
        for (sent in listOf(null, LocalDate(2026, 11, 4))) {
            val notice = assertNotNull(planDailyNotice(pantry, ingredients, today, sent))
            assertEquals(listOf("old", "zero", "two"), notice.ingredientIds)
            assertEquals(today, notice.day)
        }
    }

    @Test
    fun contentHasEstimateAndCheckForPastDate() {
        val notice = assertNotNull(planDailyNotice(
            listOf(item("two"), item("old", date = LocalDate(2026, 11, 1)), item("zero", date = LocalDate(2026, 11, 3))),
            listOf(ingredient("two"), ingredient("old"), ingredient("zero")), today, null,
        ))
        assertEquals("Gợi ý từ tủ nhà mình", notice.title)
        assertEquals(
            "Tên old: Ước tính đã qua ngày nên dùng trước; kiểm tra lại xem còn dùng được không\n" +
                "Tên zero: Ước tính nên dùng trước hôm nay\n" +
                "Tên two: Ước tính nên dùng trước khoảng 2 ngày nữa",
            notice.body,
        )
    }

    @Test
    fun currentDay_isReturnedWithoutChangingInputs() {
        val pantry = mutableListOf(item("egg"))
        val ingredients = mutableListOf(ingredient("egg"))
        val pantryBefore = pantry.toList()
        val ingredientsBefore = ingredients.toList()
        val tomorrow = today.plus(1, DateTimeUnit.DAY)
        assertEquals(tomorrow, assertNotNull(planDailyNotice(pantry, ingredients, tomorrow, today)).day)
        assertEquals(pantryBefore, pantry)
        assertEquals(ingredientsBefore, ingredients)
    }

    @Test
    fun thirtyDays_repeatedChecks_sendAtMostOncePerDay() {
        val pantry = listOf(item("egg"))
        val ingredients = listOf(ingredient("egg", 1))
        var lastSent: LocalDate? = null
        val sentDays = mutableListOf<LocalDate>()
        repeat(30) { offset ->
            val day = today.plus(offset, DateTimeUnit.DAY)
            repeat(3) {
                val notice = planDailyNotice(pantry, ingredients, day, lastSent)
                if (notice != null) {
                    sentDays += notice.day
                    lastSent = notice.day
                }
            }
        }
        assertEquals((0 until 30).map { today.plus(it, DateTimeUnit.DAY) }, sentDays)
        assertEquals(30, sentDays.groupingBy { it }.eachCount().size)
        assertEquals(setOf(1), sentDays.groupingBy { it }.eachCount().values.toSet())
    }
}
