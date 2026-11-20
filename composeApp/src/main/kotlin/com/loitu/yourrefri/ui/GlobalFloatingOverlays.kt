package com.loitu.yourrefri.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

@Composable
internal fun GlobalFloatingActionButtons(
    onOpenWeeklyPlan: () -> Unit,
    onOpenRandomWheel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.End) {
        Surface(Modifier.size(56.dp).clickable(role = Role.Button, onClick = onOpenWeeklyPlan), shape = CircleShape, shadowElevation = 6.dp, color = colors.primary) {
            Icon(AppIcons.Calendar, contentDescription = "Lên thực đơn cả tuần", tint = colors.onPrimary, modifier = Modifier.padding(16.dp))
        }
        Surface(Modifier.size(56.dp).clickable(role = Role.Button, onClick = onOpenRandomWheel), shape = CircleShape, shadowElevation = 6.dp, color = colors.cta) {
            Icon(AppIcons.Wheel, contentDescription = "Nấu ngẫu nhiên", tint = colors.onCta, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
internal fun FloatingOverlayDialog(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.86f), shape = RoundedCornerShape(24.dp), color = colors.background, shadowElevation = 8.dp) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(start = 20.dp, top = 12.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.text)
                    IconButton(onClick = onDismiss) { Icon(AppIcons.Close, contentDescription = "Đóng", tint = colors.text) }
                }
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp)) { content() }
            }
        }
    }
}
