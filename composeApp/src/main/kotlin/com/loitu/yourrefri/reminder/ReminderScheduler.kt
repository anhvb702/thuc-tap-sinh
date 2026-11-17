package com.loitu.yourrefri.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.loitu.yourrefri.feature.notice.logic.nextReminderTrigger
import com.loitu.yourrefri.feature.notice.logic.nextDefrostTrigger
import com.loitu.yourrefri.feature.settings.ui.coerceWeekdayTime
import com.loitu.yourrefri.feature.settings.ui.minutesOf
import com.loitu.yourrefri.feature.settings.ui.sundayTimeOf
import com.loitu.yourrefri.feature.settings.ui.weekdayTimeOf
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object ReminderScheduler {
    private const val REQUEST = 1730
    private const val DEFROST_REQUEST = 800
    internal const val EXTRA_KIND = "reminder_kind"
    internal const val KIND_DAILY = "daily"
    internal const val KIND_DEFROST = "defrost"
    private const val PREFS = "daily_reminder"
    private const val SUNDAY_MINUTES_KEY = "sunday_minutes"
    private const val WEEKDAY_MINUTES_KEY = "weekday_minutes"
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

    fun weekdayTime(context: Context): LocalTime = weekdayTimeOf(
        context.getSharedPreferences(PREFS, 0).getInt(WEEKDAY_MINUTES_KEY, -1)
    )

    fun setWeekdayTime(context: Context, time: LocalTime) {
        // Căn cứ quyết định Q5 (2026-11-16): Giờ nhắc ngày thường kẹp trong khung 14:00–20:00.
        context.getSharedPreferences(PREFS, 0).edit().putInt(WEEKDAY_MINUTES_KEY, minutesOf(coerceWeekdayTime(time))).apply()
        schedule(context)
    }

    @OptIn(ExperimentalTime::class)
    fun schedule(context: Context) {
        val now = Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        val trigger = nextReminderTrigger(
            enabled = enabled(context),
            now = now,
            tz = tz,
            sundayTime = sundayTime(context),
            weekdayTime = weekdayTime(context),
        )
        val alarm = context.getSystemService(AlarmManager::class.java)
        if (trigger != null) {
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.toEpochMilliseconds(), pending(context, REQUEST, KIND_DAILY))
        }
        val defrost = nextDefrostTrigger(enabled(context), now, tz)
        if (defrost != null) {
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, defrost.toEpochMilliseconds(), pending(context, DEFROST_REQUEST, KIND_DEFROST))
        }
    }
    fun cancel(context: Context) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        alarm.cancel(pending(context, REQUEST, KIND_DAILY))
        alarm.cancel(pending(context, DEFROST_REQUEST, KIND_DEFROST))
    }
    private fun pending(context: Context, request: Int, kind: String) = PendingIntent.getBroadcast(
        context, request, Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_KIND, kind),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
