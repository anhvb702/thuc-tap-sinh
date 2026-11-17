package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

internal object CookDoneCopy {
    const val title = "Xong rồi, dọn cơm thôi!"
    const val subtitle = "Chúc cả nhà ngon miệng"
    const val home = "Về trang chủ"
    const val saveLeftover = "Còn thừa, cất tủ"
    const val savedLeftover = "Đã cất vào Đồ ăn thừa"
    const val chillOption = "Ngăn mát (2 ngày)"
    const val freezeOption = "Ngăn đông (14 ngày)"
    const val confirmSave = "Cất tủ"
    const val cancelSave = "Hủy"
}

internal fun leftoverShelfDaysFor(compartment: PantryCompartment): Int =
    if (compartment == PantryCompartment.FREEZE) 14 else 2

/** Một lần nấu vừa xong: món và phần tủ đã cập nhật (nếu có). */
internal data class CookDone(
    val recipeTitle: String,
    val summary: String?,
)

/** Tên các nguyên liệu đã trừ, theo thứ tự; bỏ id không có tên. */
internal fun cookDoneNames(ids: List<String>, namesById: Map<String, String>): List<String> =
    ids.mapNotNull { namesById[it] }

/** Một dòng tóm tắt; không có gì được cập nhật thì không hiện dòng nào để bớt chữ. */
internal fun cookDoneSummary(updatedNames: List<String>): String? =
    if (updatedNames.isEmpty()) null else cookResultMessage(updatedNames)

/**
 * Màn chúc mừng sau khi nấu xong. Không tự điều hướng: người gọi nối [onHome].
 * Cố ý không có "Hoàn tác": nấu xong rồi thì không quay lại được.
 */
@Composable
internal fun CookDoneScreen(
    done: CookDone,
    onHome: () -> Unit,
    onSaveLeftover: ((title: String, compartment: PantryCompartment) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    var showLeftoverDialog by remember(done.recipeTitle) { mutableStateOf(false) }
    var leftoverSaved by remember(done.recipeTitle) { mutableStateOf(false) }
    var selectedCompartment by remember(done.recipeTitle) { mutableStateOf(PantryCompartment.CHILL) }

    if (showLeftoverDialog && onSaveLeftover != null) {
        AlertDialog(
            onDismissRequest = { showLeftoverDialog = false },
            title = { Text(CookDoneCopy.saveLeftover) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = done.recipeTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.text,
                    )
                    listOf(
                        PantryCompartment.CHILL to CookDoneCopy.chillOption,
                        PantryCompartment.FREEZE to CookDoneCopy.freezeOption,
                    ).forEach { (compartment, label) ->
                        val selected = selectedCompartment == compartment
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(role = Role.RadioButton) { selectedCompartment = compartment },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) colors.primarySoft else colors.surface,
                            border = BorderStroke(1.dp, if (selected) colors.primary else colors.line),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick = { selectedCompartment = compartment },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.text,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveLeftover(done.recipeTitle, selectedCompartment)
                        leftoverSaved = true
                        showLeftoverDialog = false
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(CookDoneCopy.confirmSave)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLeftoverDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(CookDoneCopy.cancelSave)
                }
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(112.dp).clip(CircleShape).background(colors.primarySoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcons.Check, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = CookDoneCopy.title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
            textAlign = TextAlign.Center,
        )
        Text(
            text = done.recipeTitle,
            style = MaterialTheme.typography.titleMedium,
            color = colors.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = CookDoneCopy.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.text,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
        done.summary?.let { summary ->
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onHome,
            colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        ) {
            Text(CookDoneCopy.home, fontWeight = FontWeight.Bold)
        }
        if (onSaveLeftover != null) {
            Spacer(Modifier.height(12.dp))
            if (leftoverSaved) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                    shape = RoundedCornerShape(16.dp),
                    color = colors.primarySoft,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(AppIcons.Check, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = CookDoneCopy.savedLeftover,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.accentText,
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { showLeftoverDialog = true },
                    border = BorderStroke(1.dp, colors.line),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.text),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                ) {
                    Text(CookDoneCopy.saveLeftover, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
