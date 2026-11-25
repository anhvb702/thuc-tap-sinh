package com.loitu.yourrefri.feature.settings.presentation

data class SettingsUiState(
    val backupStatus: String? = null,
    val restoreConfirmationPending: Boolean = false,
    val backupBusy: Boolean = false,
)
sealed interface SettingsIntent {
    data object ClearBackupStatus : SettingsIntent
    data class ExportBackup(val writeJson: suspend (String) -> Unit) : SettingsIntent
    data class StageImportJson(val readJson: suspend () -> String) : SettingsIntent
    data object ConfirmRestore : SettingsIntent
    data object CancelRestore : SettingsIntent
}
sealed interface SettingsEffect {
    data object BackupRestored : SettingsEffect
}
