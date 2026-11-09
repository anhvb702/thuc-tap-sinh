package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loitu.yourrefri.ui.theme.DeepCharcoal
import com.loitu.yourrefri.ui.theme.MutedGray
import com.loitu.yourrefri.ui.theme.SageGreen
import com.loitu.yourrefri.ui.theme.SageGreenStrong
import com.loitu.yourrefri.ui.theme.SageGreenText
import com.loitu.yourrefri.ui.theme.SoftGreen
import com.loitu.yourrefri.ui.theme.Terracotta
import com.loitu.yourrefri.ui.theme.TerracottaText
import com.loitu.yourrefri.ui.theme.WarmBeige
import com.loitu.yourrefri.ui.theme.WarmIvory
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.canCook
import com.loitu.yourrefri.feature.recipe.logic.filterRecipesByDurationInclusive
import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import com.loitu.yourrefri.feature.recipe.logic.RecipeRecommendation
import com.loitu.yourrefri.feature.recipe.logic.recommendRecipes
import com.loitu.yourrefri.feature.recipe.logic.ingredientIdsToConsume
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import com.loitu.yourrefri.feature.recipe.logic.buildWeeklyMealPlan
import com.loitu.yourrefri.feature.recipe.logic.buildWeeklyShoppingList
import com.loitu.yourrefri.feature.recipe.logic.shoppingIdsToStock

