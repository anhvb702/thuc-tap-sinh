package com.loitu.yourrefri

import android.os.Bundle
import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.loitu.yourrefri.feature.backup.data.BackupCodec
import com.loitu.yourrefri.feature.backup.data.PantryBackupRepositoryImpl
import com.loitu.yourrefri.feature.pantry.data.PantryRepositoryImpl
import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.ShelfTable
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.RecipeBundle
import com.loitu.yourrefri.feature.pantry.ui.PantryGridViewModel
import com.loitu.yourrefri.feature.settings.ui.SettingsBackupCopy
import com.loitu.yourrefri.shared.db.DriverFactory
import com.loitu.yourrefri.shared.db.createDatabase
import com.loitu.yourrefri.ui.MainScaffold
import com.loitu.yourrefri.ui.theme.YourRefriTheme
import com.loitu.yourrefri.reminder.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YourRefriTheme {
                App()
            }
        }
    }
}

private data class AppDependencies(
    val pantryRepository: PantryRepositoryImpl,
    val ingredients: List<Ingredient>,
    val backupRepository: PantryBackupRepositoryImpl,
    val recipes: List<Recipe>,
)

@OptIn(ExperimentalTime::class)
@Composable
private fun App() {
    val context = LocalContext.current
    var reminderEnabled by remember { mutableStateOf(ReminderScheduler.enabled(context)) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        reminderEnabled = granted
        ReminderScheduler.setEnabled(context, granted)
    }
    LaunchedEffect(Unit) { ReminderScheduler.schedule(context) }
    val dependencies = remember(context) {
        val driver = DriverFactory(context.applicationContext).create()
        val db = createDatabase(driver)
        val repo = PantryRepositoryImpl(
            db = db,
            clock = Clock.System,
            timeZone = TimeZone.currentSystemDefault(),
            ioContext = Dispatchers.IO,
        )
        val json = context.assets.open("shelf_table.json").bufferedReader().use { it.readText() }
        val parsedIngredients = ShelfTable.parse(json)
        val recipeJson = context.assets.open("recipes.json").bufferedReader().use { it.readText() }
        AppDependencies(
            pantryRepository = repo,
            ingredients = parsedIngredients,
            backupRepository = PantryBackupRepositoryImpl(db, Dispatchers.IO),
            recipes = RecipeBundle.parse(recipeJson),
        )
    }

    val pantryRepository = dependencies.pantryRepository
    val ingredients = dependencies.ingredients
    val backupRepository = dependencies.backupRepository
    val viewModel = remember(pantryRepository, ingredients) {
        PantryGridViewModel(
            repository = pantryRepository,
            ingredients = ingredients,
            clock = Clock.System,
            timeZone = TimeZone.currentSystemDefault(),
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh(TimeZone.currentSystemDefault())
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var backupStatus by remember { mutableStateOf<String?>(null) }
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }

    val exportPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                backupStatus = try {
                    withContext(Dispatchers.IO) {
                        val json = backupRepository.exportJson()
                        val output = context.contentResolver.openOutputStream(uri)
                            ?: error("Không mở được file để ghi")
                        output.bufferedWriter(Charsets.UTF_8).use { it.write(json) }
                    }
                    SettingsBackupCopy.exportSuccess
                } catch (_: Exception) {
                    "Chưa thể lưu bản sao lưu. Hãy thử chọn vị trí khác."
                }
            }
        }
    }

    val importPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)
                            ?.use { it.readText() } ?: error("Không mở được file")
                    }
                    BackupCodec.decode(json)
                    pendingRestoreJson = json
                    backupStatus = null
                } catch (_: Exception) {
                    backupStatus = SettingsBackupCopy.restoreFailure
                }
            }
        }
    }

    MainScaffold(
        state = state,
        recipes = dependencies.recipes,
        onAction = viewModel::dispatch,
        reminderEnabled = reminderEnabled,
        onReminderChanged = { enabled ->
            if (!enabled) {
                reminderEnabled = false
                ReminderScheduler.setEnabled(context, false)
            } else if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                reminderEnabled = true
                ReminderScheduler.setEnabled(context, true)
            }
        },
        backupStatus = backupStatus,
        restoreConfirmationPending = pendingRestoreJson != null,
        onExportBackup = {
            backupStatus = null
            exportPicker.launch("yourrefri_backup.json")
        },
        onImportBackup = {
            backupStatus = null
            importPicker.launch(arrayOf("application/json", "text/json"))
        },
        onConfirmRestore = {
            val json = pendingRestoreJson ?: return@MainScaffold
            pendingRestoreJson = null
            scope.launch {
                backupStatus = try {
                    withContext(Dispatchers.IO) { backupRepository.restoreJson(json) }
                    "Đã khôi phục dữ liệu từ bản sao lưu."
                } catch (_: Exception) {
                    "Chưa thể khôi phục dữ liệu. Dữ liệu hiện tại vẫn được giữ nguyên."
                }
            }
        },
        onCancelRestore = { pendingRestoreJson = null },
    )
}
