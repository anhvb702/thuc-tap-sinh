@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.loitu.yourrefri.feature.recipe.ui

import java.io.File
import com.loitu.yourrefri.feature.notice.logic.ReminderSnapshot
import com.loitu.yourrefri.feature.notice.logic.WEEKDAY_REMINDER_TIME
import com.loitu.yourrefri.feature.notice.logic.nextReminderTrigger
import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.MealSlotKey
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanEntry
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class LiteCustomRecipeUiTest {
    private fun source(path: String): String {
        val roots = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
        val file = roots.map { File(it, "composeApp/src/main/kotlin/com/loitu/yourrefri/$path") }
            .first { it.isFile }
        return file.readText()
    }

    @Test fun liteRecipeFormHasRequiredInputsAndActionsWithoutEmoji() {
        val screen = source("feature/recipe/ui/RecipeScreen.kt")
        assertTrue(screen.contains("Tên món"))
        assertTrue(screen.contains("Danh sách nguyên liệu cần trừ"))
        assertTrue(screen.contains("onAddCustomRecipe("))
        assertTrue(screen.contains("onDeleteCustomRecipe(customRecipe.id)"))
        assertFalse(screen.contains("🍳"))
    }

    @Test fun editingUsesExistingRecipeAndTheShoppingTabIsIndependent() {
        val screen = source("feature/recipe/ui/RecipeScreen.kt")
        assertTrue(screen.contains("editingLiteRecipe = customRecipe"))
        assertTrue(screen.contains("onEditCustomRecipe(editLiteRecipe(editing, liteRecipeTitle, ids))"))
        assertTrue(screen.contains("RecipeScreenMode.SHOPPING -> DailyShoppingSection(state = state, onStock = onStockShopping)"))
    }

    @Test fun mainActivityPassesMergedRecipesToEveryScaffoldConsumer() {
        val activity = source("MainActivity.kt")
        val scaffold = source("ui/MainScaffold.kt")
        assertTrue(activity.contains("dependencies.recipes + customRecipes"))
        assertTrue(activity.contains("recipes = allRecipes"))
        assertTrue(scaffold.contains("recipes = recipes"))
        assertTrue(scaffold.contains("recipes = visibleRecipes"))
    }

    @Test fun defrostReceiverResolvesBundledAndCustomRecipes() {
        val day = LocalDate(2026, 11, 13)
        val bundled = Recipe("bundle-1", "Canh", listOf("Nấu"), listOf(Slot(listOf("rau"))))
        val custom = Recipe("custom-1", "Gà om", listOf("Nấu"), listOf(Slot(listOf("custom-chicken"))))
        val snapshot = ReminderSnapshot(
            weeklyPlan = listOf(WeeklyMealPlanEntry(day, MealSlot.DINNER, "custom-1", 2)),
            recipes = listOf(bundled) + custom,
            ingredients = listOf(Ingredient("custom-chicken", "Gà", "thit", 3)),
            pantry = listOf(PantryItem("custom-chicken", true, day, PantryCompartment.FREEZE)),
        )
        assertEquals(listOf("Gà"), snapshot.defrostNamesFor(day))
    }

    // Căn cứ quyết định Q4 & Q23 (2026-11-16): Chỉ có 1 thông báo/ngày; ngày có đồ ngăn đông dời sang 08:00.
    @Test fun defrostAlarmUsesEightAmTriggerWhenFrozenDinnerAndWeekdayTimeOtherwise() {
        val tz = TimeZone.of("UTC+07:00")
        val now = Instant.parse("2026-11-12T10:00:00Z")
        val day = LocalDate(2026, 11, 13)
        val recipe = Recipe("dinner", "Cá kho", listOf("Nấu"), listOf(Slot(listOf("ca-hoi"))))
        val ingredients = listOf(Ingredient("ca-hoi", "Cá hồi", "ca", 2))
        val frozen = listOf(PantryItem("ca-hoi", true, day, PantryCompartment.FREEZE))
        val plan = listOf(WeeklyMealPlanEntry(day, MealSlot.DINNER, "dinner", 2))

        fun triggerFor(snapshot: ReminderSnapshot) = nextReminderTrigger(
            enabled = true,
            now = now,
            tz = tz,
            weekdayTime = WEEKDAY_REMINDER_TIME,
            hasFrozenDinnerForDay = snapshot::hasFrozenDinnerFor,
        )

        assertEquals(
            Instant.parse("2026-11-13T01:00:00Z"),
            triggerFor(ReminderSnapshot(plan, listOf(recipe), ingredients, frozen)),
        )
        assertEquals(
            Instant.parse("2026-11-13T09:00:00Z"),
            triggerFor(ReminderSnapshot(plan, listOf(recipe), ingredients, frozen, disabledMeals = listOf(MealSlotKey(day, MealSlot.DINNER)))),
        )
        assertEquals(
            Instant.parse("2026-11-13T09:00:00Z"),
            triggerFor(ReminderSnapshot(plan, listOf(recipe), ingredients, pantry = emptyList())),
        )
    }

    @Test fun recipeDetailHidesStepsAndHandsFreeWhenStepsAreEmpty() {
        val detail = source("feature/recipe/ui/RecipeDetailScreen.kt")
        assertTrue(detail.contains("if (showSteps)"))
        assertTrue(detail.contains("if (showHandsFree && onStartHandsFree != null)"))
        assertTrue(detail.contains("Nấu xong, cập nhật tủ"))
        assertTrue(detail.contains("Giữ lại đồ còn dư"))
    }
}
