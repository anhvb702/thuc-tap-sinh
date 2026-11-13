package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import com.loitu.yourrefri.feature.recipe.logic.shoppingIdsToStock
import com.loitu.yourrefri.ui.theme.AppTheme

/**
 * Tab "Thực đơn tuần": mục tiêu, khẩu vị, các thẻ ngày, đổi thực đơn, xem cách nấu, Đã nấu xong.
 * Không tự cuộn và không tự vẽ nền; màn cha (RecipeScreen) lo phần đó.
 */
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
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    var openedDay by remember { mutableStateOf<Int?>(null) }
    var goalMenuOpen by remember { mutableStateOf(false) }
    var preferenceMenuOpen by remember { mutableStateOf(false) }
    Column(modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = colors.surfaceSoft,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.line)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = "Lưu ý về bữa cơm gia đình:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
                Text(
                    text = "Thực đơn được thiết kế cho Bữa cơm chính trong ngày (Trưa hoặc Tối) gồm 1 món mặn + 1 món canh/rau để cả nhà ăn ngon mà không bị áp lực lên lịch quá nhiều bữa.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.text,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Text("Chọn định hướng và khẩu vị, mình xếp sẵn mâm cơm (Món mặn + Canh/Rau) cả tuần.", style = MaterialTheme.typography.bodyMedium, color = colors.muted, modifier = Modifier.padding(bottom = 6.dp))
        Text(WEEKLY_DIET_GOAL_LABEL, style = MaterialTheme.typography.labelLarge, color = colors.text)
        androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { goalMenuOpen = true },
                shape = RoundedCornerShape(16.dp), color = colors.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.line),
            ) {
                Text(dietGoalFieldText(goal) + "  ▾", modifier = Modifier.padding(12.dp), color = colors.text)
            }
            DropdownMenu(expanded = goalMenuOpen, onDismissRequest = { goalMenuOpen = false }) {
                DietGoal.entries.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.labelVi) },
                        onClick = { onGoal(item); goalMenuOpen = false },
                        modifier = Modifier.heightIn(min = 48.dp),
                    )
                }
            }
        }
        Text(WEEKLY_FAMILY_PREFERENCE_LABEL, style = MaterialTheme.typography.labelLarge, color = colors.text, modifier = Modifier.padding(top = 4.dp))
        androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { preferenceMenuOpen = true },
                shape = RoundedCornerShape(16.dp), color = colors.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.line),
            ) {
                Text(preferenceFieldText(preferences) + "  ▾", modifier = Modifier.padding(12.dp), color = colors.text)
            }
            DropdownMenu(expanded = preferenceMenuOpen, onDismissRequest = { preferenceMenuOpen = false }) {
                FamilyPreferenceTag.entries.forEach { item ->
                    val selected = item in preferences
                    DropdownMenuItem(
                        text = { Text(item.labelVi) },
                        onClick = { onPreference(item) },
                        trailingIcon = { Checkbox(checked = selected, onCheckedChange = null) },
                        modifier = Modifier.heightIn(min = 48.dp).semantics(mergeDescendants = true) {
                            contentDescription = preferenceOptionDescription(item.labelVi, selected)
                        },
                    )
                }
            }
        }
        plan.forEach { day ->
            val isExpanded = openedDay == day.dayIndex
            Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(containerColor = colors.surface), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(dayMealCardHeader(day.dayLabelVi, day.mealTypeVi), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.accentText)
                        val totalMinutes = day.dishes.mapNotNull { it.minutes }.maxOrNull()
                        Text(totalMinutes?.let { "Khoảng $it phút" } ?: "Thời gian chưa rõ", style = MaterialTheme.typography.bodySmall, color = colors.muted)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Món chính: ${day.recipe.title}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.text)
                    day.sideRecipe?.let { side ->
                        Text("Canh/Rau: ${side.title}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = colors.text, modifier = Modifier.padding(top = 2.dp))
                    }
                    Text(day.matchReasonVi, style = MaterialTheme.typography.bodySmall, color = colors.accentText, modifier = Modifier.padding(top = 4.dp))
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { openedDay = if (isExpanded) null else day.dayIndex },
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text(if (isExpanded) "Thu gọn cách nấu ▲" else "Xem cách nấu ▼", color = colors.accentText, fontWeight = FontWeight.SemiBold)
                        }
                        TextButton(
                            onClick = { onSwap(day.dayIndex) },
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text("Đổi thực đơn ↻", color = colors.text)
                        }
                    }
                    if (isExpanded) {
                        day.dishes.forEach { dish ->
                            Spacer(Modifier.height(8.dp))
                            Text("Cách nấu ${dish.title}:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.text)
                            dish.steps.forEachIndexed { index, step ->
                                Text("${index + 1}. $step", style = MaterialTheme.typography.bodyMedium, color = colors.text, modifier = Modifier.padding(vertical = 3.dp))
                            }
                            Button(
                                onClick = { onRequestCook(dish) },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp).heightIn(min = 48.dp),
                            ) {
                                Text("Đã nấu xong: ${dish.title}", color = colors.onPrimary)
                            }
                            if (updatedRecipeResult?.first == dish.id) {
                                Text(updatedRecipeResult.second, style = MaterialTheme.typography.bodySmall, color = colors.accentText, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
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
    val selectedIds = shoppingIdsToStock(shopping, uncheckedIds)
    val stockListForAction = remember(shopping, uncheckedIds) {
        shopping.filter { (!it.alreadyInPantry || it.missingQuantity > 0) && it.ingredient.id !in uncheckedIds }
            .flatMap { item -> List(item.missingQuantity.coerceAtLeast(1)) { item.ingredient.id } }
    }
    Column(modifier.fillMaxWidth()) {
        Text("Danh sách đi chợ tuần này", style = MaterialTheme.typography.titleLarge, color = colors.text, modifier = Modifier.padding(top = 14.dp))
        Text(shoppingSummaryText(buyItems.size, shopping.size - buyItems.size), color = colors.accentText)
        buyItems.forEach { item ->
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = item.ingredient.id !in uncheckedIds,
                    onCheckedChange = { onToggleChecked(item.ingredient.id, it) },
                    colors = CheckboxDefaults.colors(checkedColor = colors.primary, uncheckedColor = colors.muted, checkmarkColor = colors.onPrimary),
                )
                Column {
                    Text(item.ingredient.nameVi, color = colors.text, fontWeight = FontWeight.Medium)
                    Text(
                        text = shoppingItemSubtitle(
                            mealCount = item.mealCount,
                            availableQuantity = item.availableQuantity,
                            missingQuantity = item.missingQuantity,
                        ),
                        color = colors.muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
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
            }, enabled = onStock != null && selectedIds.isNotEmpty(), colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary, disabledContainerColor = colors.line, disabledContentColor = colors.muted), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                // stockActionButtonLabel còn emoji vì test SW-02 khóa chuỗi gốc; giao diện chỉ hiện phần chữ.
                Text(stockActionButtonLabel(selectedIds.size).removeSuffix(" 🧊")) // guard:allow
            }
        }
        result?.let { Text(it, Modifier.padding(top = 8.dp), color = colors.accentText) }
    }
}
