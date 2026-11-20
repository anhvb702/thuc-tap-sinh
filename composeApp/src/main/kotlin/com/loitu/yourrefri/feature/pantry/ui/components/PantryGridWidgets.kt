package com.loitu.yourrefri.feature.pantry.ui

import android.Manifest
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.core.content.ContextCompat
import com.loitu.yourrefri.ui.components.ChunkyButton
import com.loitu.yourrefri.ui.components.FoodThumb
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.SHELF_DAY_OPTIONS
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

@Composable
internal fun PantryCategoryChips(selectedCategories: Set<PantryCategory>, onToggle: (PantryCategory) -> Unit) {
    val colors = AppTheme.colors
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PantryCategory.entries.forEach { category ->
            val selected = category in selectedCategories
            val shape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).clip(shape)
                    .background(if (selected) colors.primary else colors.surface)
                    .border(1.dp, if (selected) colors.primary else colors.line, shape)
                    .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onToggle(category) })
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = category.labelVi,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) colors.onPrimary else colors.text,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

internal enum class PantryAddFeature(val title: String, val description: String, val icon: ImageVector) {
    INGREDIENT("Thêm nguyên liệu", "Chọn nhanh rau, thịt, gia vị từ danh mục", AppIcons.Leaf),
    OTHER("Thêm món khác", "Tự nhập tên món chưa có trong danh sách", AppIcons.Pencil),
    LEFTOVER("Cất món thừa", "Lưu đồ ăn dư, hạn ước tính 2 ngày", AppIcons.Bowl),
    COMBO("Thêm combo đi chợ", "Bỏ nhanh gói đồ quen thuộc vào tủ", AppIcons.Cart),
    VOICE("Nói để thêm", "Đọc tên nguyên liệu bằng giọng nói", AppIcons.Mic),
}

