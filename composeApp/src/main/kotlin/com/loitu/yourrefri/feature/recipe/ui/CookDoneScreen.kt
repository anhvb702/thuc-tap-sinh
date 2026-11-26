package com.loitu.yourrefri.feature.recipe.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import com.loitu.yourrefri.ui.components.RecipeThumb
import com.loitu.yourrefri.ui.components.areSystemAnimatorsEnabled
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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

internal fun fireworkParticleOffset(
    angleRad: Float,
    speed: Float,
    progress: Float,
    gravity: Float = 260f,
): Pair<Float, Float> {
    val p = progress.coerceIn(0f, 1f)
    val dx = cos(angleRad) * speed * p
    val dy = sin(angleRad) * speed * p + 0.5f * gravity * p * p
    return dx to dy
}

internal fun fireworkParticleAlpha(progress: Float): Float =
    (1f - progress.coerceIn(0f, 1f)).coerceIn(0f, 1f)

/** Màn chúc mừng sau khi nấu xong. Người gọi nối [onHome]. */
@Composable
internal fun CookDoneScreen(
    done: CookDone,
    onHome: () -> Unit,
    onUndo: (List<com.loitu.yourrefri.feature.pantry.logic.PantryGridAction>) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val animatorsEnabled = areSystemAnimatorsEnabled()
    Box(modifier.fillMaxSize()) {
        CookDoneContent(done, onHome, onUndo, animatorsEnabled)
        // The log ID arrives asynchronously for the same cooking completion.
        CookFireworksOverlay(done.copy(logId = null), animatorsEnabled, Modifier.matchParentSize())
    }
}

@Composable
private fun CookDoneContent(
    done: CookDone,
    onHome: () -> Unit,
    onUndo: (List<com.loitu.yourrefri.feature.pantry.logic.PantryGridAction>) -> Unit,
    animatorsEnabled: Boolean,
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
        modifier = Modifier
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
        var popped by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { popped = true }
        val popScale by animateFloatAsState(
            targetValue = if (popped) 1f else celebrationPopStartScale(animatorsEnabled),
            animationSpec = if (animatorsEnabled) spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow) else snap(),
            label = "cookDonePop",
        )
        Box(
            modifier = Modifier.size(112.dp).graphicsLayer { scaleX = popScale; scaleY = popScale }
                .clip(CircleShape).background(colors.primarySoft),
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

@Composable
private fun CookFireworksOverlay(done: CookDone, animatorsEnabled: Boolean, modifier: Modifier) {
    if (!animatorsEnabled) return

    val colors = AppTheme.colors
    val palette = listOf(colors.cta, colors.primary, colors.accentText, colors.primarySoft)
    val progress = remember(done) { Animatable(0f) }
    LaunchedEffect(done) {
        progress.animateTo(1f, tween(durationMillis = 1400, easing = LinearEasing))
    }
    Canvas(modifier) {
        val p = progress.value
        if (p <= 0f || p >= 1f) return@Canvas
        val alpha = fireworkParticleAlpha(p)
        val dpScale = 1.dp.toPx()
        val left = Offset(size.width * 0.28f, size.height * 0.33f)
        val right = Offset(size.width * 0.72f, size.height * 0.27f)
        repeat(28) { index ->
            val origin = if (index < 14) left else right
            val angle = (index * 2 * PI / 14 + if (index < 14) 0.0 else PI / 14).toFloat()
            val speed = 78f + (index % 5) * 15f
            val (dx, dy) = fireworkParticleOffset(angle, speed, p)
            val center = origin + Offset(dx * dpScale, dy * dpScale)
            val color = palette[index % palette.size]
            if (index % 3 == 0) {
                val tail = Offset(cos(angle), sin(angle)) * (5.dp.toPx() * alpha)
                drawLine(color, center - tail, center, 2.dp.toPx(), StrokeCap.Round, alpha = alpha)
            } else {
                drawCircle(color, radius = (2 + index % 3).dp.toPx(), center = center, alpha = alpha)
            }
        }
    }
}
