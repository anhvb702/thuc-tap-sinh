package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.key
import androidx.compose.ui.text.style.TextAlign
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.semantics.Role
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import com.loitu.yourrefri.ui.theme.TerracottaSoft // guard:allow - reasonPillColors bị test khóa
import com.loitu.yourrefri.ui.theme.WarmBeigeSoft // guard:allow - reasonPillColors bị test khóa
import com.loitu.yourrefri.ui.theme.WarmBeigeText // guard:allow - reasonPillColors bị test khóa
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
import com.loitu.yourrefri.ui.theme.DeepCharcoal // guard:allow - hàm màu cũ bị test khóa
import com.loitu.yourrefri.ui.theme.SageGreen // guard:allow - hàm màu cũ
import com.loitu.yourrefri.ui.theme.SageGreenText // guard:allow - hàm màu cũ
import com.loitu.yourrefri.ui.theme.SoftGreen // guard:allow - hàm màu cũ
import com.loitu.yourrefri.ui.theme.Terracotta // guard:allow - hàm màu cũ
import com.loitu.yourrefri.ui.theme.TerracottaText // guard:allow - hàm màu cũ
import com.loitu.yourrefri.ui.theme.WarmBeige // guard:allow - hàm màu cũ
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.canCook
import com.loitu.yourrefri.feature.recipe.logic.filterRecipesByDurationInclusive
import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.RandomPick
import com.loitu.yourrefri.feature.recipe.logic.pickRandomRecipe
import com.loitu.yourrefri.feature.recipe.logic.recommendRecipes
import com.loitu.yourrefri.feature.recipe.logic.ingredientIdsToConsume
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import com.loitu.yourrefri.feature.recipe.logic.buildWeeklyMealPlan
import com.loitu.yourrefri.feature.recipe.logic.buildWeeklyShoppingList
import com.loitu.yourrefri.feature.recipe.logic.shoppingIdsToStock

