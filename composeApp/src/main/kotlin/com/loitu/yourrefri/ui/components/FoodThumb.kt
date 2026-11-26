package com.loitu.yourrefri.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.loitu.yourrefri.feature.pantry.ui.FoodGlyph
import com.loitu.yourrefri.feature.pantry.ui.foodGlyphFor
import com.loitu.yourrefri.feature.pantry.ui.foodIconFor
import com.loitu.yourrefri.ui.theme.AppIcons
import com.loitu.yourrefri.ui.theme.AppTheme
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Khung ảnh món ăn dùng chung. Có ảnh thật trong assets (food/<id>.webp|jpg|png, recipes/<id>...) thì hiện ảnh,
 * chưa có thì hiện icon minh họa màu trong khung. Ảnh đi kèm APK nên vẫn chạy khi không có mạng.
 */
internal fun foodAssetCandidates(ingredientId: String): List<String> = assetCandidates("food", ingredientId)

internal fun recipeAssetCandidates(recipeId: String): List<String> = assetCandidates("recipes", recipeId)

internal fun iconAssetCandidates(ingredientId: String): List<String> = assetCandidates("icons", ingredientId)

private fun assetCandidates(folder: String, id: String): List<String> {
    if (id.isBlank() || '/' in id || '\\' in id || ".." in id) return emptyList()
    return listOf("webp", "jpg", "png").map { "$folder/$id.$it" }
}

internal fun computeInSampleSize(srcWidth: Int, srcHeight: Int, reqSizePx: Int): Int {
    if (srcWidth <= 0 || srcHeight <= 0 || reqSizePx <= 0) return 1
    val maxSample = minOf(srcWidth, srcHeight) / reqSizePx
    var sample = 1
    while (sample <= maxSample / 2) sample *= 2
    return sample
}

private fun sizeBucketFor(reqSizePx: Int): Long =
    ((reqSizePx.coerceAtLeast(32).toLong() + 31) / 32) * 32

internal fun cacheKeyFor(candidates: List<String>, reqSizePx: Int): String =
    "${candidates.firstOrNull().orEmpty()}:${sizeBucketFor(reqSizePx)}"

private val assetBitmapCache by lazy {
    object : LruCache<String, ImageBitmap>(16 * 1024) {
        override fun sizeOf(key: String, value: ImageBitmap): Int =
            ((value.width * value.height * 4) / 1024).coerceAtLeast(1)
    }
}
private val missingAssetPaths = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
private val assetLoadLocks = Array(16) { Any() }

private fun peekCachedAsset(candidates: List<String>, reqSizePx: Int): ImageBitmap? =
    candidates.firstNotNullOfOrNull { path -> assetBitmapCache.get(cacheKeyFor(listOf(path), reqSizePx)) }

private suspend fun loadAssetAsync(
    context: Context,
    candidates: List<String>,
    reqSizePx: Int,
): ImageBitmap? = withContext(Dispatchers.IO) {
    // Cached entries must also satisfy the largest request in their bucket.
    val targetSizePx = sizeBucketFor(reqSizePx).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    for (path in candidates) {
        ensureActive()
        val cacheKey = cacheKeyFor(listOf(path), reqSizePx)
        // Concurrent requests for the same path must share the first miss.
        synchronized(assetLoadLocks[path.hashCode() and assetLoadLocks.lastIndex]) {
            ensureActive()
            if (path in missingAssetPaths) return@synchronized
            assetBitmapCache.get(cacheKey)?.let { return@withContext it }
            try {
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.assets.open(path).use { BitmapFactory.decodeStream(it, null, options) }
                options.inSampleSize = computeInSampleSize(options.outWidth, options.outHeight, targetSizePx)
                options.inJustDecodeBounds = false
                context.assets.open(path).use { BitmapFactory.decodeStream(it, null, options) }
                    ?.asImageBitmap()?.let { bitmap ->
                        assetBitmapCache.put(cacheKey, bitmap)
                        return@withContext bitmap
                    }
            } catch (_: IOException) {
                missingAssetPaths.add(path)
            }
        }
    }
    null
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
        FoodGlyph.SPICE -> if (dark) Color(0xFF3A3322) to Color(0xFFE8D8B9) else Color(0xFFF1E8D2) to Color(0xFF7A6330)
        FoodGlyph.BOWL -> if (dark) Color(0xFF2E3C2B) to Color(0xFF9DBB9C) else Color(0xFFDDE8D9) to Color(0xFF3F6B3E)
    }
}

@Composable
fun FoodThumb(
    ingredientId: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    shape: Shape = CircleShape,
    urgent: Boolean = false,
    group: String? = null,
) {
    val context = LocalContext.current
    val reqSizePx = with(LocalDensity.current) { size.roundToPx().coerceAtLeast(48) }
    val candidates = remember(ingredientId) { foodAssetCandidates(ingredientId) + iconAssetCandidates(ingredientId) }
    val cached = remember(candidates, reqSizePx) { peekCachedAsset(candidates, reqSizePx) }
    val photo = key(candidates, reqSizePx) {
        val photo by produceState(initialValue = cached, key1 = candidates, key2 = reqSizePx) {
            if (value == null) value = loadAssetAsync(context, candidates, reqSizePx)
        }
        photo
    }
    val frame = modifier.size(size).clip(shape)
    if (photo != null) {
        Image(bitmap = photo, contentDescription = null, contentScale = ContentScale.Crop, modifier = frame)
        return
    }
    val colors = AppTheme.colors
    val glyph = foodGlyphFor(ingredientId, group)
    val (tintBg, tintFg) = foodTint(glyph)
    Box(
        modifier = frame.background(if (urgent) colors.warnSoft else tintBg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = foodIconFor(ingredientId, group),
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
    val reqSizePx = with(LocalDensity.current) { size.roundToPx().coerceAtLeast(48) }
    val candidates = remember(recipeId) { recipeAssetCandidates(recipeId) }
    val cached = remember(candidates, reqSizePx) { peekCachedAsset(candidates, reqSizePx) }
    val photo = key(candidates, reqSizePx) {
        val photo by produceState(initialValue = cached, key1 = candidates, key2 = reqSizePx) {
            if (value == null) value = loadAssetAsync(context, candidates, reqSizePx)
        }
        photo
    }
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
