package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import com.loitu.yourrefri.feature.recipe.logic.ingredientIdsToConsume
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import com.loitu.yourrefri.ui.theme.DeepCharcoal // guard:allow - hàm màu cũ bị test khóa
import com.loitu.yourrefri.ui.theme.SageGreen // guard:allow - hàm màu cũ
import com.loitu.yourrefri.ui.theme.SageGreenText // guard:allow - hàm màu cũ
import com.loitu.yourrefri.ui.theme.SoftGreen // guard:allow - hàm màu cũ
import com.loitu.yourrefri.ui.theme.Terracotta // guard:allow - hàm màu cũ
import com.loitu.yourrefri.ui.theme.TerracottaSoft // guard:allow - reasonPillColors bị test khóa
import com.loitu.yourrefri.ui.theme.TerracottaText // guard:allow - hàm màu cũ
import com.loitu.yourrefri.ui.theme.WarmBeige // guard:allow - hàm màu cũ
import com.loitu.yourrefri.ui.theme.WarmBeigeSoft // guard:allow - reasonPillColors bị test khóa
import com.loitu.yourrefri.ui.theme.WarmBeigeText // guard:allow - reasonPillColors bị test khóa

@Composable
internal fun RecipeCookConfirmationHost(
    showConfirmation: Boolean,
    pendingRecipe: Recipe?,
    presentation: RecipeCookPresentation?,
    onDismiss: () -> Unit,
    onResult: (String, String) -> Unit,
) {
    if (!showConfirmation || pendingRecipe == null) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (presentation?.ingredientNames.isNullOrEmpty()) "Ch?a c? nguy?n li?u c?n c?p nh?t" else "C?p nh?t nguy?n li?u?") },
        text = { Text(presentation?.confirmationMessage.orEmpty()) },
        confirmButton = {
            Button(onClick = {
                presentation?.resultMessage?.let { onResult(pendingRecipe.id, it) }
                onDismiss()
            }) { Text("??ng") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("?? sau") } },
    )
}

@Composable
internal fun HeroEmptyCard() {
    val colors = AppTheme.colors
    Column(Modifier.fillMaxWidth().heightIn(min = 140.dp).clip(RoundedCornerShape(16.dp)).background(colors.ctaSoft), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(AppIcons.Pot, contentDescription = null, tint = colors.ctaText, modifier = Modifier.size(36.dp))
        Text(HERO_EMPTY_MESSAGE, style = MaterialTheme.typography.bodyLarge, color = colors.ctaText, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
    }
}

@Composable
internal fun HeroIngredientHints(recipe: Recipe, state: PantryGridState) {
    val colors = AppTheme.colors
    val availableIds = state.pantry.values.filter { it.have }.map { it.ingredientId }.toSet()
    val names = state.ingredients.associate { it.id to it.nameVi }
    val urgentIds = cellsOf(state).filter { it.have && it.daysLeft in 0..2 }.mapTo(HashSet()) { it.ingredientId }
    val haveNames = ingredientIdsToConsume(recipe, availableIds, urgentIds).mapNotNull(names::get)
    val missingNames = recipe.required.filter { slot -> slot.anyOf.none { it in availableIds } }.mapNotNull { it.anyOf.firstOrNull()?.let(names::get) }
    if (haveNames.isNotEmpty()) Text("Đã có: ${haveNames.joinToString(", ")}", color = colors.accentText, modifier = Modifier.padding(top = 10.dp))
    if (missingNames.isNotEmpty()) Text("Có thể thêm: ${missingNames.joinToString(", ")}", color = colors.warnText, modifier = Modifier.padding(top = 2.dp))
}

@Composable
internal fun InlineRecipeSteps(recipe: Recipe, stepChecks: MutableMap<String, Boolean>, resultMessage: String?, onCooked: () -> Unit) {
    val colors = AppTheme.colors
    Column(Modifier.fillMaxWidth().padding(top = 12.dp).clip(RoundedCornerShape(16.dp)).background(colors.surface).border(2.dp, colors.primary, RoundedCornerShape(16.dp)).padding(14.dp)) {
        Text("Cách nấu", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.text, modifier = Modifier.padding(bottom = 8.dp))
        recipe.steps.forEachIndexed { index, step ->
            val key = "${recipe.id}_$index"
            val checked = stepChecks[key] ?: false
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                Checkbox(checked, { stepChecks[key] = it }, colors = CheckboxDefaults.colors(checkedColor = colors.primary, uncheckedColor = colors.muted, checkmarkColor = colors.onPrimary))
                Text(step, style = MaterialTheme.typography.bodyMedium.copy(color = if (checked) colors.muted else colors.text), modifier = Modifier.padding(top = 12.dp))
            }
        }
        Button(onClick = onCooked, colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(top = 12.dp).heightIn(min = 48.dp)) { Text("Đã nấu xong món này", fontWeight = FontWeight.SemiBold) }
        resultMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colors.accentText, modifier = Modifier.padding(top = 8.dp)) }
    }
}

