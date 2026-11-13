package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import kotlinx.coroutines.launch

/**
 * Màn chi tiết một món. Không tự điều hướng: người gọi nối [onBack], [onCook] và [onUndo].
 * [onCook] nhận danh sách id nguyên liệu cần giảm; [onUndo] nhận các action đưa tủ về trạng thái trước khi nấu.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
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
    var ticked by rememberSaveable(recipe.id) { mutableStateOf(listOf<Int>()) }
    val chips = remember(recipe, state) { recipeIngredientChips(recipe, state) }
    var showLeftover by remember(recipe.id) { mutableStateOf(false) }
    val candidateIds = remember(recipe, state) { recipeCookPresentation(recipe, state, callbackAvailable = true).ingredientIds }
    val namesById = remember(state.ingredients) { state.ingredients.associate { it.id to it.nameVi } }

    // Chụp tủ TRƯỚC khi nấu để hoàn tác đúng trạng thái cũ; phần "còn lại" không bị trừ nên cũng không bị hoàn tác.
    fun finishCook(kept: Set<String>) {
        val before = state
        val ids = ingredientsToConsume(candidateIds, kept)
        onCook(ids)
        if (!showDoneSnackbar) return
        val undo = cookUndoActions(before, ids)
        scope.launch {
            val result = host.showSnackbar(
                message = cookDoneMessage(hasUpdates = ids.isNotEmpty()),
                actionLabel = if (undo.isNotEmpty()) "Hoàn tác" else null,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) onUndo(undo)
        }
    }
    if (showLeftover) {
        CookLeftoverDialog(
            ingredients = candidateIds.distinct().map { it to (namesById[it] ?: it) },
            onConfirm = { kept ->
                showLeftover = false
                finishCook(kept)
            },
            onDismiss = { showLeftover = false },
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
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.text,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(AppIcons.Clock, contentDescription = null, tint = colors.muted, modifier = Modifier.size(16.dp))
                    Text(
                        text = recipe.minutes?.let { "Khoảng $it phút" } ?: "Thời gian chưa rõ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.muted,
                    )
                }

                Text(
                    text = "Nguyên liệu",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.text,
                    modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    chips.forEach { IngredientChip(it) }
                }

                Text(
                    text = "Cách nấu",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.text,
                    modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
                )
                recipe.steps.forEachIndexed { index, step ->
                    StepRow(
                        title = stepTitle(index),
                        text = step,
                        checked = index in ticked,
                        onCheckedChange = { on -> ticked = if (on) ticked + index else ticked - index },
                    )
                }
            }

            Surface(color = colors.background, border = BorderStroke(1.dp, colors.line)) {
                Column {
                if (onStartHandsFree != null) {
                    Button(
                        onClick = onStartHandsFree,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceSoft, contentColor = colors.text),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                            .heightIn(min = 52.dp),
                    ) {
                        Icon(AppIcons.ChefHat, contentDescription = null, modifier = Modifier.size(22.dp))
                        Text("Nấu rảnh tay", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Button(
                    onClick = {
                        // Có nguyên liệu sắp trừ thì hỏi "Dùng hết hay còn lại?" trước; không có thì nấu xong luôn.
                        if (candidateIds.isEmpty()) finishCook(emptySet()) else showLeftover = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .heightIn(min = 52.dp),
                ) {
                    Text("Nấu xong, cập nhật tủ", fontWeight = FontWeight.SemiBold)
                }
                }
            }
        }
        // Chỉ vẽ snackbar riêng khi người gọi không truyền host của mình.
        if (snackbarHostState == null) {
            SnackbarHost(
                hostState = localHost,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = if (onStartHandsFree != null) 136.dp else 76.dp),
            )
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

@Composable
private fun StepRow(title: String, text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(shape)
            .background(if (checked) colors.surfaceSoft else colors.surface)
            .border(1.dp, colors.line, shape)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .heightIn(min = 56.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (checked) colors.primary else Color.Transparent)
                .border(1.5.dp, if (checked) colors.primary else colors.muted, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) Icon(AppIcons.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.accentText,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (checked) colors.muted else colors.text,
                textDecoration = if (checked) TextDecoration.LineThrough else null,
            )
        }
    }
}
