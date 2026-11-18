package com.loitu.yourrefri.feature.home.ui

import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.home.logic.homeGreeting
import com.loitu.yourrefri.feature.pantry.logic.ExpiringItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.feature.pantry.logic.expiringList
import com.loitu.yourrefri.feature.pantry.ui.estimateDaysText
import com.loitu.yourrefri.feature.pantry.ui.isLeftoverUrgent
import com.loitu.yourrefri.feature.pantry.data.LeftoverItem
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import com.loitu.yourrefri.feature.cooklog.ui.CookHistoryCopy
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import com.loitu.yourrefri.feature.recipe.logic.recommendRecipes
import com.loitu.yourrefri.feature.recipe.logic.RandomPick
import com.loitu.yourrefri.feature.recipe.logic.pickRandomRecipe
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import kotlin.random.Random
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.logic.defaultMealFor
import com.loitu.yourrefri.feature.recipe.logic.recipesForMeal
import com.loitu.yourrefri.feature.recipe.ui.MealCopy
import com.loitu.yourrefri.feature.recipe.ui.PillChip
import com.loitu.yourrefri.feature.recipe.ui.ShortcutCard
import com.loitu.yourrefri.feature.recipe.ui.WEEKLY_BUTTON_LABEL
import com.loitu.yourrefri.feature.recipe.ui.emptyDietRecommendationCopy
import com.loitu.yourrefri.feature.recipe.ui.emptyRecommendationCopy
import com.loitu.yourrefri.feature.recipe.ui.shortReasonLabel
import com.loitu.yourrefri.ui.components.FoodThumb
import com.loitu.yourrefri.ui.components.RecipeThumb
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import com.loitu.yourrefri.ui.theme.SageGreenStrong // guard:allow

// Nút hành động chính: nền xanh sage đậm, chữ trắng (WCAG AA >= 4.5).
internal data class ActionStyle(val container: Color, val content: Color)

internal val weeklyPlanActionStyle = ActionStyle(container = SageGreenStrong, content = Color.White) // guard:allow

// Cỡ chữ hệ thống từ 1.2 trở lên thì khối trong thẻ tủ xếp dọc, không đặt cạnh nhau.
internal fun summaryButtonBelowText(fontScale: Float): Boolean = fontScale >= 1.2f

// Lời chào dùng chung từ :shared có kèm ký hiệu trang trí; giao diện mới chỉ hiện chữ.
internal fun greetingText(hour: Int): String = homeGreeting(hour).replace(" 🌿", "") // guard:allow (chuỗi dùng chung ở :shared còn ký hiệu, chỉ cắt khi hiển thị)

// Bản ngắn của statusText cho thẻ hẹp; tiêu đề mục đã ghi "Hạn là ước tính".
internal fun shortEstimateText(daysLeft: Int): String = estimateDaysText(daysLeft)

// Số món đang có theo khu vực bảo quản (ngăn mát / ngăn đông).
internal data class PantryZoneCounts(val fridge: Int, val freezer: Int)

internal fun pantryZoneCounts(haveItems: List<PantryCell>): PantryZoneCounts {
    val freezer = haveItems.count { it.compartment == PantryCompartment.FREEZE }
    return PantryZoneCounts(fridge = haveItems.size - freezer, freezer = freezer)
}

// Dòng chữ dưới tên món cần dùng sớm; luôn là ước tính, không khẳng định an toàn.
internal fun urgentCaption(daysLeft: Int): String = estimateDaysText(daysLeft)

// Q25: ngăn đông quá hạn ghi số ngày như các ngăn khác (cùng estimateDaysText).
internal fun urgentCaption(daysLeft: Int, isExpiredFrozen: Boolean): String = urgentCaption(daysLeft)

internal sealed interface UrgentStripEntry {
    val key: String
    val name: String
    val caption: String
    val isLeftover: Boolean

    data class Leftover(
        val item: LeftoverItem,
        override val caption: String,
    ) : UrgentStripEntry {
        override val key: String get() = "leftover:${item.id}"
        override val name: String get() = item.title
        override val isLeftover: Boolean get() = true
    }

    data class Ingredient(
        val item: ExpiringItem,
        override val caption: String,
    ) : UrgentStripEntry {
        override val key: String get() = "ingredient:${item.item.ingredientId}"
        override val name: String get() = item.ingredient.nameVi
        override val isLeftover: Boolean get() = false
    }
}

private fun leftoverDaysLeftForHome(item: LeftoverItem, today: LocalDate): Int {
    val expiry = item.markedAt.plus(item.shelfDays, DateTimeUnit.DAY)
    return (expiry.toEpochDays() - today.toEpochDays()).toInt()
}

