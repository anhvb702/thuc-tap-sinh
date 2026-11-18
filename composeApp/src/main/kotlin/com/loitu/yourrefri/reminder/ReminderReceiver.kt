package com.loitu.yourrefri.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.app.PendingIntent
import android.os.Build
import com.loitu.yourrefri.MainActivity
import com.loitu.yourrefri.R
import com.loitu.yourrefri.feature.notice.logic.planDailyNotice
import com.loitu.yourrefri.feature.pantry.data.PantryRepositoryImpl
import com.loitu.yourrefri.feature.pantry.data.ShelfTable
import com.loitu.yourrefri.shared.db.DriverFactory
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ReminderReceiver : BroadcastReceiver() {
    @OptIn(ExperimentalTime::class)
    override fun onReceive(context: Context, intent: Intent?) {
        if (!ReminderScheduler.enabled(context)) return
        val pending = goAsync()
        Thread {
            var driver: app.cash.sqldelight.db.SqlDriver? = null
            try {
                val prefs = context.getSharedPreferences("daily_reminder", 0)
                val tz = TimeZone.currentSystemDefault()
                val today = Clock.System.now().toLocalDateTime(tz).date
                val openedDriver = DriverFactory(context.applicationContext).create()
                driver = openedDriver
                val db = createDatabase(openedDriver)
                val repository = PantryRepositoryImpl(db, Clock.System, tz, Dispatchers.IO)
                val base = ShelfTable.parse(context.assets.open("shelf_table.json").bufferedReader().use { it.readText() })
                val overrides = runBlocking { repository.observeShelfOverrides().first() }
                val notice = planDailyNotice(
                    runBlocking { repository.observe().first() }, base.map { it.copy(userShelfDays = overrides[it.id]) },
                    today, prefs.getString("last_sent", null)?.let(kotlinx.datetime.LocalDate::parse),
                )
                val manager = context.getSystemService(NotificationManager::class.java)
                val legacy = if (Build.VERSION.SDK_INT >= 26) manager.getNotificationChannel(ReminderChannel.LEGACY_ID) else null
                val legacyImportance = legacy?.importance
                if (notice != null && ReminderChannel.shouldNotify(legacyImportance) && (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        // Tạo kênh mới trước, xóa kênh cũ sau: nếu bị gián đoạn giữa chừng thì kênh cũ vẫn còn và lần sau cho kết quả giống hệt.
                        val channel = NotificationChannel(ReminderChannel.ID, ReminderChannel.NAME, ReminderChannel.importanceFor(legacyImportance))
                        if (legacy != null) {
                            // Giữ âm thanh và rung người dùng đã chỉnh; legacy.sound == null nghĩa là đã chọn "Không có âm".
                            channel.setSound(legacy.sound, legacy.audioAttributes)
                            channel.enableVibration(legacy.shouldVibrate())
                        }
                        manager.createNotificationChannel(channel)
                        manager.deleteNotificationChannel(ReminderChannel.LEGACY_ID)
                    }
                    if (prefs.edit().putString("last_sent", today.toString()).commit()) {
                        val openApp = PendingIntent.getActivity(
                            context,
                            1730,
                            Intent(context, MainActivity::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                        )
                        val picture = runCatching {
                            context.assets.open(ReminderPicture.assetFor(today.dayOfYear)).use { android.graphics.BitmapFactory.decodeStream(it) }
                        }.getOrNull()
                        val builder = notificationBuilder(context)
                        manager.notify(1730, builder.setSmallIcon(R.drawable.ic_notification_fridge)
                            .setContentTitle(notice.title)
                            .setContentText(notice.body.lineSequence().take(2).joinToString(" · "))
                            .setStyle(picture?.let { android.app.Notification.BigPictureStyle().bigPicture(it).setSummaryText(notice.body) }
                                ?: android.app.Notification.BigTextStyle().bigText(notice.body))
                            .setContentIntent(openApp)
                            .setCategory(android.app.Notification.CATEGORY_REMINDER)
                            .setPriority(android.app.Notification.PRIORITY_HIGH)
                            .apply { if (Build.VERSION.SDK_INT < 26) setDefaults(android.app.Notification.DEFAULT_VIBRATE) }
                            .setAutoCancel(true).build())
                    }
                }
            } catch (_: Exception) {
                // A later scheduled reminder can retry if local data is temporarily unavailable.
            } finally {
                completeReminderWork(
                    closeDriver = { driver?.close() },
                    scheduleNext = { ReminderScheduler.schedule(context) },
                    finish = { pending.finish() },
                )
            }
        }.start()
    }
}

@Suppress("DEPRECATION")
private fun notificationBuilder(context: Context): android.app.Notification.Builder =
    if (Build.VERSION.SDK_INT >= 26) android.app.Notification.Builder(context, ReminderChannel.ID)
    else android.app.Notification.Builder(context)

class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) { ReminderScheduler.schedule(context) }
}
