package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.ui.isFreezerItem
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Seasoning
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.logic.formatSlotMeasure
import com.loitu.yourrefri.feature.recipe.logic.measureEstimateNote
import com.loitu.yourrefri.feature.recipe.logic.seasoningEstimateNote
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import com.loitu.yourrefri.ui.components.RecipeThumb
import com.loitu.yourrefri.ui.components.FoodThumb
import com.loitu.yourrefri.ui.components.pressScale
import kotlinx.coroutines.launch

/**
 * Màn chi tiết một món. Không tự điều hướng: người gọi nối [onBack], [onCook] và [onUndo].
 * [onCook] nhận danh sách id nguyên liệu cần giảm; [onUndo] nhận các action đưa tủ về trạng thái trước khi nấu.
 */
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    state: PantryGridState,
    onBack: () -> Unit,
    onCook: (List<String>) -> Unit,
    snackbarHostState: SnackbarHostState? = null,
    modifier: Modifier = Modifier,
    onUndo: (List<PantryGridAction>) -> Unit = {},
    // Yêu thích và Nấu rảnh tay: nút rảnh tay chỉ hiện khi người gọi truyền onStartHandsFree.
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onStartHandsFree: (() -> Unit)? = null,
    // false khi người gọi tự hiện màn chúc mừng nên không cần thông báo nhỏ có Hoàn tác ở đây.
    showDoneSnackbar: Boolean = true,
) {
    val colors = AppTheme.colors
    val localHost = remember { SnackbarHostState() }
    val host = snackbarHostState ?: localHost
    val scope = rememberCoroutineScope()
    var showManualChecklist by remember(recipe.id) { mutableStateOf(false) }
    var selectedServings by rememberSaveable(recipe.id) { mutableIntStateOf(recipe.servings) }
    val hasMeasures = remember(recipe) { (recipe.required + recipe.optional).any { it.measures.isNotEmpty() } }

    fun finishCook() {
        val plan = buildDirectCookPlan(recipe, state, callbackAvailable = true) ?: return
        val before = state
        onCook(plan.ingredientIds)
        if (!showDoneSnackbar) return
        val undo = cookUndoActions(before, plan.ingredientIds)
        scope.launch {
            val result = host.showSnackbar(
                message = cookDoneMessage(hasUpdates = plan.ingredientIds.isNotEmpty()),
                actionLabel = if (undo.isNotEmpty()) "Hoàn tác" else null,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) onUndo(undo)
        }
    }
    if (showManualChecklist) {
        ManualCookChecklistDialog(
            recipe = recipe,
            onDismiss = { showManualChecklist = false },
            onDone = {
                showManualChecklist = false
                finishCook()
            },
        )
    }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(role = Role.Button, onClick = onBack)
                            .semantics { contentDescription = "Quay lại" },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(AppIcons.Back, contentDescription = null, tint = colors.text, modifier = Modifier.size(24.dp))
                    }
                    FavoriteHeartButton(title = recipe.title, isFavorite = isFavorite, onToggle = onToggleFavorite)
                }
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.line),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        RecipeThumb(
                            recipeId = recipe.id,
                            modifier = Modifier.fillMaxWidth(),
                            size = 144.dp,
                            shape = RoundedCornerShape(14.dp),
                        )
                        Text(
                            text = recipe.title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 27.sp, fontWeight = FontWeight.SemiBold),
                            color = colors.text,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                        Row(
                            modifier = Modifier.padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(AppIcons.Clock, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(16.dp))
                            Text(
                                text = (recipe.minutes?.let { "Khoảng $it phút" } ?: "Thời gian chưa rõ") +
                                    " · " + servingsDifficultyLabel(recipe.minutes, selectedServings),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.accentText,
                            )
                        }
                    }
                }

                Text(
                    text = "Nguyên liệu chính",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.text,
                    modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
                )
                if (hasMeasures || recipe.seasonings.isNotEmpty()) {
                    ServingSelector(selectedServings, onSelect = { selectedServings = it })
                }
                DefrostNoticeBanner(
                    freezerIngredientNames(recipe, state),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                )
                recipeIngredientRows(recipe.required, state, recipe.servings, selectedServings).forEach { row ->
                    IngredientRow(row)
                }
                if (recipe.optional.isNotEmpty()) {
                    Text(
                        text = "Có thể thêm",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.accentText,
                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp),
                    )
                    recipeIngredientRows(recipe.optional, state, recipe.servings, selectedServings).forEach { row ->
                        IngredientRow(row)
                    }
                }
                if (recipe.seasonings.isNotEmpty()) {
                    RecipeSeasonings(recipe.seasonings, recipe.servings, selectedServings)
                }
                Spacer(Modifier.height(8.dp))
            }

            Surface(color = colors.background, border = BorderStroke(1.dp, colors.line)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val manualInteraction = remember { MutableInteractionSource() }
                    Button(
                        onClick = { showManualChecklist = true },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1.3f).heightIn(min = 52.dp).pressScale(manualInteraction),
                        interactionSource = manualInteraction,
                    ) { Text("Nấu thủ công", fontWeight = FontWeight.Bold) }
                    if (onStartHandsFree != null) {
                        val handsFreeInteraction = remember { MutableInteractionSource() }
                        OutlinedButton(
                            onClick = onStartHandsFree,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.text),
                            border = BorderStroke(1.dp, colors.line),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp).pressScale(handsFreeInteraction),
                            interactionSource = handsFreeInteraction,
                        ) {
                            Icon(AppIcons.ChefHat, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("Rảnh tay", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                }
            }
        }
        // Chỉ vẽ snackbar riêng khi người gọi không truyền host của mình.
        if (snackbarHostState == null) {
            SnackbarHost(
                hostState = localHost,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 76.dp),
            )
        }
    }
}

