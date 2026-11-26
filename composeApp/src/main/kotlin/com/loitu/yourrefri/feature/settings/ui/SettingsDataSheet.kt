package com.loitu.yourrefri.feature.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import com.loitu.yourrefri.ui.theme.ThemeMode
import com.loitu.yourrefri.ui.theme.ThemeModeCopy

internal const val SETTINGS_DATA_TITLE = "Dữ liệu & ứng dụng"
internal const val SETTINGS_EXPORT_LABEL = "Xuất"
internal const val SETTINGS_IMPORT_LABEL = "Nhập"
internal const val SETTINGS_PREVIEW_REMINDER_LABEL = "Xem thử thông báo"
internal enum class SettingsMainCard { REMINDER, TASTE_HABIT, DATA_APP }
internal val SETTINGS_MAIN_CARDS = SettingsMainCard.entries
internal enum class DataSheetSection(val title: String) {
    THEME(ThemeModeCopy.title), BACKUP(SettingsPresentation.backupGroupTitle), TRANSFER(SettingsPresentation.transferTitle),
    TEST_REMINDER("Thử thông báo ngay"), ABOUT("Về ứng dụng"),
}
internal val SETTINGS_DATA_SHEET_SECTIONS = DataSheetSection.entries
internal enum class SettingsSheetType { TASTE, DATA }

internal fun shouldShowSundayTimeRow(reminderEnabled: Boolean): Boolean = reminderEnabled
internal fun shouldShowInlineReminderPreview(): Boolean = true
internal fun settingsDataSummary(themeMode: ThemeMode, backupStatus: String? = null): String =
    listOfNotNull(themeMode.label, SettingsPresentation.backupGroupTitle, backupStatus?.takeUnless(String::isBlank)).joinToString(" · ")
internal fun resolveActiveSheet(requested: SettingsSheetType?, restoreConfirmationPending: Boolean): SettingsSheetType? =
    nextRequestedSheet(requested, restoreConfirmationPending)

internal fun nextRequestedSheet(requested: SettingsSheetType?, restoreConfirmationPending: Boolean): SettingsSheetType? =
    if (restoreConfirmationPending) null else requested

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun SettingsDataSheet(
    themeMode: ThemeMode, onThemeModeChange: (ThemeMode) -> Unit,
    backupStatus: String?, onExportBackup: () -> Unit, onImportBackup: () -> Unit,
    onTestReminder: () -> Unit, appVersionName: String, onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val horizontalPadding = Modifier.padding(horizontal = 20.dp)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = colors.background, contentColor = colors.text,
    ) {
        Row(horizontalPadding, verticalAlignment = Alignment.CenterVertically) {
            Text(SETTINGS_DATA_TITLE, Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleLarge)
            IconButton(onDismiss, Modifier.size(48.dp)) { Icon(AppIcons.Close, SettingsPresentation.tasteCloseLabel, tint = colors.muted) }
        }
        Column(horizontalPadding.weight(1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SETTINGS_DATA_SHEET_SECTIONS.forEach { section ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (section != DataSheetSection.TEST_REMINDER) {
                        Text(section.title, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleSmall)
                    }
                    when (section) {
                        DataSheetSection.THEME -> FlowRow(
                            modifier = Modifier.selectableGroup(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            for (mode in SettingsPresentation.themeModeOrder) SelectChip(mode.label, mode == themeMode, { onThemeModeChange(mode) })
                        }
                        DataSheetSection.BACKUP -> {
                            Text(SettingsBackupCopy.backupContents, color = colors.muted)
                            SecondaryButton(SETTINGS_EXPORT_LABEL, SettingsBackupCopy.exportLabel, onExportBackup, AppIcons.Save)
                            SecondaryButton(SETTINGS_IMPORT_LABEL, SettingsBackupCopy.importLabel, onImportBackup, AppIcons.Load)
                            if (!backupStatus.isNullOrBlank()) {
                                Text(
                                    backupStatus,
                                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                                    color = if (backupStatus.startsWith("Chưa thể")) colors.warnText else colors.accentText,
                                )
                            }
                        }
                        DataSheetSection.TRANSFER -> SettingsBackupCopy.transferSteps.forEachIndexed { index, step ->
                            Text("${index + 1}. $step", color = colors.muted)
                        }
                        DataSheetSection.TEST_REMINDER -> SecondaryButton(section.title, section.title, onTestReminder)
                        DataSheetSection.ABOUT -> {
                            Text(SettingsBackupCopy.privacy, color = colors.muted)
                            SettingsPresentation.versionLine(appVersionName)?.let { Text(it, color = colors.muted) }
                        }
                    }
                }
            }
        }
        Button(
            onDismiss, horizontalPadding.fillMaxWidth().padding(vertical = 8.dp).heightIn(min = 48.dp),
            shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
        ) { Text(SettingsPresentation.doneLabel) }
    }
}
