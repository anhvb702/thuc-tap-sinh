package com.loitu.yourrefri.reminder

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.loitu.yourrefri.MainActivity
import com.loitu.yourrefri.R

/** Dựng và hiện popup nhắc ăn uống (xem docs/tests/NOTIFY-01.md). */
object ReminderNotifier {
    const val NOTIFICATION_ID = 1730

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
        val card = buildReminderCardModel(title, body)
        val art = picture?.let { ReminderCardRenderer.renderDishArt(it) }
        val collapsed = cardViews(context, R.layout.notification_reminder_collapsed, card, art)
        val expanded = cardViews(context, R.layout.notification_reminder_expanded, card, art).apply {
            setTextViewText(R.id.notify_badge, card.badge)
        }
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, ReminderChannel.ID)
        else Notification.Builder(context)
        builder.setSmallIcon(R.drawable.ic_notification_fridge)
            .setCustomContentView(collapsed)
            .setCustomBigContentView(expanded)
            .setCustomHeadsUpContentView(collapsed)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setPriority(Notification.PRIORITY_HIGH)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setContentIntent(open)
            .addAction(Notification.Action.Builder(null, "Xem món & nấu ngay", open).build())
            .setAutoCancel(true)
        if (Build.VERSION.SDK_INT < 26) builder.setDefaults(Notification.DEFAULT_VIBRATE)
        manager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun cardViews(context: Context, layoutId: Int, card: ReminderCardModel, art: Bitmap?): RemoteViews =
        RemoteViews(context.packageName, layoutId).apply {
            setTextViewText(R.id.notify_hero, card.hero)
            setTextViewText(R.id.notify_sub, card.subtitle)
            if (art != null) setImageViewBitmap(R.id.notify_art, art)
            else setViewVisibility(R.id.notify_art, View.GONE)
        }

    /** Đọc ảnh món đóng gói trong assets; lỗi đọc thì null và popup vẫn hiện không ảnh. */
    fun loadPicture(context: Context, assetPath: String): Bitmap? = runCatching {
        context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }
    }.getOrNull()
}
