package com.loitu.yourrefri.feature.recipe.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.ui.SlideSegmentState.COMPLETED
import com.loitu.yourrefri.feature.recipe.ui.SlideSegmentState.CURRENT
import com.loitu.yourrefri.feature.recipe.ui.SlideSegmentState.UPCOMING
import java.io.File

class HandsFreeStepsTest {
    private fun recipe(steps: List<String>, stepMinutes: List<Int?> = emptyList()) = Recipe(
        id = "test-recipe",
        title = "Món thử",
        steps = steps,
        required = listOf(Slot(listOf("ingredient"))),
        stepMinutes = stepMinutes,
    )

    @Test
    fun stepTimerPrefersStructuredMinutesAndReturnsSeconds() {
        assertEquals(600, stepTimerSeconds(recipe(listOf("Nấu 2 phút"), listOf(10)), 0))
    }

    @Test
    fun stepTimerFallsBackToMinutesInStepText() {
        assertEquals(900, stepTimerSeconds(recipe(listOf("Kho nhỏ lửa trong 15 phút")), 0))
    }

    @Test
    fun stepTimerIsNullForPreparationServingAndInvalidIndexes() {
        val recipe = recipe(listOf("Rửa rau", "Dọn ra mâm"), listOf(null, null))
        assertEquals(null, stepTimerSeconds(recipe, 0))
        assertEquals(null, stepTimerSeconds(recipe, 1))
        assertEquals(null, stepTimerSeconds(recipe, 2))
    }
    @Test
    fun nextAdvancesUntilLastThenStays() {
        var s = HandsFreeState.of(0, 3)
        s = s.next(); assertEquals(1, s.stepIndex)
        s = s.next(); assertEquals(2, s.stepIndex)
        s = s.next(); assertEquals(2, s.stepIndex)
    }

    @Test
    fun previousGoesBackUntilFirstThenStays() {
        var s = HandsFreeState.of(2, 3)
        s = s.previous(); assertEquals(1, s.stepIndex)
        s = s.previous(); assertEquals(0, s.stepIndex)
        s = s.previous(); assertEquals(0, s.stepIndex)
    }

    @Test
    fun firstAndLastFlags() {
        assertTrue(HandsFreeState.of(0, 4).isFirst)
        assertFalse(HandsFreeState.of(0, 4).isLast)
        assertTrue(HandsFreeState.of(3, 4).isLast)
        assertFalse(HandsFreeState.of(3, 4).isFirst)
        val single = HandsFreeState.of(0, 1)
        assertTrue(single.isFirst && single.isLast)
    }

    @Test
    fun ofClampsOutOfRangeIndex() {
        assertEquals(3, HandsFreeState.of(99, 4).stepIndex)
        assertEquals(0, HandsFreeState.of(-5, 4).stepIndex)
        assertEquals(0, HandsFreeState.of(2, 0).stepIndex)
    }

    @Test
    fun labelIsOneBased() {
        assertEquals("Bước 1/4", HandsFreeState.of(0, 4).label)
        assertEquals("Bước 2/4", HandsFreeState.of(1, 4).label)
        assertEquals("Bước 4/4", HandsFreeState.of(3, 4).label)
    }

    @Test
    fun progressRunsFromFirstStepShareToFull() {
        assertEquals(0.25f, HandsFreeState.of(0, 4).progress)
        assertEquals(0.5f, HandsFreeState.of(1, 4).progress)
        assertEquals(1f, HandsFreeState.of(3, 4).progress)
        assertEquals(0f, HandsFreeState.of(0, 0).progress)
    }

    @Test
    fun emptyRecipeHasEmptyStateAndNoNavigation() {
        val s = HandsFreeState.of(0, 0)
        assertTrue(s.isEmpty)
        assertFalse(HandsFreeState.of(0, 2).isEmpty)
        assertEquals(s, s.next())
        assertEquals(s, s.previous())
        assertEquals("Món này chưa có các bước", HandsFreeCopy.EMPTY_TITLE)
    }