@Composable
@OptIn(ExperimentalTime::class)
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
    onWeeklyAction: ((WeeklyMealPlanAction) -> Unit)? = null,
    onModeChange: ((RecipeScreenMode) -> Unit)? = null,
    onOpenRecipe: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    // Yêu thích (dữ liệu do nơi gọi giữ) và Nấu rảnh tay (màn riêng); ẩn thẻ rảnh tay khi null.
    favoriteIds: Set<String> = emptySet(),
    onToggleFavorite: (String) -> Unit = {},
    onOpenHandsFree: ((String) -> Unit)? = null,
    // Thứ của hôm nay (T2 = 0 ... CN = 6) để tô nổi trong dải 7 ngày. Null: tự tính từ đồng hồ hệ thống.
    todayIndexOverride: Int? = null,
    // Nút "Nấu ngẫu nhiên": null thì ẩn. Truyền công thức CHƯA lọc chế độ ăn để nút bỏ qua khẩu vị.
    randomPool: List<Recipe>? = null,
    dietRestriction: DietRestriction = DietRestriction.NONE,
) {
    val colors = AppTheme.colors
    val randomSource = remember { kotlin.random.Random.Default }
    var randomShown by remember { mutableStateOf(false) }
    var randomPick by remember { mutableStateOf<RandomPick?>(null) }
    fun rollRandom() {
        // Rút lại thì tránh lặp món vừa ra; không còn món nào khác thì thẻ báo nhẹ và giữ nút "Quay lại".
        randomPick = pickRandomRecipe(randomPool.orEmpty(), state, randomPick?.recipe?.id, randomSource)
        randomShown = true
    }
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

    val goal = weeklyGoal ?: localGoal
    val preferences = weeklyPreferences ?: localPreferences
    val swappedOffsets = weeklySwappedOffsets ?: localSwappedOffsets
    val uncheckedIds = weeklyUncheckedIds ?: localUncheckedIds

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

    // Có nguyên liệu sắp trừ thì hỏi từng món "Dùng hết hay còn lại?"; không có thì dùng hộp xác nhận cũ.
    val askLeftover = showCookConfirmation && onCookRecipe != null && !pendingCookPresentation?.ingredientIds.isNullOrEmpty()
    if (askLeftover) {
        val presentation = pendingCookPresentation!!
        val recipe = pendingCookRecipe!!
        val names = state.ingredients.associate { it.id to it.nameVi }
        CookLeftoverDialog(
            ingredients = presentation.ingredientIds.distinct().map { it to (names[it] ?: it) },
            onConfirm = { kept ->
                val consumed = ingredientsToConsume(presentation.ingredientIds, kept)
                showCookConfirmation = false
                pendingCookRecipe = null
                // Luôn báo "nấu xong" (kể cả khi giữ lại hết) để hiện màn chúc mừng và ghi vào "Đã nấu".
                onCookRecipe?.invoke(recipe, consumed)
                updatedRecipeResult = recipe.id to cookResultMessage(consumed.mapNotNull { names[it] })
            },
            onDismiss = {
                showCookConfirmation = false
                pendingCookRecipe = null
            },
        )
    } else if (showCookConfirmation) {
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
                            onCookRecipe?.invoke(recipe, idsToDispatch)
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

    val shopping = remember(weeklyPlan, state, preferences) {
        buildWeeklyShoppingList(weeklyPlan, state, preferences, includeLunchMealPrep = LUNCH_MEAL_PREP_ENABLED)
    }
    val recommendations = recommendRecipes(recipes, state)
    val recommendationIds = recommendations.map { it.recipe.id }
    val resolvedInitialRecipeId = resolveInitialRecipeId(initialRecipeId, recommendationIds)
    // Có onOpenRecipe thì chạm thẻ mở màn chi tiết; không có thì bung cách nấu ngay dưới thẻ nổi bật.
    val expandInPlace = onOpenRecipe == null
    var selectedRecipeId by remember(recommendationIds, initialRecipeId) {
        mutableStateOf(resolvedInitialRecipeId.takeIf { initialRecipeId != null })
    }
    var selectedDuration by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(initialRecipeId) {
        if (shouldClearDurationForRequest(initialRecipeId)) selectedDuration = null
    }
    val todayIndex = todayIndexOverride
        ?: remember { todayDayIndex(Clock.System.now(), TimeZone.currentSystemDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        RecipeScreenTitle()
        RecipeTabChips(
            selected = mode,
            onSelect = { updateMode(it) },
            modifier = Modifier.padding(top = 12.dp, bottom = 16.dp),
        )

        when (mode) {
            RecipeScreenMode.WEEKLY -> WeeklyMealPlanSection(
                plan = weeklyPlan,
                goal = goal,
                preferences = preferences,
                onGoal = {
                    localGoal = it
                    if (onWeeklyAction != null) onWeeklyAction(WeeklyMealPlanAction.SetGoal(it))
                    else onWeeklyGoalChange?.invoke(it)
                },
                onPreference = { tag ->
                    val next = togglePreferenceTag(preferences, tag)
                    localPreferences = next
                    if (onWeeklyAction != null) onWeeklyAction(WeeklyMealPlanAction.TogglePreference(tag))
                    else onWeeklyPreferencesChange?.invoke(next)
                },
                onSwap = { dayIdx ->
                    val next = bumpDaySwapOffset(swappedOffsets, dayIdx)
                    localSwappedOffsets = next
                    if (onWeeklyAction != null) onWeeklyAction(WeeklyMealPlanAction.SwapDay(dayIdx))
                    else onWeeklySwappedOffsetsChange?.invoke(next)
                },
                onRequestCook = { recipe ->
                    pendingCookRecipe = recipe
                    showCookConfirmation = true
                },
                updatedRecipeResult = updatedRecipeResult,
            )

            RecipeScreenMode.SHOPPING -> WeeklyShoppingSection(
                shopping = shopping,
                uncheckedIds = uncheckedIds,
                onToggleChecked = { id, checked ->
                    val next = if (checked) uncheckedIds - id else uncheckedIds + id
                    localUncheckedIds = next
                    if (onWeeklyAction != null) onWeeklyAction(WeeklyMealPlanAction.ToggleIngredient(id, checked))
                    else onWeeklyUncheckedIdsChange?.invoke(next)
                },
                onStock = onStockIngredients,
            )

            RecipeScreenMode.DAILY -> {
                // Bộ lọc thời gian gọn, cuộn ngang được khi chữ lớn.
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    recipeDurationFilterOptions().forEach { option ->
                        PillChip(
                            label = option.label,
                            selected = selectedDuration == option.threshold,
                            onClick = { selectedDuration = option.threshold },
                        )
                    }
                    if (randomPool != null) {
                        PillChip(
                            label = RandomRecipeCopy.button,
                            selected = randomShown,
                            onClick = { rollRandom() },
                            icon = AppIcons.ChefHat,
                        )
                    }
                }
                if (randomShown) {
                    RandomRecipeCard(
                        pick = randomPick,
                        outsideDiet = randomPick?.let { isOutsideDiet(it.recipe, dietRestriction, state.ingredients) } == true,
                        onOpen = { randomPick?.let { onOpenRecipe?.invoke(it.recipe.id) } },
                        onTryAnother = { rollRandom() },
                        onBack = { randomShown = false; randomPick = null },
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }

                val visibleRecipes = filterRecipesByDurationInclusive(recommendations, selectedDuration) { item ->
                    item.recipe.minutes
                }
                val hero = heroRecommendations(visibleRecipes, resolvedInitialRecipeId.takeIf { initialRecipeId != null })

                Spacer(modifier = Modifier.height(12.dp))
                if (recommendations.isEmpty()) {
                    HeroEmptyCard()
                } else if (visibleRecipes.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Chưa có món trong khoảng thời gian này.", color = colors.text)
                        PillChip(
                            label = "Xem tất cả món",
                            selected = false,
                            onClick = { selectedDuration = null },
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                } else {
                    // Đổi bộ lọc thì pager quay về thẻ đầu.
                    key(hero.map { it.recipe.id }) {
                        val pagerState = rememberPagerState(initialPage = 0) { hero.size }
                        RecipeHeroPager(
                            items = hero,
                            pagerState = pagerState,
                            favoriteIds = favoriteIds,
                            onToggleFavorite = onToggleFavorite,
                            onOpen = { id ->
                                if (onOpenRecipe != null) {
                                    onOpenRecipe(id)
                                } else {
                                    selectedRecipeId = id
                                    updatedRecipeResult = null
                                }
                            },
                        )
                        val current = hero.getOrNull(pagerState.currentPage) ?: hero.first()
                        HeroIngredientHints(current.recipe, state)
                        if (expandInPlace) {
                            hero.firstOrNull { it.recipe.id == selectedRecipeId }?.let { selected ->
                                InlineRecipeSteps(
                                    recipe = selected.recipe,
                                    stepChecks = stepChecks,
                                    resultMessage = updatedRecipeResult?.takeIf { it.first == selected.recipe.id }?.second,
                                    onCooked = {
                                        pendingCookRecipe = selected.recipe
                                        showCookConfirmation = true
                                    },
                                )
                            }
                        }
                        DailyShortcuts(weeklyPlan, todayIndex, shopping, { updateMode(it) }, current.recipe.id.takeIf { onOpenHandsFree != null }, onOpenHandsFree)
                    }
                }
                // Chưa có gợi ý hoặc đang lọc: vẫn cho xem thực đơn tuần và đi chợ.
                if (recommendations.isEmpty() || visibleRecipes.isEmpty()) {
                    DailyShortcuts(weeklyPlan, todayIndex, shopping, { updateMode(it) }, null, null)
                }
            }
        }
    }
}

/** Trạng thái rỗng của khu thẻ nổi bật. */
@Composable
private fun HeroEmptyCard() {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp)
            .clip(shape)
            .background(colors.ctaSoft),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(AppIcons.Pot, contentDescription = null, tint = colors.ctaText, modifier = Modifier.size(36.dp))
        Text(
            text = HERO_EMPTY_MESSAGE,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.ctaText,
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
            textAlign = TextAlign.Center,
        )
    }
}

/** Dòng "Đã có / Có thể thêm" của món đang ở thẻ nổi bật (giữ thông tin từ danh sách cũ). */
@Composable
private fun HeroIngredientHints(recipe: Recipe, state: PantryGridState) {
    val colors = AppTheme.colors
    val pantryItems = state.pantry.values.toList()
    val availableIds = pantryItems.filter { it.have }.map { it.ingredientId }.toSet()
    val allNames = state.ingredients.associate { it.id to it.nameVi }
    val urgentIds = cellsOf(state)
        .filter { it.have && it.daysLeft in 0..2 }
        .mapTo(HashSet()) { it.ingredientId }
    val haveNames = ingredientIdsToConsume(recipe, availableIds, urgentIds).mapNotNull(allNames::get)
    val missingNames = recipe.required.filter { slot -> slot.anyOf.none { it in availableIds } }
        .mapNotNull { slot -> slot.anyOf.firstOrNull()?.let(allNames::get) ?: slot.anyOf.firstOrNull() }
    if (haveNames.isNotEmpty()) {
        Text(
            text = "Đã có: ${haveNames.joinToString(", ")}",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.accentText,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
    if (missingNames.isNotEmpty()) {
        Text(
            text = "Có thể thêm: ${missingNames.joinToString(", ")}",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.warnText,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

/** Cách nấu bung tại chỗ (khi người gọi không truyền onOpenRecipe). */
@Composable
private fun InlineRecipeSteps(
    recipe: Recipe,
    stepChecks: MutableMap<String, Boolean>,
    resultMessage: String?,
    onCooked: () -> Unit,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(shape)
            .background(colors.surface)
            .border(2.dp, colors.primary, shape)
            .padding(14.dp),
    ) {
        Text(
            text = "Cách nấu",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.text,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        recipe.steps.forEachIndexed { index, step ->
            val key = "${recipe.id}_$index"
            val isChecked = stepChecks[key] ?: false
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { stepChecks[key] = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.primary,
                        uncheckedColor = colors.muted,
                        checkmarkColor = colors.onPrimary,
                    ),
                )
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodyMedium.copy(color = if (isChecked) colors.muted else colors.text),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
        Button(
            onClick = onCooked,
            colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp).heightIn(min = 48.dp),
        ) {
            Text("Đã nấu xong món này", fontWeight = FontWeight.SemiBold)
        }
        if (resultMessage != null) {
            Text(
                text = resultMessage,
                style = MaterialTheme.typography.bodySmall,
                color = colors.accentText,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

/** Dải 7 ngày, thẻ đi chợ và (nếu có) thẻ Nấu rảnh tay. */
@Composable
private fun DailyShortcuts(
    weeklyPlan: List<com.loitu.yourrefri.feature.recipe.logic.PlannedDayMeal>,
    todayIndex: Int,
    shopping: List<com.loitu.yourrefri.feature.recipe.logic.ShoppingListItem>,
    onGoTo: (RecipeScreenMode) -> Unit,
    handsFreeRecipeId: String?,
    onOpenHandsFree: ((String) -> Unit)?,
) {
    // Thực đơn tuần đã có nút riêng ở Trang chủ và tab "Thực đơn tuần"; không lặp lại dải 7 ngày ở đây.
    Spacer(modifier = Modifier.height(20.dp))
    ShortcutCard(
        icon = AppIcons.Cart,
        title = SHOPPING_CARD_TITLE,
        subtitle = SHOPPING_CARD_SUBTITLE,
        extraLine = shoppingCardCountText(shopping.count { !it.alreadyInPantry || it.missingQuantity > 0 }),
        onClick = { onGoTo(RecipeScreenMode.SHOPPING) },
    )
    if (onOpenHandsFree != null && handsFreeRecipeId != null) {
        Spacer(modifier = Modifier.height(12.dp))
        ShortcutCard(
            icon = AppIcons.ChefHat,
            title = HANDS_FREE_CARD_TITLE,
            subtitle = HANDS_FREE_CARD_SUBTITLE,
            onClick = { onOpenHandsFree(handsFreeRecipeId) },
        )
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

// Màu nhãn lý do: cùng bảng màu với thẻ gợi ý ở màn Hôm nay.
internal fun reasonPillColors(reason: RecommendationReason): Pair<Color, Color> = when (reason) {
    RecommendationReason.USE_SOON -> TerracottaSoft to TerracottaText
    RecommendationReason.QUICK -> WarmBeigeSoft to WarmBeigeText
    RecommendationReason.READY, RecommendationReason.OTHER -> SoftGreen to DeepCharcoal
}

// Bản vẽ theo chế độ sáng/tối của nhãn lý do (bản hằng số ở trên giữ cho test).
@Composable
internal fun reasonPillColorsThemed(reason: RecommendationReason): Pair<Color, Color> {
    val colors = AppTheme.colors
    return when (reason) {
        RecommendationReason.USE_SOON -> colors.warnSoft to colors.warnText
        RecommendationReason.QUICK -> colors.beigeSoft to colors.beigeText
        RecommendationReason.READY, RecommendationReason.OTHER -> colors.primarySoft to colors.text
    }
}

// Thanh chia đoạn "Hôm nay | Cả tuần"; mỗi nửa cao >= 48dp.
@Composable
internal fun RecipeSegmentBar(selectedMode: RecipeScreenMode, onSelect: (RecipeScreenMode) -> Unit) {
    val modes = listOf(RecipeScreenMode.DAILY, RecipeScreenMode.WEEKLY)
    val labels = recipeSegmentLabels()
    val colors = AppTheme.colors
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceSoft,
        border = BorderStroke(1.dp, colors.line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            modes.forEachIndexed { index, segmentMode ->
                val selected = segmentMode == selectedMode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) colors.primary else Color.Transparent)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(segmentMode) })
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = labels[index],
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (selected) colors.onPrimary else colors.text,
                    )
                }
            }
        }
    }
}
