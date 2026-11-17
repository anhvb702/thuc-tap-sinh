package com.loitu.yourrefri.feature.notice.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.LeftoverItem
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanEntry
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DefrostNoticeTest {
    private val today = LocalDate(2026, 11, 13)
    private val recipe = Recipe("dinner", "Cá kho", listOf("Nấu"), listOf(Slot(listOf("ca-hoi"))))
    private val ingredients = listOf(Ingredient("ca-hoi", "Cá hồi", "ca", 2), Ingredient("egg", "Trứng", "khac", 7))
    private fun entry(meal: MealSlot = MealSlot.DINNER, id: String = "dinner") =
        WeeklyMealPlanEntry(today, meal, id, 2)

    @Test
    fun todaysDinnerUsesFrozenSeafood_returnsGentleNotice() {
        val notice = assertNotNull(planDefrostNotice(today, listOf(entry()), listOf(recipe), ingredients,
            listOf(PantryItem("ca-hoi", true, today, PantryCompartment.FREEZE))))
        assertEquals(today, notice.day)
        assertEquals(listOf("ca-hoi"), notice.ingredientIds)
        assertEquals("Chuẩn bị rã đông", notice.title)
        assertEquals("Cá hồi đang ở ngăn đông; mình có thể chuyển xuống ngăn mát để chuẩn bị cho bữa tối nay nhé.", notice.body)
    }

    @Test
    fun missingDinnerOrUnknownRecipe_returnsNull() {
        val frozen = listOf(PantryItem("ca-hoi", true, today, PantryCompartment.FREEZE))
        assertNull(planDefrostNotice(today, emptyList(), listOf(recipe), ingredients, frozen))
        assertNull(planDefrostNotice(today, listOf(entry(id = "missing")), listOf(recipe), ingredients, frozen))
    }

    @Test
    fun lunchOnlyDoesNotTrigger() {
        assertNull(planDefrostNotice(today, listOf(entry(MealSlot.LUNCH)), listOf(recipe), ingredients,
            listOf(PantryItem("ca-hoi", true, today, PantryCompartment.FREEZE))))
    }

    @Test
    fun chilledAbsentOrNotHaveDoesNotTrigger() {
        assertNull(planDefrostNotice(today, listOf(entry()), listOf(recipe), ingredients,
            listOf(PantryItem("ca-hoi", true, today))))
        assertNull(planDefrostNotice(today, listOf(entry()), listOf(recipe), ingredients,
            listOf(PantryItem("ca-hoi", false, today, PantryCompartment.FREEZE))))
    }

    @Test
    fun groupBasedMeatIsRecognized() {
        val meatRecipe = Recipe("meat", "Món thịt", listOf("Nấu"), listOf(Slot(listOf("custom-pork"))))
        assertNotNull(planDefrostNotice(today, listOf(entry(id = "meat")), listOf(meatRecipe),
            listOf(Ingredient("custom-pork", "Thịt", "thit", 3)),
            listOf(PantryItem("custom-pork", true, today, PantryCompartment.FREEZE))))
    }

    @Test
    fun frozenLeftoverInPlannedDinnerIsRecognized() {
        val frozenLeftover = LeftoverItem("lo-freeze", "Cá kho", PantryCompartment.FREEZE, today, 14)
        val chillLeftover = LeftoverItem("lo-chill", "Canh chua", PantryCompartment.CHILL, today, 2)
        val names = defrostNamesForDay(
            day = today,
            weeklyPlan = listOf(entry()),
            recipes = listOf(recipe),
            ingredients = ingredients,
            pantry = emptyList(),
            leftovers = listOf(frozenLeftover, chillLeftover),
        )
        assertEquals(listOf("Cá kho"), names)
    }
}
