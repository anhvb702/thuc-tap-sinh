package com.loitu.yourrefri

import android.os.Bundle
import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.loitu.yourrefri.feature.settings.presentation.SettingsEffect
import com.loitu.yourrefri.feature.settings.presentation.SettingsIntent
import com.loitu.yourrefri.feature.settings.presentation.SettingsUiState
import com.loitu.yourrefri.feature.settings.presentation.SettingsViewModel
import com.loitu.yourrefri.feature.settings.ui.LocalWeekdayReminderSettings
import com.loitu.yourrefri.feature.settings.ui.WeekdayReminderSettings
import com.loitu.yourrefri.feature.recipe.ui.FavoriteRecipesAction
import com.loitu.yourrefri.feature.recipe.ui.FavoriteRecipesEffect
import com.loitu.yourrefri.feature.recipe.ui.FavoriteRecipesViewModel
import com.loitu.yourrefri.feature.recipe.ui.UnlockedRecipesViewModel
import com.loitu.yourrefri.feature.recipe.ui.WeeklyMealPlanViewModel
import com.loitu.yourrefri.feature.recipe.ui.WeeklyMealPlanEffect
import com.loitu.yourrefri.di.AppGraph
import com.loitu.yourrefri.di.AppContainer
import com.loitu.yourrefri.shared.db.DatabaseTooNewException
import com.loitu.yourrefri.telemetry.TelemetryEvent
import com.loitu.yourrefri.ui.DatabaseTooNewNotice
import com.loitu.yourrefri.ui.MainScaffold
import com.loitu.yourrefri.ui.MainScaffoldCollectionBinding
import com.loitu.yourrefri.ui.MainScaffoldSettingsBinding
import com.loitu.yourrefri.ui.theme.ThemePrefs
import com.loitu.yourrefri.ui.theme.YourRefriTheme
import com.loitu.yourrefri.reminder.ReminderIntents
import com.loitu.yourrefri.reminder.ReminderOpenRequest
import com.loitu.yourrefri.reminder.ReminderReceiver
import com.loitu.yourrefri.reminder.ReminderScheduler
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class MainActivity : ComponentActivity() {
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        ReminderOpenRequest.request(intent.getStringExtra(ReminderIntents.EXTRA_RECIPE_ID))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Chỉ đọc intent khi tạo mới: xoay màn hình không mở lại món đã mở.
        if (savedInstanceState == null) ReminderOpenRequest.request(intent?.getStringExtra(ReminderIntents.EXTRA_RECIPE_ID))
        // DB mới hơn app: hiện thông báo cập nhật thay vì crash; dữ liệu không bị đụng tới.
        val appContainer = try { AppGraph.get(this) } catch (_: DatabaseTooNewException) { null }
        if (savedInstanceState == null) {
            appContainer?.telemetry?.track(TelemetryEvent.ScreenViewed("app_launch"))
        }
        setContent {
            val context = LocalContext.current
            if (appContainer == null) {
                YourRefriTheme(darkTheme = ThemePrefs.read(context).isDark(isSystemInDarkTheme())) {
                    DatabaseTooNewNotice()
                }
            } else {
                val settingsViewModel: SettingsViewModel = viewModel(factory = remember(appContainer) { appContainer.settingsViewModelFactory() })
                val settingsState by settingsViewModel.state.collectAsState()
                YourRefriTheme(darkTheme = settingsState.themeMode.isDark(isSystemInDarkTheme())) {
                    CompositionLocalProvider(
                        LocalWeekdayReminderSettings provides WeekdayReminderSettings(
                            weekdayTime = settingsState.weekdayTime,
                            onWeekdayTimeChange = { settingsViewModel.onIntent(SettingsIntent.SetWeekdayTime(it)) },
                        ),
                    ) {
                        App(appContainer, settingsViewModel, settingsState)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun App(dependencies: AppContainer, settingsViewModel: SettingsViewModel, settingsState: SettingsUiState) {
    val context = LocalContext.current
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        settingsViewModel.onIntent(SettingsIntent.NotificationPermissionResult(granted))
    }
    fun hasNotificationPermission() = Build.VERSION.SDK_INT < 33 ||
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    LaunchedEffect(Unit) { ReminderScheduler.schedule(context) }

    val viewModel: com.loitu.yourrefri.feature.pantry.ui.PantryGridViewModel = viewModel(
        factory = remember(dependencies) { dependencies.pantryGridViewModelFactory() },
    )
    val homeViewModel: com.loitu.yourrefri.feature.home.presentation.HomeViewModel = viewModel(
        factory = remember(dependencies) { dependencies.homeViewModelFactory() },
    )
    val weeklyMealPlanViewModel: WeeklyMealPlanViewModel = viewModel(
        factory = remember(dependencies) { dependencies.weeklyMealPlanViewModelFactory() },
    )
    LaunchedEffect(weeklyMealPlanViewModel, settingsState.tasteProfile) {
        weeklyMealPlanViewModel.updateTasteProfile(settingsState.tasteProfile)
    }

    val favoriteViewModel: FavoriteRecipesViewModel = viewModel(
        factory = remember(dependencies) { dependencies.favoriteRecipesViewModelFactory() },
    )
    val favoriteState by favoriteViewModel.state.collectAsState()
    val unlockedRecipesVm: UnlockedRecipesViewModel = viewModel(
        factory = remember(dependencies) { dependencies.unlockedRecipesViewModelFactory() },
    )
    val unlockedIds by unlockedRecipesVm.unlockedIds.collectAsState()
    val unlocksLoaded by unlockedRecipesVm.loaded.collectAsState()
    val mysteryUnlockFailedId by unlockedRecipesVm.failedId.collectAsState()
    val unlocksLoadFailed by unlockedRecipesVm.loadFailed.collectAsState()
    val cookHistoryViewModel: com.loitu.yourrefri.feature.cooklog.ui.CookHistoryViewModel = viewModel(
        factory = remember(dependencies) { dependencies.cookHistoryViewModelFactory() },
    )
    val cookHistoryState by cookHistoryViewModel.state.collectAsState()
    val appVersionName = remember(context) {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        } catch (_: Exception) {
            ""
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh(TimeZone.currentSystemDefault())
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    DisposableEffect(lifecycleOwner, weeklyMealPlanViewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                weeklyMealPlanViewModel.refresh(TimeZone.currentSystemDefault())
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val state by viewModel.state.collectAsState()
    val weeklyMealPlanState by weeklyMealPlanViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(unlocksLoadFailed) {
        if (unlocksLoadFailed) snackbarHostState.showSnackbar("Chưa đọc được các món đã mở khóa.")
    }
    LaunchedEffect(weeklyMealPlanViewModel) {
        weeklyMealPlanViewModel.effects.collect { effect ->
            val message = when (effect) {
                WeeklyMealPlanEffect.SettingsSaveFailed -> "Chưa lưu được thực đơn tuần."
                WeeklyMealPlanEffect.SettingsLoadFailed -> "Chưa đọc được thực đơn tuần."
            }
            snackbarHostState.showSnackbar(message)
        }
    }
    LaunchedEffect(favoriteViewModel) {
        favoriteViewModel.effects.collect { effect ->
            val message = when (effect) {
                FavoriteRecipesEffect.SaveFailed -> "Chưa lưu được món yêu thích."
                FavoriteRecipesEffect.LoadFailed -> "Chưa đọc được món yêu thích."
            }
            snackbarHostState.showSnackbar(message)
        }
    }
    LaunchedEffect(settingsViewModel) {
        settingsViewModel.effects.collect { effect ->
            when (effect) {
                SettingsEffect.BackupRestored -> {
                    weeklyMealPlanViewModel.reloadSettings()
                    favoriteViewModel.reload()
                    cookHistoryViewModel.reload()
                    unlockedRecipesVm.reload()
                }
                SettingsEffect.RequestNotificationPermission -> notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                SettingsEffect.TriggerTestReminder -> context.sendBroadcast(
                    android.content.Intent(context, ReminderReceiver::class.java).setAction(ReminderIntents.ACTION_TEST),
                )
            }
        }
    }

    val contentResolver = context.applicationContext.contentResolver
    val exportPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            settingsViewModel.onIntent(SettingsIntent.ExportBackup { json ->
                val output = contentResolver.openOutputStream(uri) ?: error("Không mở được file để ghi")
                output.bufferedWriter(Charsets.UTF_8).use { it.write(json) }
            })
        }
    }

    val importPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            settingsViewModel.onIntent(SettingsIntent.StageImportJson {
                contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() } ?: error("Không mở được file")
            })
        }
    }

    MainScaffold(
        homeViewModel = homeViewModel,
        state = state,
        weeklyMealPlanState = weeklyMealPlanState,
        onWeeklyMealPlanAction = weeklyMealPlanViewModel::dispatch,
        snackbarHostState = snackbarHostState,
        recipes = dependencies.recipes,
        onAction = viewModel::dispatch,
        currentHour = { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour },
        settings = MainScaffoldSettingsBinding(
            uiState = settingsState,
            appVersionName = appVersionName,
            onExportBackup = {
                settingsViewModel.onIntent(SettingsIntent.ClearBackupStatus)
                exportPicker.launch("yourrefri_backup.json")
            },
            onImportBackup = {
                settingsViewModel.onIntent(SettingsIntent.ClearBackupStatus)
                importPicker.launch(arrayOf("application/json", "text/json"))
            },
            onConfirmRestore = { settingsViewModel.onIntent(SettingsIntent.ConfirmRestore) },
            onCancelRestore = { settingsViewModel.onIntent(SettingsIntent.CancelRestore) },
            onReminderChanged = { settingsViewModel.onIntent(SettingsIntent.RequestReminderChange(it, hasNotificationPermission())) },
            onTestReminder = { settingsViewModel.onIntent(SettingsIntent.RequestTestReminder(hasNotificationPermission())) },
            onThemeModeChange = { settingsViewModel.onIntent(SettingsIntent.SetThemeMode(it)) },
            onDietRestrictionChange = { settingsViewModel.onIntent(SettingsIntent.SetDietRestriction(it)) },
            onSundayTimeChange = { settingsViewModel.onIntent(SettingsIntent.SetSundayTime(it)) },
            onWeekdayTimeChange = { settingsViewModel.onIntent(SettingsIntent.SetWeekdayTime(it)) },
            onWeekendDayChange = { settingsViewModel.onIntent(SettingsIntent.SetWeekendDay(it)) },
            onWeekdayEnabledChange = { settingsViewModel.onIntent(SettingsIntent.SetWeekdayEnabled(it)) },
            onTasteProfileChange = { settingsViewModel.onIntent(SettingsIntent.SetTasteProfile(it)) },
        ),
        collections = MainScaffoldCollectionBinding(
            favoriteIds = favoriteState.ids,
            onToggleFavorite = { favoriteViewModel.dispatch(FavoriteRecipesAction.Toggle(it)) },
            cookHistoryState = cookHistoryState,
            onRecordCook = { recipeId, ids, meal, onResult -> cookHistoryViewModel.record(recipeId, ids, meal, onResult) },
            onDeleteCook = { id -> cookHistoryViewModel.dispatch(com.loitu.yourrefri.feature.cooklog.ui.CookHistoryAction.Delete(id)) },
            unlockedIds = unlockedIds,
            mysteryRewardsReady = unlocksLoaded && cookHistoryState.loaded,
            mysteryUnlockFailedId = mysteryUnlockFailedId,
            onUnlockMysteryRecipe = { unlockedRecipesVm.unlock(it.id) },
        ),
    )
}