@Composable
internal fun PantryAddSpeedDial(
    open: Boolean,
    voiceAvailable: Boolean,
    onOpenChange: (Boolean) -> Unit,
    onFeature: (PantryAddFeature) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val features = remember(voiceAvailable) {
        PantryAddFeature.entries.filter { voiceAvailable || it != PantryAddFeature.VOICE }
    }
    var focusedIndex by remember(voiceAvailable) { mutableStateOf<Int?>(null) }
    var dragging by remember(voiceAvailable) { mutableStateOf(false) }
    val changeOpen by rememberUpdatedState(onOpenChange)
    val launchFeature by rememberUpdatedState(onFeature)
    val radiusPx = with(LocalDensity.current) { 148.dp.toPx() }
    val animatorsEnabled = Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled()
    val offsets = remember(features, radiusPx) {
        radialFanAnglesDegrees(features.size).map { radialFanOffset(it, radiusPx) }
    }
    val fan = updateTransition(open, label = "pantry add fan")
    val rotation by animateFloatAsState(
        targetValue = if (open) 45f else 0f,
        animationSpec = if (animatorsEnabled) spring(stiffness = Spring.StiffnessMediumLow) else snap(),
        label = "pantry add rotation",
    )

    DisposableEffect(radiusPx, voiceAvailable) {
        onDispose { dragging = false }
    }

    fun setOpen(value: Boolean) {
        dragging = false
        focusedIndex = null
        changeOpen(value)
    }

    fun activate(index: Int) {
        setOpen(false)
        launchFeature(features[index])
    }

    Box(modifier) {
        AnimatedVisibility(
            visible = open,
            modifier = Modifier.matchParentSize(),
            enter = fadeIn(if (animatorsEnabled) tween(180) else snap()),
            exit = fadeOut(if (animatorsEnabled) tween(160) else snap()),
        ) {
            Box(
                Modifier.fillMaxSize()
                    .background(colors.text.copy(alpha = 0.28f))
                    .clickable(enabled = open, onClickLabel = "Đóng menu thêm món") { setOpen(false) },
            )
        }
        features.forEachIndexed { index, feature ->
            val progress by fan.animateFloat(
                transitionSpec = {
                    when {
                        !animatorsEnabled -> snap()
                        targetState -> tween(durationMillis = 240, delayMillis = index * 30)
                        else -> tween(durationMillis = 170, delayMillis = (features.lastIndex - index) * 20)
                    }
                },
                label = feature.title,
            ) { if (it) 1f else 0f }
            if (open || fan.currentState || fan.isRunning) {
                val focused = focusedIndex == index
                val scale by animateFloatAsState(
                    targetValue = if (focused) 1.18f else 1f,
                    animationSpec = if (animatorsEnabled) spring(stiffness = Spring.StiffnessMediumLow) else snap(),
                    label = "pantry petal scale",
                )
                val dim by animateFloatAsState(
                    targetValue = if (focusedIndex != null && !focused) 0.65f else 1f,
                    animationSpec = if (animatorsEnabled) tween(140) else snap(),
                    label = "pantry petal opacity",
                )
                val offset = offsets[index]
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 20.dp)
                        .size(48.dp)
                        .graphicsLayer {
                            translationX = offset.x * progress
                            translationY = offset.y * progress
                            scaleX = scale * (0.75f + 0.25f * progress)
                            scaleY = scaleX
                            alpha = dim * progress
                        },
                    onClick = {
                        val decision = resolveRadialTap(focusedIndex, index)
                        focusedIndex = decision.focusedIndex
                        if (decision.outcome == RadialTapOutcome.TRIGGER) activate(index)
                    },
                    enabled = open && !dragging,
                    shape = CircleShape,
                    color = if (focused) colors.primary else colors.surface,
                    contentColor = if (focused) colors.onPrimary else colors.primary,
                    border = BorderStroke(1.dp, if (focused) colors.primary else colors.line),
                    shadowElevation = if (focused) 8.dp else 2.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(feature.icon, contentDescription = feature.title, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = open,
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(horizontal = 16.dp).padding(top = 12.dp, bottom = 232.dp),
            enter = fadeIn(if (animatorsEnabled) tween(180) else snap()),
            exit = fadeOut(if (animatorsEnabled) tween(140) else snap()),
        ) {
            AnimatedContent(
                targetState = focusedIndex,
                transitionSpec = {
                    if (animatorsEnabled) fadeIn(tween(180)) togetherWith fadeOut(tween(100))
                    else fadeIn(snap()) togetherWith fadeOut(snap())
                },
                contentAlignment = Alignment.BottomEnd,
                label = "pantry add preview",
            ) { index ->
                val feature = index?.let(features::getOrNull)
                if (index == null || feature == null) {
                    Surface(shape = RoundedCornerShape(16.dp), color = colors.surface) {
                        Text(
                            "Chạm cánh quạt để xem",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.text,
                        )
                    }
                } else {
                    PantryAddFeaturePreview(feature, dragging, open && index == focusedIndex) {
                        activate(index)
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { setOpen(!open) },
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
                .size(56.dp)
                .pointerInput(radiusPx, voiceAvailable) {
                    detectDragGestures(
                        onDragStart = { setOpen(true); dragging = true },
                        onDragEnd = {
                            if (dragging) focusedIndex?.let(::activate)
                            dragging = false
                        },
                        onDragCancel = {
                            dragging = false
                            focusedIndex = null
                        },
                    ) { change, _ ->
                        // Pointer positions remain local to the FAB even after the finger leaves it.
                        focusedIndex = resolveRadialHitIndex(
                            dragX = change.position.x - size.width / 2f,
                            dragY = change.position.y - size.height / 2f,
                            count = features.size,
                            radiusPx = radiusPx,
                        )
                    }
                },
            shape = CircleShape,
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
        ) {
            Icon(
                AppIcons.Add,
                contentDescription = if (open) "Đóng menu thêm món" else "Thêm vào tủ",
                modifier = Modifier.graphicsLayer { rotationZ = rotation },
            )
        }
    }
}

@Composable
private fun PantryAddFeaturePreview(
    feature: PantryAddFeature,
    dragging: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Surface(
        onClick = onClick,
        enabled = enabled && !dragging,
        modifier = Modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.line),
        shadowElevation = 6.dp,
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    feature.icon, contentDescription = null, tint = colors.onPrimary,
                    modifier = Modifier.size(32.dp).background(colors.primary, CircleShape).padding(6.dp),
                )
                Text(feature.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.text)
            }
            Text(
                feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    if (dragging) "Thả để mở" else "Chạm để mở",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.accentText,
                )
                Icon(AppIcons.ArrowRight, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(14.dp))
            }
        }
    }
}

