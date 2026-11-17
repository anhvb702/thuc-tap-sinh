package com.loitu.yourrefri.reminder

import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import java.io.File
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderForceTest {
    @Test fun forceTestBypassesSentDateAndSuggestsDietSafeDish() {
        val recipe = Recipe("veg", "Canh rau", listOf("Nấu"), listOf(Slot(listOf("rau"))))
        val notice = createReminderNotice(
            pantry = emptyList(), ingredients = emptyList(), today = LocalDate(2026, 11, 17),
            recipes = listOf(recipe), dietRestriction = DietRestriction.VEGETARIAN,
            leftovers = emptyList(), defrostNames = emptyList(), lastSent = LocalDate(2026, 11, 17),
            lastNudgeSent = LocalDate(2026, 11, 17), forceTest = true,
        )
        assertEquals("Gợi ý món ngon hôm nay", notice?.title)
        assertTrue(notice?.body.orEmpty().contains("Canh rau"))
    }

    @Test fun forceTestReturnsNoSuggestionWhenDietExcludesEveryDish() {
        val recipe = Recipe("meat", "Thịt kho", listOf("Nấu"), listOf(Slot(listOf("thit-heo"))))
        val notice = createReminderNotice(
            pantry = emptyList(), ingredients = emptyList(), today = LocalDate(2026, 11, 17),
            recipes = listOf(recipe), dietRestriction = DietRestriction.VEGETARIAN,
            leftovers = emptyList(), defrostNames = emptyList(), lastSent = null,
            lastNudgeSent = null, forceTest = true,
        )
        assertNull(notice)
    }

    @Test fun nullPlanFallsBackToFirstDishAllowedBySavedDiet() {
        val recipes = listOf(
            Recipe("meat", "Thịt kho", listOf("Nấu"), listOf(Slot(listOf("thit-heo")))),
            Recipe("veg", "Canh rau", listOf("Nấu"), listOf(Slot(listOf("rau")))),
        )
        val suggestion = forceTestDishSuggestion(recipes, emptyList(), DietRestriction.VEGETARIAN, LocalDate(2026, 11, 17))
        assertEquals("Gợi ý món ngon hôm nay", suggestion?.title)
        assertTrue(suggestion?.body.orEmpty().contains("Canh rau"))
    }

    @Test fun explicitBroadcastAndForceModeDoNotTouchScheduledThrottleKeys() {
        val root = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
            .first { File(it, "composeApp/src/main/kotlin/com/loitu/yourrefri/reminder/ReminderReceiver.kt").isFile }
        val receiver = File(root, "composeApp/src/main/kotlin/com/loitu/yourrefri/reminder/ReminderReceiver.kt").readText()
        val scheduler = File(root, "composeApp/src/main/kotlin/com/loitu/yourrefri/reminder/ReminderScheduler.kt").readText()
        assertTrue(scheduler.contains("EXTRA_FORCE_TEST = \"force_test\""))
        assertTrue(scheduler.contains("Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_FORCE_TEST, true)"))
        assertTrue(receiver.contains("if (!forceTest && !ReminderScheduler.enabled(context)) return"))
        assertTrue(receiver.contains("lastSent = if (forceTest) null else lastSent"))
        assertTrue(receiver.contains("lastNudgeSent = if (forceTest) null else lastNudgeSent"))
        assertTrue(receiver.contains("return notice ?: forceTestDishSuggestion"))
        assertTrue(receiver.contains("if (!forceTest && isContextualNudge)"))
    }
}
