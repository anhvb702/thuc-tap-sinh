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
import com.loitu.yourrefri.feature.notice.logic.planDefrostNotice
import com.loitu.yourrefri.feature.pantry.data.PantryRepositoryImpl
import com.loitu.yourrefri.feature.pantry.data.ShelfTable
import com.loitu.yourrefri.feature.recipe.data.RecipeBundle
import com.loitu.yourrefri.feature.recipe.data.CustomRecipeRepositoryImpl
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanRepositoryImpl
import com.loitu.yourrefri.feature.settings.ui.DietPrefs
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
                val kind = intent?.getStringExtra(ReminderScheduler.EXTRA_KIND) ?: ReminderScheduler.KIND_DAILY
                val base = ShelfTable.parse(context.assets.open("shelf_table.json").bufferedReader().use { it.readText() })
                val overrides = runBlocking { repository.observeShelfOverrides().first() }
                val pantry = runBlocking { repository.observe().first() }
                val ingredients = base.map { it.copy(userShelfDays = overrides[it.id]) }
                val isDefrost = kind == ReminderScheduler.KIND_DEFROST
                var isContextualNudge = false
                val noticeContent: Pair<String, String>? = if (isDefrost) {
                    val recipesJson = context.assets.open("recipes.json").bufferedReader().use { it.readText() }
                    val weeklyPlan = runBlocking { WeeklyMealPlanRepositoryImpl(db, Dispatchers.IO).observeAll().first() }
                    val customRecipes = runBlocking { CustomRecipeRepositoryImpl(db, Dispatchers.IO).observeAll().first() }
                    planDefrostNotice(today, weeklyPlan, RecipeBundle.parse(recipesJson) + customRecipes, ingredients, pantry)
                        ?.let { it.title to it.body }
                } else {
                    val recipesJson = context.assets.open("recipes.json").bufferedReader().use { it.readText() }
                    val weeklyPlanRecipes = RecipeBundle.parse(recipesJson)
                    val customRecipes = runBlocking { CustomRecipeRepositoryImpl(db, Dispatchers.IO).observeAll().first() }
                    val notice = planDailyNotice(
                        pantry, ingredients, today,
                        prefs.getString("last_sent", null)?.let(kotlinx.datetime.LocalDate::parse),
                        recipes = weeklyPlanRecipes + customRecipes,
                        dietRestriction = DietPrefs.read(context),
                        lastNudgeSent = prefs.getString("last_nudge_sent", null)?.let(kotlinx.datetime.LocalDate::parse),
                    )
                    isContextualNudge = notice?.isContextualNudge == true
                    notice?.let { it.title to it.body }
                }
                val lastSentKey = if (isDefrost) "defrost_last_sent" else "last_sent"
                val notificationId = if (isDefrost) 800 else 1730
                if (noticeContent != null && prefs.getString(lastSentKey, null) != today.toString() &&
                    (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)) {
                    val (noticeTitle, noticeBody) = noticeContent
                    val manager = context.getSystemService(NotificationManager::class.java)
                    if (Build.VERSION.SDK_INT >= 26) {
                        manager.createNotificationChannel(NotificationChannel("daily_reminder", "Nhắc nhở YourRefri", NotificationManager.IMPORTANCE_DEFAULT))
                    }
                    if (prefs.edit().putString(lastSentKey, today.toString()).commit()) {
                        if (isContextualNudge) prefs.edit().putString("last_nudge_sent", today.toString()).commit()
                        val openApp = PendingIntent.getActivity(
                            context,
                            notificationId,
                            Intent(context, MainActivity::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                        )
                        val builder = notificationBuilder(context)
                        manager.notify(notificationId, builder.setSmallIcon(R.drawable.ic_notification_fridge)
                            .setContentTitle(noticeTitle)
                            .setContentText(noticeBody.lineSequence().take(2).joinToString(" · "))
                            .setStyle(android.app.Notification.BigTextStyle().bigText(noticeBody))
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
