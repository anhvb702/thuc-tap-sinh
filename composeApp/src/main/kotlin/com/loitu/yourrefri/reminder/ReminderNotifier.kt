package com.loitu.yourrefri.reminder

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Build
import android.widget.RemoteViews
import com.loitu.yourrefri.MainActivity
import com.loitu.yourrefri.R

/** Dựng và hiện popup nhắc ăn uống kiểu thẻ (xem docs/tests/NOTIFY-01.md). */
object ReminderNotifier {
    const val NOTIFICATION_ID = 1730
    private const val PICTURE_PX = 216 // 72dp ở mật độ 3x

    /** Bấm popup mở thẳng màn nấu rảnh tay của công thức [recipeId]. */
    @Suppress("DEPRECATION")
    fun post(context: Context, title: String, body: String, recipeId: String, picture: Bitmap?) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val open = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(ReminderIntents.EXTRA_RECIPE_ID, recipeId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val views = RemoteViews(context.packageName, R.layout.notification_reminder).apply {
            setTextViewText(R.id.reminder_title, title)
            setTextViewText(R.id.reminder_body, body)
            if (picture != null) setImageViewBitmap(R.id.reminder_picture, circle(picture, PICTURE_PX))
        }
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, ReminderChannel.ID)
        else Notification.Builder(context)
        builder.setSmallIcon(R.drawable.ic_notification_fridge)
            .setContentTitle(title)
            .setContentText(body)
            .setCustomContentView(views)
            .setStyle(Notification.DecoratedCustomViewStyle())
            .setCategory(Notification.CATEGORY_REMINDER)
            .setPriority(Notification.PRIORITY_HIGH)
            .setContentIntent(open)
            .setAutoCancel(true)
        if (Build.VERSION.SDK_INT < 26) builder.setDefaults(Notification.DEFAULT_VIBRATE)
        manager.notify(NOTIFICATION_ID, builder.build())
    }

    /** Đọc ảnh món đóng gói trong assets; lỗi đọc thì null và popup vẫn hiện không ảnh. */
    fun loadPicture(context: Context, assetPath: String): Bitmap? = runCatching {
        context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }
    }.getOrNull()

    /** Cắt ảnh vuông ở giữa rồi làm tròn, để đặt ở góc dưới phải như thẻ mẫu. */
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
