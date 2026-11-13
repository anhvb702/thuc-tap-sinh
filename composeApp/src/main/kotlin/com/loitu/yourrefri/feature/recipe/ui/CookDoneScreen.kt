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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

internal object CookDoneCopy {
    const val title = "Xong rồi, dọn cơm thôi!"
    const val subtitle = "Chúc cả nhà ngon miệng"
    const val home = "Về trang chủ"
}

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
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
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
    }
}