/** Tiêu đề nhóm: icon + "Rau củ (6)". */
@Composable
internal fun CategoryHeader(group: PantryCategoryGroup) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(categoryIconFor(group.key), contentDescription = null, tint = colors.accentText, modifier = Modifier.size(22.dp))
        Text(
            text = group.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
        )
    }
}

internal fun categoryIconFor(key: String) = when (key) {
    "rau-cu" -> AppIcons.Leaf
    "thit" -> AppIcons.Meat
    "trai-cay" -> AppIcons.Tomato
    "sua-trung" -> AppIcons.Milk
    "khac-dau-nam" -> AppIcons.Tofu
    else -> AppIcons.Utensils
}

/** Thanh hành động nổi ở đáy: hai nút dạng viên thuốc cao >= 52dp. */
@Composable
internal fun PantryActionBar(
    voiceAvailable: Boolean,
    onManual: () -> Unit,
    onVoice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ActionPill(
            label = PantryActionCopy.manual,
            icon = AppIcons.Keyboard,
            container = colors.surfaceSoft,
            content = colors.text,
            borderColor = colors.line,
            description = PantryScreenCopy.addFoodDescription,
            onClick = onManual,
            modifier = Modifier.weight(1f),
        )
        if (voiceAvailable) {
            ActionPill(
                label = PantryActionCopy.voice,
                icon = AppIcons.Mic,
                container = colors.cta,
                content = colors.onCta,
                borderColor = colors.cta,
                description = PantryActionCopy.voiceDescription,
                onClick = onVoice,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun ActionPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    container: Color,
    content: Color,
    borderColor: Color,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(50))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        shape = RoundedCornerShape(50),
        color = container,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = content,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
    }
}

/** Bảng nhỏ "Đang nghe...": chữ nghe được hiện dần, có Dừng / Hủy. Không tự thêm món nào. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VoiceSheet(
    phase: VoicePhase,
    partial: String,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onManual: () -> Unit,
) {
    val colors = AppTheme.colors
    ModalBottomSheet(
        onDismissRequest = onCancel,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = colors.background,
        contentColor = colors.text,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(AppIcons.Mic, contentDescription = null, tint = colors.ctaText, modifier = Modifier.size(28.dp))
                Text(
                    text = when (phase) {
                        VoicePhase.LISTENING -> VOICE_LISTENING_TITLE
                        VoicePhase.NOT_HEARD -> VOICE_NOT_HEARD
                        else -> VOICE_UNAVAILABLE_NOW
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
            }
            if (phase == VoicePhase.LISTENING) {
                Text(
                    text = partial.ifBlank { VOICE_LISTENING_HINT },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (partial.isBlank()) colors.muted else colors.text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.line, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
                Button(
                    onClick = onStop,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta),
                ) { Text("Dừng", fontWeight = FontWeight.Bold) }
                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("Hủy", color = colors.muted)
                }
            } else {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cta, contentColor = colors.onCta),
                ) { Text("Nói lại", fontWeight = FontWeight.Bold) }
                TextButton(onClick = onManual, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("Chọn thủ công", color = colors.accentText)
                }
                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("Đóng", color = colors.muted)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/**
 * Hàng thực phẩm: khung ảnh 48dp, tên, hạn ước tính và bộ đếm − / + tròn 48dp.
 * Chạm vào tên để đổi trạng thái có / hết; "Sửa hạn" và "Đổi đơn vị" giữ vùng chạm 48dp.
 */
