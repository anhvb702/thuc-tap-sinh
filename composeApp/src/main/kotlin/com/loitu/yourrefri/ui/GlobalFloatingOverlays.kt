package com.loitu.yourrefri.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.loitu.yourrefri.ui.components.areSystemAnimatorsEnabled
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GlobalFloatingActionButtons(
    onOpenWeeklyPlan: () -> Unit,
    onOpenRandomWheel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val animatorsEnabled = areSystemAnimatorsEnabled()
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GlobalFloatingActionButton(
            AppIcons.Calendar, "Thực đơn", colors.primary, colors.onPrimary, 0, spinIcon = false, animatorsEnabled, onOpenWeeklyPlan,
        )
        GlobalFloatingActionButton(
            AppIcons.Wheel, "Quay món", colors.cta, colors.onCta, 1, spinIcon = true, animatorsEnabled, onOpenRandomWheel,
        )
    }
}

@Composable
private fun GlobalFloatingActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    index: Int,
    spinIcon: Boolean,
    animatorsEnabled: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val entrance by animateFloatAsState(
        targetValue = if (entered || !animatorsEnabled) 1f else 0f,
        animationSpec = if (animatorsEnabled) tween(260, delayMillis = index * 60) else snap(),
        label = "global action entrance",
    )
    val scale by animateFloatAsState(
        targetValue = if (animatorsEnabled && pressed) 0.92f else 1f,
        animationSpec = if (animatorsEnabled) {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        } else snap(),
        label = "fabPress",
    )
    val iconMotion = if (animatorsEnabled) {
        rememberInfiniteTransition(label = "global action icon").animateFloat(
            initialValue = if (spinIcon) -12f else 0f,
            targetValue = if (spinIcon) 12f else -2f,
            animationSpec = infiniteRepeatable(tween(1300), RepeatMode.Reverse),
            label = "global icon motion",
        )
    } else null
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .semantics { role = Role.Button }
            .graphicsLayer {
                val progress = if (animatorsEnabled) entrance else 1f
                scaleX = scale * (0.85f + 0.15f * progress)
                scaleY = scaleX
                alpha = progress
                translationY = (1f - progress) * 8.dp.toPx()
            },
        shape = RoundedCornerShape(999.dp),
        shadowElevation = 6.dp,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp).graphicsLayer {
                    val motion = iconMotion?.value ?: 0f
                    if (spinIcon) rotationZ = motion else translationY = motion.dp.toPx()
                },
            )
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun FloatingOverlayDialog(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.86f), shape = RoundedCornerShape(24.dp), color = colors.background, shadowElevation = 8.dp) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(start = 20.dp, top = 12.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.text)
                    IconButton(onClick = onDismiss) { Icon(AppIcons.Close, contentDescription = "Đóng", tint = colors.text) }
                }
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp)) { content() }
            }
        }
    }
}
