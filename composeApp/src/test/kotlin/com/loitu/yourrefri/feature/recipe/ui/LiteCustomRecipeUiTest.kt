package com.loitu.yourrefri.feature.recipe.ui

import java.io.File
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
        val receiver = source("reminder/ReminderReceiver.kt")
        assertTrue(receiver.contains("RecipeBundle.parse(recipesJson) + customRecipes"))
        assertTrue(receiver.contains("CustomRecipeRepositoryImpl(db, Dispatchers.IO).observeAll()"))
    }

    // Căn cứ quyết định Q4 & Q23 (2026-11-16): Chỉ có 1 thông báo/ngày; ngày có đồ ngăn đông dời sang 08:00.
    @Test fun defrostAlarmUsesEightAmTriggerAndExistingChannel() {
        val scheduler = source("reminder/ReminderScheduler.kt")
        val receiver = source("reminder/ReminderReceiver.kt")
        assertTrue(scheduler.contains("nextReminderTrigger("))
        assertTrue(scheduler.contains("LEGACY_DEFROST_REQUEST"))
        assertTrue(receiver.contains("defrostNamesForDay("))
        assertTrue(receiver.contains("planDailyNotice("))
        assertTrue(receiver.contains("NotificationChannel(\"daily_reminder\""))
    }

    @Test fun recipeDetailHidesStepsAndHandsFreeWhenStepsAreEmpty() {
        val detail = source("feature/recipe/ui/RecipeDetailScreen.kt")
        assertTrue(detail.contains("if (showSteps)"))
        assertTrue(detail.contains("if (showHandsFree && onStartHandsFree != null)"))
        assertTrue(detail.contains("Nấu xong, cập nhật tủ"))
        assertTrue(detail.contains("Giữ lại đồ còn dư"))
    }
}
