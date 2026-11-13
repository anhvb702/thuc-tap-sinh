package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.logic.PlannedDayMeal
import com.loitu.yourrefri.feature.recipe.logic.RecipeRecommendation
import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class RecipeTabsPresentationTest {
    private fun recipe(id: String, steps: Int = 3, minutes: Int? = 20) = Recipe(
        id = id,
        title = "Món $id",
        steps = List(steps) { "Bước ${it + 1}" },
        required = listOf(Slot(listOf("trung-ga"))),
        minutes = minutes,
    )

    private fun rec(id: String, reason: RecommendationReason = RecommendationReason.OTHER) =
        RecipeRecommendation(recipe(id), reason)

    private fun day(index: Int) = PlannedDayMeal(
        dayIndex = index,
        dayLabelVi = "Thứ ${index + 2}",
        recipe = recipe("m$index"),
        matchReasonVi = "",
    )

    // --- Thẻ tab ---
    @Test fun tabsMapToModesInOrderWithAgreedLabels() {
        val tabs = recipeTabSpecs()
        assertEquals(
            listOf(RecipeScreenMode.DAILY, RecipeScreenMode.WEEKLY, RecipeScreenMode.SHOPPING),
            tabs.map { it.mode },
        )
        assertEquals(listOf("Gợi ý hôm nay", "Thực đơn tuần", "Đi chợ"), tabs.map { it.label })
    }

    @Test fun oldEnumValuesStillExist() {
        assertEquals(RecipeScreenMode.DAILY, RecipeScreenMode.valueOf("DAILY"))
        assertEquals(RecipeScreenMode.WEEKLY, RecipeScreenMode.valueOf("WEEKLY"))
    }

    // --- Mức độ ---
    @Test fun difficultyEasyForFewStepsAndShortTime() {
        assertEquals("Dễ nấu", difficultyLabel(recipe("a", steps = 4, minutes = 30)))
    }

    @Test fun difficultyMediumWhenMoreStepsOrLonger() {
        assertEquals("Vừa sức", difficultyLabel(recipe("a", steps = 5, minutes = 20)))
        assertEquals("Vừa sức", difficultyLabel(recipe("a", steps = 3, minutes = 31)))
    }

    @Test fun difficultyUnknownTimeFallsBackToSteps() {
        assertEquals("Dễ nấu", difficultyLabel(recipe("a", steps = 2, minutes = null)))
        assertEquals("Vừa sức", difficultyLabel(recipe("a", steps = 7, minutes = null)))
    }

    // --- Thẻ nổi bật ---
    @Test fun heroBadgeFirstCardSaysBestFit() {
        assertEquals("Gợi ý phù hợp nhất", heroBadgeLabel(0, RecommendationReason.QUICK))
        assertEquals(recommendationLabel(RecommendationReason.QUICK), heroBadgeLabel(1, RecommendationReason.QUICK))
        assertEquals(recommendationLabel(RecommendationReason.USE_SOON), heroBadgeLabel(2, RecommendationReason.USE_SOON))
    }

    @Test fun heroTakesAtMostThree() {
        val items = (1..5).map { rec("r$it") }
        assertEquals(listOf("r1", "r2", "r3"), heroRecommendations(items, null).map { it.recipe.id })
    }

    @Test fun heroKeepsRequestedRecipeEvenWhenBeyondThree() {
        val items = (1..5).map { rec("r$it") }
        val ids = heroRecommendations(items, "r5").map { it.recipe.id }
        assertEquals(3, ids.size)
        assertEquals("r5", ids.first())
        assertTrue(ids.containsAll(listOf("r1", "r2")))
    }

    @Test fun heroIgnoresUnknownRequestedId() {
        val items = (1..4).map { rec("r$it") }
        assertEquals(listOf("r1", "r2", "r3"), heroRecommendations(items, "zzz").map { it.recipe.id })
    }

    @Test fun heroEmptyStaysEmpty() {
        assertTrue(heroRecommendations(emptyList(), "r1").isEmpty())
    }

    @Test fun minutesLabel() {
        assertEquals("30 phút", heroMinutesLabel(30))
        assertEquals("Thời gian chưa rõ", heroMinutesLabel(null))
    }

    @Test fun favoriteDescriptionFollowsState() {
        assertEquals("Thêm Trứng chiên vào yêu thích", favoriteToggleDescription("Trứng chiên", false))
        assertEquals("Bỏ Trứng chiên khỏi yêu thích", favoriteToggleDescription("Trứng chiên", true))
    }

    // --- Dải 7 ngày ---
    @Test fun weekStripHasSevenChipsWithShortLabels() {
        val chips = weekDayChips(emptyList(), todayIndex = null)
        assertEquals(listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN"), chips.map { it.label })
        assertTrue(chips.none { it.hasMeal })
        assertTrue(chips.none { it.isToday })
    }

    @Test fun weekStripMarksDaysWithMealsOnly() {
        val chips = weekDayChips(listOf(day(0), day(2), day(6)), todayIndex = null)
        assertEquals(listOf(true, false, true, false, false, false, true), chips.map { it.hasMeal })
    }

    @Test fun weekStripMarksOnlyToday() {
        val chips = weekDayChips(listOf(day(3)), todayIndex = 3)
        assertEquals(listOf(3), chips.filter { it.isToday }.map { it.dayIndex })
        assertTrue(chips[3].hasMeal)
    }

    @Test fun todayIndexUsesMondayAsZeroInGivenTimeZone() {
        // 2026-11-09 là thứ Hai; 2026-11-15 là Chủ nhật.
        assertEquals(0, todayDayIndex(Instant.parse("2026-11-09T05:00:00Z"), TimeZone.UTC))
        assertEquals(6, todayDayIndex(Instant.parse("2026-11-15T05:00:00Z"), TimeZone.UTC))
    }

    @Test fun todayIndexRespectsTimeZoneAtDayBoundary() {
        // 20:00 UTC Chủ nhật = 03:00 thứ Hai ở UTC+7.
        val instant = Instant.parse("2026-11-15T20:00:00Z")
        assertEquals(6, todayDayIndex(instant, TimeZone.UTC))
        assertEquals(0, todayDayIndex(instant, TimeZone.of("Asia/Ho_Chi_Minh")))
    }

    @Test fun dayChipDescriptionIsSpokenInFull() {
        val withMeal = WeekDayChip(dayIndex = 0, label = "T2", hasMeal = true, isToday = true)
        val empty = WeekDayChip(dayIndex = 6, label = "CN", hasMeal = false, isToday = false)
        assertEquals("Thứ 2, hôm nay, đã có món", weekDayChipDescription(withMeal))
        assertEquals("Chủ nhật, chưa xếp món", weekDayChipDescription(empty))
    }

    // --- Thẻ phụ ---
    @Test fun shoppingCardTextShowsCountOrReassures() {
        assertEquals("3 món cần mua", shoppingCardCountText(3))
        assertEquals("Tuần này nhà mình đã đủ nguyên liệu", shoppingCardCountText(0))
        assertEquals("Đã trừ phần đang có trong tủ", SHOPPING_CARD_SUBTITLE)
    }

    @Test fun handsFreeAndEmptyCopy() {
        assertEquals("Chế độ Nấu rảnh tay", HANDS_FREE_CARD_TITLE)
        assertEquals("Từng bước rõ ràng, màn hình luôn sáng", HANDS_FREE_CARD_SUBTITLE)
        assertEquals("Thêm vài món vào tủ, mình sẽ gợi ý ngay", HERO_EMPTY_MESSAGE)
    }

    @Test fun newCopyAvoidsForbiddenWords() {
        val all = listOf(
            HERO_TOP_LABEL, SHOPPING_CARD_SUBTITLE, HANDS_FREE_CARD_TITLE, HANDS_FREE_CARD_SUBTITLE, HERO_EMPTY_MESSAGE,
            shoppingCardCountText(2), shoppingCardCountText(0),
        ) + recipeTabSpecs().map { it.label }
        val banned = listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn")
        all.forEach { text -> banned.forEach { assertFalse(text.lowercase().contains(it), "$text chứa '$it'") } }
    }
}
