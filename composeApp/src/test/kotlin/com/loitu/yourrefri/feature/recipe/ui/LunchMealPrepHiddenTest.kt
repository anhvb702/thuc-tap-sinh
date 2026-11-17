package com.loitu.yourrefri.feature.recipe.ui

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LunchMealPrepHiddenTest {
    private fun source(path: String): String {
        val relative = "src/main/kotlin/com/loitu/yourrefri/$path"
        return listOf(File(relative), File("composeApp", relative))
            .first { it.isFile }.readText()
    }

    @Test fun lunchPrepIsWiredForTwoMealBatchCooking() {
        val screen = source("feature/recipe/ui/RecipeScreen.kt")
        val scaffold = source("ui/MainScaffold.kt")
        assertTrue(screen.contains("weeklyIncludeLunchMealPrep"))
        assertTrue(screen.contains("onToggleLunchMealPrep"))
        assertTrue(scaffold.contains("weeklyIncludeLunchMealPrep"))
    }

    @Test fun weeklyContentHasBatchCookToggleLabel() {
        val content = source("feature/recipe/ui/WeeklyMealPlanContent.kt")
        assertTrue(content.contains("Nấu gộp cùng bữa tối (x2 phần)"))
    }

    @Test fun pantryStockSubtitleWithoutLunch() {
        assertEquals(
            "Cần cho 2 bữa (trong tủ có 1, mua thêm 1)",
            shoppingItemSubtitle(2, 1, 1),
        )
    }
}
