package com.loitu.yourrefri.feature.settings.presentation

import com.loitu.yourrefri.feature.settings.ui.SettingsBackupCopy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlin.test.*
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val port = FakeSettingsBackupPort()
    private val viewModel = SettingsViewModel(port, dispatcher)
    private val state get() = viewModel.state.value
    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun stageImport_validJson_validatesAndOpensConfirmDialog() = runTest {
        stage()
        assertEquals(listOf("{}"), port.validated)
        assertEquals(SettingsUiState(restoreConfirmationPending = true), state)
    }

    @Test fun stageImport_invalidJson_showsErrorAndDoesNotOpenConfirm() = runTest {
        stage()
        stage("invalid")
        assertEquals(SettingsUiState(backupStatus = SettingsBackupCopy.restoreFailure), state)
        dispatch(SettingsIntent.ConfirmRestore)
        assertTrue(port.restored.isEmpty())
    }

    @Test fun cancelRestore_clearsPendingWithoutCallingRestore() = runTest {
        stage()
        dispatch(SettingsIntent.CancelRestore)
        assertEquals(SettingsUiState(), state)
        dispatch(SettingsIntent.ConfirmRestore)
        assertTrue(port.restored.isEmpty())
    }

    @Test fun confirmRestore_doubleTap_callsRestoreOnlyOnce() = runTest {
        stage()
        viewModel.onIntent(SettingsIntent.ConfirmRestore)
        runCurrent()
        assertEquals(SettingsUiState(backupBusy = true), state)
        viewModel.onIntent(SettingsIntent.ConfirmRestore)
        advanceUntilIdle()
        viewModel.onIntent(SettingsIntent.ConfirmRestore)
        advanceUntilIdle()
        assertEquals(listOf("{}"), port.restored)
    }

    @Test fun exportOrStageWhileRestoreInProgress_isIgnored() = runTest {
        stage()
        viewModel.onIntent(SettingsIntent.ConfirmRestore)
        runCurrent()
        assertEquals(SettingsUiState(backupBusy = true), state)

        val written = mutableListOf<String>()
        var importRead = false
        viewModel.onIntent(SettingsIntent.ExportBackup { written.add(it) })
        viewModel.onIntent(SettingsIntent.StageImportJson {
            importRead = true
            "{}"
        })
        runCurrent()
        assertTrue(written.isEmpty())
        assertFalse(importRead)
        assertEquals(SettingsUiState(backupBusy = true), state)

        advanceUntilIdle()
        assertEquals(0, port.exportCount)
        assertTrue(written.isEmpty())
        assertFalse(importRead)
        assertEquals(listOf("{}"), port.validated)
        assertEquals(SettingsUiState("Đã khôi phục dữ liệu từ bản sao lưu."), state)
    }

    @Test fun confirmRestore_failure_showsFailureAndDoesNotEmitReloadEffect() = runTest {
        port.restoreFails = true
        stage()
        dispatch(SettingsIntent.ConfirmRestore)
        assertEquals(SettingsUiState("Chưa thể khôi phục dữ liệu. Dữ liệu hiện tại vẫn được giữ nguyên."), state)
        assertNull(withTimeoutOrNull(1) { viewModel.effects.first() })
    }

    @Test fun confirmRestore_success_updatesStatusAndEmitsBackupRestoredEffect() = runTest {
        stage()
        dispatch(SettingsIntent.ConfirmRestore)
        assertEquals(SettingsUiState("Đã khôi phục dữ liệu từ bản sao lưu."), state)
        assertEquals(SettingsEffect.BackupRestored, withTimeoutOrNull(1) { viewModel.effects.first() })
    }

    @Test fun exportBackup_successAndFailure() = runTest {
        val written = mutableListOf<String>()
        dispatch(SettingsIntent.ExportBackup { written.add(it) })
        assertEquals(listOf("{}"), written)
        assertEquals(SettingsBackupCopy.exportSuccess, state.backupStatus)
        dispatch(SettingsIntent.ExportBackup { error("write failed") })
        assertEquals("Chưa thể lưu bản sao lưu. Hãy thử chọn vị trí khác.", state.backupStatus)
    }

    private fun dispatch(intent: SettingsIntent) {
        viewModel.onIntent(intent)
        dispatcher.scheduler.advanceUntilIdle()
    }
    private fun stage(json: String = "{}") = dispatch(SettingsIntent.StageImportJson { json })
    private class FakeSettingsBackupPort : SettingsBackupPort {
        val validated = mutableListOf<String>()
        val restored = mutableListOf<String>()
        var exportCount = 0
        var restoreFails = false
        override suspend fun exportJson(): String {
            exportCount++
            return "{}"
        }
        override fun validateJson(json: String) {
            validated.add(json)
            require(json == "{}")
        }
        override suspend fun restoreJson(json: String) {
            restored.add(json)
            delay(10)
            check(!restoreFails)
        }
    }
}