// Q17 / Q27 (R4-02) & Q25 (R4-05): Đồ ăn thừa cần dùng sớm đứng đầu dải, tiếp theo là nguyên liệu từ expiringList().
internal fun buildUrgentStrip(
    state: PantryGridState,
    leftovers: List<LeftoverItem>,
): List<UrgentStripEntry> {
    val urgentLeftovers = leftovers
        .map { item -> item to leftoverDaysLeftForHome(item, state.today) }
        .filter { (item, _) -> isLeftoverUrgent(item, state.today) }
        .sortedWith(compareBy<Pair<LeftoverItem, Int>> { it.second }.thenBy { it.first.id })
        .map { (item, daysLeft) ->
            UrgentStripEntry.Leftover(
                item = item,
                caption = urgentCaption(
                    daysLeft = daysLeft,
                    isExpiredFrozen = item.compartment == PantryCompartment.FREEZE && daysLeft < 0,
                ),
            )
        }

    val expiringIngredients = expiringList(
        pantry = state.pantry.values.toList(),
        ingredients = state.ingredients,
        today = state.today,
    ).map { expiring ->
        UrgentStripEntry.Ingredient(
            item = expiring,
            caption = urgentCaption(expiring.daysLeft, isExpiredFrozen = expiring.isExpiredFrozen),
        )
    }

    return urgentLeftovers + expiringIngredients
}

internal fun shouldShowDefrostCard(defrostItems: List<String>): Boolean = defrostItems.isNotEmpty()

internal fun defrostSummaryText(defrostItems: List<String>): String =
    "Cho bữa tối hôm nay: ${defrostItems.joinToString(", ")}"

// Chữ lớn từ 1.3 thì dải gợi ý món cuộn ngang để tên món không bị cắt.
internal fun recipeStripScrolls(fontScale: Float): Boolean = fontScale >= 1.3f

internal const val homeRecipeLimit = 3

internal data class GachaIngredient(val id: String, val have: Boolean)

internal fun gachaIngredients(recipe: Recipe, state: PantryGridState): List<GachaIngredient> =
    recipe.required.map { slot ->
        val present = slot.anyOf.firstOrNull { state.pantry[it]?.have == true }
        GachaIngredient(present ?: slot.anyOf.first(), present != null)
    }

/**
 * Dải quay: [candidates] lặp 3 vòng, rồi món trúng, rồi 3 món đệm bên phải.
 * Trả về dải và vị trí món trúng (luôn có món đệm ở cả hai bên).
 */
internal fun gachaReel(candidates: List<Recipe>, winner: Recipe): Pair<List<Recipe>, Int> {
    val prefix = List(3) { candidates }.flatten()
    return (prefix + listOf(winner) + candidates.take(3)) to prefix.size
}

internal fun pickHomeGacha(recipes: List<Recipe>, state: PantryGridState, previousId: String?, random: Random): RandomPick? =
    pickRandomRecipe(recipes, state, previousId.takeIf { recipes.size > 1 }, random)

internal fun pantryCardDescription(count: Int, zones: PantryZoneCounts): String =
    if (count == 0) {
        "Tủ đang trống. Chạm để chọn món"
    } else {
        "Trong tủ hiện có $count nguyên liệu, ${zones.fridge} ở ngăn mát, ${zones.freezer} ở ngăn đông. Chạm để xem tủ"
    }

