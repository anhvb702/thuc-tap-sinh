package com.loitu.yourrefri.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.loitu.yourrefri.feature.notice.logic.WeekendReminderDay
import com.loitu.yourrefri.feature.notice.logic.clampWeekdayReminderTime
import com.loitu.yourrefri.feature.notice.logic.nextReminderTrigger
import com.loitu.yourrefri.feature.settings.ui.minutesOf
import com.loitu.yourrefri.feature.settings.ui.sundayTimeOf
import com.loitu.yourrefri.feature.settings.ui.weekdayTimeOf
import com.loitu.yourrefri.feature.settings.ui.weekendDayOf
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object ReminderScheduler {
    private const val REQUEST = 1730
    private const val PREFS = "daily_reminder"
    private const val SUNDAY_MINUTES_KEY = "sunday_minutes"
    private const val WEEKDAY_MINUTES_KEY = "weekday_minutes"
    private const val WEEKEND_DAY_KEY = "weekend_day"
    private const val WEEKDAY_ENABLED_KEY = "weekday_enabled"
    fun enabled(context: Context) = context.getSharedPreferences(PREFS, 0).getBoolean("enabled", false)
    fun setEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, 0).edit().putBoolean("enabled", value).apply()
        if (value) schedule(context) else cancel(context)
    }

    /** Giờ nhắc đi chợ cuối tuần; giữ khóa lưu cũ và mặc định 8:00. */
    fun sundayTime(context: Context): LocalTime =
        sundayTimeOf(context.getSharedPreferences(PREFS, 0).getInt(SUNDAY_MINUTES_KEY, -1))

    fun setSundayTime(context: Context, time: LocalTime) {
        context.getSharedPreferences(PREFS, 0).edit().putInt(SUNDAY_MINUTES_KEY, minutesOf(time)).apply()
        schedule(context)
    }

    fun weekendDay(context: Context): WeekendReminderDay =
        weekendDayOf(context.getSharedPreferences(PREFS, 0).getString(WEEKEND_DAY_KEY, null))

    fun setWeekendDay(context: Context, day: WeekendReminderDay) {
        context.getSharedPreferences(PREFS, 0).edit().putString(WEEKEND_DAY_KEY, day.name).apply()
        schedule(context)
    }

    fun weekdayEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, 0).getBoolean(WEEKDAY_ENABLED_KEY, false)

    fun setWeekdayEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, 0).edit().putBoolean(WEEKDAY_ENABLED_KEY, value).apply()
        schedule(context)
    }

    fun weekdayTime(context: Context): LocalTime =
        weekdayTimeOf(context.getSharedPreferences(PREFS, 0).getInt(WEEKDAY_MINUTES_KEY, -1))

    fun setWeekdayTime(context: Context, time: LocalTime) {
        val clamped = clampWeekdayReminderTime(time)
        context.getSharedPreferences(PREFS, 0).edit().putInt(WEEKDAY_MINUTES_KEY, minutesOf(clamped)).apply()
        schedule(context)
    }

    @OptIn(ExperimentalTime::class)
    fun schedule(context: Context) {
        val trigger = nextReminderTrigger(
            enabled = enabled(context),
            now = Clock.System.now(),
            tz = TimeZone.currentSystemDefault(),
            sundayTime = sundayTime(context),
            weekdayTime = weekdayTime(context),
            weekendDay = weekendDay(context),
            weekdayEnabled = weekdayEnabled(context),
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
