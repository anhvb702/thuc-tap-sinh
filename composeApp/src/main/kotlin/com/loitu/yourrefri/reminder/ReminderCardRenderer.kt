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
    internal const val CONTENT_SAFE_INSET_PX = 48
    internal const val CARD_CORNER_RADIUS_PX = 28f
    internal const val DISH_DIAMETER_PX = 332f
    internal const val DISH_RIGHT_MARGIN_PX = 36f
    internal const val BADGE_TEXT_SIZE_PX = 26f
    internal const val TITLE_TEXT_SIZE_PX = 58f
    internal const val BODY_TEXT_SIZE_PX = 36f
    private const val PAD = CONTENT_SAFE_INSET_PX.toFloat()
    private const val BADGE = "YOURREFRI · GỢI Ý BẾP NHÀ"

    internal data class DishBleedLayout(val diameter: Float, val centerX: Float, val centerY: Float, val textWidth: Int)

    fun accentColorArgb(): Int = LightAppColors.notifyHeroStart.toArgb()

    internal fun computeDishBleedLayout(hasPicture: Boolean): DishBleedLayout {
        val centerX = WIDTH - DISH_RIGHT_MARGIN_PX - DISH_DIAMETER_PX / 2f
        val centerY = HEIGHT / 2f
        val textRight = if (hasPicture) {
            centerX - DISH_DIAMETER_PX / 2f - 32f
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
        val background = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(),
                accentColorArgb(), colors.notifyHeroEnd.toArgb(), Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRoundRect(
            0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), CARD_CORNER_RADIUS_PX, CARD_CORNER_RADIUS_PX, background,
        )
        val layout = computeDishBleedLayout(hasPicture = picture != null)
        if (picture != null) drawDish(canvas, picture, onHero, layout)

        val label = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = onHero
            textSize = BADGE_TEXT_SIZE_PX
            typeface = Typeface.DEFAULT_BOLD
        }

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = onHero
            textSize = TITLE_TEXT_SIZE_PX
            typeface = Typeface.DEFAULT_BOLD
        }
        val titleLayout = createTextLayout(title, titlePaint, layout.textWidth, maxLines = 2)

        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF7EBE6.toInt()
            textSize = BODY_TEXT_SIZE_PX
        }
        val bodyLayout = createTextLayout(body, bodyPaint, layout.textWidth, maxLines = 2)

        val badgeHeight = 48f
        val badgeRadius = badgeHeight / 2f
        val badgeToTitleGap = 22f
        val titleToBodyGap = 16f
        val totalHeight = badgeHeight + badgeToTitleGap + titleLayout.height + titleToBodyGap + bodyLayout.height
        val top = ((HEIGHT - totalHeight) / 2f).coerceAtLeast(32f)

        val badgeBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.notifyHeroBadgeBg.toArgb() }
        val badgeCenterY = top + badgeRadius
        canvas.drawRoundRect(
            PAD, top, PAD + label.measureText(BADGE) + 36f, top + badgeHeight,
            badgeRadius, badgeRadius, badgeBackground,
        )
        canvas.drawText(BADGE, PAD + 18f, badgeCenterY - (label.ascent() + label.descent()) / 2f, label)

        val titleTop = top + badgeHeight + badgeToTitleGap
        drawLayout(canvas, titleLayout, titleTop)
        drawLayout(canvas, bodyLayout, titleTop + titleLayout.height + titleToBodyGap)

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
