package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

internal const val EMPTY_FRIDGE_TITLE = "Tủ nhà mình đang trống, hết đồ để nấu rồi"
internal const val EMPTY_FRIDGE_SUBTITLE = "Mình đi chợ thêm ít đồ tươi rồi quay lại chọn món nhé."
internal const val EMPTY_FRIDGE_SHOP_ACTION = "Đi chợ thôi nào"
internal const val EMPTY_FRIDGE_HOME_ACTION = "Về trang chủ"

internal fun isPantryEmpty(state: PantryGridState): Boolean = state.pantry.values.none { it.have }

@Composable
internal fun EmptyFridgeGate(
    onGoShopping: () -> Unit,
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = AppIcons.Fridge,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = EMPTY_FRIDGE_TITLE,
                color = colors.text,
                fontWeight = FontWeight.Bold,
            )
            Text(text = EMPTY_FRIDGE_SUBTITLE, color = colors.muted)
            Button(
                onClick = onGoShopping,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                ),
            ) {
                Text(EMPTY_FRIDGE_SHOP_ACTION, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onBackHome, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(EMPTY_FRIDGE_HOME_ACTION, color = colors.primary)
            }
        }
    }
}
