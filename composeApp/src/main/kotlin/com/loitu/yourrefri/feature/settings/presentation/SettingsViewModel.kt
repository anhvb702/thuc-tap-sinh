package com.loitu.yourrefri.feature.settings.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.loitu.yourrefri.core.mvi.MviViewModel
import com.loitu.yourrefri.feature.backup.data.BackupCodec
import com.loitu.yourrefri.feature.backup.data.PantryBackupRepositoryImpl
import com.loitu.yourrefri.feature.notice.logic.DEFAULT_WEEKDAY_REMINDER_TIME
import com.loitu.yourrefri.feature.notice.logic.WeekendReminderDay
import com.loitu.yourrefri.feature.notice.logic.clampWeekdayReminderTime
import com.loitu.yourrefri.feature.recipe.data.TasteProfileRepository
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.settings.ui.DietPrefs
import com.loitu.yourrefri.feature.settings.ui.SettingsBackupCopy
import com.loitu.yourrefri.reminder.ReminderScheduler
import com.loitu.yourrefri.ui.theme.ThemeMode
import com.loitu.yourrefri.ui.theme.ThemePrefs
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.LocalTime

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

interface SettingsPreferencesPort {
    fun readReminderEnabled(): Boolean = false
    fun setReminderEnabled(enabled: Boolean) = Unit
    fun readSundayTime(): LocalTime = LocalTime(8, 0)
    fun setSundayTime(time: LocalTime) = Unit
    fun weekdayTime(): LocalTime = DEFAULT_WEEKDAY_REMINDER_TIME
    fun setWeekdayTime(time: LocalTime) {}
    fun readWeekendDay(): WeekendReminderDay = WeekendReminderDay.SUNDAY
    fun setWeekendDay(day: WeekendReminderDay) = Unit
    fun readWeekdayEnabled(): Boolean = false
    fun setWeekdayEnabled(enabled: Boolean) = Unit
    fun readDietRestriction(): DietRestriction = DietRestriction.NONE
    fun setDietRestriction(restriction: DietRestriction) = Unit
    fun readThemeMode(): ThemeMode = ThemeMode.SYSTEM
    fun setThemeMode(mode: ThemeMode) = Unit
    fun observeTasteProfile(): Flow<FamilyTasteProfile> = emptyFlow()
    suspend fun saveTasteProfile(profile: FamilyTasteProfile) = Unit
}
private object NoOpSettingsPreferencesPort : SettingsPreferencesPort

class DefaultSettingsPreferencesPort(private val context: Context, private val tasteRepo: TasteProfileRepository) : SettingsPreferencesPort {
    override fun readReminderEnabled() = ReminderScheduler.enabled(context)
    override fun setReminderEnabled(enabled: Boolean) = ReminderScheduler.setEnabled(context, enabled)
    override fun readSundayTime() = ReminderScheduler.sundayTime(context)
    override fun setSundayTime(time: LocalTime) = ReminderScheduler.setSundayTime(context, time)
    override fun weekdayTime() = ReminderScheduler.weekdayTime(context)
    override fun setWeekdayTime(time: LocalTime) = ReminderScheduler.setWeekdayTime(context, time)
    override fun readWeekendDay() = ReminderScheduler.weekendDay(context)
    override fun setWeekendDay(day: WeekendReminderDay) = ReminderScheduler.setWeekendDay(context, day)
    override fun readWeekdayEnabled() = ReminderScheduler.weekdayEnabled(context)
    override fun setWeekdayEnabled(enabled: Boolean) = ReminderScheduler.setWeekdayEnabled(context, enabled)
    override fun readDietRestriction() = DietPrefs.read(context)
    override fun setDietRestriction(restriction: DietRestriction) = DietPrefs.write(context, restriction)
    override fun readThemeMode() = ThemePrefs.read(context)
    override fun setThemeMode(mode: ThemeMode) = ThemePrefs.write(context, mode)
    override fun observeTasteProfile() = tasteRepo.observe()
    override suspend fun saveTasteProfile(profile: FamilyTasteProfile) = tasteRepo.save(profile)
}