@Composable
fun RecipeScreen(
    state: PantryGridState,
    recipes: List<Recipe>,
    initialRecipeId: String? = null,
    onCookRecipe: ((Recipe, List<String>) -> Unit)? = null,
    initialMode: RecipeScreenMode = RecipeScreenMode.DAILY,
    onStockIngredients: ((List<String>) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var mode by remember { mutableStateOf(initialMode) }
    var goal by remember { mutableStateOf(DietGoal.BALANCED) }
    var preferences by remember { mutableStateOf(emptySet<FamilyPreferenceTag>()) }
    var swappedOffsets by remember { mutableStateOf(emptyMap<Int, Int>()) }
    val weeklyPlan = remember(recipes, goal, preferences, swappedOffsets) {
        buildWeeklyMealPlan(recipes, state, goal, preferences, swappedOffsets)
    }
    if (mode == RecipeScreenMode.WEEKLY) {
        WeeklyMealPlanContent(
            plan = weeklyPlan, shopping = buildWeeklyShoppingList(weeklyPlan, state), goal = goal,
            preferences = preferences, offsets = swappedOffsets,
            onGoal = { goal = it }, onPreference = { preferences = togglePreferenceTag(preferences, it) },
            onSwap = { swappedOffsets = bumpDaySwapOffset(swappedOffsets, it) },
            onStock = onStockIngredients, onDaily = { mode = RecipeScreenMode.DAILY }, modifier = modifier,
        )
        return
    }
    val recommendations = recommendRecipes(recipes, state)
    val recommendationIds = recommendations.map { it.recipe.id }
    val resolvedInitialRecipeId = resolveInitialRecipeId(initialRecipeId, recommendationIds)
    var selectedRecipeId by remember(recommendationIds, initialRecipeId) {
        mutableStateOf(resolvedInitialRecipeId)
    }
    var selectedDuration by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(initialRecipeId) {
        if (shouldClearDurationForRequest(initialRecipeId)) selectedDuration = null
    }
    val stepChecks = remember { mutableStateMapOf<String, Boolean>() }
    var updatedRecipeResult by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showCookConfirmation by remember { mutableStateOf(false) }
    var pendingCookRecipe by remember { mutableStateOf<Recipe?>(null) }
    val pendingCookPresentation = pendingCookRecipe?.let { recipe ->
        recipeCookPresentation(recipe, state, callbackAvailable = onCookRecipe != null)
    }

    if (showCookConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showCookConfirmation = false
                pendingCookRecipe = null
            },
            title = {
                Text(
                    if (pendingCookPresentation?.ingredientNames.isNullOrEmpty()) {
                        "Chưa có nguyên liệu cần cập nhật"
                    } else {
                        "Cập nhật nguyên liệu?"
                    }
                )
            },
            text = { Text(pendingCookPresentation?.confirmationMessage.orEmpty()) },
            confirmButton = {
                Button(onClick = {
                    showCookConfirmation = false
                    pendingCookRecipe?.let { recipe ->
                        val presentation = pendingCookPresentation
                        if (presentation != null) {
                            val idsToDispatch = confirmedCookIngredientIds(
                                presentation = presentation,
                                confirmed = true,
                                callbackAvailable = onCookRecipe != null,
                            )
                            if (idsToDispatch.isNotEmpty()) onCookRecipe?.invoke(recipe, idsToDispatch)
                            if (onCookRecipe != null) {
                                presentation.resultMessage?.let { updatedRecipeResult = recipe.id to it }
                            }
                        }
                    }
                    pendingCookRecipe = null
                }) {
                    Text(
                        when {
                            onCookRecipe == null -> "Đóng"
                            pendingCookPresentation?.ingredientIds.isNullOrEmpty() -> "Đã nấu xong"
                            else -> "Cập nhật"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCookConfirmation = false
                    pendingCookRecipe = null
                }) { Text("Để sau") }
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = true, onClick = {}, modifier = Modifier.weight(1f).heightIn(min = 48.dp), label = { Text("🍲 Gợi ý hôm nay") })
            FilterChip(selected = false, onClick = { mode = RecipeScreenMode.WEEKLY }, modifier = Modifier.weight(1f).heightIn(min = 48.dp), label = { Text("📅 Thực đơn tuần & Đi chợ") })
        }
        Text(
            text = "Hôm nay nấu gì? 🍲",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = DeepCharcoal,
        )
        Text(
            text = "3 gợi ý phù hợp nhất với nguyên liệu đang có trong nhà",
            style = MaterialTheme.typography.bodySmall,
            color = MutedGray,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp),
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            recipeDurationFilterOptions().forEach { option ->
                FilterChip(
                    modifier = Modifier.heightIn(min = 48.dp),
                    selected = selectedDuration == option.threshold,
                    onClick = { selectedDuration = option.threshold },
                    label = { Text(option.label) },
                )
            }
        }

        val visibleRecipes = filterRecipesByDurationInclusive(recommendations, selectedDuration) { item ->
            item.recipe.minutes
        }
        if (recommendations.isEmpty()) {
            Text(
                "Chưa có công thức để gợi ý.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedGray,
                modifier = Modifier.padding(vertical = 24.dp),
            )
        } else if (visibleRecipes.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Chưa có món trong khoảng thời gian này.", color = DeepCharcoal)
                FilterChip(
                    selected = false,
                    onClick = { selectedDuration = null },
                    label = { Text("Xem tất cả món") },
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }

        // Danh sách món theo thứ tự gợi ý
        visibleRecipes.forEach { recommendation ->
            val recipe = recommendation.recipe
            val isSelected = recipe.id == selectedRecipeId
            val bringIntoViewRequester = remember(recipe.id) { BringIntoViewRequester() }
            LaunchedEffect(initialRecipeId, recipe.id) {
                if (shouldBringRecipeIntoView(initialRecipeId, resolvedInitialRecipeId, recipe.id)) {
                    bringIntoViewRequester.bringIntoView()
                }
            }
            val pantryItems = state.pantry.values.toList()
            val availableIds = pantryItems.filter { it.have }.map { it.ingredientId }.toSet()
            val allNames = state.ingredients.associate { it.id to it.nameVi }
            val urgentIds = cellsOf(state)
                .filter { it.have && it.daysLeft in 0..2 }
                .mapTo(HashSet()) { it.ingredientId }
            val haveNames = ingredientIdsToConsume(recipe, availableIds, urgentIds).mapNotNull(allNames::get)
            val missingNames = recipe.required.filter { slot -> slot.anyOf.none { it in availableIds } }
                .mapNotNull { slot -> slot.anyOf.firstOrNull()?.let(allNames::get) ?: slot.anyOf.firstOrNull() }
            val readyToCook = canCook(recipe, pantryItems)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) SageGreenStrong else Color(0xFFE5E7EB),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        selectedRecipeId = recipe.id
                        updatedRecipeResult = null
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFFF9FBF8) else Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (readyToCook) {
                        Surface(shape = RoundedCornerShape(6.dp), color = SageGreenStrong) {
                            Text(
                                text = "Nấu được ngay",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = recommendationColor(recommendation.reason).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = recommendationLabel(recommendation.reason),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                color = recommendationTextColor(recommendation.reason),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = recipe.minutes?.let { "$it phút" } ?: "Thời gian chưa rõ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedGray
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepCharcoal
                    )

                    if (haveNames.isNotEmpty()) {
                        Text(
                            text = "Đã có: ${haveNames.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SageGreenText,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    if (missingNames.isNotEmpty()) {
                        Text(
                            text = "Cần thêm: ${missingNames.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TerracottaText,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Nếu món được chọn: bung chi tiết công thức 4 bước
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Cách nấu",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                            color = DeepCharcoal,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        recipe.steps.forEachIndexed { index, step ->
                            val key = "${recipe.id}_$index"
                            val isChecked = stepChecks[key] ?: false
                            Row(
                            modifier = Modifier
                                    .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { stepChecks[key] = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = SageGreenStrong,
                                        uncheckedColor = MutedGray
                                    )
                                )
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isChecked) MutedGray else DeepCharcoal
                                    ),
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Nút hành động sau khi nấu xong
                        Button(
                            onClick = {
                                pendingCookRecipe = recipe
                                showCookConfirmation = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreenStrong),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Đã nấu xong món này 🎉", color = Color.White, fontWeight = FontWeight.Medium)
                        }

                        if (updatedRecipeResult?.first == recipe.id) {
                            Text(
                                text = updatedRecipeResult?.second.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = SageGreenText,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun recommendationColor(reason: RecommendationReason): Color = when (reason) {
    RecommendationReason.USE_SOON -> Terracotta
    RecommendationReason.READY -> SageGreen
    RecommendationReason.QUICK -> WarmBeige
    RecommendationReason.OTHER -> SoftGreen
}

internal fun recommendationTextColor(reason: RecommendationReason): Color = when (reason) {
    RecommendationReason.USE_SOON -> TerracottaText
    RecommendationReason.READY -> SageGreenText
    RecommendationReason.QUICK, RecommendationReason.OTHER -> DeepCharcoal
}

internal data class RecipeDurationFilterOption(val label: String, val threshold: Int?)

internal fun recipeDurationFilterOptions(): List<RecipeDurationFilterOption> = listOf(
    RecipeDurationFilterOption("Tất cả", null),
    RecipeDurationFilterOption("Tối đa 15 phút", 15),
    RecipeDurationFilterOption("Tối đa 30 phút", 30),
)

@Composable
private fun WeeklyMealPlanContent(
    plan: List<com.loitu.yourrefri.feature.recipe.logic.PlannedDayMeal>,
    shopping: List<com.loitu.yourrefri.feature.recipe.logic.ShoppingListItem>,
    goal: DietGoal,
    preferences: Set<FamilyPreferenceTag>,
    offsets: Map<Int, Int>,
    onGoal: (DietGoal) -> Unit,
    onPreference: (FamilyPreferenceTag) -> Unit,
    onSwap: (Int) -> Unit,
    onStock: ((List<String>) -> Unit)?,
    onDaily: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val unchecked = remember(shopping) { mutableStateMapOf<String, Boolean>() }
    var openedDay by remember { mutableStateOf<Int?>(null) }
    var result by remember { mutableStateOf<String?>(null) }
    val buyItems = shopping.filterNot { it.alreadyInPantry }
    val selectedIds = shoppingIdsToStock(shopping, unchecked.filterValues { !it }.keys)
    Column(modifier.fillMaxSize().background(WarmIvory).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = false, onClick = onDaily, modifier = Modifier.weight(1f).heightIn(min = 48.dp), label = { Text("🍲 Gợi ý hôm nay") })
            FilterChip(selected = true, onClick = {}, modifier = Modifier.weight(1f).heightIn(min = 48.dp), label = { Text("📅 Thực đơn tuần & Đi chợ") })
        }
        Text("Chọn định hướng và khẩu vị, mình lo phần còn lại.", style = MaterialTheme.typography.bodyMedium, color = MutedGray, modifier = Modifier.padding(vertical = 8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DietGoal.entries.forEach { item -> FilterChip(item == goal, { onGoal(item) }, label = { Text(item.labelVi) }, modifier = Modifier.heightIn(min = 48.dp)) }
            FamilyPreferenceTag.entries.forEach { item -> FilterChip(item in preferences, { onPreference(item) }, label = { Text(item.labelVi) }, modifier = Modifier.heightIn(min = 48.dp)) }
        }
        plan.forEach { day ->
            Card(Modifier.fillMaxWidth().padding(vertical = 5.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(14.dp)) {
                    Text(day.dayLabelVi, style = MaterialTheme.typography.titleMedium, color = SageGreenText)
                    Text(day.recipe.title, Modifier.clickable { openedDay = if (openedDay == day.dayIndex) null else day.dayIndex }, style = MaterialTheme.typography.titleMedium, color = DeepCharcoal)
                    Text(day.recipe.minutes?.let { "$it phút" } ?: "Thời gian chưa rõ", color = MutedGray)
                    Text(day.matchReasonVi, color = SageGreenText)
                    TextButton(onClick = { onSwap(day.dayIndex) }) { Text("Đổi món ↻") }
                    if (openedDay == day.dayIndex) day.recipe.steps.take(4).forEachIndexed { index, step -> Text("${index + 1}. $step", Modifier.padding(vertical = 4.dp)) }
                }
            }
        }
        Text("🛒 Danh sách đi chợ tuần này", style = MaterialTheme.typography.titleLarge, color = DeepCharcoal, modifier = Modifier.padding(top = 14.dp))
        Text(shoppingSummaryText(buyItems.size, shopping.size - buyItems.size), color = SageGreenText)
        buyItems.forEach { item ->
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = unchecked[item.ingredient.id] != false, onCheckedChange = { unchecked[item.ingredient.id] = it })
                Column { Text(item.ingredient.nameVi, color = DeepCharcoal); Text("Dùng cho ${item.mealCount} bữa", color = MutedGray) }
            }
        }
        val haveItems = shopping.filter { it.alreadyInPantry }
        if (haveItems.isNotEmpty()) {
            Text("Đã có sẵn trong tủ: ${haveItems.joinToString(", ") { it.ingredient.nameVi }}", color = MutedGray, modifier = Modifier.padding(top = 4.dp))
        }
        if (buyItems.isEmpty()) {
            Text("Tuần này nhà mình đã đủ nguyên liệu 🎉", Modifier.padding(top = 8.dp), color = SageGreenText)
        } else {
            Button(onClick = {
                onStock?.invoke(selectedIds)
                if (onStock != null) result = stockResultMessage(selectedIds.size)
            }, enabled = onStock != null && selectedIds.isNotEmpty(), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(stockActionButtonLabel(selectedIds.size))
            }
        }
        result?.let { Text(it, Modifier.padding(top = 8.dp), color = SageGreenText) }
    }
}
