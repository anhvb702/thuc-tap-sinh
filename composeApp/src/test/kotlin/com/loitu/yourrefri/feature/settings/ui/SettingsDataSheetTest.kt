package com.loitu.yourrefri.feature.settings.ui

import com.loitu.yourrefri.ui.theme.ThemeMode
import kotlin.test.*

class SettingsDataSheetTest {
    @Test fun settingsMainCards_hasExactlyThreeCompactCards() =
        assertEquals(listOf("REMINDER", "TASTE_HABIT", "DATA_APP"), SETTINGS_MAIN_CARDS.map { it.name })
    @Test fun shouldShowSundayTimeRow_collapsesWhenReminderDisabled() {
        assertFalse(shouldShowSundayTimeRow(false))
        assertTrue(shouldShowSundayTimeRow(true))
    }

    @Test fun settingsDataSheetSections_listsAllFiveSectionsInOrder() =
        assertEquals(
            listOf(DataSheetSection.THEME, DataSheetSection.BACKUP, DataSheetSection.TRANSFER, DataSheetSection.TEST_REMINDER, DataSheetSection.ABOUT),
            SETTINGS_DATA_SHEET_SECTIONS,
        )

    @Test fun settingsDataSummary_includesThemeModeAndBackupStatus() {
        assertEquals("Tối · Sao lưu & khôi phục · Đã lưu bản sao", settingsDataSummary(ThemeMode.DARK, "Đã lưu bản sao"))
        assertEquals("Theo máy · Sao lưu & khôi phục", settingsDataSummary(ThemeMode.SYSTEM))
        assertEquals("Sáng · Sao lưu & khôi phục", settingsDataSummary(ThemeMode.LIGHT, null))
        assertEquals("Sáng · Sao lưu & khôi phục", settingsDataSummary(ThemeMode.LIGHT, ""))
        assertEquals("Sáng · Sao lưu & khôi phục", settingsDataSummary(ThemeMode.LIGHT, "  "))
    }

    @Test fun resolveActiveSheet_closesSheetWhenRestoreConfirmationPending() {
        for (sheet in SettingsSheetType.entries) {
            assertNull(resolveActiveSheet(sheet, true))
            assertEquals(sheet, resolveActiveSheet(sheet, false))
        }
        assertNull(resolveActiveSheet(null, false))
    }

    @Test fun nextRequestedSheet_clearsRequestWhenRestoreConfirmationIsPending() {
        for (sheet in listOf(null, SettingsSheetType.TASTE, SettingsSheetType.DATA)) {
            assertNull(nextRequestedSheet(sheet, restoreConfirmationPending = true))
            assertEquals(sheet, nextRequestedSheet(sheet, restoreConfirmationPending = false))
        }
    }

    @Test fun settingsDataSheetLabels_haveNoBannedCopy() {
        val labels = SETTINGS_DATA_SHEET_SECTIONS.map { it.title } + SettingsPresentation.themeModeOrder.map { it.label } +
            SettingsBackupCopy.transferSteps + listOf(SETTINGS_DATA_TITLE, SettingsPresentation.doneLabel, SettingsPresentation.tasteCloseLabel,
                SettingsBackupCopy.backupContents, SettingsBackupCopy.privacy, SETTINGS_EXPORT_LABEL, SETTINGS_IMPORT_LABEL,
                SettingsBackupCopy.exportLabel, SettingsBackupCopy.importLabel)
        val banned = listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", "điểm số", "so sánh", "streak", "chuỗi ngày liên tiếp", "xếp hạng", "so với người khác", "đáng xấu hổ") // lint-copy: rule
        for (label in labels) for (word in banned) assertFalse(label.contains(word, ignoreCase = true), label)
    }
}
