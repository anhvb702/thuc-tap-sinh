package com.loitu.yourrefri.ui

import com.loitu.yourrefri.feature.cooklog.ui.CookHistoryState
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.settings.presentation.SettingsUiState
import com.loitu.yourrefri.ui.theme.ThemeMode
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MainScaffoldBindingsTest {
    @Test fun settingsBinding_defaultsMatchSettingsUiState() {
        val binding = MainScaffoldSettingsBinding()
        assertEquals(SettingsUiState(), binding.uiState)
        assertEquals("", binding.appVersionName)
    }

    @Test fun settingsBinding_preservesCustomWeekdayAndSundayTimes() {
        val state = SettingsUiState(weekdayTime = LocalTime(18, 30), sundayTime = LocalTime(9, 15))
        assertEquals(state, MainScaffoldSettingsBinding(uiState = state).uiState)
    }

    @Test fun settingsBinding_dispatchesAllCallbacks() {
        val events = mutableListOf<Any>()
        val sunday = LocalTime(9, 15)
        val weekday = LocalTime(18, 30)
        val taste = FamilyTasteProfile(hasYoungKids = true)
        val binding = MainScaffoldSettingsBinding(
            onExportBackup = { events += "export" },
            onImportBackup = { events += "import" },
            onConfirmRestore = { events += "confirm" },
            onCancelRestore = { events += "cancel" },
            onReminderChanged = { events += it },
            onTestReminder = { events += "test" },
            onThemeModeChange = { events += it },
            onDietRestrictionChange = { events += it },
            onSundayTimeChange = { events += it },
            onWeekdayTimeChange = { events += it },
            onTasteProfileChange = { events += it },
        )
        binding.onExportBackup()
        binding.onImportBackup()
        binding.onConfirmRestore()
        binding.onCancelRestore()
        binding.onReminderChanged(true)
        binding.onTestReminder()
        binding.onThemeModeChange(ThemeMode.DARK)
        binding.onDietRestrictionChange(DietRestriction.NO_SEAFOOD)
        binding.onSundayTimeChange(sunday)
        binding.onWeekdayTimeChange(weekday)
        binding.onTasteProfileChange(taste)
        assertEquals(
            listOf<Any>("export", "import", "confirm", "cancel", true, "test", ThemeMode.DARK,
                DietRestriction.NO_SEAFOOD, sunday, weekday, taste),
            events,
        )
    }

    @Test fun collectionBinding_defaultsAreSafeAndEmpty() {
        val binding = MainScaffoldCollectionBinding()
        assertTrue(binding.favoriteIds.isEmpty())
        assertEquals(CookHistoryState(), binding.cookHistoryState)
        assertTrue(binding.unlockedIds.isEmpty())
        assertTrue(binding.mysteryRewardsReady)
        assertNull(binding.mysteryUnlockFailedId)
    }

    @Test fun collectionBinding_defaultOnRecordCook_returnsNull() {
        val results = mutableListOf<Long?>()
        MainScaffoldCollectionBinding().onRecordCook("soup", listOf("carrot"), MealSlot.DINNER) { results += it }
        assertEquals(listOf<Long?>(null), results)
    }

    @Test fun collectionBinding_dispatchesFavoriteDeleteAndUnlockCallbacks() {
        val recipe = Recipe("soup", "Soup", listOf("Simmer"), listOf(Slot(listOf("carrot"))), isMystery = true)
        val events = mutableListOf<Any>()
        val binding = MainScaffoldCollectionBinding(
            onToggleFavorite = { events += it },
            onDeleteCook = { events += it },
            onUnlockMysteryRecipe = { events += it },
        )
        binding.onToggleFavorite(recipe.id)
        binding.onDeleteCook(42L)
        binding.onUnlockMysteryRecipe(recipe)
        assertEquals(listOf<Any>(recipe.id, 42L, recipe), events)
    }
}
