package com.loitu.yourrefri.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

@Composable
internal fun SectionTitle(text: String, icon: ImageVector? = null) {
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = AppTheme.colors.accentText, modifier = Modifier.size(20.dp))
        }
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.text)
    }
}

@Composable
internal fun SettingsCard(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(AppTheme.colors.surface).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
}

@Composable
internal fun SettingsNavRow(title: String, summary: String, icon: ImageVector, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, null, tint = colors.accentText, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
            Text(summary, fontSize = 13.sp, color = colors.muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Icon(AppIcons.ArrowRight, null, tint = colors.muted, modifier = Modifier.size(20.dp))
    }
}

@Composable
internal fun ChipRow(title: String, subtitle: String? = null, chip: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppTheme.colors.text)
            if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = AppTheme.colors.muted)
        }
        chip()
    }
}

@Composable
internal fun ToggleChip(enabled: Boolean, description: String, onChange: (Boolean) -> Unit) {
    val colors = AppTheme.colors
    Switch(
        checked = enabled,
        onCheckedChange = onChange,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .semantics { contentDescription = "$description: ${SettingsPresentation.toggleChipLabel(enabled)}" },
        colors = SwitchDefaults.colors(
            checkedThumbColor = colors.onPrimary,
            checkedTrackColor = colors.primary,
            checkedBorderColor = colors.primary,
            uncheckedThumbColor = colors.muted,
            uncheckedTrackColor = colors.surfaceSoft,
            uncheckedBorderColor = colors.line,
        ),
    )
}

@Composable
internal fun ValueChip(text: String, description: String, onClick: () -> Unit, dropdown: Boolean = false) {
    val colors = AppTheme.colors
    Row(Modifier.heightIn(min = 48.dp).widthIn(min = 72.dp).clip(RoundedCornerShape(20.dp)).background(colors.primarySoft).semantics { contentDescription = "$description: $text" }.clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
        Text(text, Modifier.widthIn(max = 150.dp), textAlign = TextAlign.Center, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.accentText)
        if (dropdown) Icon(AppIcons.ChevronDown, null, tint = colors.accentText, modifier = Modifier.size(16.dp))
    }
}

@Composable
internal fun SelectChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Box(modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(20.dp)).background(if (selected) colors.primary else colors.surfaceSoft).selectable(selected = selected, role = Role.RadioButton, onClick = onClick).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
        Text(text, textAlign = TextAlign.Center, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (selected) colors.onPrimary else colors.text)
    }
}

@Composable
internal fun SecondaryButton(label: String, description: String, onClick: () -> Unit, icon: ImageVector? = null) {
    val colors = AppTheme.colors
    Row(Modifier.fillMaxWidth().heightIn(min = 50.dp).clip(RoundedCornerShape(20.dp)).background(colors.primarySoft).semantics { contentDescription = description }.clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
        if (icon != null) Icon(icon, null, tint = colors.accentText, modifier = Modifier.size(20.dp))
        Text(label, textAlign = TextAlign.Center, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.accentText)
    }
}
