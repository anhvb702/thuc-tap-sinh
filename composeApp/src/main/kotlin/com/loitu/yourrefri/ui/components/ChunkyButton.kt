package com.loitu.yourrefri.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Nút nổi có cạnh dưới: khi nhấn, mặt nút trượt xuống che cạnh, tạo cảm giác bấm thật.
 */
@Composable
fun ChunkyButton(
    onClick: () -> Unit,
    container: Color,
    edge: Color,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    depth: Dp = 6.dp,
    minHeight: Dp = 56.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val sink = if (areSystemAnimatorsEnabled()) {
        animateDpAsState(
            targetValue = if (pressed) depth else 0.dp,
            animationSpec = tween(durationMillis = 90),
            label = "chunkySink",
        ).value
    } else {
        0.dp
    }
    Box(modifier = modifier.pressScale(interaction)) {
        // Cạnh dưới: nằm cả vùng nút, mặt nút che lên trên khi không nhấn.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(edge),
        )
        Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = sink)
                .clip(shape)
                .background(container)
                .heightIn(min = minHeight)
                .clickable(
                    interactionSource = interaction,
                    indication = LocalIndication.current,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
        // Chỗ giữ cạnh dưới: cao bằng depth, để lớp cạnh hiện ra khi mặt nút không trượt xuống.
        Spacer(Modifier.height(depth))
        }
    }
}
