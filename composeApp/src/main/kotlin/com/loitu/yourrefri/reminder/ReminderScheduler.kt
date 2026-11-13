package com.loitu.yourrefri.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.loitu.yourrefri.feature.notice.logic.nextReminderTrigger
import com.loitu.yourrefri.feature.settings.ui.minutesOf
import com.loitu.yourrefri.feature.settings.ui.sundayTimeOf
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object ReminderScheduler {
    private const val REQUEST = 1730
    private const val PREFS = "daily_reminder"
    private const val SUNDAY_MINUTES_KEY = "sunday_minutes"
    fun enabled(context: Context) = context.getSharedPreferences(PREFS, 0).getBoolean("enabled", false)
    fun setEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, 0).edit().putBoolean("enabled", value).apply()
        if (value) schedule(context) else cancel(context)
    }

    /** Giờ nhắc đi chợ vào Chủ Nhật; mặc định 8:00. */
    fun sundayTime(context: Context): LocalTime =
        sundayTimeOf(context.getSharedPreferences(PREFS, 0).getInt(SUNDAY_MINUTES_KEY, -1))

    fun setSundayTime(context: Context, time: LocalTime) {
        context.getSharedPreferences(PREFS, 0).edit().putInt(SUNDAY_MINUTES_KEY, minutesOf(time)).apply()
        schedule(context)
    }

    @OptIn(ExperimentalTime::class)
    fun schedule(context: Context) {
        val trigger = nextReminderTrigger(
            enabled = enabled(context),
            now = Clock.System.now(),
            tz = TimeZone.currentSystemDefault(),
            sundayTime = sundayTime(context),
        ) ?: return
        val alarm = context.getSystemService(AlarmManager::class.java)
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.toEpochMilliseconds(), pending(context))
    }
    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(pending(context))
    }
    private fun pending(context: Context) = PendingIntent.getBroadcast(
        context, REQUEST, Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