@Composable
fun HomeScreen(
    state: PantryGridState,
    recipes: List<Recipe>,
    onNavigateToPantry: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    onNavigateToWeeklyRecipes: () -> Unit = onNavigateToRecipes,
    onAddDailyShopping: (List<String>) -> Unit = {},
    onAdjustDiet: () -> Unit = {},
    // Lối vào "Đã nấu"; null thì ẩn.
    onNavigateToHistory: (() -> Unit)? = null,
    // Bữa đang chọn (do MainScaffold giữ để lịch sử "Đã nấu" ghi đúng bữa này).
    meal: MealSlot = defaultMealFor(state.hourOfDay),
    onMealChange: (MealSlot) -> Unit = {},
    leftovers: List<LeftoverItem> = emptyList(),
    defrostItems: List<String> = emptyList(),
    onOpenLeftovers: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val haveItems = cellsOf(state).filter { it.have }
    val zones = pantryZoneCounts(haveItems)
    val urgentEntries = buildUrgentStrip(state, leftovers)
    // Bữa trưa ưu tiên món nhanh; bữa tối giữ nguyên danh sách.
    val shownMeal = homeMealChoice(meal)
    val recommendations = recommendRecipes(recipesForMeal(recipes, shownMeal), state).take(homeRecipeLimit)
    var showGacha by remember { mutableStateOf(false) }

    if (showGacha) {
        HomeGachaDialog(
            recipes = recipes,
            state = state,
            onDismiss = { showGacha = false },
            onAdjustDiet = { showGacha = false; onAdjustDiet() },
            onAddDailyShopping = onAddDailyShopping,
            onOpenRecipe = { id -> showGacha = false; onNavigateToRecipe(id) },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = greetingText(state.hourOfDay),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
        )
        Text(
            text = MealCopy.heading(meal),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
        )
        // Chọn bữa: bữa trưa ưu tiên món nhanh, bữa tối như cũ; bữa này cũng là bữa ghi vào "Đã nấu".
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 14.dp)) {
            homeMealChoices().forEach { option ->
                PillChip(label = MealCopy.label(option), selected = option == shownMeal, onClick = { onMealChange(option) })
            }
        }

        WeeklyPlanButton(onClick = onNavigateToWeeklyRecipes)
        if (shouldShowDefrostCard(defrostItems)) {
            Spacer(modifier = Modifier.height(12.dp))
            DefrostReminderCard(items = defrostItems, onClick = onNavigateToWeeklyRecipes)
        }
        Spacer(modifier = Modifier.height(16.dp))

        PantrySummaryCard(count = haveItems.size, zones = zones, onOpenPantry = onNavigateToPantry)

        Spacer(modifier = Modifier.height(16.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = colors.surfaceSoft, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Không biết ăn gì? Đừng lo", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.text)
                Button(onClick = { showGacha = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Icon(AppIcons.Wheel, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Nấu ngẫu nhiên")
                }
                Text("Miễn phí, không mua gì thêm, chỉ chọn món ăn thôi.", style = MaterialTheme.typography.bodySmall, color = colors.muted)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(title = "Cần dùng sớm", action = "Xem tất cả", onAction = onNavigateToPantry)
        if (urgentEntries.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.line),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(AppIcons.Leaf, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(22.dp))
                    Text(
                        text = "Tủ lạnh đang rất gọn gàng, không có món nào sắp hỏng cả!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.accentText,
                    )
                }
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(urgentEntries, key = { it.key }) { entry ->
                    UrgentCard(
                        ingredientId = (entry as? UrgentStripEntry.Ingredient)?.item?.item?.ingredientId,
                        name = entry.name,
                        caption = entry.caption,
                        onClick = if (entry.isLeftover) onOpenLeftovers else onNavigateToPantry,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(title = "Gợi ý bữa cơm hôm nay", action = "Xem thêm", onAction = onNavigateToRecipes)
        if (recommendations.isEmpty()) {
            if (recipes.isEmpty()) {
                Text(
                    text = emptyDietRecommendationCopy.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted,
                )
                TextButton(onClick = onAdjustDiet, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(emptyDietRecommendationCopy.actionLabel)
                }
            } else {
                Text(
                    text = emptyRecommendationCopy.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted,
                )
                TextButton(onClick = onNavigateToPantry, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(emptyRecommendationCopy.actionLabel)
                }
            }
        } else if (recipeStripScrolls(LocalDensity.current.fontScale)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(recommendations, key = { it.recipe.id }) { r ->
                    HomeRecipeCard(r.recipe, r.reason, Modifier.width(176.dp)) { onNavigateToRecipe(r.recipe.id) }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                recommendations.forEach { r ->
                    HomeRecipeCard(r.recipe, r.reason, Modifier.weight(1f).fillMaxHeight()) { onNavigateToRecipe(r.recipe.id) }
                }
            }
        }

        if (onNavigateToHistory != null) {
            Spacer(modifier = Modifier.height(16.dp))
            ShortcutCard(
                icon = AppIcons.Clock,
                title = CookHistoryCopy.title,
                subtitle = CookHistoryCopy.homeSubtitle,
                onClick = onNavigateToHistory,
            )
        }
    }
}

@Composable
private fun HomeGachaDialog(
    recipes: List<Recipe>,
    state: PantryGridState,
    onDismiss: () -> Unit,
    onAdjustDiet: () -> Unit,
    onAddDailyShopping: (List<String>) -> Unit,
    onOpenRecipe: (String) -> Unit,
) {
    val colors = AppTheme.colors
    val haptic = LocalHapticFeedback.current
    var pick by remember(recipes) { mutableStateOf(pickHomeGacha(recipes, state, null, Random.Default)) }
    var roll by remember { mutableStateOf(0) }
    var landed by remember { mutableStateOf(false) }
    var added by remember { mutableStateOf(false) }
    val position = remember { Animatable(0f) }
    // Dải quay: món trúng nằm giữa, có thêm món đệm bên phải để không bị trống.
    val (reel, winnerIndex) = remember(recipes, pick, roll) {
        val candidates = recipes.shuffled(Random(roll)).take(8)
        val winner = pick?.recipe
        if (winner == null) emptyList<Recipe>() to 0 else gachaReel(candidates, winner)
    }
    LaunchedEffect(roll, pick?.recipe?.id) {
        landed = false
        if (pick != null) {
            position.snapTo(0f)
            val finalPosition = winnerIndex.toFloat()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || ValueAnimator.areAnimatorsEnabled()) {
                position.animateTo(finalPosition, tween(durationMillis = 4200, easing = FastOutSlowInEasing))
            } else {
                position.snapTo(finalPosition)
            }
            landed = true
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nấu ngẫu nhiên") },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val chosen = pick
                if (chosen == null) {
                    Text("Chưa có món phù hợp với chế độ ăn hiện tại. Bạn thử điều chỉnh chế độ ăn nhé.", color = colors.text)
                    TextButton(onClick = onAdjustDiet, modifier = Modifier.heightIn(min = 48.dp)) { Text("Điều chỉnh chế độ ăn") }
                } else {
                    BoxWithConstraints(Modifier.fillMaxWidth().height(84.dp).clip(RoundedCornerShape(16.dp)).background(colors.surfaceSoft)) {
                        val left = maxWidth / 2 - 64.dp
                        Row(
                            Modifier
                                .wrapContentWidth(unbounded = true, align = Alignment.Start)
                                .offset(x = left - (position.value * 136).dp),
                        ) {
                            reel.forEachIndexed { index, item ->
                                // Vạch chéo đen tách hai món liền nhau.
                                if (index > 0) {
                                    Canvas(Modifier.width(8.dp).height(84.dp)) {
                                        drawLine(color = Color.Black, start = Offset(0f, size.height), end = Offset(size.width, 0f), strokeWidth = 3f) // guard:allow (vạch đen theo yêu cầu, không theo theme)
                                    }
                                }
                                Surface(Modifier.width(128.dp).height(84.dp), shape = RoundedCornerShape(12.dp), color = colors.surface) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                                        Text(item.title, maxLines = 2, overflow = TextOverflow.Ellipsis, color = colors.text)
                                    }
                                }
                            }
                        }
                    }
                    if (landed) {
                        Column(Modifier.semantics { liveRegion = LiveRegionMode.Assertive }) {
                            Text("Nay tổ tiên bảo bạn nấu món này nè", color = colors.accentText)
                            Text(chosen.recipe.title, style = MaterialTheme.typography.titleLarge, color = colors.text)
                        }
                        val ingredients = gachaIngredients(chosen.recipe, state)
                        val names = state.ingredients.associate { it.id to it.nameVi }
                        val have = ingredients.filter { it.have }
                        val missing = ingredients.filterNot { it.have }
                        Text("Đã có: " + if (have.isEmpty()) "Chưa có nguyên liệu" else have.joinToString(", ") { names[it.id] ?: it.id }, color = colors.text)
                        Text("Cần thêm: " + if (missing.isEmpty()) "Đã đủ nguyên liệu" else missing.joinToString(", ") { names[it.id] ?: it.id }, color = colors.text)
                        if (missing.isNotEmpty()) {
                            TextButton(onClick = { onAddDailyShopping(missing.map { it.id }.distinct()); added = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                                Text(if (added) "Đã đưa vào Đi chợ hàng ngày" else "Đưa vào Đi chợ hàng ngày")
                            }
                        }
                        Button(onClick = { onOpenRecipe(chosen.recipe.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Nấu món này") }
                        TextButton(onClick = {
                            pick = pickHomeGacha(recipes, state, chosen.recipe.id, Random.Default)
                            added = false
                            roll++
                        }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Chọn món khác") }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) { Text("Đóng") } },
    )
}

@Composable
private fun SectionHeader(title: String, action: String, onAction: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onAction, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(action, color = colors.ctaText, fontWeight = FontWeight.SemiBold)
            Icon(AppIcons.Chevron, contentDescription = null, tint = colors.ctaText, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun IconBadge(icon: ImageVector, container: Color, tint: Color, size: Int = 40) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size((size * 0.55f).dp))
    }
}

// Thẻ "Trong tủ hiện có": cả thẻ là một nút mở tủ.
@Composable
private fun PantrySummaryCard(count: Int, zones: PantryZoneCounts, onOpenPantry: () -> Unit) {
    val colors = AppTheme.colors
    val stacked = summaryButtonBelowText(LocalDensity.current.fontScale)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .semantics(mergeDescendants = true) { contentDescription = pantryCardDescription(count, zones) }
            .clickable(role = Role.Button, onClick = onOpenPantry),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceSoft,
        border = BorderStroke(1.dp, colors.primarySoft),
    ) {
        if (count == 0) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconBadge(AppIcons.Fridge, colors.primarySoft, colors.accentText)
                    Column {
                        Text(
                            "Tủ đang trống",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.text,
                        )
                        Text(
                            "Mình thêm vài món quen thuộc nhé",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.accentText,
                        )
                    }
                }
                Button(
                    onClick = onOpenPantry,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Chọn món", fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            val countBlock: @Composable () -> Unit = {
                Column {
                    Text("Trong tủ hiện có", style = MaterialTheme.typography.bodySmall, color = colors.muted)
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "$count",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.text,
                        )
                        Text(
                            "nguyên liệu",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.muted,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                }
            }
            val stats: @Composable () -> Unit = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ZoneStat(AppIcons.Fridge, zones.fridge, "Ngăn mát")
                    ZoneStat(AppIcons.Snowflake, zones.freezer, "Ngăn đông")
                }
            }
            if (stacked) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    countBlock()
                    stats()
                }
            } else {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(modifier = Modifier.weight(1f)) { countBlock() }
                    stats()
                }
            }
        }
    }
}

