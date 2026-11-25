package com.loitu.yourrefri.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
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
import com.loitu.yourrefri.ui.theme.ThemeModeCopy
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalLayoutApi::class)
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

    var openPicker by rememberSaveable { mutableStateOf<String?>(null) }
    when (openPicker) {
        PICKER_TASTE -> PickerDialog(SettingsPresentation.tasteTitle, { openPicker = null }) {
            FamilyPreferenceTag.entries.forEach { tag ->
                val checked = tag in weeklyPreferences
                ChoiceRow(tag.labelVi, checked, Role.Checkbox, { onWeeklyPreferencesChange(SettingsPresentation.togglePreference(weeklyPreferences, tag)) }) {
                    Checkbox(checked = checked, onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = colors.primary, checkmarkColor = colors.onPrimary, uncheckedColor = colors.muted))
                }
            }
        }
        PICKER_GOAL -> PickerDialog(SettingsPresentation.goalTitle, { openPicker = null }) {
            DietGoal.entries.forEach { goal ->
                ChoiceRow(goal.labelVi, goal == weeklyGoal, Role.RadioButton, { onWeeklyGoalChange(goal) }) { ChoiceRadio(goal == weeklyGoal) }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = SettingsPresentation.bottomPaddingDp.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column {
            Text("YOURREFRI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.accentText)
            Text(SettingsPresentation.title, fontSize = 27.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
        }

        SectionTitle(SettingsPresentation.reminderGroupTitle, AppIcons.Bell)
        SettingsCard {
            ChipRow(SettingsPresentation.reminderTitle, SettingsPresentation.reminderTime) {
                ToggleChip(reminderEnabled, SettingsPresentation.reminderTitle, onReminderChanged)
            }
            ChipRow("Chủ Nhật nhắc đi chợ", "Chạm để điều chỉnh") {
                ValueChip(
                    text = ReminderCopy.sundayTimeLabel(sundayTime).substringAfter("lúc "),
                    description = "Đổi giờ nhắc đi chợ Chủ Nhật",
                    onClick = {
                        android.app.TimePickerDialog(context, { _, hour, minute -> onSundayTimeChange(LocalTime(hour, minute)) }, sundayTime.hour, sundayTime.minute, true).show()
                    },
                )
            }
            Text(ReminderCopy.subtitle, fontSize = 12.sp, color = colors.muted)
            SecondaryButton("Thử thông báo ngay", "Thử thông báo ngay", onTestReminder)
        }

        SectionTitle(SettingsPresentation.tasteGroupTitle, AppIcons.Heart)
        SettingsCard {
            Text(SettingsPresentation.dietTitle, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.text)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DietRestriction.entries.forEach { option ->
                    SelectChip(option.labelVi, option == dietRestriction, { onDietRestrictionChange(option) })
                }
            }
            Text(dietRestriction.hintVi, fontSize = 12.sp, color = colors.muted)
            ChipRow(SettingsPresentation.tasteTitle) {
                ValueChip(SettingsPresentation.preferencesSummary(weeklyPreferences), SettingsPresentation.tasteTitle, { openPicker = PICKER_TASTE }, dropdown = true)
            }
            ChipRow(SettingsPresentation.goalTitle) {
                ValueChip(SettingsPresentation.goalSummary(weeklyGoal), SettingsPresentation.goalTitle, { openPicker = PICKER_GOAL }, dropdown = true)
            }
        }
        FamilyTasteSettingsCard(tasteProfile, onTasteProfileChange)

        SectionTitle(ThemeModeCopy.title, AppIcons.Moon)
        SettingsCard {
            Text(ThemeModeCopy.subtitle, fontSize = 12.sp, color = colors.muted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsPresentation.themeModeOrder.forEach { mode ->
                    SelectChip(mode.label, mode == themeMode, { onThemeModeChange(mode) }, Modifier.weight(1f))
                }
            }
        }

        SectionTitle(SettingsPresentation.backupGroupTitle, AppIcons.Save)
        SettingsCard {
            Text(SettingsBackupCopy.backupContents, fontSize = 14.sp, color = colors.muted)
            SecondaryButton(SettingsBackupCopy.exportLabel, "${SettingsPresentation.exportTitle}. ${SettingsPresentation.exportSubtitle}", onExportBackup, AppIcons.Save)
            SecondaryButton(SettingsBackupCopy.importLabel, "${SettingsPresentation.importTitle}. ${SettingsPresentation.importSubtitle}", onImportBackup, AppIcons.Load)
            backupStatus?.let { Text(it, fontSize = 14.sp, color = if (it.startsWith("Chưa thể")) colors.warnText else colors.accentText) }
            Text(SettingsPresentation.transferTitle, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
            SettingsBackupCopy.transferSteps.forEachIndexed { index, step -> Text("${index + 1}. $step", fontSize = 14.sp, color = colors.muted) }
        }

        Column(
            modifier = Modifier.fillMaxWidth().background(colors.primarySoft, RoundedCornerShape(20.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("YourRefri", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = colors.accentText)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(AppIcons.Lock, null, tint = colors.accentText, modifier = Modifier.size(16.dp))
                Text(SettingsPresentation.onDeviceBadge, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
            }
            Text(SettingsPresentation.onDeviceLine, fontSize = 14.sp, color = colors.text)
            Text(SettingsBackupCopy.privacy, fontSize = 12.sp, color = colors.muted)
            SettingsPresentation.versionLine(appVersionName)?.let { Text(it, fontSize = 12.sp, color = colors.muted) }
        }
    }
}

private const val PICKER_TASTE = "taste"
private const val PICKER_GOAL = "goal"

@Composable
private fun PickerDialog(title: String, onDone: () -> Unit, content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    AlertDialog(
        onDismissRequest = onDone,
        containerColor = colors.surface,
        title = { Text(title, color = colors.text) },
        text = { Column(modifier = Modifier.verticalScroll(rememberScrollState())) { content() } },
        confirmButton = {
            Button(onClick = onDone, modifier = Modifier.heightIn(min = 48.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta)) {
                Text(SettingsPresentation.doneLabel, color = colors.onCta)
            }
        },
    )
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, role: Role, onClick: () -> Unit, hint: String? = null, control: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(selected = selected, role = role, onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        control()
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = AppTheme.colors.text)
            if (hint != null) Text(hint, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.muted)
        }
    }
}

@Composable
private fun ChoiceRadio(selected: Boolean) {
    val colors = AppTheme.colors
    RadioButton(selected = selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = colors.primary, unselectedColor = colors.muted))
}