@Composable
internal fun DailyShortcuts(weeklyPlan: List<com.loitu.yourrefri.feature.recipe.logic.PlannedDayMeal>, todayIndex: Int, shopping: List<com.loitu.yourrefri.feature.recipe.logic.ShoppingListItem>, onGoTo: (RecipeScreenMode) -> Unit, handsFreeRecipeId: String?, onOpenHandsFree: ((String) -> Unit)?) {
    Spacer(Modifier.height(20.dp))
    ShortcutCard(AppIcons.Cart, SHOPPING_CARD_TITLE, SHOPPING_CARD_SUBTITLE, extraLine = shoppingCardCountText(shopping.count { !it.alreadyInPantry || it.missingQuantity > 0 }), onClick = { onGoTo(RecipeScreenMode.SHOPPING) })
    if (onOpenHandsFree != null && handsFreeRecipeId != null) {
        Spacer(Modifier.height(12.dp))
        ShortcutCard(AppIcons.ChefHat, HANDS_FREE_CARD_TITLE, HANDS_FREE_CARD_SUBTITLE, onClick = { onOpenHandsFree(handsFreeRecipeId) })
    }
}

internal fun recommendationColor(reason: RecommendationReason): Color = when (reason) { RecommendationReason.USE_SOON -> Terracotta; RecommendationReason.READY -> SageGreen; RecommendationReason.QUICK -> WarmBeige; RecommendationReason.OTHER -> SoftGreen }
internal fun recommendationTextColor(reason: RecommendationReason): Color = when (reason) { RecommendationReason.USE_SOON -> TerracottaText; RecommendationReason.READY -> SageGreenText; RecommendationReason.QUICK, RecommendationReason.OTHER -> DeepCharcoal }
internal data class RecipeDurationFilterOption(val label: String, val threshold: Int?)
internal fun recipeDurationFilterOptions() = listOf(RecipeDurationFilterOption("Tất cả", null), RecipeDurationFilterOption("Tối đa 15 phút", 15), RecipeDurationFilterOption("Tối đa 30 phút", 30))
internal fun reasonPillColors(reason: RecommendationReason): Pair<Color, Color> = when (reason) { RecommendationReason.USE_SOON -> TerracottaSoft to TerracottaText; RecommendationReason.QUICK -> WarmBeigeSoft to WarmBeigeText; RecommendationReason.READY, RecommendationReason.OTHER -> SoftGreen to DeepCharcoal }

@Composable
internal fun reasonPillColorsThemed(reason: RecommendationReason): Pair<Color, Color> {
    val colors = AppTheme.colors
    return when (reason) { RecommendationReason.USE_SOON -> colors.warnSoft to colors.warnText; RecommendationReason.QUICK -> colors.beigeSoft to colors.beigeText; RecommendationReason.READY, RecommendationReason.OTHER -> colors.primarySoft to colors.text }
}

@Composable
internal fun RecipeSegmentBar(selectedMode: RecipeScreenMode, onSelect: (RecipeScreenMode) -> Unit) {
    val colors = AppTheme.colors
    Surface(shape = RoundedCornerShape(16.dp), color = colors.surfaceSoft, border = BorderStroke(1.dp, colors.line), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(RecipeScreenMode.DAILY, RecipeScreenMode.WEEKLY).forEachIndexed { index, mode ->
                val selected = mode == selectedMode
                Box(Modifier.weight(1f).heightIn(min = 48.dp).clip(RoundedCornerShape(12.dp)).background(if (selected) colors.primary else Color.Transparent).selectable(selected = selected, role = Role.Tab, onClick = { onSelect(mode) }).padding(8.dp), contentAlignment = Alignment.Center) {
                    Text(recipeSegmentLabels()[index], style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = if (selected) colors.onPrimary else colors.text)
                }
            }
        }
    }
}