    @Test
    fun announcementReadsStepOfTotal() {
        assertEquals("Bước 2 trên 4: Đun nước", HandsFreeState.of(1, 4).announcement("Đun nước"))
    }

    @Test
    fun nextButtonLabelChangesOnLastStep() {
        assertEquals("Tiếp", HandsFreeState.of(1, 4).nextLabel)
        assertEquals("Xong, mình nấu xong rồi", HandsFreeState.of(3, 4).nextLabel)
    }

    @Test
    fun copyAvoidsForbiddenWords() {
        val all = listOf(
            HandsFreeCopy.EMPTY_TITLE, HandsFreeCopy.BACK, HandsFreeCopy.NEXT, HandsFreeCopy.DONE,
            HandsFreeCopy.CLOSE_DESCRIPTION, HandsFreeCopy.EMPTY_BACK,
        ).joinToString(" ").lowercase()
        listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn").forEach { assertFalse(it in all) } // lint-copy: rule
        assertEquals("Thoát chế độ nấu rảnh tay", HandsFreeCopy.CLOSE_DESCRIPTION)
    }

    @Test
    fun slideDirection_followsNavigationDirection() {
        assertEquals(1, slideDirection(from = 0, to = 1))
        assertEquals(1, slideDirection(from = 1, to = 4))
        assertEquals(-1, slideDirection(from = 3, to = 2))
        assertEquals(-1, slideDirection(from = 4, to = 1))
        assertEquals(0, slideDirection(from = 2, to = 2))
    }

    @Test
    fun celebrationPopScale_startsSmallAndPopsToFullSizeWithReduceMotionSafe() {
        assertEquals(0.6f, celebrationPopStartScale(animatorsEnabled = true))
        assertEquals(1f, celebrationPopStartScale(animatorsEnabled = false))
    }

    @Test
    fun slideSegmentsMarkCurrentAndKeepEarlierStepsCompleted() {
        assertEquals(listOf(CURRENT, UPCOMING, UPCOMING), slideSegmentStates(0, 3))
        assertEquals(listOf(COMPLETED, CURRENT, UPCOMING), slideSegmentStates(1, 3))
        assertEquals(listOf(COMPLETED, COMPLETED, CURRENT), slideSegmentStates(2, 3))
        assertEquals(listOf(CURRENT), slideSegmentStates(0, 1))
    }

    @Test
    fun slideSegmentsAreEmptyForNonpositiveStepCounts() {
        assertEquals(emptyList(), slideSegmentStates(2, 0))
        assertEquals(emptyList(), slideSegmentStates(2, -1))
    }

    @Test
    fun slideSegmentsClampCurrentIndexAtBothEnds() {
        assertEquals(listOf(CURRENT, UPCOMING, UPCOMING), slideSegmentStates(-1, 3))
        assertEquals(listOf(COMPLETED, COMPLETED, CURRENT), slideSegmentStates(3, 3))
    }

    @Test
    fun slideStepPillUsesOneBasedClampedPageNumbers() {
        assertEquals("Bước 1 / 4", slideStepPillLabel(-1, 4))
        assertEquals("Bước 2 / 4", slideStepPillLabel(1, 4))
        assertEquals("Bước 4 / 4", slideStepPillLabel(4, 4))
        assertEquals("Bước 0 / 0", slideStepPillLabel(0, 0))
    }

    @Test
    fun slideTimerDialShrinksAtLargeFontScale() {
        assertEquals(132, slideTimerDialDp(1.0f))
        assertEquals(132, slideTimerDialDp(1.34f))
        assertEquals(112, slideTimerDialDp(1.35f))
    }

    @Test
    fun handsFreeScreenUsesHorizontalPagerAndRoundedSlideCard() {
        val path = "src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/HandsFreeCookScreen.kt"
        val source = listOf(File(path), File("composeApp", path)).first { it.isFile }.readText()
        assertTrue("HorizontalPager(" in source)
        assertTrue("RoundedCornerShape(28.dp)" in source)
    }
}
