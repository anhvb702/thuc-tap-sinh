package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

/** Mục "Thực đơn tuần này" + nút chữ "Xem chi tiết" và dải 7 ngày T2..CN. */
@Composable
internal fun WeekStripSection(
    chips: List<WeekDayChip>,
    onOpenWeek: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = WEEK_SECTION_TITLE,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onOpenWeek, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(WEEK_SECTION_ACTION, color = colors.accentText, fontWeight = FontWeight.SemiBold)
                Icon(AppIcons.Chevron, contentDescription = null, tint = colors.accentText, modifier = Modifier.padding(start = 2.dp).size(16.dp))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            chips.forEach { chip -> WeekDayPill(chip, onClick = onOpenWeek, modifier = Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun WeekDayPill(chip: WeekDayChip, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(24.dp)
    val bg = if (chip.isToday) colors.cta else colors.surface
    val fg = if (chip.isToday) colors.onCta else colors.text
    Column(
        modifier = modifier
            .heightIn(min = 72.dp)
            .clip(shape)
            .background(bg)
            .then(if (chip.isToday) Modifier else Modifier.border(1.dp, colors.line, shape))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = weekDayChipDescription(chip) }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        Text(chip.label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = fg)
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (chip.hasMeal) (if (chip.isToday) colors.onCta else colors.primarySoft) else colors.surfaceSoft),
            contentAlignment = Alignment.Center,
        ) {
            if (chip.hasMeal) {
                Icon(
                    AppIcons.Check,
                    contentDescription = null,
                    tint = if (chip.isToday) colors.cta else colors.accentText,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

/** Thẻ lối tắt: icon trong vòng tròn, tiêu đề, phụ đề (tối đa 2 dòng phụ) và chevron. */
@Composable
internal fun ShortcutCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    extraLine: String? = null,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.line, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.primarySoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.text)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.muted)
            if (extraLine != null) {
                Text(extraLine, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = colors.accentText)
            }
        }
        Icon(AppIcons.Chevron, contentDescription = null, tint = colors.muted, modifier = Modifier.size(20.dp))
    }
}
