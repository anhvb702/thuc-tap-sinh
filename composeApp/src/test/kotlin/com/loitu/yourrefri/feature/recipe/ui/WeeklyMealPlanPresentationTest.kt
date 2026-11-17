package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.MealSlotKey
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.logic.PlannedDayMeal
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class WeeklyMealPlanPresentationTest {
    @Test
    fun weeklyChipSections_haveSeparateShortLabels() {
        assertEquals("Định hướng", WEEKLY_DIET_GOAL_LABEL)
        assertEquals("Khẩu vị", WEEKLY_FAMILY_PREFERENCE_LABEL)
    }

    @Test fun mealPresetsAndEmptyDayCopyAreAvailable() {
        assertEquals("Nghỉ bếp / Ăn ngoài", EMPTY_MEAL_DAY_LABEL)
        assertEquals(setOf(com.loitu.yourrefri.feature.recipe.data.MealSlot.DINNER), enabledMealSlots(WeeklyMealPreset.ONE_MEAL, 1))
        assertEquals(setOf(com.loitu.yourrefri.feature.recipe.data.MealSlot.LUNCH, com.loitu.yourrefri.feature.recipe.data.MealSlot.DINNER), enabledMealSlots(WeeklyMealPreset.TWO_MEALS, 1))
        assertEquals(com.loitu.yourrefri.feature.recipe.data.MealSlot.entries.toSet(), enabledMealSlots(WeeklyMealPreset.THREE_MEALS, 1))
        assertEquals(setOf(com.loitu.yourrefri.feature.recipe.data.MealSlot.DINNER), enabledMealSlots(WeeklyMealPreset.WORKWEEK, 4))
        assertEquals(setOf(com.loitu.yourrefri.feature.recipe.data.MealSlot.LUNCH, com.loitu.yourrefri.feature.recipe.data.MealSlot.DINNER), enabledMealSlots(WeeklyMealPreset.WORKWEEK, 5))
    }

    @Test fun disabledMealHidesAndRestoresWithoutChangingPlannedDishes() {
        val monday = LocalDate(2026, 11, 9)
        val schedule = MealSlot.entries.map { slot ->
            val recipe = Recipe(slot.name, slot.name, listOf("Cook"), listOf(Slot(listOf("rice"))))
            PlannedDayMeal(0, "Thứ 2", recipe, "", mealSlot = slot)
        }
        val disabled = listOf(MealSlotKey(monday, MealSlot.BREAKFAST))

        assertEquals(listOf(MealSlot.LUNCH, MealSlot.DINNER), enabledMealsForDay(schedule, 0, monday, disabled).map { it.mealSlot })
        assertEquals(schedule.map { it.recipe.id }, enabledMealsForDay(schedule, 0, monday, emptyList()).map { it.recipe.id })
        assertEquals(listOf(MealSlot.BREAKFAST, MealSlot.LUNCH, MealSlot.DINNER), schedule.map { it.mealSlot })
        assertEquals(emptyList(), enabledMealsForDay(schedule, 0, monday, MealSlot.entries.map { MealSlotKey(monday, it) }))
    }

    @Test
    fun lunchPrepFeedbackNamesAffectedIngredientsAndDayLabel() {
        assertEquals("Danh sách đi chợ đã tính thêm phần cơm trưa cho 6 nguyên liệu.", lunchPrepSummaryText(6))
        assertEquals("🍱 + cơm trưa", lunchPrepDayLabel())
    }

    @Test fun preferenceToggleAddsAndRemoves() {
        assertEquals(setOf(FamilyPreferenceTag.SEAFOOD), togglePreferenceTag(emptySet(), FamilyPreferenceTag.SEAFOOD))
        assertEquals(emptySet(), togglePreferenceTag(setOf(FamilyPreferenceTag.SEAFOOD), FamilyPreferenceTag.SEAFOOD))
    }
    @Test fun swapOffsetIncrementsOnlyRequestedDay() {
        assertEquals(mapOf(1 to 4, 3 to 1), bumpDaySwapOffset(mapOf(1 to 4), 3))
        assertEquals(mapOf(1 to 5), bumpDaySwapOffset(mapOf(1 to 4), 1))
    }
    @Test fun copyShowsCountsAndStockResult() {
        assertEquals("2 món cần mua · 3 món đã có trong tủ", shoppingSummaryText(2, 3))
        assertEquals("Đã mua xong (2 món) · Cất vào tủ lạnh 🧊", stockActionButtonLabel(2))
        assertEquals("Đã cất 2 món vào tủ lạnh.", stockResultMessage(2))
    }
    @Test fun zeroStockResultIsGentle() {
        assertEquals("Đã mua xong (0 món) · Cất vào tủ lạnh 🧊", stockActionButtonLabel(0))
        assertEquals("Danh sách đã được cập nhật.", stockResultMessage(0))
    }
    @Test fun dayHeaderAndShoppingSubtitleSupportLunchMealPrep() {
        assertEquals("Thứ 2 · Bữa cơm chính", dayMealCardHeader("Thứ 2"))
        assertEquals("Thứ 3 · Bữa trưa & tối", dayMealCardHeader("Thứ 3", "Bữa trưa & tối"))
        assertEquals("Dùng cho 2 bữa", shoppingItemSubtitle(2, 0, 2, isLunchPrep = false))
        assertEquals("Dùng cho 4 phần (có phần cơm trưa mang đi)", shoppingItemSubtitle(4, 0, 4, isLunchPrep = true))
        assertEquals(
            "Cần cho 4 phần (gồm cơm trưa) · trong tủ có 1, mua thêm 3",
            shoppingItemSubtitle(4, 1, 3, isLunchPrep = true),
        )
    }
    @Test fun lateWeekMeatSuggestsFreezerAndAllowsChillOverride() {
        val item = com.loitu.yourrefri.feature.recipe.logic.ShoppingListItem(
            com.loitu.yourrefri.feature.pantry.data.Ingredient("pork", "Thịt", "thit", 7), 1, false,
            usedDayIndexes = setOf(4),
        )
        assertEquals(com.loitu.yourrefri.feature.pantry.data.PantryCompartment.FREEZE, suggestedWeeklyCompartment(item))
        assertEquals(listOf("pork" to com.loitu.yourrefri.feature.pantry.data.PantryCompartment.CHILL), weeklyStockCompartments(listOf("pork"), mapOf("pork" to com.loitu.yourrefri.feature.pantry.data.PantryCompartment.CHILL)))
    }

    @Test fun shoppingItemsAreGroupedIntoThreeAisles() {
        val items = listOf("rau-la", "thit", "sua-trung").map { id ->
            com.loitu.yourrefri.feature.recipe.logic.ShoppingListItem(
                com.loitu.yourrefri.feature.pantry.data.Ingredient(id, id, id, 5), 1, false,
            )
        }
        assertEquals(listOf("Rau củ & Quả", "Thịt & Hải sản", "Trứng, Đậu & Đồ khô"), weeklyShoppingAisles(items).map { it.first })
    }}
