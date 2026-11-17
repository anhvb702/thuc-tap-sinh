package com.loitu.yourrefri.feature.notice.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
    fun emptyWeekdayListSuggestsDietSafeRecipeAndThrottlesForThreeDays() {
        val recipes = listOf(
            Recipe("beef", "Bò xào", listOf("Nấu"), listOf(Slot(listOf("thit-bo")))),
            Recipe("egg", "Trứng hấp", listOf("Hấp"), listOf(Slot(listOf("trung")))),
        )
        val first = assertNotNull(planDailyNotice(
            emptyList(), listOf(ingredient("thit-bo"), ingredient("trung")), today, null,
            sundayShopping = false, recipes = recipes, dietRestriction = DietRestriction.VEGETARIAN,
        ))
        assertEquals("Nay trời dịu nhẹ, tối nay làm món Trứng hấp là hết ý luôn nè!", first.body)
        assertTrue(first.isContextualNudge)
        assertNull(planDailyNotice(
            emptyList(), listOf(ingredient("thit-bo"), ingredient("trung")), today.plus(2, DateTimeUnit.DAY), null,
            sundayShopping = false, recipes = recipes, dietRestriction = DietRestriction.VEGETARIAN,
            lastNudgeSent = today,
        ))
        assertNotNull(planDailyNotice(
            emptyList(), listOf(ingredient("thit-bo"), ingredient("trung")), today.plus(3, DateTimeUnit.DAY), null,
            sundayShopping = false, recipes = recipes, dietRestriction = DietRestriction.VEGETARIAN,
            lastNudgeSent = today,
        ))
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

    // Căn cứ quyết định Q5/Q13a (2026-11-16): Thông báo liệt kê tối đa 3 món, sau đó là "và vài món khác".
    @Test
    fun contentHasEstimateAndCheckForPastDate() {
        val notice = assertNotNull(planDailyNotice(
            listOf(item("two"), item("old", date = LocalDate(2026, 11, 1)), item("zero", date = LocalDate(2026, 11, 3))),
            listOf(ingredient("two"), ingredient("old"), ingredient("zero")), today, null,
        ))
        assertEquals("Gợi ý từ tủ nhà mình", notice.title)
        assertEquals(
            "Tên old, Tên zero, Tên two nên dùng trước (hạn ước tính). Mở xem gợi ý món nhé.",
            notice.body,
        )
    }

    // Căn cứ quyết định Q13a (2026-11-16): Nếu > 3 món thì hiển thị "$names và vài món khác".
    @Test
    fun contentWithMoreThanThreeItems_usesVaiMonKhac() {
        val notice = assertNotNull(planDailyNotice(
            listOf(
                item("two"),
                item("old", date = LocalDate(2026, 11, 1)),
                item("zero", date = LocalDate(2026, 11, 3)),
                item("four", date = LocalDate(2026, 11, 3)),
            ),
            listOf(ingredient("two"), ingredient("old"), ingredient("zero"), ingredient("four")), today, null,
        ))
        assertEquals("Gợi ý từ tủ nhà mình", notice.title)
        assertEquals(
            "Tên old, Tên four, Tên zero và vài món khác nên dùng trước (hạn ước tính). Mở xem gợi ý món nhé.",
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
