package com.loitu.yourrefri.reminder

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils

/**
 * Vẽ cả thẻ nhắc (nền terracotta, chữ, ảnh món tròn) thành một ảnh để hiện bằng BigPictureStyle.
 * Cách này lấp đầy khung thông báo; layout tự vẽ (RemoteViews) luôn bị khung hệ thống bọc quanh.
 */
object ReminderCardRenderer {
    const val WIDTH = 960
    const val HEIGHT = 440
    private const val PAD = 48f
    private const val PICTURE_SIZE = 300

    private val background = Color.parseColor("#D98C70")
    private val text = Color.parseColor("#343B34")

    fun render(title: String, body: String, picture: Bitmap?): Bitmap {
        val out = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        // Nền phủ kín toàn ảnh, không bo góc: màu tràn tới mép vùng ảnh của thông báo.
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), Paint(Paint.ANTI_ALIAS_FLAG).apply { color = background })

        // Chừa chỗ cho ảnh món ở góc dưới phải, chữ không chạy sang đó.
        val reserved: Float = if (picture != null) PICTURE_SIZE + 24f else 0f
        val textWidth = (WIDTH - 2 * PAD - reserved).toInt()

        val label = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = text; textSize = 34f }
        canvas.drawText("YourRefri", PAD, PAD + 34f, label)

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = text; textSize = 58f; typeface = Typeface.DEFAULT_BOLD }
        val titleLayout = layout(title, titlePaint, textWidth, maxLines = 2)
        canvas.save()
        canvas.translate(PAD, PAD + 70f)
        titleLayout.draw(canvas)
        canvas.restore()

        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = text; textSize = 38f }
        val bodyTop = PAD + 70f + titleLayout.height + 16f
        val bodyLayout = layout(body, bodyPaint, textWidth, maxLines = 2)
        canvas.save()
        canvas.translate(PAD, bodyTop)
        bodyLayout.draw(canvas)
        canvas.restore()

        if (picture != null) {
            val dish = circle(picture, PICTURE_SIZE)
            canvas.drawBitmap(dish, (WIDTH - PAD - PICTURE_SIZE).toFloat(), (HEIGHT - PAD - PICTURE_SIZE).toFloat(), null)
        }
        return out
    }

    private fun layout(content: String, paint: TextPaint, width: Int, maxLines: Int): StaticLayout =
        StaticLayout.Builder.obtain(content, 0, content.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()

    /** Cắt ảnh vuông ở giữa rồi làm tròn. */
    private fun circle(src: Bitmap, size: Int): Bitmap {
        val side = minOf(src.width, src.height)
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val from = Rect((src.width - side) / 2, (src.height - side) / 2, (src.width + side) / 2, (src.height + side) / 2)
        canvas.drawBitmap(src, from, Rect(0, 0, size, size), paint)
        return out
    }
}
