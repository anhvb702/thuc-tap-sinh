package com.loitu.yourrefri.feature.home.ui

import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.logic.RecipeRecommendation
import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import com.loitu.yourrefri.feature.recipe.ui.canFinishManualCook
import com.loitu.yourrefri.feature.recipe.ui.manualCookProgress
import com.loitu.yourrefri.feature.recipe.ui.toggleManualCookStep
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HomeSpotlightAndManualCookTest {
    private fun rec(id: String, minutes: Int? = 15, reason: RecommendationReason = RecommendationReason.READY) =
        RecipeRecommendation(
            recipe = Recipe(id = id, title = id, steps = listOf("b1", "b2"), required = listOf(Slot(listOf("egg"))), minutes = minutes),
            reason = reason,
        )

    @Test
    fun activeSpotlight_picksFirstNonDismissedInPriorityOrder() {
        val list = listOf(rec("first"), rec("second"), rec("third"))
        assertEquals("first", activeSpotlightSuggestion(list, emptySet())?.recipe?.id)
        assertEquals("second", activeSpotlightSuggestion(list, setOf("first"))?.recipe?.id)
        assertNull(activeSpotlightSuggestion(list, setOf("first", "second", "third")))
    }

    @Test
    fun activeSpotlight_emptyListReturnsNull_andResetRestoresFirst() {
        assertNull(activeSpotlightSuggestion(emptyList(), emptySet()))
        val list = listOf(rec("first"))
        assertEquals("first", activeSpotlightSuggestion(list, resetDismissedSuggestions())?.recipe?.id)
    }

    @Test
    fun spotlightSubtitle_formatsMinutesAndReason() {
        assertEquals("15 phút · Đủ nguyên liệu", spotlightSubtitle(rec("a", 15, RecommendationReason.READY)))
        assertEquals("10 phút · Nhanh", spotlightSubtitle(rec("b", 10, RecommendationReason.QUICK)))
        assertEquals("Đủ nguyên liệu", spotlightSubtitle(rec("c", null, RecommendationReason.USE_SOON)))
    }

    @Test
    fun manualCook_toggleAndProgressAndFinishBoundary() {
        val step0 = toggleManualCookStep(emptySet(), 0)
        assertEquals(setOf(0), step0)
        assertEquals(0.25f, manualCookProgress(step0, 4))
        assertFalse(canFinishManualCook(step0, 4))

        val unchecked = toggleManualCookStep(step0, 0)
        assertEquals(emptySet(), unchecked)
        assertEquals(0f, manualCookProgress(unchecked, 4))

        val threeOfFour = setOf(0, 1, 2)
        assertFalse(canFinishManualCook(threeOfFour, 4))
        assertEquals(0.75f, manualCookProgress(threeOfFour, 4))

        val allFour = setOf(0, 1, 2, 3)
        assertTrue(canFinishManualCook(allFour, 4))
        assertEquals(1f, manualCookProgress(allFour, 4))
    }

    @Test
    fun manualCook_zeroOrNegativeStepsAllowsFinishWithoutDivideByZero() {
        assertTrue(canFinishManualCook(emptySet(), 0))
        assertEquals(1f, manualCookProgress(emptySet(), 0))
        assertTrue(canFinishManualCook(emptySet(), -1))
    }

    @Test
    fun userGreetingText_defaultsToAndyAndTrimsCustomName() {
        assertEquals("Chào Andy", userGreetingText())
        assertEquals("Chào Andy", userGreetingText("   "))
        assertEquals("Chào Minh", userGreetingText("  Minh  "))
    }
}
