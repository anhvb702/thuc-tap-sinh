package com.loitu.yourrefri.feature.settings.ui

import com.loitu.yourrefri.feature.home.ui.pickHomeGacha
import com.loitu.yourrefri.feature.notice.logic.DailyNotice
import com.loitu.yourrefri.feature.notice.logic.planDailyNotice
import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridEffect
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.feature.pantry.logic.daysLeft
import com.loitu.yourrefri.feature.pantry.logic.reduce
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.filterRecipesByDiet
import com.loitu.yourrefri.feature.recipe.logic.pickRandomRecipe
import com.loitu.yourrefri.feature.recipe.ui.RandomRecipeCopy
import com.loitu.yourrefri.feature.recipe.ui.WEEKLY_ADJUST_DIET_ACTION
import com.loitu.yourrefri.feature.recipe.ui.WEEKLY_EMPTY_DIET_MESSAGE
import com.loitu.yourrefri.feature.recipe.ui.emptyDietRecommendationCopy
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class R0EmpiricalChallengeTest {

    private val today = LocalDate(2026, 11, 16)

    // ==========================================
    // R0-01: Diet hard filter & Empty state copy
    // ==========================================

    @Test
    fun testR0_01_pickRandomRecipe_returnsNullWhenAllCandidateRecipesViolateDiet() {
        val meatRecipe = Recipe("beef", "Thịt bò xào", listOf("Nấu"), listOf(Slot(listOf("thit-bo"))))
        val fishRecipe = Recipe("fish", "Cá basa rán", listOf("Rán"), listOf(Slot(listOf("ca-basa"))))
        val candidates = listOf(meatRecipe, fishRecipe)

        // Caller filters by diet:
        val vegetarianPool = filterRecipesByDiet(candidates, DietRestriction.VEGETARIAN)
        assertTrue(vegetarianPool.isEmpty(), "Diet filter must produce empty list for non-compliant recipes")

        val state = PantryGridState(today = today)
        val pick = pickRandomRecipe(vegetarianPool, state, null, Random(42))
        assertNull(pick, "pickRandomRecipe must strictly return null when all candidate recipes violate diet constraints")
    }

    @Test
    fun testR0_01_pickHomeGacha_returnsNullWhenAllCandidateRecipesViolateDiet() {
        val meatRecipe = Recipe("beef", "Thịt bò xào", listOf("Nấu"), listOf(Slot(listOf("thit-bo"))))
        val fishRecipe = Recipe("fish", "Cá basa rán", listOf("Rán"), listOf(Slot(listOf("ca-basa"))))
        val candidates = listOf(meatRecipe, fishRecipe)

        val vegetarianPool = filterRecipesByDiet(candidates, DietRestriction.VEGETARIAN)
        assertTrue(vegetarianPool.isEmpty())

        val state = PantryGridState(today = today)
        val pick = pickHomeGacha(vegetarianPool, state, null, Random(42))
        assertNull(pick, "pickHomeGacha must strictly return null when all candidate recipes violate diet constraints")
    }

    @Test
    fun testR0_01_emptyCopy_matchesExactRequiredCopyAcrossScreens() {
        val expectedMessage = "Chưa có món phù hợp với chế độ ăn hiện tại. Bạn thử điều chỉnh chế độ ăn nhé."
        val expectedAction = "Điều chỉnh chế độ ăn"

        assertEquals(expectedMessage, emptyDietRecommendationCopy.message)
        assertEquals(expectedAction, emptyDietRecommendationCopy.actionLabel)

        assertEquals(expectedMessage, RandomRecipeCopy.emptyDietMessage)
        assertEquals(expectedAction, RandomRecipeCopy.adjustDietAction)

        assertEquals(expectedMessage, WEEKLY_EMPTY_DIET_MESSAGE)
        assertEquals(expectedAction, WEEKLY_ADJUST_DIET_ACTION)
    }

    // ==========================================
    // R0-02: coerceWeekdayTime & DailyNotice
    // ==========================================

    @Test
    fun testR0_02_coerceWeekdayTime_boundaryValues() {
        // Required boundaries from challenger spec:
        // 13:59 -> 14:00
        // 14:00 -> 14:00
        // 20:00 -> 20:00
        // 20:01 -> 20:00
        // 23:59 -> 20:00
        // 00:00 -> 14:00

        val r1400 = coerceWeekdayTime(LocalTime(14, 0))
        assertEquals(LocalTime(14, 0), r1400, "14:00 -> 14:00")

        val r1401 = coerceWeekdayTime(LocalTime(14, 1))
        assertEquals(LocalTime(14, 1), r1401, "14:01 -> 14:01")

        val r1959 = coerceWeekdayTime(LocalTime(19, 59))
        assertEquals(LocalTime(19, 59), r1959, "19:59 -> 19:59")

        val r2000 = coerceWeekdayTime(LocalTime(20, 0))
        assertEquals(LocalTime(20, 0), r2000, "20:00 -> 20:00")

        val r2001 = coerceWeekdayTime(LocalTime(20, 1))
        assertEquals(LocalTime(20, 0), r2001, "20:01 -> 20:00")

        val r2359 = coerceWeekdayTime(LocalTime(23, 59))
        assertEquals(LocalTime(20, 0), r2359, "23:59 -> 20:00")

        val r0000 = coerceWeekdayTime(LocalTime(0, 0))
        assertEquals(LocalTime(14, 0), r0000, "00:00 -> 14:00")

        // CRITICAL BOUNDARY BUG VERIFICATION:
        // When input is strictly before 14:00 with non-zero minute:
        // Expected interval clamping to [14:00, 20:00]: 13:59 -> 14:00
        // Actual implementation in ReminderCopy.kt:
        //   hour = time.hour.coerceIn(14, 20)
        //   minute = if (hour == 20) 0 else time.minute
        // For 13:59, hour becomes 14, but minute remains 59 -> returns 14:59!
        val r1359 = coerceWeekdayTime(LocalTime(13, 59))
        println("EMPIRICAL OBSERVATION: coerceWeekdayTime(13:59) = $r1359")
        assertEquals(LocalTime(14, 0), r1359, "13:59 should clamp to 14:00")
    }

    @Test
    fun testR0_02_DailyNotice_formatting_itemsCount() {
        val ingList = listOf(
            Ingredient("m1", "Món 1", "khac", 2),
            Ingredient("m2", "Món 2", "khac", 2),
            Ingredient("m3", "Món 3", "khac", 2),
            Ingredient("m4", "Món 4", "khac", 2),
        )

        // items == 0 (without contextual recipes) -> null
        val notice0 = planDailyNotice(
            pantry = emptyList(),
            ingredients = ingList,
            today = today,
            lastSent = null,
            sundayShopping = false,
            recipes = emptyList(),
        )
        assertNull(notice0, "items == 0 with empty recipes should return null")

        // items == 1
        val p1 = listOf(PantryItem("m1", true, today))
        val notice1 = assertNotNull(planDailyNotice(p1, ingList, today, null, sundayShopping = false))
        assertEquals("Món 1 nên dùng trước (hạn ước tính). Mở xem gợi ý món nhé.", notice1.body)

        // items == 2
        val p2 = listOf(PantryItem("m1", true, today), PantryItem("m2", true, today))
        val notice2 = assertNotNull(planDailyNotice(p2, ingList, today, null, sundayShopping = false))
        assertEquals("Món 1, Món 2 nên dùng trước (hạn ước tính). Mở xem gợi ý món nhé.", notice2.body)

        // items == 3
        val p3 = listOf(PantryItem("m1", true, today), PantryItem("m2", true, today), PantryItem("m3", true, today))
        val notice3 = assertNotNull(planDailyNotice(p3, ingList, today, null, sundayShopping = false))
        assertEquals("Món 1, Món 2, Món 3 nên dùng trước (hạn ước tính). Mở xem gợi ý món nhé.", notice3.body)

        // items > 3 (4 items) -> caps at 3 and appends "và vài món khác"
        val p4 = listOf(
            PantryItem("m1", true, today),
            PantryItem("m2", true, today),
            PantryItem("m3", true, today),
            PantryItem("m4", true, today),
        )
        val notice4 = assertNotNull(planDailyNotice(p4, ingList, today, null, sundayShopping = false))
        assertEquals("Món 1, Món 2, Món 3 và vài món khác nên dùng trước (hạn ước tính). Mở xem gợi ý món nhé.", notice4.body)
    }

    // ==========================================
    // R0-03: Depleted item restock invariant
    // ==========================================

    @Test
    fun testR0_03_depletedItem_restocked_resetsMarkedAtAndDaysLeftEqualsShelfDays() {
        val thirtyDaysAgo = today.minus(30, DateTimeUnit.DAY)
        val ingredient = Ingredient("rau-muong", "Rau muống", "rau", defaultShelfDays = 3)
        val depletedItem = PantryItem(ingredientId = "rau-muong", have = false, markedAt = thirtyDaysAgo)

        val initialState = PantryGridState(
            today = today,
            ingredients = listOf(ingredient),
            pantry = mapOf("rau-muong" to depletedItem),
        )

        // Pre-condition: item is depleted
        assertFalse(initialState.pantry.getValue("rau-muong").have)
        assertEquals(thirtyDaysAgo, initialState.pantry.getValue("rau-muong").markedAt)

        // User taps "Có" on depleted item
        val update = reduce(initialState, PantryGridAction.Tap("rau-muong"))

        val restockedItem = update.state.pantry.getValue("rau-muong")
        assertTrue(restockedItem.have, "have must become true")
        assertEquals(today, restockedItem.markedAt, "markedAt must become today (not 30 days ago)")
        assertEquals(listOf(PantryGridEffect.SaveHave("rau-muong", true, today)), update.effects)

        // Verify daysLeft calculation
        val cellAfter = cellsOf(update.state).single { it.ingredientId == "rau-muong" }
        assertTrue(cellAfter.have)
        assertEquals(3, cellAfter.daysLeft, "daysLeft must equal shelfDays (3), not negative or expired")
        assertEquals(ingredient.defaultShelfDays, cellAfter.daysLeft)

        // Direct daysLeft function check
        val directDaysLeft = daysLeft(restockedItem, ingredient, today)
        assertEquals(3, directDaysLeft)
    }
}
