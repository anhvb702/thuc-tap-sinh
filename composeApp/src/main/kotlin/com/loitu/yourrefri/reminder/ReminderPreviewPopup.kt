package com.loitu.yourrefri.reminder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.ui.components.RecipeThumb
import com.loitu.yourrefri.ui.theme.AppTheme

internal data class ReminderPopupData(val title: String, val body: String, val recipeId: String, val dishTitle: String)

internal fun buildReminderPopupData(state: PantryGridState, dayOfYear: Int): ReminderPopupData {
    val entry = ReminderPicture.entryFor(dayOfYear)
    val urgentNames = cellsOf(state).filter { it.have && it.daysLeft != null && it.daysLeft in 0..2 }
        .sortedBy { it.daysLeft }.take(2).joinToString(", ") { it.nameVi }
    return if (urgentNames.isNotEmpty()) ReminderPopupData(
        "Tối nay nấu ${entry.titleVi} nhé",
        "Trong tủ có $urgentNames nên dùng trước — thử ngay món ${entry.titleVi} gọn lẹ cho bữa cơm nhé!",
        entry.recipeId, entry.titleVi,
    ) else ReminderPopupData(
        "Gợi ý bữa cơm: ${entry.titleVi}",
        "Món ${entry.titleVi} dễ nấu, ấm bụng cho bữa cơm nhà. Bấm để xem công thức và bắt đầu nấu ngay!",
        entry.recipeId, entry.titleVi,
    )
}

@Composable
internal fun ReminderPopupBoxDialog(data: ReminderPopupData, onOpenRecipe: (String) -> Unit, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = colors.surface, border = BorderStroke(1.dp, colors.line)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("THÔNG BÁO NHẮC BẾP NHÀ", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = colors.accentText)
                RecipeThumb(recipeId = data.recipeId, modifier = Modifier.fillMaxWidth(), size = 160.dp, shape = RoundedCornerShape(16.dp))
                Text(data.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.text)
                Text(data.body, style = MaterialTheme.typography.bodyMedium, color = colors.muted)
                Button(onClick = { onOpenRecipe(data.recipeId) }, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)) {
                    Text("Xem công thức & nấu ngay", color = colors.onPrimary)
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Để sau", color = colors.muted) }
            }
        }
    }
}
