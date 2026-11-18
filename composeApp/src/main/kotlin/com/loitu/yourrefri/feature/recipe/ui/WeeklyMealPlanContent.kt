package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.MealSlotKey
import com.loitu.yourrefri.feature.recipe.logic.PlannedDayMeal
import com.loitu.yourrefri.ui.theme.AppIcons
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import com.loitu.yourrefri.feature.recipe.logic.shoppingIdsToStock
import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.recipe.data.ExtraShoppingItem
import com.loitu.yourrefri.ui.theme.AppTheme

internal const val WEEKLY_EMPTY_DIET_MESSAGE = "Chưa có món phù hợp với chế độ ăn hiện tại. Bạn thử điều chỉnh chế độ ăn nhé."
internal const val WEEKLY_ADJUST_DIET_ACTION = "Điều chỉnh chế độ ăn"

/**
 * Tab "Thực đơn tuần": mục tiêu, khẩu vị, các thẻ ngày, đổi thực đơn, xem cách nấu, Đã nấu xong.
 * Không tự cuộn và không tự vẽ nền; màn cha (RecipeScreen) lo phần đó.
 */
@Composable
internal fun WeeklyMealPlanSection(
    weeklyPlanSchedule: List<PlannedDayMeal>,
    weekStart: LocalDate,
    disabledMeals: List<MealSlotKey> = emptyList(),
    includeLunchMealPrep: Boolean = false,
    onApplyMealPreset: (WeeklyMealPreset) -> Unit = {},
    onToggleMealSlot: (Int, MealSlot) -> Unit = { _, _ -> },
    onToggleLunchMealPrep: (Boolean) -> Unit = {},
    goal: DietGoal,
    preferences: Set<FamilyPreferenceTag>,
    onGoal: (DietGoal) -> Unit,
    onPreference: (FamilyPreferenceTag) -> Unit,
    onSwap: (Int) -> Unit,
    onRequestCook: (Recipe) -> Unit,
    updatedRecipeResult: Pair<String, String>?,
    cookedRecipeIdsByDay: Map<Int, Set<String>> = emptyMap(),
    confirmed: Boolean = false,
    onConfirmWeek: () -> Unit = {},
    onOpenWeekendShopping: () -> Unit = {},
    onAdjustDiet: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val haptic = LocalHapticFeedback.current
    var openedDay by remember { mutableStateOf<Int?>(null) }
    var goalMenuOpen by remember { mutableStateOf(false) }
    var preferenceMenuOpen by remember { mutableStateOf(false) }
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(
                WeeklyMealPreset.ONE_MEAL to "1 bữa/ngày (Bữa Tối)",
                WeeklyMealPreset.TWO_MEALS to "2 bữa/ngày (Trưa + Tối)",
                WeeklyMealPreset.WORKWEEK to "Lịch đi làm (T2–T6: 1 bữa · T7–CN: 2 bữa)",
            ).forEach { (preset, label) ->
                PillChip(
                    label,
                    selected = (0..6).all { dayIndex ->
                        WEEKLY_MEAL_SLOTS.all { slot ->
                            (MealSlotKey(weekStart + DatePeriod(days = dayIndex), slot) !in disabledMeals) ==
                                (slot in enabledMealSlots(preset, dayIndex))
                        }
                    },
                    onClick = { onApplyMealPreset(preset) },
                )
            }
        }
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
        if (weeklyPlanSchedule.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.line),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = WEEKLY_EMPTY_DIET_MESSAGE,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.text,
                    )
                    TextButton(onClick = onAdjustDiet, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(WEEKLY_ADJUST_DIET_ACTION, color = colors.accentText, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            val planByDay = weeklyPlanSchedule.groupBy { it.dayIndex }
            (0..6).forEach { dayIndex ->
                val dayMeals = planByDay[dayIndex].orEmpty()
                val dayLabel = dayMeals.firstOrNull()?.dayLabelVi ?: listOf("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật")[dayIndex]
                Text(dayLabel, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = colors.text, modifier = Modifier.padding(top = 8.dp))
                val dayDate = weekStart + DatePeriod(days = dayIndex)
                val skipDaySelected = WEEKLY_MEAL_SLOTS.all { slot -> MealSlotKey(dayDate, slot) in disabledMeals }
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    WEEKLY_MEAL_SLOTS.forEach { slot ->
                        val isEnabled = MealSlotKey(dayDate, slot) !in disabledMeals
                        PillChip(
                            label = if (slot == MealSlot.LUNCH) "Trưa" else "Tối",
                            selected = isEnabled,
                            onClick = { onToggleMealSlot(dayIndex, slot) },
                        )
                    }
                    PillChip(
                        label = "Bỏ qua",
                        selected = skipDaySelected,
                        onClick = {
                            WEEKLY_MEAL_SLOTS.forEach { slot ->
                                if (MealSlotKey(dayDate, slot) !in disabledMeals) {
                                    onToggleMealSlot(dayIndex, slot)
                                }
                            }
                        },
                    )
                }
                val enabledMeals = enabledMealsForDay(weeklyPlanSchedule, dayIndex, weekStart, disabledMeals)
                if (enabledMeals.isEmpty()) {
                    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(containerColor = colors.surface), shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(AppIcons.Leaf, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(22.dp))
                            Text(EMPTY_MEAL_DAY_LABEL, style = MaterialTheme.typography.bodyMedium, color = colors.text)
                        }
                    }
                    return@forEach
                }
                if (MealSlot.LUNCH in enabledMeals.map { it.mealSlot } && MealSlot.DINNER in enabledMeals.map { it.mealSlot }) {
                    PillChip(
                        label = "Nấu gộp cùng bữa tối (x2 phần)",
                        selected = includeLunchMealPrep,
                        onClick = { onToggleLunchMealPrep(!includeLunchMealPrep) },
                    )
                }
                enabledMeals.forEach { day ->
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
                        if (wasCookedOnDay(cookedRecipeIdsByDay, day.dayIndex, day.recipe.id)) {
                            CookedBadge()
                        }
                        day.sideRecipe?.let { side ->
                            Text("Canh/Rau: ${side.title}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = colors.text, modifier = Modifier.padding(top = 2.dp))
                            if (wasCookedOnDay(cookedRecipeIdsByDay, day.dayIndex, side.id)) {
                                CookedBadge()
                            }
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
                                    onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onRequestCook(dish) },
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
            Button(
                // Một nút: xác nhận thực đơn rồi mở đi chợ cuối tuần luôn.
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (!confirmed) onConfirmWeek()
                    onOpenWeekendShopping()
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp).heightIn(min = 48.dp),
            ) {
                Text(if (confirmed) "Đi chợ cuối tuần" else "Xác nhận và đi chợ cuối tuần")
            }
        }
    }
}