@Composable
internal fun WarmFoodCard(
    cell: PantryCell,
    quantity: Int,
    selected: Boolean = false,
    onTap: () -> Unit,
    onEditShelf: () -> Unit,
    onEditUnit: () -> Unit,
    onEditQuantity: () -> Unit,
    onIncreaseQty: () -> Unit,
    onDecreaseQty: () -> Unit,
) {
    val colors = AppTheme.colors
    val cardCopy = pantryCardCopy(cell.have, quantity)
    val urgent = pantryRowUrgent(cell.have, cell.daysLeft)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (cell.have) colors.surface else Color.Transparent,
        border = BorderStroke(2.dp, if (selected) colors.primary else if (cell.have) colors.line else colors.primarySoft),
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable(onClick = onTap)
                    .semantics {
                        contentDescription = if (cell.have) {
                            "Đánh dấu hết ${cell.nameVi}"
                        } else {
                            "Đánh dấu có ${cell.nameVi}"
                        }
                    },
            ) {
                FoodThumb(
                    ingredientId = cell.ingredientId,
                    size = 36.dp,
                    shape = RoundedCornerShape(10.dp),
                    urgent = urgent,
                    modifier = Modifier.alpha(if (cell.have) 1f else 0.55f),
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(
                        text = cell.nameVi,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (cell.have) colors.text else colors.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (cardCopy.showEstimatedStatus) {
                        cell.daysLeft?.let { days ->
                            Text(
                                text = pantryRowStatus(days),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (urgent) FontWeight.SemiBold else FontWeight.Normal,
                                ),
                                color = if (urgent) colors.ctaText else colors.accentText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    } else {
                        Text(
                            text = cardCopy.statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            if (cell.have) {
                QuantityStepper(
                    name = cell.nameVi,
                    quantity = quantity,
                    unit = cell.unit,
                    onDecrease = onDecreaseQty,
                    onIncrease = onIncreaseQty,
                    onEdit = onEditQuantity,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onEditShelf,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                            .semantics { contentDescription = "Sửa hạn ước tính cho ${cell.nameVi}" },
                        contentPadding = PaddingValues(0.dp),
                    ) { Text("Sửa hạn", style = MaterialTheme.typography.labelSmall, color = colors.accentText) }
                    TextButton(
                        onClick = onEditUnit,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                            .semantics { contentDescription = "Đổi đơn vị số lượng ${cell.nameVi}, hiện là ${cell.unit}" },
                        contentPadding = PaddingValues(0.dp),
                    ) { Text("Đổi đơn vị", style = MaterialTheme.typography.labelSmall, color = colors.muted, textAlign = TextAlign.Center) }
                }
            }
        }
    }
}

@Composable
internal fun QuantityStepper(
    name: String,
    quantity: Int,
    unit: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onDecrease)
                .semantics {
                    contentDescription = if (quantity > 1) "Giảm số lượng $name" else "Đánh dấu hết $name"
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcons.Minus, contentDescription = null, tint = colors.text, modifier = Modifier.size(32.dp)
                .background(colors.primarySoft, CircleShape).padding(8.dp))
        }
        val quantityShape = RoundedCornerShape(10.dp)
        Box(
            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                .clip(quantityShape)
                .border(1.dp, colors.line, quantityShape)
                .clickable(role = Role.Button, onClick = onEdit)
                .semantics { contentDescription = "Chỉnh số lượng $name" },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$quantity $unit",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.text,
                textAlign = TextAlign.Center,
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onIncrease)
                .semantics { contentDescription = "Tăng số lượng $name" },
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcons.Plus, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(32.dp)
                .background(colors.primarySoft, CircleShape).padding(8.dp))
        }
    }
}

@Composable
internal fun ShelfDayOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = colors.primary,
                    unselectedColor = colors.muted,
                ),
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.text,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

/** Ô chạm trong lưới chọn món: icon nét trong vòng tròn 36dp và tên món (không cắt). */
@Composable
internal fun QuickAddTile(
    name: String,
    iconId: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Surface(
        modifier = modifier
            .heightIn(min = 88.dp)
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() })
            .semantics { contentDescription = quickAddTileDescription(name, selected) },
        shape = RoundedCornerShape(16.dp),
        color = if (selected) colors.primarySoft else colors.surfaceSoft,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.line),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (selected) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(colors.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = AppIcons.Check,
                        contentDescription = null,
                        tint = colors.accentText,
                        modifier = Modifier.size(22.dp),
                    )
                }
            } else {
                // Ảnh món hoặc icon riêng (FoodThumb); chưa có thì icon nhóm.
                FoodThumb(ingredientId = iconId, size = 36.dp, shape = CircleShape)
            }
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.text,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

