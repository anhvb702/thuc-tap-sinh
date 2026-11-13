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

    @Test fun lunchPrepIsDisabledForShopping() {
        assertFalse(LUNCH_MEAL_PREP_ENABLED)
    }

    @Test fun weeklyContentHasNoLunchPrepChip() {
        val content = source("feature/recipe/ui/WeeklyMealPlanContent.kt")
        assertFalse(content.contains("cơm trưa hôm sau"))
        assertFalse(content.contains("lunchPrepSummaryText"))
        assertFalse(content.contains("lunchPrepDayLabel"))
        assertFalse(content.contains("isLunchPrep"))
    }

    @Test fun recipeScreenAndScaffoldDoNotPassLunchPrep() {
        val screen = source("feature/recipe/ui/RecipeScreen.kt")
        val scaffold = source("ui/MainScaffold.kt")
        assertTrue(screen.contains("includeLunchMealPrep = LUNCH_MEAL_PREP_ENABLED"))
        assertFalse(screen.contains("weeklyIncludeLunchMealPrep ?:"))
        assertFalse(screen.contains("onToggleLunchMealPrep ="))
        assertFalse(scaffold.contains("weeklyIncludeLunchMealPrep ="))
    }

    @Test fun pantryStockSubtitleWithoutLunch() {
        assertEquals(
            "Cần cho 2 bữa (trong tủ có 1, mua thêm 1)",
            shoppingItemSubtitle(2, 1, 1),
        )
    }
}
