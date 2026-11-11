package com.loitu.yourrefri.feature.settings.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsBackupCopyTest {
    @Test
    fun privacyCopyExplainsOfflineUseAndOnDeviceStorage() {
        assertTrue(SettingsBackupCopy.privacy.contains("không cần kết nối mạng"))
        assertTrue(SettingsBackupCopy.privacy.contains("chỉ lưu trên điện thoại này"))
        assertTrue(SettingsBackupCopy.privacy.contains("không tự đồng bộ"))
    }

    @Test
    fun transferStepsExplainManualSaveMoveAndOpenInOrder() {
        assertEquals(
            listOf(
                "Lưu bản sao thành tệp trên điện thoại cũ.",
                "Tự chuyển tệp sang điện thoại mới.",
                "Trên máy mới, mở YourRefri và chọn tệp bản sao.",
            ),
            SettingsBackupCopy.transferSteps,
        )
    }

    @Test
    fun backupActionsUsePlainDistinctLabels() {
        assertEquals("Lưu bản sao vào tệp", SettingsBackupCopy.exportLabel)
        assertEquals("Mở tệp bản sao để khôi phục", SettingsBackupCopy.importLabel)
    }

    @Test
    fun backupCopyExplainsIncludedHistoryAndExcludedPhotos() {
        assertTrue(SettingsBackupCopy.backupContents.contains("có lịch sử nấu"))
        assertTrue(SettingsBackupCopy.backupContents.contains("không có ảnh đính kèm"))
    }

    @Test
    fun restoreCopyWarnsThatCurrentDataWillBeReplaced() {
        assertTrue(SettingsBackupCopy.restoreWarning.contains("sẽ thay thế"))
        assertTrue(SettingsBackupCopy.restoreWarning.contains("trên điện thoại này"))
    }

    @Test
    fun restoreActionsKeepConfirmationAndWayToCancel() {
        assertEquals("Khôi phục", SettingsBackupCopy.restoreLabel)
        assertEquals("Để sau", SettingsBackupCopy.cancelRestoreLabel)
    }

    @Test
    fun backupResultMessagesUsePlainYourRefriFileLanguage() {
        val messages = listOf(SettingsBackupCopy.exportSuccess, SettingsBackupCopy.restoreFailure)

        assertTrue(messages.all { !it.contains("JSON", ignoreCase = true) })
        assertTrue(messages.all { it.contains("tệp bản sao YourRefri") })
    }
}
