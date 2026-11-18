package com.loitu.yourrefri.reminder

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.loitu.yourrefri.MainActivity
import com.loitu.yourrefri.R
import android.os.Build

/** Dựng và hiện popup nhắc ăn uống (xem docs/tests/NOTIFY-01.md). */
object ReminderNotifier {
    const val NOTIFICATION_ID = 1730

    /**
     * Thẻ là một ảnh vẽ sẵn (ReminderCardRenderer) đặt bằng BigPictureStyle, nên lấp đầy khung thông báo.
     * Bấm popup mở thẳng màn nấu rảnh tay của công thức [recipeId].
     */
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
        val card = ReminderCardRenderer.render(title, body, picture)
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, ReminderChannel.ID)
        else Notification.Builder(context)
        builder.setSmallIcon(R.drawable.ic_notification_fridge)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(Notification.BigPictureStyle().bigPicture(card))
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
}
