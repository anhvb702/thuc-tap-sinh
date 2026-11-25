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
    private const val PAD = CONTENT_SAFE_INSET_PX.toFloat()
    private const val PICTURE_SIZE = 280f
    private const val BADGE = "YOURREFRI · GỢI Ý BẾP NHÀ"

    fun render(title: String, body: String, picture: Bitmap?): Bitmap {
        val out = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val colors = LightAppColors
        val onHero = colors.notifyHeroOn.toArgb()
        val background = Paint().apply {
            shader = LinearGradient(
                0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(),
                colors.notifyHeroStart.toArgb(), colors.notifyHeroEnd.toArgb(), Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), background)
        canvas.clipRect(PAD, 0f, WIDTH - PAD, HEIGHT.toFloat())
        if (picture != null) drawDish(canvas, picture, onHero)

        val reserved = if (picture != null) PICTURE_SIZE + 32f else 0f
        val textWidth = (WIDTH - 2 * PAD - reserved).toInt()

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
        val titleLayout = layout(title, titlePaint, textWidth, maxLines = 2)
        val titleTop = 100f
        drawLayout(canvas, titleLayout, titleTop)

        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF7EBE6.toInt()
            textSize = 30f
        }
        val bodyTop = titleTop + titleLayout.height + 12f
        drawLayout(canvas, layout(body, bodyPaint, textWidth, maxLines = 2), bodyTop)

        return out
    }

    private fun drawLayout(canvas: Canvas, layout: StaticLayout, top: Float) {
        canvas.save()
        canvas.translate(PAD, top)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun layout(content: String, paint: TextPaint, width: Int, maxLines: Int): StaticLayout =
        StaticLayout.Builder.obtain(content, 0, content.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()

    private fun drawDish(canvas: Canvas, picture: Bitmap, frameColor: Int) {
        val radius = PICTURE_SIZE / 2f
        val centerX = WIDTH - PAD - radius
        val centerY = HEIGHT / 2f
        canvas.drawCircle(centerX, centerY, radius, dishPaint(picture, PICTURE_SIZE, centerX, centerY))

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
