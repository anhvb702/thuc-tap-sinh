package com.loitu.yourrefri.feature.notice.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import kotlinx.datetime.LocalDate
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
    @Test fun threeItems_namesTwoAndCountsRest() {
        assertEquals("A, B và 1 món khác nên dùng trước (hạn ước tính). Mở xem gợi ý món nhé.", notice(3).body)
        assertEquals("A, B và 2 món khác nên dùng trước (hạn ước tính). Mở xem gợi ý món nhé.", notice(4).body)
    }
    @Test fun body_hasEstimateAndUseFirst() {
        assertTrue(notice(1).body.contains("nên dùng trước (hạn ước tính)"))
    }
    @Test fun body_hasNoBannedWords() {
        for (count in 1..4) for (word in listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn")) {
            assertFalse(notice(count).body.contains(word, ignoreCase = true))
        }
    }
    @Test fun ingredientIdsStillListEveryItem() = assertEquals(listOf("a", "b", "c", "d"), notice(4).ingredientIds)
}
