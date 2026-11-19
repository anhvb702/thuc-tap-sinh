package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.ui.isFreezerItem
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

internal fun ingredientZoneLabel(ingredientId: String): String =
    if (isFreezerItem(ingredientId)) "Ngăn đông · Cần rã đông trước" else "Ngăn mát"

internal fun freezerIngredientNames(recipe: Recipe, state: PantryGridState): List<String> {
    val namesById = state.ingredients.associate { it.id to it.nameVi }
    val recipeIds = (recipe.required + recipe.optional).flatMap { it.anyOf }
    return recipeIds
        .distinct()
        .filter { id -> state.pantry[id]?.have == true && isFreezerItem(id) }
        .map { id -> namesById[id] ?: id }
        .distinct()
}

internal fun defrostNoticeText(freezerNames: List<String>): String? =
    freezerNames.takeIf { it.isNotEmpty() }?.let {
        "Lưu ý rã đông: ${it.joinToString(", ")} đang ở ngăn đông — nên rã đông trước khi nấu nhé."
    }

@Composable
internal fun DefrostNoticeBanner(freezerNames: List<String>, modifier: Modifier = Modifier) {
    val notice = defrostNoticeText(freezerNames) ?: return
    val colors = AppTheme.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.warnSoft,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = AppIcons.Snowflake,
                contentDescription = null,
                tint = colors.warnText,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = notice,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.warnText,
            )
        }
    }
}