@Composable
private fun ManualCookChecklistDialog(
    recipe: Recipe,
    onDismiss: () -> Unit,
    onDone: () -> Unit,
) {
    val colors = AppTheme.colors
    var checkedSteps by rememberSaveable(recipe.id) { mutableStateOf(emptyList<Int>()) }
    val checkedSet = checkedSteps.toSet()
    val totalSteps = recipe.steps.size
    val allStepsChecked = canFinishManualCook(checkedSet, totalSteps)
    val animatedProgress by animateFloatAsState(
        targetValue = manualCookProgress(checkedSet, totalSteps),
        animationSpec = tween(260),
        label = "manualCookProgress",
    )
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.86f),
            shape = RoundedCornerShape(24.dp),
            color = colors.background,
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(
                        recipe.title,
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.text,
                        maxLines = 2,
                    )
                    IconButton(onClick = onDismiss) { Icon(AppIcons.Close, contentDescription = "Đóng", tint = colors.text) }
                }
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(999.dp)),
                    color = colors.primary,
                    trackColor = colors.surfaceSoft,
                )
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(recipe.steps) { index, step ->
                        val isChecked = index in checkedSet
                        val stepBackground by animateColorAsState(
                            targetValue = if (isChecked) colors.surfaceSoft else colors.surface,
                            label = "manualStepBackground",
                        )
                        val stepTextColor by animateColorAsState(
                            targetValue = if (isChecked) colors.muted else colors.text,
                            label = "manualStepText",
                        )
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable(role = Role.Checkbox) {
                                checkedSteps = toggleManualCookStep(checkedSet, index).toList()
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = stepBackground,
                            border = BorderStroke(1.dp, colors.line),
                        ) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Checkbox(checked = isChecked, onCheckedChange = null)
                                Text(step, style = MaterialTheme.typography.bodyLarge, color = stepTextColor)
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    visible = allStepsChecked,
                    enter = slideInVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) { it / 2 } +
                        fadeIn(tween(220)) + expandVertically(tween(220)),
                    exit = fadeOut(tween(150)) + shrinkVertically(tween(150)),
                ) {
                    Button(
                        onClick = onDone,
                        enabled = allStepsChecked,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.cta,
                            contentColor = colors.onCta,
                            disabledContainerColor = colors.cta,
                            disabledContentColor = colors.onCta,
                        ),
                    ) { Text("Đã nấu xong", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun ServingSelector(selectedServings: Int, onSelect: (Int) -> Unit) {
    val colors = AppTheme.colors
    Text("Khẩu phần (người)", style = MaterialTheme.typography.labelLarge, color = colors.muted)
    Row(
        Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 6.dp).selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (count in 1..4) {
            val selected = count == selectedServings
            Surface(
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).clip(CircleShape)
                    .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(count) })
                    .semantics { contentDescription = "Chọn $count người ăn" },
                shape = CircleShape,
                color = if (selected) colors.primary else colors.surface,
                contentColor = if (selected) colors.onPrimary else colors.text,
                border = if (selected) null else BorderStroke(1.dp, colors.line),
            ) {
                Box(Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                    Text(count.toString(), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
    Text(
        text = measureEstimateNote(selectedServings),
        style = MaterialTheme.typography.bodySmall,
        color = colors.muted,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun RecipeSeasonings(seasonings: List<Seasoning>, baseServings: Int, targetServings: Int) {
    val colors = AppTheme.colors
    Text(
        text = "Gia vị cần dùng",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = colors.text,
        modifier = Modifier.padding(top = 18.dp, bottom = 2.dp),
    )
    Text(
        text = seasoningEstimateNote(baseServings, targetServings),
        style = MaterialTheme.typography.bodySmall,
        color = colors.muted,
        modifier = Modifier.padding(bottom = 8.dp),
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            seasonings.forEach { seasoning ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(AppIcons.Spice, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(18.dp))
                    Text(seasoning.name, Modifier.weight(1f), color = colors.text, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = seasoning.amountText,
                        modifier = Modifier.weight(1f),
                        color = colors.muted,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

private data class RecipeIngredientRow(val id: String, val name: String, val have: Boolean, val measureText: String?)

private fun recipeIngredientRows(
    slots: List<Slot>,
    state: PantryGridState,
    baseServings: Int,
    targetServings: Int,
): List<RecipeIngredientRow> {
    val haveIds = state.pantry.values.filter { it.have }.mapTo(HashSet()) { it.ingredientId }
    val names = state.ingredients.associate { it.id to it.nameVi }
    return slots.map { slot ->
        val id = slot.anyOf.firstOrNull { it in haveIds } ?: slot.anyOf.first()
        RecipeIngredientRow(
            id = id,
            name = names[id] ?: id,
            have = id in haveIds,
            measureText = formatSlotMeasure(slot, id, baseServings, targetServings),
        )
    }
}

@Composable
private fun IngredientRow(row: RecipeIngredientRow) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FoodThumb(ingredientId = row.id, size = 32.dp, shape = CircleShape)
        Column(modifier = Modifier.weight(2f)) {
            Text(row.name, style = MaterialTheme.typography.bodyLarge, color = if (row.have) colors.text else colors.muted)
            if (row.have) {
                Text(
                    text = ingredientZoneLabel(row.id),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isFreezerItem(row.id)) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                    color = if (isFreezerItem(row.id)) colors.warnText else colors.muted,
                )
            }
        }
        if (row.measureText != null || !row.have) {
            Column(
                modifier = if (row.measureText != null) Modifier.weight(1f) else Modifier,
                horizontalAlignment = Alignment.End,
            ) {
                row.measureText?.let { measure ->
                    Text(measure, style = MaterialTheme.typography.bodyMedium, color = colors.text, textAlign = TextAlign.End)
                }
                if (!row.have) {
                    Text("chưa có", style = MaterialTheme.typography.bodySmall, color = colors.muted, textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
private fun IngredientChip(chip: RecipeIngredientChip) {
    val colors = AppTheme.colors
    val suffix = missingChipSuffix(chip.have)
    val shape = RoundedCornerShape(999.dp)
    val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
    Row(
        modifier = Modifier
            .heightIn(min = 36.dp)
            .clip(shape)
            .then(
                if (chip.have) {
                    Modifier.background(colors.primarySoft, shape)
                } else {
                    Modifier.drawBehind {
                        drawRoundRect(
                            color = colors.muted.copy(alpha = 0.5f),
                            cornerRadius = CornerRadius(size.height / 2),
                            style = Stroke(width = 1.5.dp.toPx(), pathEffect = dash),
                        )
                    }
                }
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (chip.have) {
            Icon(AppIcons.Check, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(16.dp))
        }
        Text(
            text = if (suffix != null) "${chip.name} · $suffix" else chip.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (chip.have) colors.text else colors.muted,
        )
    }
}
