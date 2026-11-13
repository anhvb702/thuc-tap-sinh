package com.loitu.yourrefri.feature.settings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import com.loitu.yourrefri.ui.theme.ThemeMode
import com.loitu.yourrefri.ui.theme.ThemeModeCopy
import kotlinx.datetime.LocalTime

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    reminderEnabled: Boolean = false,
    onReminderChanged: (Boolean) -> Unit = {},
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
                Button(
                    onClick = onConfirmRestore,
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta),
                ) { Text(SettingsBackupCopy.restoreLabel, color = colors.onCta) }
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
        PICKER_TASTE -> PickerDialog(title = SettingsPresentation.tasteTitle, onDone = { openPicker = null }) {
            FamilyPreferenceTag.entries.forEach { tag ->
                val checked = tag in weeklyPreferences
                ChoiceRow(
                    label = tag.labelVi,
                    selected = checked,
                    role = Role.Checkbox,
                    onClick = {
                        onWeeklyPreferencesChange(SettingsPresentation.togglePreference(weeklyPreferences, tag))
                    },
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(
                            checkedColor = colors.primary,
                            checkmarkColor = colors.onPrimary,
                            uncheckedColor = colors.muted,
                        ),
                    )
                }
            }
        }
        PICKER_GOAL -> PickerDialog(title = SettingsPresentation.goalTitle, onDone = { openPicker = null }) {
            DietGoal.entries.forEach { goal ->
                ChoiceRow(
                    label = goal.labelVi,
                    selected = goal == weeklyGoal,
                    role = Role.RadioButton,
                    onClick = { onWeeklyGoalChange(goal) },
                ) { ChoiceRadio(goal == weeklyGoal) }
            }
        }
        PICKER_DIET -> PickerDialog(title = SettingsPresentation.dietTitle, onDone = { openPicker = null }) {
            DietRestriction.entries.forEach { option ->
                ChoiceRow(
                    label = option.labelVi,
                    hint = option.hintVi,
                    selected = option == dietRestriction,
                    role = Role.RadioButton,
                    onClick = { onDietRestrictionChange(option) },
                ) { ChoiceRadio(option == dietRestriction) }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = SettingsPresentation.title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
        )

        GroupHeader(AppIcons.Bell, "Thông báo")
        SettingsGroup {
            SettingRow(
                title = SettingsPresentation.reminderTitle,
                subtitle = SettingsPresentation.reminderTime,
                onClick = { onReminderChanged(!reminderEnabled) },
                trailing = {
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = null,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onPrimary,
                            checkedTrackColor = colors.primary,
                            uncheckedThumbColor = colors.muted,
                            uncheckedTrackColor = colors.surfaceSoft,
                            uncheckedBorderColor = colors.line,
                        ),
                    )
                },
            )
            HorizontalDivider(color = colors.line)
            // Chủ Nhật lời nhắc đi chợ thay cho lời nhắc thường, vẫn tối đa 1 lời nhắc mỗi ngày.
            ValueRow(
                title = "Chủ Nhật nhắc đi chợ",
                value = ReminderCopy.sundayTimeLabel(sundayTime).substringAfter("lúc "),
                onClick = {
                    android.app.TimePickerDialog(
                        context,
                        { _, hour, minute -> onSundayTimeChange(LocalTime(hour, minute)) },
                        sundayTime.hour,
                        sundayTime.minute,
                        true,
                    ).show()
                },
            )
            // Chữ giải thích: tối đa 1 lời nhắc mỗi ngày, hạn là ước tính, tắt vẫn thấy đủ trong app.
            Text(
                text = ReminderCopy.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            )
        }

        GroupHeader(AppIcons.Utensils, "Khẩu vị & Thói quen")
        SettingsGroup {
            ValueRow(
                title = SettingsPresentation.tasteTitle,
                value = SettingsPresentation.preferencesSummary(weeklyPreferences),
                onClick = { openPicker = PICKER_TASTE },
            )
            HorizontalDivider(color = colors.line)
            ValueRow(
                title = SettingsPresentation.dietTitle,
                value = dietRestriction.labelVi,
                onClick = { openPicker = PICKER_DIET },
            )
            HorizontalDivider(color = colors.line)
            ValueRow(
                title = SettingsPresentation.goalTitle,
                value = SettingsPresentation.goalSummary(weeklyGoal),
                onClick = { openPicker = PICKER_GOAL },
            )
        }

        GroupHeader(AppIcons.Moon, ThemeModeCopy.title)
        SettingsGroup {
            Text(
                text = ThemeModeCopy.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
            )
            ThemeModeSelector(selected = themeMode, onSelect = onThemeModeChange)
        }

        GroupHeader(AppIcons.Save, "Sao lưu & Khôi phục")
        SettingsGroup {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = SettingsBackupCopy.backupContents,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                )
                SettingsBackupCopy.transferSteps.forEachIndexed { index, step ->
                    Text(
                        text = "${index + 1}. $step",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            HorizontalDivider(color = colors.line)
            SettingRow(
                title = SettingsPresentation.exportTitle,
                subtitle = SettingsPresentation.exportSubtitle,
                description = SettingsBackupCopy.exportLabel,
                onClick = onExportBackup,
                trailing = { RoundIconButton(AppIcons.Save) },
            )
            HorizontalDivider(color = colors.line)
            SettingRow(
                title = SettingsPresentation.importTitle,
                subtitle = SettingsPresentation.importSubtitle,
                description = SettingsBackupCopy.importLabel,
                onClick = onImportBackup,
                trailing = { RoundIconButton(AppIcons.Load) },
            )
            backupStatus?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (it.startsWith("Chưa thể")) colors.warnText else colors.accentText,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
                )
            }
        }

        GroupHeader(AppIcons.Info, "Về YOURREFRI")
        SettingsGroup {
            SettingsPresentation.versionLine(appVersionName)?.let { line ->
                SettingRow(title = line)
                HorizontalDivider(color = colors.line)
            }
            SettingRow(
                title = SettingsPresentation.onDeviceLine,
                subtitle = SettingsBackupCopy.privacy,
            )
        }
    }
}

