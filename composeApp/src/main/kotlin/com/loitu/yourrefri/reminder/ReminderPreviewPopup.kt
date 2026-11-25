package com.loitu.yourrefri.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.ui.components.RecipeThumb
import com.loitu.yourrefri.ui.components.pressScale
import com.loitu.yourrefri.ui.theme.AppTheme

internal data class ReminderPopupData(
    val title: String,
    val body: String,
    val recipeId: String,
    val dishTitle: String,
    val badgeLabel: String = "",
    val urgentSummary: String? = null,
    val urgentCount: Int = 0,
)

internal fun buildReminderPopupData(state: PantryGridState, dayOfYear: Int): ReminderPopupData {
    val entry = ReminderPicture.entryFor(dayOfYear)
    val urgentItems = cellsOf(state)
        .filter { it.have && it.daysLeft != null && it.daysLeft in 0..2 }
        .sortedBy { it.daysLeft }
    val urgentCount = urgentItems.size
    val urgentNames = urgentItems.take(2).joinToString(", ") { it.nameVi }
    val (title, body) = if (urgentNames.isNotEmpty()) {
        "Tối nay nấu ${entry.titleVi} nhé" to
            "Trong tủ có $urgentNames nên dùng trước — thử ngay món ${entry.titleVi} gọn lẹ cho bữa cơm nhé!"
    } else {
        "Gợi ý bữa cơm: ${entry.titleVi}" to
            "Món ${entry.titleVi} dễ nấu, ấm bụng cho bữa cơm nhà. Bấm để xem công thức và bắt đầu nấu ngay!"
    }
    return ReminderPopupData(
        title = title,
        body = body,
        recipeId = entry.recipeId,
        dishTitle = entry.titleVi,
        badgeLabel = if (urgentCount > 0) "NÊN NẤU SỚM" else "GỢI Ý BẾP NHÀ",
        urgentSummary = if (urgentCount > 0) "$urgentCount món nên dùng sớm: $urgentNames" else null,
        urgentCount = urgentCount,
    )
}

@Composable
internal fun ReminderPopupBoxDialog(data: ReminderPopupData, onOpenRecipe: (String) -> Unit, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    val typography = MaterialTheme.typography
    val interaction = remember { MutableInteractionSource() }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = colors.surface, shadowElevation = 12.dp) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(listOf(colors.notifyHeroStart, colors.notifyHeroEnd)))
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = data.badgeLabel,
                            modifier = Modifier.background(colors.notifyHeroBadgeBg, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            style = typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.notifyHeroOn,
                        )
                        Text(
                            text = data.dishTitle,
                            style = typography.titleLarge,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.notifyHeroOn,
                        )
                        Text(
                            text = if (data.urgentCount > 0) "${data.urgentCount} món ưu tiên dùng sớm"
                                else "Bữa cơm nhà gợi ý hôm nay",
                            style = typography.bodySmall,
                            color = colors.notifyHeroOn,
                        )
                    }
                    RecipeThumb(
                        recipeId = data.recipeId,
                        modifier = Modifier.border(3.dp, colors.notifyHeroOn, CircleShape),
                        size = 94.dp,
                        shape = CircleShape,
                    )
                }
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(data.title, style = typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.text)
                    data.urgentSummary?.let { summary ->
                        Text(
                            text = summary,
                            modifier = Modifier.background(colors.warnSoft, RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            style = typography.labelMedium,
                            color = colors.warnText,
                        )
                    }
                    Text(data.body, style = typography.bodyMedium, color = colors.muted)
                    Button(
                        onClick = { onOpenRecipe(data.recipeId) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).pressScale(interaction),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta),
                        interactionSource = interaction,
                    ) {
                        Text("Xem công thức & nấu ngay")
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text("Để sau", color = colors.muted)
                    }
                }
            }
        }
    }
}
