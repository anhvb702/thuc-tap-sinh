package com.loitu.yourrefri.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.loitu.yourrefri.MainActivity
import com.loitu.yourrefri.R

/** Dựng và hiện popup nhắc ăn uống (xem docs/tests/NOTIFY-01.md). */
object ReminderNotifier {
    const val NOTIFICATION_ID = 1730

    /** Bấm popup mở thẳng màn nấu rảnh tay của công thức [recipeId]. */
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
        val content = buildReminderBigPictureContent(title, body)
        val banner = resolveBigPictureStyle {
            picture?.let { ReminderCardRenderer.render(content.hero, content.subtitle, it) }
        }
        val style = if (banner != null) {
            NotificationCompat.BigPictureStyle()
                .bigPicture(banner)
                .bigLargeIcon(null as Bitmap?)
                .also { pictureStyle ->
                    if (Build.VERSION.SDK_INT >= 31) pictureStyle.showBigPictureWhenCollapsed(true)
                }
        } else {
            NotificationCompat.BigTextStyle().bigText(content.contentText)
        }
        val builder = NotificationCompat.Builder(context, ReminderChannel.ID)
            .setSmallIcon(R.drawable.ic_notification_fridge)
            .setContentTitle(content.contentTitle)
            .setContentText(content.contentText)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(open)
            .addAction(0, content.actionLabel, open)
            .setAutoCancel(true)
            .setStyle(style)
        if (Build.VERSION.SDK_INT < 26) builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
        manager.notify(NOTIFICATION_ID, builder.build())
    }

    /** Đọc ảnh món đóng gói trong assets; lỗi đọc thì null và popup vẫn hiện không ảnh. */
    fun loadPicture(context: Context, assetPath: String): Bitmap? = runCatching {
        context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }
    }.getOrNull()
}
