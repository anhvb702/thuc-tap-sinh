package com.loitu.yourrefri.feature.settings.presentation

import androidx.lifecycle.viewModelScope
import com.loitu.yourrefri.core.mvi.MviViewModel
import com.loitu.yourrefri.feature.backup.data.BackupCodec
import com.loitu.yourrefri.feature.backup.data.PantryBackupRepositoryImpl
import com.loitu.yourrefri.feature.settings.ui.SettingsBackupCopy
import kotlinx.coroutines.*

interface SettingsBackupPort {
    suspend fun exportJson(): String
    fun validateJson(json: String)
    suspend fun restoreJson(json: String)
}

class DefaultSettingsBackupPort(private val repository: PantryBackupRepositoryImpl) : SettingsBackupPort {
    override suspend fun exportJson() = repository.exportJson()
    override fun validateJson(json: String) { BackupCodec.decode(json) }
    override suspend fun restoreJson(json: String) { repository.restoreJson(json) }
}

class SettingsViewModel(
    private val backupPort: SettingsBackupPort,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : MviViewModel<SettingsUiState, SettingsIntent, SettingsEffect>(SettingsUiState()) {
    private var pendingRestoreJson: String? = null
    private var operationInProgress = false
    override fun onIntent(intent: SettingsIntent) {
        viewModelScope.launch {
            when (intent) {
                SettingsIntent.ClearBackupStatus -> updateState { copy(backupStatus = null) }
                is SettingsIntent.ExportBackup -> {
                    if (operationInProgress) return@launch
                    operationInProgress = true
                    updateState { copy(backupBusy = true) }
                    try {
                        val exported = backupResult { intent.writeJson(backupPort.exportJson()) } != null
                        val status = if (exported) SettingsBackupCopy.exportSuccess else "Chưa thể lưu bản sao lưu. Hãy thử chọn vị trí khác."
                        updateState { copy(backupStatus = status) }
                    } finally {
                        operationInProgress = false
                        updateState { copy(backupBusy = false) }
                    }
                }
                is SettingsIntent.StageImportJson -> {
                    if (operationInProgress) return@launch
                    operationInProgress = true
                    updateState { copy(backupBusy = true) }
                    try {
                        val json = backupResult { intent.readJson().also(backupPort::validateJson) }
                        pendingRestoreJson = json
                        val status = if (json == null) SettingsBackupCopy.restoreFailure else null
                        updateState { copy(restoreConfirmationPending = json != null, backupStatus = status) }
                    } finally {
                        operationInProgress = false
                        updateState { copy(backupBusy = false) }
                    }
                }
                SettingsIntent.CancelRestore -> {
                    pendingRestoreJson = null
                    updateState { copy(restoreConfirmationPending = false) }
                }
                SettingsIntent.ConfirmRestore -> {
                    val json = pendingRestoreJson
                    if (json == null || operationInProgress) return@launch
                    pendingRestoreJson = null
                    operationInProgress = true
                    updateState { copy(restoreConfirmationPending = false, backupBusy = true) }
                    try {
                        val restored = backupResult { backupPort.restoreJson(json) } != null
                        val status = if (restored) "Đã khôi phục dữ liệu từ bản sao lưu."
                        else "Chưa thể khôi phục dữ liệu. Dữ liệu hiện tại vẫn được giữ nguyên."
                        updateState { copy(backupStatus = status) }
                        if (restored) sendEffect(SettingsEffect.BackupRestored)
                    } finally {
                        operationInProgress = false
                        updateState { copy(backupBusy = false) }
                    }
                }
            }
        }
    }
    private suspend fun <T : Any> backupResult(operation: suspend () -> T): T? = try {
        withContext(ioDispatcher) { operation() }
    } catch (exception: Exception) {
        if (exception is CancellationException) throw exception
        null
    }
}
