package com.loitu.yourrefri.reminder

import android.app.NotificationManager
import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReminderPopupBoxTest {
    @Test
    fun vibrationPolicyRestoresHeadsUpForFreshAndDefaultLegacyChannels() {
        assertTrue(ReminderChannel.shouldVibrate(null, false))
        assertTrue(ReminderChannel.shouldVibrate(NotificationManager.IMPORTANCE_DEFAULT, false))
        assertFalse(ReminderChannel.shouldVibrate(NotificationManager.IMPORTANCE_HIGH, false))
        assertTrue(ReminderChannel.shouldVibrate(NotificationManager.IMPORTANCE_HIGH, true))
        assertFalse(ReminderChannel.shouldVibrate(NotificationManager.IMPORTANCE_LOW, true))
        assertFalse(ReminderChannel.shouldVibrate(NotificationManager.IMPORTANCE_NONE, true))
    }

    @Test
    fun urgentItemsAreSortedAndLimitedInPopupCopy() {
        val today = LocalDate(2026, 11, 19)
        val state = PantryGridState(
            today = today,
            ingredients = listOf(
                Ingredient("rau", "Rau ngót", "fresh", 4),
                Ingredient("thit", "Thịt băm", "fresh", 4),
                Ingredient("trung", "Trứng", "fresh", 4),
            ),
            pantry = listOf(
                PantryItem("rau", true, LocalDate(2026, 11, 17)),
                PantryItem("thit", true, LocalDate(2026, 11, 16)),
                PantryItem("trung", true, LocalDate(2026, 11, 15)),
            ).associateBy { it.ingredientId },
        )

        val data = buildReminderPopupData(state, 2)

        assertEquals("canh-rau-ngot-thit-bam", data.recipeId)
        assertEquals("Canh rau ngót thịt băm", data.dishTitle)
        assertEquals("Tối nay nấu Canh rau ngót thịt băm nhé", data.title)
        assertTrue(data.body.contains("Trứng, Thịt băm"))
        assertFalse(data.body.contains("Rau ngót"))
        assertCopyHasNoBannedWords(data.title + data.body)
    }

    @Test
    fun emptyPantryUsesGentleDishSuggestion() {
        val data = buildReminderPopupData(PantryGridState(LocalDate(2026, 11, 19)), 2)

        assertEquals("canh-rau-ngot-thit-bam", data.recipeId)
        assertEquals("Canh rau ngót thịt băm", data.dishTitle)
        assertEquals("Gợi ý bữa cơm: Canh rau ngót thịt băm", data.title)
        assertTrue(data.body.contains("dễ nấu"))
        assertCopyHasNoBannedWords(data.title + data.body)
    }

    private fun assertCopyHasNoBannedWords(copy: String) {
        listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên", "điểm", "streak").forEach { banned -> // lint-copy: rule
            assertFalse(copy.contains(banned, ignoreCase = true), "chứa từ cấm: $banned")
        }
    }
}