class SettingsViewModel(
    private val backupPort: SettingsBackupPort,
    private val preferencesPort: SettingsPreferencesPort = NoOpSettingsPreferencesPort,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : MviViewModel<SettingsUiState, SettingsIntent, SettingsEffect>(
    SettingsUiState(
        reminderEnabled = preferencesPort.readReminderEnabled(),
        sundayTime = preferencesPort.readSundayTime(),
        weekdayTime = preferencesPort.weekdayTime(),
        weekendDay = preferencesPort.readWeekendDay(),
        weekdayEnabled = preferencesPort.readWeekdayEnabled(),
        dietRestriction = preferencesPort.readDietRestriction(),
        themeMode = preferencesPort.readThemeMode(),
    ),
) {
    constructor(backupPort: SettingsBackupPort, dispatcher: CoroutineDispatcher) :
        this(backupPort, NoOpSettingsPreferencesPort, dispatcher)

    private var pendingPermissionReason: NotificationPermissionReason? = null
    private val tasteMutex = Mutex()
    private var latestPendingTaste: FamilyTasteProfile? = null
    private var awaitingTaste: FamilyTasteProfile? = null
    private var persistedTaste = FamilyTasteProfile()
    private var pendingRestoreJson: String? = null
    private var operationInProgress = false

    init {
        viewModelScope.launch {
            preferencesPort.observeTasteProfile().collect { profile ->
                if (profile == awaitingTaste && latestPendingTaste == null) awaitingTaste = null
                if (!tasteMutex.isLocked && awaitingTaste == null) {
                    persistedTaste = profile
                    updateState { copy(tasteProfile = profile) }
                }
            }
        }
    }

    override fun onIntent(intent: SettingsIntent) {
        viewModelScope.launch {
            when (intent) {
                is SettingsIntent.RequestReminderChange -> if (!intent.enabled || intent.hasPermission) {
                    setReminderEnabled(intent.enabled)
                } else requestPermission(NotificationPermissionReason.ENABLE_DAILY_REMINDER)
                is SettingsIntent.RequestTestReminder -> if (intent.hasPermission) {
                    sendEffect(SettingsEffect.TriggerTestReminder)
                } else requestPermission(NotificationPermissionReason.TEST_REMINDER)
                is SettingsIntent.NotificationPermissionResult -> {
                    val reason = pendingPermissionReason
                    pendingPermissionReason = null
                    when (reason) {
                        NotificationPermissionReason.ENABLE_DAILY_REMINDER -> setReminderEnabled(intent.granted)
                        NotificationPermissionReason.TEST_REMINDER -> if (intent.granted) sendEffect(SettingsEffect.TriggerTestReminder)
                        null -> Unit
                    }
                }
                is SettingsIntent.SetSundayTime -> preferencesPort.setSundayTime(intent.time).let {
                    updateState { copy(sundayTime = intent.time) }
                }
                is SettingsIntent.SetWeekdayTime -> {
                    val clamped = clampWeekdayReminderTime(intent.time)
                    preferencesPort.setWeekdayTime(clamped)
                    updateState { copy(weekdayTime = clamped) }
                }
                is SettingsIntent.SetWeekendDay -> {
                    preferencesPort.setWeekendDay(intent.day)
                    updateState { copy(weekendDay = intent.day) }
                }
                is SettingsIntent.SetWeekdayEnabled -> {
                    preferencesPort.setWeekdayEnabled(intent.enabled)
                    updateState { copy(weekdayEnabled = intent.enabled) }
                }
                is SettingsIntent.SetDietRestriction -> preferencesPort.setDietRestriction(intent.restriction).let {
                    updateState { copy(dietRestriction = intent.restriction) }
                }
                is SettingsIntent.SetThemeMode -> preferencesPort.setThemeMode(intent.mode).let {
                    updateState { copy(themeMode = intent.mode) }
                }
                is SettingsIntent.SetTasteProfile -> saveTasteProfile(intent.profile)
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
    private fun requestPermission(reason: NotificationPermissionReason) {
        pendingPermissionReason = reason
        sendEffect(SettingsEffect.RequestNotificationPermission)
    }

    private fun setReminderEnabled(enabled: Boolean) {
        preferencesPort.setReminderEnabled(enabled)
        updateState { copy(reminderEnabled = enabled) }
    }

    private suspend fun saveTasteProfile(profile: FamilyTasteProfile) {
        awaitingTaste = profile
        latestPendingTaste = profile
        updateState { copy(tasteProfile = profile) }
        if (!tasteMutex.tryLock()) return
        try {
            while (true) {
                val next = latestPendingTaste ?: break
                latestPendingTaste = null
                if (backupResult { preferencesPort.saveTasteProfile(next) } != null) persistedTaste = next
                else if (latestPendingTaste == null) {
                    awaitingTaste = null
                    updateState { copy(tasteProfile = persistedTaste) }
                }
            }
        } finally {
            tasteMutex.unlock()
        }
    }

    private suspend fun <T : Any> backupResult(operation: suspend () -> T): T? = try {
        withContext(ioDispatcher) { operation() }
    } catch (exception: Exception) {
        if (exception is CancellationException) throw exception
        null
    }
}
