package com.loitu.yourrefri.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.ui.theme.DeepCharcoal
import com.loitu.yourrefri.ui.theme.MutedGray
import com.loitu.yourrefri.ui.theme.SageGreen
import com.loitu.yourrefri.ui.theme.SageGreenStrong
import com.loitu.yourrefri.ui.theme.SageGreenText
import com.loitu.yourrefri.ui.theme.SoftGreen
import com.loitu.yourrefri.ui.theme.SoftGreenLight
import com.loitu.yourrefri.ui.theme.WarmIvory

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
) {
    if (restoreConfirmationPending) {
        AlertDialog(
            onDismissRequest = onCancelRestore,
            title = { Text(SettingsBackupCopy.restoreTitle) },
            text = { Text(SettingsBackupCopy.restoreWarning) },
            confirmButton = {
                Button(onClick = onConfirmRestore) { Text(SettingsBackupCopy.restoreLabel) }
            },
            dismissButton = {
                TextButton(onClick = onCancelRestore) { Text(SettingsBackupCopy.cancelRestoreLabel) }
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Cá nhân & Cài đặt ⚙️",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = DeepCharcoal,
        )
        Text(
            text = "Thiết lập theo thói quen sinh hoạt của gia đình",
            style = MaterialTheme.typography.bodySmall,
            color = MutedGray,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp),
        )

        // Cài đặt thông báo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Nhắc nhở bữa tối ⏰",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepCharcoal
                )
                Text(
                    text = "Tối đa 1 thông báo/ngày lúc 17:30; gợi ý món từ đồ nên dùng sớm. Lần đầu bật, điện thoại sẽ hỏi quyền thông báo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedGray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bật nhắc lúc 17:30",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = DeepCharcoal
                    )
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = onReminderChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SageGreenStrong
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sao lưu dữ liệu Offline
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Dữ liệu & Quyền riêng tư 🔒",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepCharcoal
                )
                Text(
                    text = SettingsBackupCopy.privacy,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedGray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
                Text(
                    text = SettingsBackupCopy.backupContents,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedGray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                SettingsBackupCopy.transferSteps.forEachIndexed { index, step ->
                    Text(
                        text = "${index + 1}. $step",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedGray,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }

                Button(
                    onClick = onExportBackup,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Text(SettingsBackupCopy.exportLabel, color = DeepCharcoal, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = onImportBackup,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(top = 8.dp)
                ) {
                    Text(SettingsBackupCopy.importLabel, color = DeepCharcoal, fontWeight = FontWeight.Medium)
                }

                backupStatus?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (it.startsWith("Chưa thể")) Color(0xFF9A4E36) else SageGreenText,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Thông điệp ấm áp
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftGreenLight),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "YourRefri — Người trợ lý nhỏ trong căn bếp gia đình",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = SageGreenText
                )
                Text(
                    text = "Giúp bạn không cần nhớ hết đồ trong tủ, nấu ăn dễ dàng hơn và trân trọng thực phẩm mỗi ngày.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
