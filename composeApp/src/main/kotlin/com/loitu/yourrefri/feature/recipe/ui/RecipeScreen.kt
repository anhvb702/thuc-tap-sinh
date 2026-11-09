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
    weeklyGoal: DietGoal? = null,
    onWeeklyGoalChange: ((DietGoal) -> Unit)? = null,
    weeklyPreferences: Set<FamilyPreferenceTag>? = null,
    onWeeklyPreferencesChange: ((Set<FamilyPreferenceTag>) -> Unit)? = null,
    weeklySwappedOffsets: Map<Int, Int>? = null,
    onWeeklySwappedOffsetsChange: ((Map<Int, Int>) -> Unit)? = null,
    weeklyUncheckedIds: Set<String>? = null,
    onWeeklyUncheckedIdsChange: ((Set<String>) -> Unit)? = null,
    weeklyIncludeLunchMealPrep: Boolean? = null,
    onWeeklyIncludeLunchMealPrepChange: ((Boolean) -> Unit)? = null,
    onModeChange: ((RecipeScreenMode) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var localMode by remember(initialMode) { mutableStateOf(initialMode) }
    val mode = localMode
    fun updateMode(newMode: RecipeScreenMode) {
        localMode = newMode
        onModeChange?.invoke(newMode)
    }

    var localGoal by remember { mutableStateOf(DietGoal.BALANCED) }
    var localPreferences by remember { mutableStateOf(emptySet<FamilyPreferenceTag>()) }
    var localSwappedOffsets by remember { mutableStateOf(emptyMap<Int, Int>()) }
    var localUncheckedIds by remember { mutableStateOf(emptySet<String>()) }
    var localIncludeLunchMealPrep by remember { mutableStateOf(false) }

    val goal = weeklyGoal ?: localGoal
    val preferences = weeklyPreferences ?: localPreferences
    val swappedOffsets = weeklySwappedOffsets ?: localSwappedOffsets
    val uncheckedIds = weeklyUncheckedIds ?: localUncheckedIds
    val includeLunchMealPrep = weeklyIncludeLunchMealPrep ?: localIncludeLunchMealPrep

    var initialPantrySnapshot by remember { mutableStateOf(state) }
    LaunchedEffect(state.pantry.isNotEmpty()) {
        if (initialPantrySnapshot.pantry.isEmpty() && state.pantry.isNotEmpty()) {
            initialPantrySnapshot = state
        }
    }

    val weeklyPlan = remember(recipes, initialPantrySnapshot, goal, preferences, swappedOffsets) {
        buildWeeklyMealPlan(recipes, initialPantrySnapshot, goal, preferences, swappedOffsets)
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

    if (mode == RecipeScreenMode.WEEKLY) {
        WeeklyMealPlanContent(
            plan = weeklyPlan,
            shopping = buildWeeklyShoppingList(weeklyPlan, state, preferences, includeLunchMealPrep = includeLunchMealPrep),
            goal = goal,
            preferences = preferences,
            includeLunchMealPrep = includeLunchMealPrep,
            onToggleLunchMealPrep = {
                localIncludeLunchMealPrep = it
                onWeeklyIncludeLunchMealPrepChange?.invoke(it)
            },
            uncheckedIds = uncheckedIds,
            onToggleChecked = { id, checked ->
                val next = if (checked) uncheckedIds - id else uncheckedIds + id
                localUncheckedIds = next
                onWeeklyUncheckedIdsChange?.invoke(next)
            },
            onGoal = {
                localGoal = it
                onWeeklyGoalChange?.invoke(it)
            },
            onPreference = { tag ->
                val next = togglePreferenceTag(preferences, tag)
                localPreferences = next
                onWeeklyPreferencesChange?.invoke(next)
            },
            onSwap = { dayIdx ->
                val next = bumpDaySwapOffset(swappedOffsets, dayIdx)
                localSwappedOffsets = next
                onWeeklySwappedOffsetsChange?.invoke(next)
            },
            onRequestCook = { recipe ->
                pendingCookRecipe = recipe
                showCookConfirmation = true
            },
            updatedRecipeResult = updatedRecipeResult,
            onStock = onStockIngredients,
            onDaily = { updateMode(RecipeScreenMode.DAILY) },
            modifier = modifier,
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = true, onClick = {}, modifier = Modifier.weight(1f).heightIn(min = 48.dp), label = { Text("🍲 Gợi ý hôm nay") })
            FilterChip(selected = false, onClick = { updateMode(RecipeScreenMode.WEEKLY) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp), label = { Text("📅 Thực đơn tuần & Đi chợ") })
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
    includeLunchMealPrep: Boolean,
    onToggleLunchMealPrep: (Boolean) -> Unit,
    uncheckedIds: Set<String>,
    onToggleChecked: (String, Boolean) -> Unit,
    onGoal: (DietGoal) -> Unit,
    onPreference: (FamilyPreferenceTag) -> Unit,
    onSwap: (Int) -> Unit,
    onRequestCook: (Recipe) -> Unit,
    updatedRecipeResult: Pair<String, String>?,
    onStock: ((List<String>) -> Unit)?,
    onDaily: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var openedDay by remember { mutableStateOf<Int?>(null) }
    var result by remember { mutableStateOf<String?>(null) }
    val buyItems = shopping.filter { !it.alreadyInPantry || it.missingQuantity > 0 }
    val selectedIds = shoppingIdsToStock(shopping, uncheckedIds)
    val stockListForAction = remember(shopping, uncheckedIds) {
        shopping.filter { (!it.alreadyInPantry || it.missingQuantity > 0) && it.ingredient.id !in uncheckedIds }
            .flatMap { item -> List(item.missingQuantity.coerceAtLeast(1)) { item.ingredient.id } }
    }
    Column(modifier.fillMaxSize().background(WarmIvory).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = false, onClick = onDaily, modifier = Modifier.weight(1f).heightIn(min = 48.dp), label = { Text("🍲 Gợi ý hôm nay") })
            FilterChip(selected = true, onClick = {}, modifier = Modifier.weight(1f).heightIn(min = 48.dp), label = { Text("📅 Thực đơn tuần & Đi chợ") })
        }
        Surface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF3F6F2),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2EBE0))
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = "💡 Lưu ý về bữa cơm gia đình:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = DeepCharcoal,
                )
                Text(
                    text = "Thực đơn được thiết kế cho Bữa cơm chính trong ngày (Trưa hoặc Tối) gồm 1 món mặn + 1 món canh/rau để cả nhà ăn ngon mà không bị áp lực lên lịch quá nhiều bữa.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DeepCharcoal,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Text("Chọn định hướng và khẩu vị, mình xếp sẵn mâm cơm (Món mặn + Canh/Rau) cả tuần.", style = MaterialTheme.typography.bodyMedium, color = MutedGray, modifier = Modifier.padding(bottom = 6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DietGoal.entries.forEach { item -> FilterChip(item == goal, { onGoal(item) }, label = { Text(item.labelVi) }, modifier = Modifier.heightIn(min = 48.dp)) }
            FamilyPreferenceTag.entries.forEach { item -> FilterChip(item in preferences, { onPreference(item) }, label = { Text(item.labelVi) }, modifier = Modifier.heightIn(min = 48.dp)) }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            FilterChip(
                selected = includeLunchMealPrep,
                onClick = { onToggleLunchMealPrep(!includeLunchMealPrep) },
                modifier = Modifier.heightIn(min = 48.dp),
                label = { Text(if (includeLunchMealPrep) "🍱 Đang tính thêm phần cơm trưa hôm sau" else "🍱 Nấu thêm phần cơm trưa hôm sau") }
            )
        }
        plan.forEach { day ->
            val isExpanded = openedDay == day.dayIndex
            Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(dayMealCardHeader(day.dayLabelVi, day.mealTypeVi), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SageGreenText)
                        val totalMinutes = day.dishes.mapNotNull { it.minutes }.maxOrNull()
                        Text(totalMinutes?.let { "⏱️ ~$it phút" } ?: "Thời gian chưa rõ", style = MaterialTheme.typography.bodySmall, color = MutedGray)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("🍖 Món chính: ${day.recipe.title}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DeepCharcoal)
                    day.sideRecipe?.let { side ->
                        Text("🥬 Canh/Rau: ${side.title}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = DeepCharcoal, modifier = Modifier.padding(top = 2.dp))
                    }
                    Text(day.matchReasonVi, style = MaterialTheme.typography.bodySmall, color = SageGreenText, modifier = Modifier.padding(top = 4.dp))
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { openedDay = if (isExpanded) null else day.dayIndex },
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text(if (isExpanded) "Thu gọn cách nấu ▲" else "Xem cách nấu ▼", color = SageGreenText, fontWeight = FontWeight.SemiBold)
                        }
                        TextButton(
                            onClick = { onSwap(day.dayIndex) },
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text("Đổi thực đơn ↻", color = DeepCharcoal)
                        }
                    }
                    if (isExpanded) {
                        day.dishes.forEach { dish ->
                            Spacer(Modifier.height(8.dp))
                            Text("Cách nấu ${dish.title}:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = DeepCharcoal)
                            dish.steps.forEachIndexed { index, step ->
                                Text("${index + 1}. $step", style = MaterialTheme.typography.bodyMedium, color = DeepCharcoal, modifier = Modifier.padding(vertical = 3.dp))
                            }
                            Button(
                                onClick = { onRequestCook(dish) },
                                colors = ButtonDefaults.buttonColors(containerColor = SageGreenStrong),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp).heightIn(min = 48.dp),
                            ) {
                                Text("Đã nấu xong: ${dish.title} 🎉", color = Color.White)
                            }
                            if (updatedRecipeResult?.first == dish.id) {
                                Text(updatedRecipeResult.second, style = MaterialTheme.typography.bodySmall, color = SageGreenText, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
        Text("🛒 Danh sách đi chợ tuần này", style = MaterialTheme.typography.titleLarge, color = DeepCharcoal, modifier = Modifier.padding(top = 14.dp))
        Text(shoppingSummaryText(buyItems.size, shopping.size - buyItems.size), color = SageGreenText)
        buyItems.forEach { item ->
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = item.ingredient.id !in uncheckedIds, onCheckedChange = { onToggleChecked(item.ingredient.id, it) })
                Column {
                    Text(item.ingredient.nameVi, color = DeepCharcoal, fontWeight = FontWeight.Medium)
                    Text(
                        text = shoppingItemSubtitle(
                            mealCount = item.mealCount,
                            availableQuantity = item.availableQuantity,
                            missingQuantity = item.missingQuantity,
                            isLunchPrep = includeLunchMealPrep,
                        ),
                        color = MutedGray,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        val haveItems = shopping.filter { it.alreadyInPantry && it.missingQuantity == 0 }
        if (haveItems.isNotEmpty()) {
            Text("Đã đủ cho cả tuần: ${haveItems.joinToString(", ") { it.ingredient.nameVi }}", color = MutedGray, modifier = Modifier.padding(top = 4.dp))
        }
        if (buyItems.isEmpty()) {
            Text("Tuần này nhà mình đã đủ nguyên liệu 🎉", Modifier.padding(top = 8.dp), color = SageGreenText)
        } else {
            Button(onClick = {
                onStock?.invoke(stockListForAction)
                if (onStock != null) result = stockResultMessage(selectedIds.size)
            }, enabled = onStock != null && selectedIds.isNotEmpty(), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(stockActionButtonLabel(selectedIds.size))
            }
        }
        result?.let { Text(it, Modifier.padding(top = 8.dp), color = SageGreenText) }
    }
}