private const val PICKER_TASTE = "taste"
private const val PICKER_GOAL = "goal"
private const val PICKER_DIET = "diet"

/** Bảng chọn: danh sách lựa chọn và nút "Xong" màu cam đất. */
@Composable
private fun PickerDialog(title: String, onDone: () -> Unit, content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    AlertDialog(
        onDismissRequest = onDone,
        containerColor = colors.surface,
        title = { Text(title, color = colors.text) },
        text = { Column(modifier = Modifier.verticalScroll(rememberScrollState())) { content() } },
        confirmButton = {
            Button(
                onClick = onDone,
                modifier = Modifier.heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta),
            ) { Text(SettingsPresentation.doneLabel, color = colors.onCta) }
        },
    )
}

@Composable
private fun ChoiceRow(
    label: String,
    selected: Boolean,
    role: Role,
    onClick: () -> Unit,
    hint: String? = null,
    control: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = selected, role = role, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        control()
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = AppTheme.colors.text)
            if (hint != null) {
                Text(hint, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.muted)
            }
        }
    }
}

@Composable
private fun ChoiceRadio(selected: Boolean) {
    val colors = AppTheme.colors
    RadioButton(
        selected = selected,
        onClick = null,
        colors = RadioButtonDefaults.colors(selectedColor = colors.primary, unselectedColor = colors.muted),
    )
}

@Composable
private fun GroupHeader(icon: ImageVector, text: String) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(20.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = colors.muted,
        )
    }
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AppTheme.colors.surface,
        border = BorderStroke(1.dp, AppTheme.colors.line),
    ) {
        Column { content() }
    }
}

/** Ba lựa chọn Theo máy | Sáng | Tối; vùng chạm 48dp, nhãn xuống dòng khi chữ lớn. */
@Composable
private fun ThemeModeSelector(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ThemeMode.entries.forEach { mode ->
            val isSelected = mode == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) colors.primary else colors.surfaceSoft)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(mode) })
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = mode.label,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isSelected) colors.onPrimary else colors.text,
                )
            }
        }
    }
}

/** Dòng có giá trị bên phải kèm mũi tên; chạm để mở bảng chọn. */
@Composable
private fun ValueRow(title: String, value: String, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = colors.text,
        )
        Text(
            text = value,
            modifier = Modifier.widthIn(max = 150.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.accentText,
        )
        ChevronIcon()
    }
}

/** Nút tròn có icon nét ở cuối dòng sao lưu; cả dòng đã chạm được nên đây chỉ là dấu hiệu. */
@Composable
private fun RoundIconButton(icon: ImageVector) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(colors.ctaSoft),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = colors.ctaText, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String? = null,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = AppTheme.colors
    val base = Modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)
    Row(
        modifier = (if (onClick != null) base.clickable(role = Role.Button, onClick = onClick) else base)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = colors.text,
            )
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = colors.muted)
            }
            if (description != null) {
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = colors.muted)
            }
        }
        trailing?.invoke()
    }
}

@Composable
private fun ChevronIcon() {
    Icon(AppIcons.Chevron, contentDescription = null, tint = AppTheme.colors.muted, modifier = Modifier.size(20.dp))
}
