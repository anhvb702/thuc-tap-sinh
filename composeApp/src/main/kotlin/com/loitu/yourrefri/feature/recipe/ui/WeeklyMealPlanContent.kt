package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import com.loitu.yourrefri.feature.recipe.logic.shoppingIdsToStock
import com.loitu.yourrefri.ui.components.FoodThumb
import com.loitu.yourrefri.ui.components.RecipeThumb
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

internal const val WEEKLY_SHOPPING_HELPER_BANNER = "Mua đến đâu, cất đến đó."

@Composable
private fun DropdownFieldText(text: String) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = colors.text,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(AppIcons.ChevronDown, contentDescription = null, tint = colors.muted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun CompactDishRow(recipe: Recipe, onRequestCook: (Recipe) -> Unit) {
    val title = compactDishTitles(recipe.title, null).firstOrNull() ?: return
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .clickable(role = Role.Button) { onRequestCook(recipe) },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecipeThumb(recipeId = recipe.id, size = 40.dp)
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colors.text,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun WeeklyMealPlanSection(
    plan: List<com.loitu.yourrefri.feature.recipe.logic.PlannedDayMeal>,
    goal: DietGoal,
    preferences: Set<FamilyPreferenceTag>,
    onGoal: (DietGoal) -> Unit,
    onPreference: (FamilyPreferenceTag) -> Unit,
    onSwap: (Int) -> Unit,
    onRequestCook: (Recipe) -> Unit,
    updatedRecipeResult: Pair<String, String>?,
    weekStart: kotlinx.datetime.LocalDate? = null,
    modifier: Modifier = Modifier,
    onConfirmWeek: () -> Unit = {},
    onNavigate: (RecipeScreenMode) -> Unit = {},
    includeLunchMealPrep: Boolean = false,
    onIncludeLunchMealPrepChange: (Boolean) -> Unit = {},
) {
    val colors = AppTheme.colors
    var goalMenuOpen by remember { mutableStateOf(false) }
    var preferenceMenuOpen by remember { mutableStateOf(false) }
    var weekConfirmed by rememberSaveable { mutableStateOf(false) }
    var selectedMealOption by rememberSaveable {
        mutableStateOf(if (includeLunchMealPrep) WeeklyMealOption.BOTH else WeeklyMealOption.DINNER)
    }
    LaunchedEffect(includeLunchMealPrep) {
        if (includeLunchMealPrep) selectedMealOption = WeeklyMealOption.BOTH
    }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WeeklyMealOption.entries.forEach { option ->
                val selected = option == selectedMealOption
                Surface(
                    modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) colors.primarySoft else colors.surface,
                    border = BorderStroke(1.dp, if (selected) colors.primary else colors.line),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp).selectable(selected = selected, role = Role.RadioButton) {
                            selectedMealOption = option
                            weekConfirmed = false
                            onIncludeLunchMealPrepChange(option.includesLunchPrep)
                        }.padding(horizontal = 4.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = compactMealOptionLabel(option),
                            color = if (selected) colors.accentText else colors.text,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(WEEKLY_DIET_GOAL_LABEL, style = MaterialTheme.typography.labelSmall, color = colors.muted)
                Box(Modifier.fillMaxWidth()) {
                    Surface(
                        onClick = { goalMenuOpen = true },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.line),
                    ) {
                        DropdownFieldText(dietGoalFieldText(goal))
                    }
                    DropdownMenu(expanded = goalMenuOpen, onDismissRequest = { goalMenuOpen = false }) {
                        DietGoal.entries.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.labelVi) },
                                onClick = {
                                    onGoal(item)
                                    weekConfirmed = false
                                    goalMenuOpen = false
                                },
                                modifier = Modifier.heightIn(min = 48.dp),
                            )
                        }
                    }
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(WEEKLY_FAMILY_PREFERENCE_LABEL, style = MaterialTheme.typography.labelSmall, color = colors.muted)
                Box(Modifier.fillMaxWidth()) {
                    Surface(
                        onClick = { preferenceMenuOpen = true },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.line),
                    ) {
                        DropdownFieldText(preferenceFieldText(preferences))
                    }
                    DropdownMenu(expanded = preferenceMenuOpen, onDismissRequest = { preferenceMenuOpen = false }) {
                        FamilyPreferenceTag.entries.forEach { item ->
                            val selected = item in preferences
                            DropdownMenuItem(
                                text = { Text(item.labelVi) },
                                onClick = {
                                    onPreference(item)
                                    weekConfirmed = false
                                },
                                trailingIcon = { Checkbox(checked = selected, onCheckedChange = null) },
                                modifier = Modifier.heightIn(min = 48.dp).semantics(mergeDescendants = true) {
                                    contentDescription = preferenceOptionDescription(item.labelVi, selected)
                                },
                            )
                        }
                    }
                }
            }
        }
        plan.forEach { day ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, colors.line),
            ) {
                Column(Modifier.padding(start = 12.dp, end = 8.dp, bottom = 8.dp)) {
                    val dayTitle = compactDayTitle(day.dayLabelVi, weekStart, day.dayIndex)
                    val totalMinutes = listOfNotNull(day.recipe.minutes, day.sideRecipe?.minutes).maxOrNull()
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = dayTitle,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.text,
                        )
                        compactDurationLabel(totalMinutes)?.let { duration ->
                            Text(duration, style = MaterialTheme.typography.labelMedium, color = colors.muted)
                        }
                        IconButton(onClick = {
                            weekConfirmed = false
                            onSwap(day.dayIndex)
                        }) {
                            Icon(
                                AppIcons.Shuffle,
                                contentDescription = "Đổi món $dayTitle",
                                tint = colors.accentText,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    day.dishes.forEach { dish -> CompactDishRow(dish, onRequestCook) }
                }
            }
        }
        if (!weekConfirmed) {
            Button(
                onClick = {
                    handleWeeklyConfirm(
                        plan,
                        {
                            weekConfirmed = true
                            onConfirmWeek()
                        },
                        { onNavigate(RecipeScreenMode.SHOPPING) },
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            ) {
                Text("Chốt thực đơn & Đi chợ")
            }
        }
    }
}

