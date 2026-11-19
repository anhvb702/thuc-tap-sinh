package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class RecipeFigmaPresentationTest {
    private fun recipe(id: String, minutes: Int? = 15) = Recipe(
        id = id, title = "Món $id", steps = listOf("a"), required = listOf(Slot(listOf("x"))), minutes = minutes,
    )

    @Test fun eyebrowShowsRecipeCount() {
        assertEquals("MÓN ĂN · 10 CÔNG THỨC", recipeEyebrow(10))
        assertEquals("MÓN ĂN", recipeEyebrow(0))
    }

    @Test fun headlineFollowsMode() {
        assertEquals("Hôm nay ăn gì?", recipeHeadline(RecipeScreenMode.DAILY))
        assertEquals("Thực đơn tuần này", recipeHeadline(RecipeScreenMode.WEEKLY))
        assertEquals("Giỏ đi chợ lẻ", recipeHeadline(RecipeScreenMode.SHOPPING))
    }

    @Test fun numberedEyebrowIsPaddedAndUppercase() {
        assertEquals("01 · DÙNG ĐỒ CẦN DÙNG SỚM", heroEyebrow(0, RecommendationReason.USE_SOON))
        assertEquals("02 · NẤU ĐƯỢC NGAY", heroEyebrow(1, RecommendationReason.READY))
        assertEquals("03 · MỘT MÓN NHANH", heroEyebrow(2, RecommendationReason.QUICK))
        assertEquals("04 · GỢI Ý CHO GIA ĐÌNH", heroEyebrow(3, RecommendationReason.OTHER))
    }

    @Test fun metaLineUsesDifficultyWhenServingsAreUnavailable() {
        assertEquals("15 phút · Dễ nấu", recipeMetaLine(recipe("a", 15)))
        assertEquals("Thời gian chưa rõ · Dễ nấu", recipeMetaLine(recipe("a", null)))
    }

    @Test fun coverageLine() {
        assertEquals("Có đủ 2/2 nguyên liệu chính", ingredientCoverageLabel(2, 2))
        assertEquals("Có 1/3 nguyên liệu chính", ingredientCoverageLabel(1, 3))
        assertEquals("", ingredientCoverageLabel(0, 0))
    }

    @Test fun dietChipsExcludeNoneAndToggleBack() {
        assertEquals(listOf(DietRestriction.NO_SEAFOOD, DietRestriction.NO_RED_MEAT, DietRestriction.VEGETARIAN), dietChipOptions())
        assertEquals(DietRestriction.NONE, nextDietRestriction(DietRestriction.VEGETARIAN, DietRestriction.VEGETARIAN))
        assertEquals(DietRestriction.NO_SEAFOOD, nextDietRestriction(DietRestriction.VEGETARIAN, DietRestriction.NO_SEAFOOD))
    }

    @Test fun dietNoteOnlyWhenRestricted() {
        assertNull(dietSelectionNote(DietRestriction.NONE))
        assertEquals("Đang chọn: không hải sản", dietSelectionNote(DietRestriction.NO_SEAFOOD))
    }

    @Test fun alternativeSkipsHeroAndCycles() {
        val all = listOf(recipe("a"), recipe("b"), recipe("c"), recipe("d"))
        val hero = setOf("a", "b")
        assertEquals("c", alternativeRecipe(all, hero, 0)?.id)
        assertEquals("d", alternativeRecipe(all, hero, 1)?.id)
        assertEquals("c", alternativeRecipe(all, hero, 2)?.id)
        assertNull(alternativeRecipe(all, setOf("a", "b", "c", "d"), 0))
        assertEquals("d", alternativeRecipe(all, hero, -1)?.id)
        assertEquals("b", alternativeRecipe(listOf(recipe("a"), recipe("b"), recipe("c")), emptySet(), -2)?.id)
    }

    @Test fun newCopyAvoidsForbiddenWords() {
        val texts = listOf(recipeEyebrow(3), recipeHeadline(RecipeScreenMode.DAILY), RECIPE_DAILY_SUBTITLE, ALTERNATIVE_EYEBROW, RECIPE_FOOTER_NOTE, alternativeStatusLabel(true), alternativeStatusLabel(false), VIEW_COOK_ACTION) + RecommendationReason.entries.map { heroEyebrow(0, it) }
        val banned = listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn") // lint-copy: rule
        texts.forEach { t -> banned.forEach { assertFalse(t.lowercase().contains(it), "$t chứa '$it'") } }
    }
}