@Composable
private fun CookedBadge() {
    val colors = AppTheme.colors
    Surface(shape = RoundedCornerShape(16.dp), color = colors.primarySoft) {
        Text("Đã nấu", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelMedium, color = colors.accentText)
    }
}

/** Tab "Đi chợ": danh sách đi chợ của thực đơn tuần và nút "Đã mua xong · Cất vào tủ lạnh". */
@Composable
internal fun WeeklyShoppingSection(
    shopping: List<com.loitu.yourrefri.feature.recipe.logic.ShoppingListItem>,
    uncheckedIds: Set<String>,
    onToggleChecked: (String, Boolean) -> Unit,
    onStock: ((List<String>) -> Unit)?,
    onStockWithCompartments: ((List<Pair<String, PantryCompartment>>) -> Unit)? = null,
    extraShoppingItems: List<ExtraShoppingItem> = emptyList(),
    ingredients: List<Ingredient> = emptyList(),
    onRemoveExtraShoppingItem: (String) -> Unit = {},
    onClearExtraShoppingItems: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var result by remember { mutableStateOf<String?>(null) }
    var compartments by remember(shopping) {
        mutableStateOf(shopping.associate { it.ingredient.id to suggestedWeeklyCompartment(it) })
    }
    val buyItems = shopping.filter { !it.alreadyInPantry || it.missingQuantity > 0 }
    val selectedIds = shoppingIdsToStock(shopping, uncheckedIds)
    val stockListForAction = remember(shopping, uncheckedIds) {
        shopping.filter { (!it.alreadyInPantry || it.missingQuantity > 0) && it.ingredient.id !in uncheckedIds }
            .flatMap { item -> List(item.missingQuantity.coerceAtLeast(1)) { item.ingredient.id } }
    }
    val extraDisplayItems = remember(extraShoppingItems, ingredients) {
        extraShoppingDisplayItems(extraShoppingItems, ingredients)
    }
    val extraCompartments = remember(extraShoppingItems, ingredients) {
        extraShoppingCompartments(extraShoppingItems, ingredients)
    }
    val combinedStockList = remember(stockListForAction, extraShoppingItems) {
        combinedWeeklyStockIds(stockListForAction, extraShoppingItems)
    }
    val totalSelectedCount = remember(selectedIds, extraShoppingItems) {
        combinedWeeklySelectedCount(selectedIds, extraShoppingItems)
    }
    val buyItemIds = remember(buyItems) { buyItems.map { it.ingredient.id }.toSet() }
    val checklist = buyItems.map {
        WeeklyChecklistItem(it.ingredient.nameVi, compartments[it.ingredient.id] ?: suggestedWeeklyCompartment(it), it.ingredient.id !in uncheckedIds)
    } + extraDisplayItems
        .filter { (id, _) -> id !in buyItemIds }
        .map { (id, name) -> WeeklyChecklistItem(name, extraCompartments[id] ?: PantryCompartment.CHILL, true) }
    Column(modifier.fillMaxWidth()) {
        Text("Danh sách đi chợ tuần này", style = MaterialTheme.typography.titleLarge, color = colors.text, modifier = Modifier.padding(top = 14.dp))
        if (extraDisplayItems.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                shape = RoundedCornerShape(12.dp),
                color = colors.surfaceSoft,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.line),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        text = "Cần mua thêm",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.accentText,
                    )
                    extraDisplayItems.forEach { (ingredientId, name) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = name,
                                color = colors.text,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = { onRemoveExtraShoppingItem(ingredientId) },
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(
                                    imageVector = AppIcons.Close,
                                    contentDescription = "Bỏ $name khỏi Cần mua thêm",
                                    tint = colors.muted,
                                )
                            }
                        }
                    }
                }
            }
        }
        Text(shoppingSummaryText(buyItems.size, shopping.size - buyItems.size), color = colors.accentText, modifier = Modifier.padding(top = 8.dp))
        weeklyShoppingAisles(buyItems).forEach { (aisle, aisleItems) ->
            Text(aisle, style = MaterialTheme.typography.titleMedium, color = colors.accentText, modifier = Modifier.padding(top = 12.dp))
            aisleItems.forEach { item ->
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = item.ingredient.id !in uncheckedIds,
                    onCheckedChange = { onToggleChecked(item.ingredient.id, it) },
                    colors = CheckboxDefaults.colors(checkedColor = colors.primary, uncheckedColor = colors.muted, checkmarkColor = colors.onPrimary),
                )
                Column(Modifier.weight(1f)) {
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
                TextButton(onClick = {
                    val current = compartments[item.ingredient.id] ?: suggestedWeeklyCompartment(item)
                    val next = if (current == PantryCompartment.FREEZE) PantryCompartment.CHILL else PantryCompartment.FREEZE
                    compartments = compartments + (item.ingredient.id to next)
                }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(if ((compartments[item.ingredient.id] ?: suggestedWeeklyCompartment(item)) == PantryCompartment.FREEZE) "Ngăn đông" else "Ngăn mát")
                }
            }
            }
        }
        val haveItems = shopping.filter { it.alreadyInPantry && it.missingQuantity == 0 }
        if (haveItems.isNotEmpty()) {
            Text("Đã đủ cho cả tuần: ${haveItems.joinToString(", ") { it.ingredient.nameVi }}", color = colors.muted, modifier = Modifier.padding(top = 4.dp))
        }
        if (buyItems.isEmpty() && extraDisplayItems.isEmpty()) {
            Text("Tuần này nhà mình đã đủ nguyên liệu", Modifier.padding(top = 8.dp), color = colors.accentText)
        } else {
            TextButton(onClick = {
                val text = weeklyChecklistText(checklist)
                clipboard.setText(AnnotatedString(text))
                shareToZaloOrChooser(context, text)
            }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Gửi qua Zalo")
            }
            Button(onClick = {
                if (onStockWithCompartments != null) {
                    onStockWithCompartments(weeklyStockCompartments(combinedStockList, extraCompartments + compartments))
                } else {
                    onStock?.invoke(combinedStockList)
                }
                if (extraShoppingItems.isNotEmpty()) {
                    onClearExtraShoppingItems()
                }
                if (onStockWithCompartments != null || onStock != null) result = stockResultMessage(totalSelectedCount)
            }, enabled = (onStockWithCompartments != null || onStock != null) && totalSelectedCount > 0, colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary, disabledContainerColor = colors.line, disabledContentColor = colors.muted), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                // stockActionButtonLabel còn emoji vì test SW-02 khóa chuỗi gốc; giao diện chỉ hiện phần chữ.
                Text(stockActionButtonLabel(totalSelectedCount).removeSuffix(" 🧊")) // guard:allow
            }
        }
        result?.let { Text(it, Modifier.padding(top = 8.dp), color = colors.accentText) }
    }
}
