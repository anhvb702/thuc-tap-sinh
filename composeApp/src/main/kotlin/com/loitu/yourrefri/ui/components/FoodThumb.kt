package com.loitu.yourrefri.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.ui.FoodGlyph
import com.loitu.yourrefri.feature.pantry.ui.foodGlyphFor
import com.loitu.yourrefri.feature.pantry.ui.foodIconFor
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme

/**
 * Khung ảnh món ăn dùng chung. Có ảnh thật trong assets (food/<id>.webp|jpg|png, recipes/<id>...) thì hiện ảnh,
 * chưa có thì hiện icon minh họa màu trong khung. Ảnh đi kèm APK nên vẫn chạy khi không có mạng.
 */
internal fun foodAssetCandidates(ingredientId: String): List<String> = assetCandidates("food", ingredientId)

internal fun recipeAssetCandidates(recipeId: String): List<String> = assetCandidates("recipes", recipeId)

private fun assetCandidates(folder: String, id: String): List<String> {
    if (id.isBlank() || '/' in id || '\\' in id || ".." in id) return emptyList()
    return listOf("webp", "jpg", "png").map { "$folder/$id.$it" }
}

private fun loadAsset(context: Context, candidates: List<String>): ImageBitmap? {
    for (path in candidates) {
        try {
            context.assets.open(path).use { stream ->
                BitmapFactory.decodeStream(stream)?.let { return it.asImageBitmap() }
            }
        } catch (_: Exception) {
            // Không có ảnh này: thử định dạng kế tiếp.
        }
    }
    return null
}

/** Màu nền và màu icon theo nhóm thực phẩm; sáng và tối đều đạt tương phản đồ họa >= 3:1. */
@Composable
internal fun foodTint(glyph: FoodGlyph): Pair<Color, Color> {
    val dark = AppTheme.colors.isDark
    return when (glyph) {
        FoodGlyph.TOMATO -> if (dark) Color(0xFF4A2A24) to Color(0xFFF4A292) else Color(0xFFFBE3DC) to Color(0xFFC2452D)
        FoodGlyph.EGG -> if (dark) Color(0xFF453A1A) to Color(0xFFF2D27A) else Color(0xFFFFF1C9) to Color(0xFF8A5F00)
        FoodGlyph.MEAT -> if (dark) Color(0xFF4A2A30) to Color(0xFFF0A8B1) else Color(0xFFF8DDE0) to Color(0xFFA8434F)
        FoodGlyph.FISH -> if (dark) Color(0xFF22343F) to Color(0xFF9CC3DD) else Color(0xFFDCE9F2) to Color(0xFF3F6C8C)
        FoodGlyph.MILK -> if (dark) Color(0xFF2A3340) to Color(0xFFB4C4DC) else Color(0xFFE6ECF4) to Color(0xFF55688A)
        FoodGlyph.LEAF -> if (dark) Color(0xFF2E3C2B) to Color(0xFF9DBB9C) else Color(0xFFDDE8D9) to Color(0xFF3F6B3E)
        FoodGlyph.TOFU -> if (dark) Color(0xFF3A3322) to Color(0xFFE8D8B9) else Color(0xFFF1E8D2) to Color(0xFF7A6330)
    }
}

@Composable
fun FoodThumb(
    ingredientId: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    shape: Shape = CircleShape,
    urgent: Boolean = false,
) {
    val context = LocalContext.current
    val photo = remember(ingredientId) { loadAsset(context, foodAssetCandidates(ingredientId)) }
    val frame = modifier.size(size).clip(shape)
    if (photo != null) {
        Image(bitmap = photo, contentDescription = null, contentScale = ContentScale.Crop, modifier = frame)
        return
    }
    val colors = AppTheme.colors
    val (tintBg, tintFg) = foodTint(foodGlyphFor(ingredientId))
    Box(
        modifier = frame.background(if (urgent) colors.warnSoft else tintBg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = foodIconFor(ingredientId),
            contentDescription = null,
            tint = if (urgent) colors.warnText else tintFg,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}

/** Ảnh món (thẻ gợi ý, thẻ nổi bật). Chưa có ảnh thì hiện nồi trên nền cam đất nhạt. */
@Composable
fun RecipeThumb(
    recipeId: String,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    shape: Shape = RoundedCornerShape(14.dp),
) {
    val context = LocalContext.current
    val photo = remember(recipeId) { loadAsset(context, recipeAssetCandidates(recipeId)) }
    val frame = modifier.size(size).clip(shape)
    if (photo != null) {
        Image(bitmap = photo, contentDescription = null, contentScale = ContentScale.Crop, modifier = frame)
        return
    }
    val colors = AppTheme.colors
    Box(modifier = frame.background(colors.ctaSoft), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = AppIcons.Pot,
            contentDescription = null,
            tint = colors.ctaText,
            modifier = Modifier.size(size * 0.45f),
        )
    }
}
