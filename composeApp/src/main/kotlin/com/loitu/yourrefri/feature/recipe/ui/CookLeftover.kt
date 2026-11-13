package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.ui.theme.AppTheme

internal object CookLeftoverCopy {
    const val title = "Dùng hết hay còn lại?"
    const val useAll = "Dùng hết"
    const val keepSome = "Còn lại"
    const val confirm = "Xong rồi"
    const val later = "Để sau"
}

internal object MealCopy {
    fun heading(meal: MealSlot): String = when (meal) {
        MealSlot.LUNCH -> "Trưa nay nhà mình ăn gì nhỉ?"
        MealSlot.DINNER -> "Tối nay nhà mình ăn gì nhỉ?"
    }

    fun label(meal: MealSlot): String = when (meal) {
        MealSlot.LUNCH -> "Bữa trưa"
        MealSlot.DINNER -> "Bữa tối"
    }
}

/** Câu kết quả sau khi nấu xong; chỉ liệt kê những nguyên liệu thực sự đã trừ. */
internal fun cookResultMessage(consumedNames: List<String>): String =
    if (consumedNames.isEmpty()) {
        "Món đã nấu xong. Chưa có nguyên liệu nào cần cập nhật trong tủ."
    } else {
        "Đã cập nhật trong tủ: ${consumedNames.joinToString(", ")}."
    }

/** Nguyên liệu sẽ bị trừ khỏi tủ: bỏ phần người dùng đánh dấu "còn lại", bỏ trùng, giữ thứ tự. */
internal fun ingredientsToConsume(candidateIds: List<String>, keptIds: Set<String>): List<String> =
    candidateIds.distinct().filter { it !in keptIds }

/**
 * Hỏi từng nguyên liệu sắp trừ: mặc định "Dùng hết"; bật "Còn lại" thì nguyên liệu ở lại trong tủ.
 * [ingredients] là cặp (id, tên). [onConfirm] nhận tập id được giữ lại.
 */
@Composable
internal fun CookLeftoverDialog(
    ingredients: List<Pair<String, String>>,
    onConfirm: (kept: Set<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    var kept by remember { mutableStateOf(emptySet<String>()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text(CookLeftoverCopy.title, color = colors.text) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ingredients.forEach { (id, name) ->
                    val isKept = id in kept
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable(role = Role.Switch) { kept = if (isKept) kept - id else kept + id },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(name, color = colors.text, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (isKept) CookLeftoverCopy.keepSome else CookLeftoverCopy.useAll,
                                color = colors.muted,
                            )
                        }
                        Switch(
                            checked = isKept,
                            onCheckedChange = null,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.onPrimary,
                                checkedTrackColor = colors.primary,
                                uncheckedThumbColor = colors.muted,
                                uncheckedTrackColor = colors.surfaceSoft,
                                uncheckedBorderColor = colors.line,
                            ),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(kept) }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(CookLeftoverCopy.confirm, color = colors.accentText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(CookLeftoverCopy.later, color = colors.muted)
            }
        },
    )
}
