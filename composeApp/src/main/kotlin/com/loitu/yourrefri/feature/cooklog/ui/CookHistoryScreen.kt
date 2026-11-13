package com.loitu.yourrefri.feature.cooklog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.cooklog.data.CookLog
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.ui.components.FoodThumb
import com.loitu.yourrefri.ui.components.RecipeThumb
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

private const val MAX_INGREDIENT_ICONS = 5

/** Màn "Đã nấu": danh sách món theo tuần, không thống kê. Chạm một dòng hoặc "Nấu lại" để mở lại công thức. */
@Composable
fun CookHistoryScreen(
    state: CookHistoryState,
    recipes: List<Recipe>,
    onOpenRecipe: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val titles = recipes.associate { it.id to it.title }
    Column(modifier = modifier.fillMaxSize().background(colors.background).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
                Icon(AppIcons.Back, contentDescription = "Quay lại", tint = colors.accentText, modifier = Modifier.size(20.dp))
            }
            Text(
                text = CookHistoryCopy.title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.text,
            )
        }
        if (state.loaded && state.weeks.isEmpty()) {
            Text(
                text = CookHistoryCopy.empty,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.muted,
                modifier = Modifier.padding(top = 24.dp),
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                state.weeks.forEach { week ->
                    item(key = "week-${week.label}") {
                        Text(
                            text = CookHistoryCopy.weekLabel(week.label),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.text,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        )
                    }
                    items(week.entries, key = { it.id }) { log ->
                        CookHistoryRow(log, titles[log.recipeId], onOpen = { onOpenRecipe(log.recipeId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CookHistoryRow(log: CookLog, title: String?, onOpen: () -> Unit) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    val shown = log.savedIngredientIds.take(MAX_INGREDIENT_ICONS)
    val hidden = log.savedIngredientIds.size - shown.size
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.line, shape)
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RecipeThumb(recipeId = log.recipeId, size = 56.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title ?: CookHistoryCopy.unknownRecipe,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.text,
            )
            Text(cookDateLabel(log.cookedOn), style = MaterialTheme.typography.bodySmall, color = colors.muted)
            if (shown.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    shown.forEach { id -> FoodThumb(ingredientId = id, size = 24.dp) }
                    if (hidden > 0) {
                        Text(CookHistoryCopy.moreIngredients(hidden), style = MaterialTheme.typography.bodySmall, color = colors.muted)
                    }
                }
            }
        }
        TextButton(onClick = onOpen, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(CookHistoryCopy.cookAgain, color = colors.accentText, fontWeight = FontWeight.SemiBold)
        }
    }
}
