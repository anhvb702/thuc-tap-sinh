package com.loitu.yourrefri.reminder

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.ui.graphics.toArgb
import com.loitu.yourrefri.ui.theme.LightAppColors

object ReminderCardRenderer {
    const val WIDTH = 1080
    const val HEIGHT = 450
    const val SAFE_ZONE_HORIZONTAL_PX = 56
    internal const val CONTENT_SAFE_INSET_PX = 152
    internal const val DISH_DIAMETER_PX = 340f
    internal const val DISH_RIGHT_BLEED_PX = 20f
    private const val PAD = CONTENT_SAFE_INSET_PX.toFloat()
    private const val BADGE = "YOURREFRI · GỢI Ý BẾP NHÀ"

    internal data class DishBleedLayout(val diameter: Float, val centerX: Float, val centerY: Float, val textWidth: Int)

    fun accentColorArgb(): Int = LightAppColors.notifyHeroStart.toArgb()

    internal fun computeDishBleedLayout(hasPicture: Boolean): DishBleedLayout {
        val centerX = WIDTH - DISH_DIAMETER_PX / 2f + DISH_RIGHT_BLEED_PX
        val centerY = HEIGHT / 2f
        val textRight = if (hasPicture) {
            centerX - DISH_DIAMETER_PX / 2f - 28f
        } else {
            WIDTH - CONTENT_SAFE_INSET_PX.toFloat()
        }
        val textWidth = (textRight - CONTENT_SAFE_INSET_PX.toFloat()).toInt().coerceAtLeast(1)
        return DishBleedLayout(DISH_DIAMETER_PX, centerX, centerY, textWidth)
    }

    fun render(title: String, body: String, picture: Bitmap?): Bitmap {
        val out = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val colors = LightAppColors
        val onHero = colors.notifyHeroOn.toArgb()
        val background = Paint().apply {
            shader = LinearGradient(
                0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(),
                accentColorArgb(), colors.notifyHeroEnd.toArgb(), Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), background)
        val layout = computeDishBleedLayout(hasPicture = picture != null)
        if (picture != null) drawDish(canvas, picture, onHero, layout)

        val label = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = onHero
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
        }
        val badgeBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.notifyHeroBadgeBg.toArgb() }
        val badgeCenterY = 60f
        canvas.drawRoundRect(
            PAD, badgeCenterY - 22f, PAD + label.measureText(BADGE) + 36f, badgeCenterY + 22f,
            22f, 22f, badgeBackground,
        )
        canvas.drawText(BADGE, PAD + 18f, badgeCenterY - (label.ascent() + label.descent()) / 2f, label)

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = onHero
            textSize = 44f
            typeface = Typeface.DEFAULT_BOLD
        }
        val titleLayout = createTextLayout(title, titlePaint, layout.textWidth, maxLines = 2)
        val titleTop = 100f
        drawLayout(canvas, titleLayout, titleTop)

        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF7EBE6.toInt()
            textSize = 30f
        }
        val bodyTop = titleTop + titleLayout.height + 12f
        val bodyLayout = createTextLayout(body, bodyPaint, layout.textWidth, maxLines = 2)
        drawLayout(canvas, bodyLayout, bodyTop)

        return out
    }

    private fun drawLayout(canvas: Canvas, layout: StaticLayout, top: Float) {
        canvas.save()
        canvas.translate(PAD, top)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun createTextLayout(content: String, paint: TextPaint, width: Int, maxLines: Int): StaticLayout =
        StaticLayout.Builder.obtain(content, 0, content.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()

    private fun drawDish(canvas: Canvas, picture: Bitmap, frameColor: Int, layout: DishBleedLayout) {
        val (diameter, centerX, centerY) = layout
        val radius = diameter / 2f
        canvas.drawCircle(centerX, centerY, radius, dishPaint(picture, diameter, centerX, centerY))

        val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        canvas.drawCircle(centerX, centerY, radius - frame.strokeWidth / 2f, frame)
    }

    private fun dishPaint(picture: Bitmap, diameter: Float, centerX: Float, centerY: Float): Paint {
        val scale = diameter / minOf(picture.width, picture.height)
        val crop = Matrix().apply {
            setScale(scale, scale)
            postTranslate(centerX - picture.width * scale / 2f, centerY - picture.height * scale / 2f)
        }
        return Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            shader = BitmapShader(picture, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
                setLocalMatrix(crop)
            }
        }
    }
}
