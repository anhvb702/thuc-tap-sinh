package com.loitu.yourrefri.reminder

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.ui.graphics.toArgb
import com.loitu.yourrefri.ui.theme.LightAppColors

/**
 * Vẽ cả thẻ nhắc (nền terracotta, chữ, ảnh món tròn) thành một ảnh để hiện bằng BigPictureStyle.
 * Cách này lấp đầy khung thông báo; layout tự vẽ (RemoteViews) luôn bị khung hệ thống bọc quanh.
 */
object ReminderCardRenderer {
    const val WIDTH = 960
    const val HEIGHT = 440
    private const val PAD = 48f
    private const val PICTURE_SIZE = 300f
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
            textSize = 52f
            typeface = Typeface.DEFAULT_BOLD
        }
        val titleLayout = layout(title, titlePaint, textWidth, maxLines = 2)
        val titleTop = 100f
        drawLayout(canvas, titleLayout, titleTop)

        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF7EBE6.toInt()
            textSize = 34f
        }
        val bodyTop = titleTop + titleLayout.height + 12f
        drawLayout(canvas, layout(body, bodyPaint, textWidth, maxLines = 2), bodyTop)

        val hint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF0D7CC.toInt()
            textSize = 26f
        }
        canvas.drawText("Chạm để mở công thức →", PAD, HEIGHT - PAD, hint)
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
        val glowRadius = radius + 80f
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                centerX, centerY, glowRadius, 0x66FFC0A1, 0x00FFC0A1, Shader.TileMode.CLAMP,
            )
        }
        canvas.drawCircle(centerX, centerY, glowRadius, glow)

        val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            setShadowLayer(18f, 0f, 10f, 0x80421C15.toInt())
        }
        canvas.drawCircle(centerX, centerY, radius, frame)

        val scale = PICTURE_SIZE / minOf(picture.width, picture.height)
        val crop = Matrix().apply {
            setScale(scale, scale)
            postTranslate(centerX - picture.width * scale / 2f, centerY - picture.height * scale / 2f)
        }
        val photo = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            shader = BitmapShader(picture, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
                setLocalMatrix(crop)
            }
        }
        canvas.drawCircle(centerX, centerY, radius, photo)

        frame.clearShadowLayer()
        frame.style = Paint.Style.STROKE
        frame.strokeWidth = 8f
        canvas.drawCircle(centerX, centerY, radius, frame)
    }
}