/** Tab "Đi chợ": danh sách đi chợ của thực đơn tuần và nút "Đã mua xong · Cất vào tủ lạnh". */
@Composable
internal fun WeeklyShoppingSection(
    shopping: List<com.loitu.yourrefri.feature.recipe.logic.ShoppingListItem>,
    uncheckedIds: Set<String>,
    onToggleChecked: (String, Boolean) -> Unit,
    onStock: ((List<String>) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    var result by remember { mutableStateOf<String?>(null) }
    val buyItems = shopping.filter { !it.alreadyInPantry || it.missingQuantity > 0 }
    // "Đã mua" theo món: chỉ trong bộ nhớ màn hình (ruling: không lưu sau khi rời màn).
    var bought by remember { mutableStateOf(setOf<String>()) }
    val buyIds = buyItems.map { it.ingredient.id }.toSet()
    val selectedIds = shoppingIdsToStock(shopping, uncheckedIds)
    val stockListForAction = remember(shopping, uncheckedIds) {
        shopping.filter { (!it.alreadyInPantry || it.missingQuantity > 0) && it.ingredient.id !in uncheckedIds }
            .flatMap { item -> List(item.missingQuantity.coerceAtLeast(1)) { item.ingredient.id } }
    }
    Column(modifier.fillMaxWidth()) {
        Text("Danh sách đi chợ tuần này", style = MaterialTheme.typography.titleLarge, color = colors.text, modifier = Modifier.padding(top = 14.dp))
        Surface(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(20.dp),
            color = colors.primarySoft,
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(shoppingSummaryText(buyItems.size, shopping.size - buyItems.size), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.accentText)
                Text(shoppingProgressLabel(bought = bought.count { it in buyIds }, total = buyItems.size), color = colors.accentText, style = MaterialTheme.typography.bodySmall)
                Text(WEEKLY_SHOPPING_HELPER_BANNER, color = colors.muted, style = MaterialTheme.typography.bodySmall)
            }
        }
        buyItems.forEach { item ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(20.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.line),
            ) {
                Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = item.ingredient.id !in uncheckedIds,
                        onCheckedChange = { onToggleChecked(item.ingredient.id, it) },
                        colors = CheckboxDefaults.colors(checkedColor = colors.primary, uncheckedColor = colors.muted, checkmarkColor = colors.onPrimary),
                    )
                    FoodThumb(ingredientId = item.ingredient.id, size = 36.dp)
                    Column(Modifier.padding(start = 12.dp).clickable { bought = if (item.ingredient.id in bought) bought - item.ingredient.id else bought + item.ingredient.id }) {
                        Text(item.ingredient.nameVi, color = colors.text, fontWeight = FontWeight.Medium, textDecoration = if (item.ingredient.id in bought) TextDecoration.LineThrough else null)
                        Text(text = shoppingItemSubtitle(mealCount = item.mealCount, availableQuantity = item.availableQuantity, missingQuantity = item.missingQuantity), color = colors.muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        val haveItems = shopping.filter { it.alreadyInPantry && it.missingQuantity == 0 }
        if (haveItems.isNotEmpty()) {
            Text("Đã đủ cho cả tuần: ${haveItems.joinToString(", ") { it.ingredient.nameVi }}", color = colors.muted, modifier = Modifier.padding(top = 4.dp))
        }
        if (buyItems.isEmpty()) {
            Text("Tuần này nhà mình đã đủ nguyên liệu", Modifier.padding(top = 8.dp), color = colors.accentText)
        } else {
            Button(onClick = {
                onStock?.invoke(stockListForAction)
                if (onStock != null) result = stockResultMessage(selectedIds.size)
            }, enabled = onStock != null && selectedIds.isNotEmpty(), colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary, disabledContainerColor = colors.line, disabledContentColor = colors.muted), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) {
                // stockActionButtonLabel còn emoji vì test SW-02 khóa chuỗi gốc; giao diện chỉ hiện phần chữ.
                Text(stockActionButtonLabel(selectedIds.size).removeSuffix(" 🧊")) // guard:allow
            }
        }
        result?.let { Text(it, Modifier.padding(top = 8.dp), color = colors.accentText) }
    }
}
