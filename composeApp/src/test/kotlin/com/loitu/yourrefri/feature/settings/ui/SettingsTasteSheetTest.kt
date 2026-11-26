package com.loitu.yourrefri.feature.settings.ui

import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.DishStyle
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.recipe.logic.TasteFlavor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SettingsTasteSheetTest {
    @Test fun tasteSheetSections_containsAllSevenCoreSectionsWithoutDuplicates() =
        assertEquals(listOf("DIET", "FAVORITE_GROUPS", "GOAL", "FLAVORS", "DISH_STYLES", "AVOIDED", "KIDS"), TASTE_SHEET_SECTIONS.map { it.name })

    @Test fun settingsTasteHabitSummary_includesDietRestrictionWhenSelected() {
        val diet = DietRestriction.VEGETARIAN
        assertEquals(diet.labelVi, settingsTasteHabitSummary(diet, emptySet(), DietGoal.BALANCED, FamilyTasteProfile()))
        val preferences = setOf(FamilyPreferenceTag.VEGGIE_TOFU)
        val goal = DietGoal.QUICK
        val taste = FamilyTasteProfile(flavors = setOf(TasteFlavor.SWEET))
        assertEquals("Ăn chay · Rau, Đậu & Nấm · Nấu nhanh · Vị hơi ngọt", settingsTasteHabitSummary(diet, preferences, goal, taste))
    }

    @Test fun settingsTasteHabitSummary_returnsDefaultWhenNothingCustomized() =
        assertEquals(
            "Ăn đa dạng · Cân bằng",
            settingsTasteHabitSummary(DietRestriction.NONE, emptySet(), DietGoal.BALANCED, FamilyTasteProfile()),
        )

    @Test fun togglePreference_changesOnlyTargetTagWithoutReplacingWholeSet() {
        val original = setOf(FamilyPreferenceTag.SEAFOOD, FamilyPreferenceTag.KID_FAVORITE)
        val added = SettingsPresentation.togglePreference(original, FamilyPreferenceTag.MEAT)
        assertEquals(original + FamilyPreferenceTag.MEAT, added)
        assertEquals(original, SettingsPresentation.togglePreference(added, FamilyPreferenceTag.MEAT))
    }

    @Test fun resetTasteHabitDefaults_restoresAllFieldsToDefaults() =
        assertEquals(TasteHabitSnapshot(DietRestriction.NONE, emptySet(), DietGoal.BALANCED, FamilyTasteProfile()), resetTasteHabitDefaults())

    @Test fun dispatchTasteHabitReset_invokesAllFourCallbacksWithDefaults() {
        val diets = mutableListOf<DietRestriction>()
        val preferences = mutableListOf<Set<FamilyPreferenceTag>>()
        val goals = mutableListOf<DietGoal>()
        val tastes = mutableListOf<FamilyTasteProfile>()

        val feedback = dispatchTasteHabitReset(
            onDiet = { diets.add(it) },
            onPreferences = { preferences.add(it) },
            onGoal = { goals.add(it) },
            onTaste = { tastes.add(it) },
        )

        assertEquals(listOf(DietRestriction.NONE), diets)
        assertEquals(listOf(emptySet<FamilyPreferenceTag>()), preferences)
        assertEquals(listOf(DietGoal.BALANCED), goals)
        assertEquals(listOf(FamilyTasteProfile()), tastes)
        assertEquals(SettingsPresentation.tasteSavedFeedback, feedback)
    }

    @Test fun applyImmediateTasteChange_invokesSaveCallbackImmediatelyWithSavedFeedback() {
        val current = TasteHabitSnapshot(diet = DietRestriction.NO_SEAFOOD, goal = DietGoal.QUICK)
        val expected = current.copy(taste = FamilyTasteProfile(hasYoungKids = true))
        val saved = mutableListOf<TasteHabitSnapshot>()
        val result = applyImmediateTasteChange(current, { it.copy(taste = expected.taste) }) { saved.add(it) }
        assertEquals(listOf(expected), saved)
        assertEquals(expected to "Đã lưu", result)
    }

    @Test fun tasteSheetLabels_haveNoBannedCopy() {
        val labels = TASTE_SHEET_SECTIONS.map { it.title } + listOf(
            SettingsPresentation.tasteGroupTitle, SettingsPresentation.tasteSavedFeedback,
            SettingsPresentation.tasteResetLabel, SettingsPresentation.doneLabel, SettingsPresentation.tasteCloseLabel,
        ) + FamilyPreferenceTag.entries.map { it.labelVi } +
            DishStyle.entries.map { it.labelVi } +
            TasteFlavor.entries.map { it.labelVi } +
            DietRestriction.entries.map { it.hintVi }
        val banned = listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", "điểm số", "so sánh", "streak") // lint-copy: rule
        for (label in labels) for (word in banned) assertFalse(label.contains(word, ignoreCase = true), label)
    }

    @Test fun tasteSheetGroups_hasExactlyThreeGroupsCoveringAllSevenSections() {
        assertEquals(listOf("Nhà mình ăn", "Hay thích", "Tuần này"), TASTE_SHEET_GROUPS.map { it.title })
        assertEquals(
            listOf(listOf("DIET", "AVOIDED", "KIDS"), listOf("FLAVORS", "DISH_STYLES"), listOf("FAVORITE_GROUPS", "GOAL")),
            TASTE_SHEET_GROUPS.map { group -> group.sections.map { it.name } },
        )
    }

    @Test fun tasteKidsChipLabel_usesMildSpiceCopy() =
        assertEquals("Ít cay · Nhạt cho trẻ", TASTE_KIDS_CHIP_LABEL)

    @Test fun tasteSheetGroupLabels_haveNoBannedCopy() {
        val labels = TASTE_SHEET_GROUPS.flatMap { listOf(it.title, it.subtitle) } + TASTE_KIDS_CHIP_LABEL
        val banned = listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", "điểm số", "so sánh", "streak", "chuỗi ngày liên tiếp", "xếp hạng", "so với người khác", "đáng xấu hổ") // lint-copy: rule
        for (label in labels) for (word in banned) assertFalse(label.contains(word, ignoreCase = true), label)
    }

    @Test fun kidsChipToggle_preservesHasYoungKidsPersistenceField() {
        val current = TasteHabitSnapshot(taste = FamilyTasteProfile(
            flavors = setOf(TasteFlavor.SWEET),
            avoidIngredientIds = setOf("tom"),
        ))
        val saved = mutableListOf<FamilyTasteProfile>()
        val toggle: (TasteHabitSnapshot) -> TasteHabitSnapshot =
            { it.copy(taste = it.taste.copy(hasYoungKids = !it.taste.hasYoungKids)) }

        val (enabled) = applyImmediateTasteChange(current, toggle) { saved.add(it.taste) }
        applyImmediateTasteChange(enabled, toggle) { saved.add(it.taste) }

        assertEquals(listOf(current.taste.copy(hasYoungKids = true), current.taste), saved)
    }
}
