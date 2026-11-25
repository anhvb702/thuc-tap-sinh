package com.loitu.yourrefri.feature.settings.presentation

import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.recipe.logic.TasteFlavor
import com.loitu.yourrefri.feature.settings.ui.SettingsBackupCopy
import com.loitu.yourrefri.feature.settings.ui.weekdayTimeOf
import com.loitu.yourrefri.ui.theme.ThemeMode
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalTime
import kotlin.test.*
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val port = FakeSettingsBackupPort()
    private val viewModel by lazy { SettingsViewModel(port, dispatcher) }
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

    @Test fun permissionDenied_keepsReminderDisabled() = runTest {
        vm.onIntent(SettingsIntent.RequestReminderChange(true, false))
        assertEquals(SettingsEffect.RequestNotificationPermission, nextEffect())
        assertTrue(preferences.reminderWrites.isEmpty())
        vm.onIntent(SettingsIntent.NotificationPermissionResult(false))
        runCurrent()
        assertFalse(vm.state.value.reminderEnabled)
        assertEquals(listOf(false), preferences.reminderWrites)
    }

    @Test fun testReminderPermissionGranted_triggersTestWithoutEnablingDailyReminder() = runTest {
        vm.onIntent(SettingsIntent.RequestTestReminder(false))
        assertEquals(SettingsEffect.RequestNotificationPermission, nextEffect())
        vm.onIntent(SettingsIntent.NotificationPermissionResult(true))
        assertEquals(SettingsEffect.TriggerTestReminder, nextEffect())
        assertFalse(vm.state.value.reminderEnabled)
        assertTrue(preferences.reminderWrites.isEmpty())
    }

    @Test fun setSundayTimeAndDiet_persistsAndUpdatesState() = runTest {
        val time = LocalTime(9, 35)
        vm.onIntent(SettingsIntent.SetSundayTime(time))
        vm.onIntent(SettingsIntent.SetDietRestriction(DietRestriction.NO_SEAFOOD))
        runCurrent()
        assertEquals(time, preferences.readSundayTime())
        assertEquals(time, vm.state.value.sundayTime)
        assertEquals(DietRestriction.NO_SEAFOOD, preferences.readDietRestriction())
        assertEquals(DietRestriction.NO_SEAFOOD, vm.state.value.dietRestriction)
    }

    @Test fun setWeekdayTime_clampsAndPersistsAndUpdatesState() = runTest {
        assertEquals(LocalTime(16, 0), vm.state.value.weekdayTime)

        val cases = listOf(
            LocalTime(20, 30) to LocalTime(20, 0),
            LocalTime(13, 15) to LocalTime(14, 0),
            LocalTime(18, 0) to LocalTime(18, 0),
        )
        cases.forEach { (selected, expected) ->
            vm.onIntent(SettingsIntent.SetWeekdayTime(selected))
            runCurrent()
            assertEquals(expected, vm.state.value.weekdayTime)
        }
        assertEquals(cases.map { it.second }, preferences.weekdayWrites)
        assertEquals(LocalTime(18, 0), preferencesViewModel().state.value.weekdayTime)
    }

    @Test fun weekdayTimeOf_defaultsTo1600AndClampsStoredMinutes() {
        assertEquals(LocalTime(16, 0), weekdayTimeOf(-1))
        assertEquals(LocalTime(16, 0), weekdayTimeOf(1440))
        assertEquals(LocalTime(14, 0), weekdayTimeOf(0))
        assertEquals(LocalTime(14, 0), weekdayTimeOf(13 * 60 + 15))
        assertEquals(LocalTime(18, 25), weekdayTimeOf(18 * 60 + 25))
        assertEquals(LocalTime(20, 0), weekdayTimeOf(20 * 60 + 30))
        assertEquals(LocalTime(20, 0), weekdayTimeOf(1439))
    }

    @Test fun permissionRequestWithoutCallback_canRetryWithLatestReason() = runTest {
        vm.onIntent(SettingsIntent.RequestReminderChange(true, false))
        assertEquals(SettingsEffect.RequestNotificationPermission, nextEffect())
        vm.onIntent(SettingsIntent.RequestTestReminder(false))
        assertEquals(SettingsEffect.RequestNotificationPermission, nextEffect())
        vm.onIntent(SettingsIntent.NotificationPermissionResult(true))
        assertEquals(SettingsEffect.TriggerTestReminder, nextEffect())
        assertFalse(vm.state.value.reminderEnabled)
        assertTrue(preferences.reminderWrites.isEmpty())
    }

    @Test fun setTasteProfile_rapidTaps_savesSequentiallyWithLatestWinning() = runTest {
        val first = FamilyTasteProfile(hasYoungKids = true)
        val latest = FamilyTasteProfile(flavors = setOf(TasteFlavor.LIGHT))
        vm.onIntent(SettingsIntent.SetTasteProfile(first))
        runCurrent()
        vm.onIntent(SettingsIntent.SetTasteProfile(FamilyTasteProfile()))
        vm.onIntent(SettingsIntent.SetTasteProfile(latest))
        runCurrent()
        assertEquals(latest, vm.state.value.tasteProfile)
        assertEquals(listOf(first), preferences.tasteWrites)
        advanceUntilIdle()
        assertEquals(listOf(first, latest), preferences.tasteWrites)
        assertEquals(latest, vm.state.value.tasteProfile)
    }

    @Test fun staleTasteFlowEmission_doesNotOverwriteNewerLocalTasteUpdate() = runTest {
        val latest = FamilyTasteProfile(hasYoungKids = true)
        vm.onIntent(SettingsIntent.SetTasteProfile(latest))
        advanceUntilIdle()
        val stale = FamilyTasteProfile(flavors = setOf(TasteFlavor.SWEET))
        preferences.taste.value = stale
        runCurrent()
        assertEquals(latest, vm.state.value.tasteProfile)
        preferences.taste.value = latest
        runCurrent()
        preferences.taste.value = stale
        runCurrent()
        assertEquals(stale, vm.state.value.tasteProfile)
    }

    @Test fun setThemeMode_persistsAndUpdatesState() = runTest {
        vm.onIntent(SettingsIntent.SetThemeMode(ThemeMode.DARK))
        runCurrent()
        assertEquals(ThemeMode.DARK, preferences.readThemeMode())
        assertEquals(ThemeMode.DARK, vm.state.value.themeMode)
        assertEquals(ThemeMode.DARK, preferencesViewModel().state.value.themeMode)
    }

    private val preferences = FakePreferences()
    private val vm by lazy { preferencesViewModel() }
    private fun preferencesViewModel() = SettingsViewModel(port, preferences, dispatcher)
    private suspend fun nextEffect() = withTimeoutOrNull(1) { vm.effects.first() }

    private class FakePreferences : SettingsPreferencesPort {
        val reminderWrites = mutableListOf<Boolean>()
        val weekdayWrites = mutableListOf<LocalTime>()
        val tasteWrites = mutableListOf<FamilyTasteProfile>()
        val taste = MutableStateFlow(FamilyTasteProfile())
        private var sunday = LocalTime(8, 0)
        private var diet = DietRestriction.NONE
        private var theme = ThemeMode.SYSTEM
        override fun setReminderEnabled(enabled: Boolean) { reminderWrites.add(enabled) }
        override fun readSundayTime() = sunday
        override fun setSundayTime(time: LocalTime) { sunday = time }
        override fun weekdayTime() = weekdayWrites.lastOrNull() ?: LocalTime(16, 0)
        override fun setWeekdayTime(time: LocalTime) { weekdayWrites.add(time) }
        override fun readDietRestriction() = diet
        override fun setDietRestriction(restriction: DietRestriction) { diet = restriction }
        override fun readThemeMode() = theme
        override fun setThemeMode(mode: ThemeMode) { theme = mode }
        override fun observeTasteProfile() = taste
        override suspend fun saveTasteProfile(profile: FamilyTasteProfile) {
            tasteWrites.add(profile)
            delay(10)
        }
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