@Composable
private fun ZoneStat(icon: ImageVector, value: Int, label: String) {
    val colors = AppTheme.colors
    Surface(shape = RoundedCornerShape(14.dp), color = colors.surface, border = BorderStroke(1.dp, colors.line)) {
        Column(
            modifier = Modifier
                .widthIn(min = 76.dp)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(icon, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(18.dp))
                Text(
                    "$value",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
            }
            Text(label, style = MaterialTheme.typography.bodySmall, color = colors.muted)
        }
    }
}

@Composable
private fun DefrostReminderCard(items: List<String>, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = colors.beigeSoft,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconBadge(AppIcons.Snowflake, colors.surface, colors.beigeText)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Nhớ rã đông",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
                Text(
                    text = defrostSummaryText(items),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.beigeText,
                )
            }
            Icon(AppIcons.Chevron, contentDescription = null, tint = colors.beigeText, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun UrgentCard(ingredientId: String?, name: String, caption: String, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Surface(
        modifier = Modifier
            .width(112.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (ingredientId != null) {
                FoodThumb(ingredientId = ingredientId, size = 64.dp, shape = RoundedCornerShape(14.dp), urgent = true)
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.warnSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(AppIcons.Pot, contentDescription = null, tint = colors.warnText, modifier = Modifier.size(28.dp))
                }
            }
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                caption,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = colors.warnText,
            )
        }
    }
}

