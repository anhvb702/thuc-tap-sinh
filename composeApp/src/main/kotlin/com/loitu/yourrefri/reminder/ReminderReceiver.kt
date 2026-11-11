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
                if (notice != null && (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)) {
                    val manager = context.getSystemService(NotificationManager::class.java)
                    if (Build.VERSION.SDK_INT >= 26) {
                        manager.createNotificationChannel(NotificationChannel("daily_reminder", "Nhắc nhở YourRefri", NotificationManager.IMPORTANCE_DEFAULT))
                    }
                    if (prefs.edit().putString("last_sent", today.toString()).commit()) {
                        val openApp = PendingIntent.getActivity(
                            context,
                            1730,
                            Intent(context, MainActivity::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                        )
                        val builder = notificationBuilder(context)
                        manager.notify(1730, builder.setSmallIcon(R.drawable.ic_notification_fridge)
                            .setContentTitle(notice.title)
                            .setContentText(notice.body.lineSequence().take(2).joinToString(" · "))
                            .setStyle(android.app.Notification.BigTextStyle().bigText(notice.body))
                            .setContentIntent(openApp)
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
    if (Build.VERSION.SDK_INT >= 26) android.app.Notification.Builder(context, "daily_reminder")
    else android.app.Notification.Builder(context)

class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) { ReminderScheduler.schedule(context) }
}
