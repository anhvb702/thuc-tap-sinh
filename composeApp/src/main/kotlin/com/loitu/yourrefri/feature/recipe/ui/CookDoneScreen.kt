package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import com.loitu.yourrefri.ui.components.RecipeThumb

internal object CookDoneCopy {
    const val title = "Xong rồi, dọn cơm thôi!"
    const val subtitle = "Chúc cả nhà ngon miệng"
    const val home = "Về trang chủ"
}

/** Một lần nấu vừa xong: món và phần tủ đã cập nhật (nếu có). */
internal data class CookDone(
    val recipeTitle: String,
    val summary: String?,
    // Hành động trả lại tủ nếu bấm Hoàn tác trong 5 giây; rỗng thì không hiện thanh hoàn tác.
    val undo: List<com.loitu.yourrefri.feature.pantry.logic.PantryGridAction> = emptyList(),
    // Id dòng "Đã nấu" vừa ghi; null khi chưa ghi xong hoặc không ghi được.
    val logId: Long? = null,
    val recipeId: String? = null,
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
    onUndo: (List<com.loitu.yourrefri.feature.pantry.logic.PantryGridAction>) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    // Đếm ngược 5 giây; hết giờ thì thanh hoàn tác tự ẩn, không còn hoàn tác được nữa.
    var secondsLeft by remember { mutableIntStateOf(5) }
    LaunchedEffect(done) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        done.recipeId?.let { recipeId ->
            RecipeThumb(
                recipeId = recipeId,
                modifier = Modifier.fillMaxWidth(),
                size = 160.dp,
                shape = RoundedCornerShape(20.dp),
            )
            Spacer(Modifier.height(20.dp))
        }
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
        if (done.undo.isNotEmpty() && secondsLeft > 0) {
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable(role = Role.Button) {
                    secondsLeft = 0
                    onUndo(done.undo)
                },
                shape = RoundedCornerShape(16.dp),
                color = colors.snackbar,
            ) {
                Text(
                    text = undoBarLabel(secondsLeft),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.onSnackbar,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(12.dp))
        }
        Button(
            onClick = onHome,
            colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        ) {
            Text(CookDoneCopy.home, fontWeight = FontWeight.Bold)
        }
    }
}
