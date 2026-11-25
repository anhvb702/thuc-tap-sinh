package com.loitu.yourrefri.feature.settings.ui

import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsPresentationTest {
    @Test fun emptyPreferences_showVarietyDefault() =
        assertEquals("Thích đa dạng", SettingsPresentation.preferencesSummary(emptySet()))

    @Test fun oneOrTwoPreferences_joinShortLabelsInEnumOrder() {
        assertEquals("Cá & Tôm", SettingsPresentation.preferencesSummary(setOf(FamilyPreferenceTag.SEAFOOD)))
        assertEquals(
            "Dễ ăn, Cá & Tôm",
            SettingsPresentation.preferencesSummary(setOf(FamilyPreferenceTag.SEAFOOD, FamilyPreferenceTag.KID_FAVORITE)),
        )
    }

    @Test fun threeOrMorePreferences_showCount() {
        assertEquals(
            "3 lựa chọn",
            SettingsPresentation.preferencesSummary(
                setOf(FamilyPreferenceTag.SEAFOOD, FamilyPreferenceTag.MEAT, FamilyPreferenceTag.VEGGIE_TOFU),
            ),
        )
        assertEquals("4 lựa chọn", SettingsPresentation.preferencesSummary(FamilyPreferenceTag.entries.toSet()))
    }

    @Test fun goalSummary_hasShortLabelForEveryGoal() {
        assertEquals("Cân bằng", SettingsPresentation.goalSummary(DietGoal.BALANCED))
        assertEquals("Nhiều rau", SettingsPresentation.goalSummary(DietGoal.LIGHT))
        assertEquals("Đủ đạm", SettingsPresentation.goalSummary(DietGoal.RICH_PROTEIN))
        assertEquals("Nấu nhanh", SettingsPresentation.goalSummary(DietGoal.QUICK))
    }

    @Test fun togglePreference_addsThenRemoves() {
        val added = SettingsPresentation.togglePreference(emptySet(), FamilyPreferenceTag.MEAT)
        assertEquals(setOf(FamilyPreferenceTag.MEAT), added)
        assertEquals(emptySet(), SettingsPresentation.togglePreference(added, FamilyPreferenceTag.MEAT))
    }

    @Test fun versionLine_hiddenWhenBlank() {
        assertNull(SettingsPresentation.versionLine(""))
        assertNull(SettingsPresentation.versionLine("   "))
        assertEquals("Phiên bản 1.0.0", SettingsPresentation.versionLine("1.0.0"))
    }

    @Test fun copy_statesOnDeviceOnlyAndHasNoBannedWords() {
        assertEquals("Lưu trữ trên thiết bị 100%, không cần tài khoản", SettingsPresentation.onDeviceLine)
        assertEquals("Chế độ ăn này đang lọc hết món, mình nới ra giúp nhé", SettingsPresentation.dietTooStrictNotice)
        val all = listOf(
            SettingsPresentation.title, SettingsPresentation.onDeviceLine, SettingsPresentation.dietTooStrictNotice,
            SettingsPresentation.reminderTitle, SettingsPresentation.reminderTime,
            SettingsPresentation.exportTitle, SettingsPresentation.exportSubtitle,
            SettingsPresentation.importTitle, SettingsPresentation.importSubtitle,
        ).joinToString(" ")
        for (word in listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn")) assertFalse(all.contains(word, ignoreCase = true))
        assertTrue(SettingsPresentation.reminderTime.contains("16:00"))
    }

    @Test fun settingsBottomPadding_leavesRoomAboveFloatingNav() {
        assertEquals(148, SettingsPresentation.bottomPaddingDp)
    }

    @Test fun cookLeftoverEyebrow_usesWarmKitchenCopyWithoutWarehouseJargon() {
        val eyebrow = com.loitu.yourrefri.feature.recipe.ui.CookLeftoverCopy.eyebrow
        assertEquals("CÒN LẠI TRONG TỦ", eyebrow)
        for (word in listOf("tồn kho", "bạn quên", "lãng phí", "bỏ lỡ", "hết hạn")) {
            assertFalse(eyebrow.contains(word, ignoreCase = true))
        }
    }
}