@Composable
private fun HomeRecipeCard(recipe: Recipe, reason: RecommendationReason, modifier: Modifier, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val (tagBg, tagFg) = reasonColors(reason)
    val time = recipe.minutes?.let { "$it phút" } ?: "Thời gian chưa rõ"
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            RecipeThumb(recipeId = recipe.id, size = 64.dp)
            Text(
                recipe.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(AppIcons.Clock, contentDescription = null, tint = colors.muted, modifier = Modifier.size(14.dp))
                Text(time, style = MaterialTheme.typography.bodySmall, color = colors.muted)
            }
            val tag = shortReasonLabel(reason)
            if (tag.isNotEmpty()) {
                Surface(shape = RoundedCornerShape(999.dp), color = tagBg) {
                    Text(
                        tag,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = tagFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun reasonColors(reason: RecommendationReason): Pair<Color, Color> {
    val colors = AppTheme.colors
    return when (reason) {
        RecommendationReason.USE_SOON -> colors.warnSoft to colors.warnText
        RecommendationReason.QUICK -> colors.beigeSoft to colors.beigeText
        RecommendationReason.READY, RecommendationReason.OTHER -> colors.primarySoft to colors.text
    }
}

// Nút lớn vào thực đơn cả tuần, đặt ngay dưới phần chào để dễ thấy.
@Composable
private fun WeeklyPlanButton(onClick: () -> Unit) {
    val colors = AppTheme.colors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = colors.primary,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(AppIcons.Calendar, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(28.dp))
            Text(
                text = WEEKLY_BUTTON_LABEL,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.onPrimary,
                modifier = Modifier.weight(1f),
            )
            Icon(AppIcons.Chevron, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(20.dp))
        }
    }
}
