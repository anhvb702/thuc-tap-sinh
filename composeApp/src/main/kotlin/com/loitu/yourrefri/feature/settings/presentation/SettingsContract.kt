package com.loitu.yourrefri.feature.settings.presentation

import com.loitu.yourrefri.feature.notice.logic.DEFAULT_WEEKDAY_REMINDER_TIME
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.ui.theme.ThemeMode
import kotlinx.datetime.LocalTime

enum class NotificationPermissionReason { ENABLE_DAILY_REMINDER, TEST_REMINDER }

data class SettingsUiState(
    val backupStatus: String? = null,
    val restoreConfirmationPending: Boolean = false,
    val backupBusy: Boolean = false,
    val reminderEnabled: Boolean = false,
    val sundayTime: LocalTime = LocalTime(8, 0),
    val dietRestriction: DietRestriction = DietRestriction.NONE,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val tasteProfile: FamilyTasteProfile = FamilyTasteProfile(),
    val weekdayTime: LocalTime = DEFAULT_WEEKDAY_REMINDER_TIME,
)
sealed interface SettingsIntent {
    data object ClearBackupStatus : SettingsIntent
    data class ExportBackup(val writeJson: suspend (String) -> Unit) : SettingsIntent
    data class StageImportJson(val readJson: suspend () -> String) : SettingsIntent
    data object ConfirmRestore : SettingsIntent
    data object CancelRestore : SettingsIntent
    data class RequestReminderChange(val enabled: Boolean, val hasPermission: Boolean) : SettingsIntent
    data class RequestTestReminder(val hasPermission: Boolean) : SettingsIntent
    data class NotificationPermissionResult(val granted: Boolean) : SettingsIntent
    data class SetSundayTime(val time: LocalTime) : SettingsIntent
    data class SetWeekdayTime(val time: LocalTime) : SettingsIntent
    data class SetDietRestriction(val restriction: DietRestriction) : SettingsIntent
    data class SetThemeMode(val mode: ThemeMode) : SettingsIntent
    data class SetTasteProfile(val profile: FamilyTasteProfile) : SettingsIntent
}
sealed interface SettingsEffect {
    data object BackupRestored : SettingsEffect
    data object RequestNotificationPermission : SettingsEffect
    data object TriggerTestReminder : SettingsEffect
}
