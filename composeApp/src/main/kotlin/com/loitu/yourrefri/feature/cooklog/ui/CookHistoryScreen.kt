package com.loitu.yourrefri.feature.cooklog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.cooklog.data.CookLog
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.ui.components.RecipeThumb
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

/** Màn "Đã nấu": danh sách món theo tuần, không thống kê. Chạm một dòng hoặc "Nấu lại" để mở lại công thức. */
@Composable
fun CookHistoryScreen(
    state: CookHistoryState,
    recipes: List<Recipe>,
    onOpenRecipe: (String) -> Unit,
    onBack: () -> Unit,
    onDelete: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val titles = recipes.associate { it.id to it.title }
    Column(modifier = modifier.fillMaxSize().background(colors.background).padding(horizontal = 24.dp)) {
        TextButton(
            onClick = onBack,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.primarySoft),
        ) {
            Icon(AppIcons.Back, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(18.dp))
            Text("Trang chủ", color = colors.accentText, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))
        }
        Text(
            text = CookHistoryCopy.eyebrow,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.accentText,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = CookHistoryCopy.title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.text,
        )
        Text(
            text = CookHistoryCopy.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
            modifier = Modifier.padding(top = 4.dp),
        )
        if (state.loaded && state.weeks.isEmpty()) {
            Text(
                text = CookHistoryCopy.empty,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.muted,
                modifier = Modifier.padding(top = 24.dp),
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                state.weeks.forEach { week ->
                    item(key = "week-${week.label}") {
                        Text(
                            text = CookHistoryCopy.weekLabel(week.label),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.text,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(week.entries, key = { it.id }) { log ->
                        CookHistoryRow(log, titles[log.recipeId], onOpen = { onOpenRecipe(log.recipeId) }, onDelete = { onDelete(log.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CookHistoryRow(log: CookLog, title: String?, onOpen: () -> Unit, onDelete: () -> Unit) {
    val colors = AppTheme.colors
    var confirmDelete by remember(log.id) { mutableStateOf(false) }
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text(CookHistoryCopy.deleteEntry) },
        text = { Text(CookHistoryCopy.deletePrompt) },
        confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) { Text(CookHistoryCopy.delete) } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(CookHistoryCopy.cancel) } },
    )
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(cookDateMealLabel(log.cookedOn, log.meal), style = MaterialTheme.typography.bodySmall, color = colors.muted)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onOpen),
        ) {
            RecipeThumb(recipeId = log.recipeId, size = 48.dp)
            Text(
                text = title ?: CookHistoryCopy.unknownRecipe,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 50.dp)
                    .clip(shape)
                    .background(colors.primarySoft)
                    .clickable(role = Role.Button, onClick = onOpen)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                Text(CookHistoryCopy.cookAgain, color = colors.accentText, fontWeight = FontWeight.SemiBold)
                Icon(AppIcons.ArrowRight, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(18.dp))
            }
            TextButton(onClick = { confirmDelete = true }, modifier = Modifier.heightIn(min = 50.dp)) {
                Text(CookHistoryCopy.deleteEntry, color = colors.muted)
            }
        }
    }
}
