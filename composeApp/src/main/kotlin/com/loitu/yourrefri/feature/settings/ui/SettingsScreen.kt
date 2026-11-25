package com.loitu.yourrefri.feature.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import com.loitu.yourrefri.ui.theme.ThemeMode
import kotlinx.datetime.LocalTime

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    reminderEnabled: Boolean = false,
    onReminderChanged: (Boolean) -> Unit = {},
    onTestReminder: () -> Unit = {},
    backupStatus: String? = null,
    restoreConfirmationPending: Boolean = false,
    onExportBackup: () -> Unit = {},
    onImportBackup: () -> Unit = {},
    onConfirmRestore: () -> Unit = {},
    onCancelRestore: () -> Unit = {},
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    weeklyGoal: DietGoal = DietGoal.BALANCED,
    onWeeklyGoalChange: (DietGoal) -> Unit = {},
    weeklyPreferences: Set<FamilyPreferenceTag> = emptySet(),
    onWeeklyPreferencesChange: (Set<FamilyPreferenceTag>) -> Unit = {},
    dietRestriction: DietRestriction = DietRestriction.NONE,
    onDietRestrictionChange: (DietRestriction) -> Unit = {},
    appVersionName: String = "",
    sundayTime: LocalTime = LocalTime(8, 0),
    onSundayTimeChange: (LocalTime) -> Unit = {},
    tasteProfile: FamilyTasteProfile = FamilyTasteProfile(),
    onTasteProfileChange: (FamilyTasteProfile) -> Unit = {},
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    if (restoreConfirmationPending) {
        AlertDialog(
            onDismissRequest = onCancelRestore,
            containerColor = colors.surface,
            title = { Text(SettingsBackupCopy.restoreTitle, color = colors.text) },
            text = { Text(SettingsBackupCopy.restoreWarning, color = colors.muted) },
            confirmButton = {
                Button(onClick = onConfirmRestore, modifier = Modifier.heightIn(min = 48.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta)) {
                    Text(SettingsBackupCopy.restoreLabel, color = colors.onCta)
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelRestore, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(SettingsBackupCopy.cancelRestoreLabel, color = colors.ctaText)
                }
            },
        )
    }

    var requestedSheet by rememberSaveable { mutableStateOf<SettingsSheetType?>(null) }
    LaunchedEffect(restoreConfirmationPending) {
        requestedSheet = nextRequestedSheet(requestedSheet, restoreConfirmationPending)
    }
    val activeSheet = resolveActiveSheet(requestedSheet, restoreConfirmationPending)
    val closeSheet: () -> Unit = { requestedSheet = null }
    if (activeSheet == SettingsSheetType.TASTE) SettingsTasteSheet(
        dietRestriction, onDietRestrictionChange, weeklyPreferences, onWeeklyPreferencesChange,
        weeklyGoal, onWeeklyGoalChange, tasteProfile, onTasteProfileChange, onDismiss = closeSheet,
    )
    if (activeSheet == SettingsSheetType.DATA) SettingsDataSheet(
        themeMode, onThemeModeChange, backupStatus, onExportBackup, onImportBackup,
        onTestReminder, appVersionName, onDismiss = closeSheet,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = SettingsPresentation.bottomPaddingDp.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(SettingsPresentation.title, fontSize = 27.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
        val sundayTimeRow: @Composable () -> Unit = {
            ChipRow("Chủ Nhật nhắc đi chợ", "Chạm để điều chỉnh") {
                ValueChip(
                    text = ReminderCopy.sundayTimeLabel(sundayTime).substringAfter("lúc "),
                    description = "Đổi giờ nhắc đi chợ Chủ Nhật",
                    onClick = {
                        android.app.TimePickerDialog(context, { _, hour, minute -> onSundayTimeChange(LocalTime(hour, minute)) }, sundayTime.hour, sundayTime.minute, true).show()
                    },
                )
            }
        }
        SETTINGS_MAIN_CARDS.forEach { card ->
            SettingsCard {
                when (card) {
                    SettingsMainCard.REMINDER -> {
                        SectionTitle(SettingsPresentation.reminderGroupTitle, AppIcons.Bell)
                        ChipRow(SettingsPresentation.reminderTitle, SettingsPresentation.reminderTime) {
                            ToggleChip(reminderEnabled, SettingsPresentation.reminderTitle, onReminderChanged)
                        }
                        AnimatedVisibility(visible = shouldShowSundayTimeRow(reminderEnabled)) { sundayTimeRow() }
                        Text(ReminderCopy.subtitle, fontSize = 12.sp, color = colors.muted)
                    }
                    SettingsMainCard.TASTE_HABIT -> SettingsNavRow(
                        SettingsPresentation.tasteGroupTitle,
                        settingsTasteHabitSummary(dietRestriction, weeklyPreferences, weeklyGoal, tasteProfile),
                        AppIcons.Heart,
                    ) { requestedSheet = SettingsSheetType.TASTE }
                    SettingsMainCard.DATA_APP -> SettingsNavRow(
                        SETTINGS_DATA_TITLE, settingsDataSummary(themeMode, backupStatus), AppIcons.Save,
                    ) { requestedSheet = SettingsSheetType.DATA }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().background(colors.primarySoft, RoundedCornerShape(20.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(AppIcons.Lock, null, tint = colors.accentText, modifier = Modifier.size(16.dp))
                Text(SettingsPresentation.onDeviceBadge, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
            }
            Text(SettingsPresentation.onDeviceLine, fontSize = 14.sp, color = colors.text)
        }
    }
}
