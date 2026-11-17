package com.loitu.yourrefri.feature.notice.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DailyNoticeCopyTest {
    private val today = LocalDate(2026, 11, 5)
    private fun notice(count: Int): DailyNotice {
        val ids = listOf("a", "b", "c", "d").take(count)
        val pantry = ids.mapIndexed { index, id -> PantryItem(id, true, LocalDate(2026, 11, index + 1)) }
        val ingredients = ids.map { Ingredient(it, it.uppercase(), "khac", 2) }
        return assertNotNull(planDailyNotice(pantry.reversed(), ingredients, today, null))
    }

    @Test fun oneItem_bodyNamesIt() = assertEquals("A nên dùng trước (hạn ước tính). Mở xem gợi ý món nhé.", notice(1).body)
    @Test fun twoItems_bothNamed() = assertEquals("A, B nên dùng trước (hạn ước tính). Mở xem gợi ý món nhé.", notice(2).body)
    // Căn cứ quyết định Q5/Q13a (2026-11-16): Thông báo liệt kê tối đa 3 món, sau đó là "và vài món khác".
    @Test fun threeItems_allThreeNamed() {
        assertEquals("A, B, C nên dùng trước (hạn ước tính). Mở xem gợi ý món nhé.", notice(3).body)
    }

    // Căn cứ quyết định Q13a (2026-11-16): Nếu > 3 món thì hiển thị "$names và vài món khác".
    @Test fun fourItems_namesThreeAndSaysVaiMonKhac() {
        assertEquals("A, B, C và vài món khác nên dùng trước (hạn ước tính). Mở xem gợi ý món nhé.", notice(4).body)
    }
    @Test fun body_hasEstimateAndUseFirst() {
        assertTrue(notice(1).body.contains("nên dùng trước (hạn ước tính)"))
    }
    @Test fun body_hasNoBannedWords() {
        val banned = listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn")
        for (count in 1..4) for (word in banned) {
            assertFalse(notice(count).body.contains(word, ignoreCase = true))
        }
        val wednesdayNotice = assertNotNull(planDailyNotice(emptyList(), emptyList(), LocalDate(2026, 11, 4), null))
        val defrostNotice = assertNotNull(planDailyNotice(emptyList(), emptyList(), today, null, defrostNames = listOf("Cá hồi")))
        val expiredFrozenNotice = assertNotNull(
            planDailyNotice(
                listOf(PantryItem("ca", true, today.minus(20, DateTimeUnit.DAY), PantryCompartment.FREEZE)),
                listOf(Ingredient("ca", "Cá hồi", "ca", 2, freezerShelfDays = 14)),
                today,
                null,
            ),
        )
        for (extra in listOf(wednesdayNotice, defrostNotice, expiredFrozenNotice)) {
            for (word in banned) {
                assertFalse(extra.body.contains(word, ignoreCase = true))
            }
        }
    }
    @Test fun ingredientIdsStillListEveryItem() = assertEquals(listOf("a", "b", "c", "d"), notice(4).ingredientIds)
}
