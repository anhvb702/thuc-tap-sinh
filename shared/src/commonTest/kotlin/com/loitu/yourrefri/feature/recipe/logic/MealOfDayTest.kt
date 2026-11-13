package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.recipe.data.MealSlot
import kotlin.test.Test
import kotlin.test.assertEquals

class MealOfDayTest {
    @Test
    fun morningAndNoon_areLunch() {
        assertEquals(MealSlot.LUNCH, defaultMealFor(0 + 6))
        assertEquals(MealSlot.LUNCH, defaultMealFor(11))
        assertEquals(MealSlot.LUNCH, defaultMealFor(13))
    }

    @Test
    fun boundary_1400_isDinner() {
        assertEquals(MealSlot.DINNER, defaultMealFor(14))
    }

    @Test
    fun eveningAndNight_areDinner() {
        assertEquals(MealSlot.DINNER, defaultMealFor(18))
        assertEquals(MealSlot.DINNER, defaultMealFor(23))
    }

    @Test
    fun midnight_isDinner() {
        // Gần nửa đêm người dùng thường nghĩ tới bữa tối hơn bữa trưa hôm sau.
        assertEquals(MealSlot.DINNER, defaultMealFor(0))
        assertEquals(MealSlot.DINNER, defaultMealFor(3))
    }

    @Test
    fun earlyMorning_before5_isDinner_andFrom5_isLunch() {
        assertEquals(MealSlot.DINNER, defaultMealFor(4))
        assertEquals(MealSlot.LUNCH, defaultMealFor(5))
    }
}