/** Phần "Hạn dùng ước tính": mặc định theo gợi ý, hoặc số ngày áp dụng cho các món đã chọn. */
@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun AddFoodEstimateSection(
    state: PantryGridState,
    query: String,
    selectedIds: Set<String>,
    days: Int?,
    onDays: (Int?) -> Unit,
) {
    val colors = AppTheme.colors
    val selectedIngredient = selectedIds.singleOrNull()?.let { id -> state.ingredients.firstOrNull { it.id == id } }
    val matched = selectedIngredient
        ?: if (selectedIds.isEmpty()) quickAddMatch(state.ingredients, query) else null
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = colors.surface,
        labelColor = colors.text,
        selectedContainerColor = colors.primary,
        selectedLabelColor = colors.onPrimary,
        disabledContainerColor = colors.surface,
        disabledLabelColor = colors.muted,
    )
    Text(
        AddFoodSheetCopy.estimateHeader,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = colors.text,
    )
    Text(
        text = when {
            selectedIds.size > 1 -> days?.let { "Áp dụng khoảng $it ngày cho các món đã chọn." }
                ?: "Mỗi món dùng hạn gợi ý riêng."
            matched == null -> "Chọn món để xem hạn dùng gợi ý."
            days != null -> "Hạn đã chọn: khoảng $days ngày"
            else -> "Hạn dùng theo gợi ý đang được chọn."
        },
        style = MaterialTheme.typography.bodySmall,
        color = colors.accentText,
        modifier = Modifier.padding(top = 2.dp),
    )
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (selectedIds.size <= 1) {
            quickAddEstimateOption(matched, days)?.let { option ->
                FilterChip(
                    modifier = Modifier.heightIn(min = 48.dp),
                    selected = option.selected,
                    onClick = { onDays(option.daysOnSelect) },
                    label = { Text(option.label) },
                    shape = RoundedCornerShape(16.dp),
                    colors = chipColors,
                )
            }
        } else if (days != null) {
            FilterChip(
                modifier = Modifier.heightIn(min = 48.dp),
                selected = false,
                onClick = { onDays(null) },
                label = { Text("Theo gợi ý riêng từng món") },
                shape = RoundedCornerShape(16.dp),
                colors = chipColors,
            )
        }
        quickAddCustomDays(matched).forEach { d ->
            FilterChip(
                modifier = Modifier.heightIn(min = 48.dp),
                selected = days == d,
                onClick = { onDays(d) },
                label = { Text(if (selectedIds.size > 1) "$d ngày · cho tất cả" else "$d ngày") },
                shape = RoundedCornerShape(16.dp),
                colors = chipColors,
            )
        }
    }
}

internal enum class VoicePhase { IDLE, LISTENING, NOT_HEARD, ERROR }

/** Chữ của ô tìm trên màn Tủ lạnh. */
internal object PantrySearchCopy {
    const val openDescription = "Tìm món trong tủ"
    const val closeDescription = "Đóng ô tìm"
    const val clearDescription = "Xóa chữ đã gõ"
    const val placeholder = "Tìm món, ví dụ: cà chua"
    const val notFound = "Chưa thấy món này, mình thêm vào tủ nhé?"
    const val addAction = "Thêm vào tủ"
    const val notFoundSectionTitle = "Không thấy món bạn cần?"
}

internal object PantryActionCopy {
    const val manual = "Thêm thủ công"
    const val voice = "Thêm bằng giọng nói"
    const val voiceDescription = "Thêm thực phẩm bằng giọng nói"
}

internal const val VOICE_MIC_TITLE = "Bật micro nhé?"
internal const val VOICE_MIC_EXPLAIN = "Mình cần micro để nghe tên món, âm thanh không rời khỏi máy."
internal const val VOICE_MIC_DENIED = "Chưa bật micro, mình vẫn thêm thủ công được nhé"
internal const val VOICE_LISTENING_TITLE = "Đang nghe..."
internal const val VOICE_LISTENING_HINT = "Bạn nói tên món, ví dụ: cà chua, trứng gà và hành lá"
internal const val VOICE_UNAVAILABLE_NOW = "Máy chưa nghe được lúc này, mình vẫn thêm thủ công được nhé"
internal const val VOICE_ALREADY_HAVE = "Những món này đã có trong tủ rồi nhé"

internal fun voiceHeardLine(heard: String): String = "Mình nghe được: $heard"

