package com.loitu.yourrefri.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.loitu.yourrefri.feature.notice.logic.nextReminderTrigger
import com.loitu.yourrefri.feature.settings.ui.coerceWeekdayTime
import com.loitu.yourrefri.feature.settings.ui.minutesOf
import com.loitu.yourrefri.feature.settings.ui.sundayTimeOf
import com.loitu.yourrefri.feature.settings.ui.weekdayTimeOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object ReminderScheduler {
    private val scheduleLock = Any()
    private var generation = 0L
    private const val REQUEST = 1730
    private const val LEGACY_DEFROST_REQUEST = 800
    internal const val EXTRA_KIND = "reminder_kind"
    internal const val EXTRA_FORCE_TEST = "force_test"
    internal const val KIND_DAILY = "daily"
    private const val LEGACY_KIND_DEFROST = "defrost"
    private const val PREFS = "daily_reminder"
    private const val SUNDAY_MINUTES_KEY = "sunday_minutes"
    private const val WEEKDAY_MINUTES_KEY = "weekday_minutes"
    fun enabled(context: Context) = context.getSharedPreferences(PREFS, 0).getBoolean("enabled", false)

    fun triggerTestNow(context: Context) {
        context.sendBroadcast(Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_FORCE_TEST, true))
    }
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
    fun schedule(
        context: Context,
        hasFrozenDinnerForDay: ((LocalDate) -> Boolean)? = null,
        onDone: () -> Unit = {},
    ) {
        val requestGeneration = synchronized(scheduleLock) {
            generation += 1
            generation
        }
        val alarm = context.getSystemService(AlarmManager::class.java)
        val shouldLoadPredicate = synchronized(scheduleLock) {
            if (requestGeneration != generation) return@synchronized false
            // Hủy báo thức rã đông cũ (nếu còn từ bản trước) để luôn giữ đúng 1 thông báo/ngày (Q4).
            alarm.cancel(pending(context, LEGACY_DEFROST_REQUEST, LEGACY_KIND_DEFROST))
            if (!enabled(context)) {
                alarm.cancel(pending(context, REQUEST, KIND_DAILY))
                false
            } else if (hasFrozenDinnerForDay != null) {
                scheduleSingleAlarm(context, alarm, hasFrozenDinnerForDay)
                false
            } else {
                true
            }
        }
        if (!shouldLoadPredicate) {
            onDone()
            return
        }
        val appContext = context.applicationContext
        Thread {
            val predicate = loadFrozenDinnerPredicate(appContext)
            synchronized(scheduleLock) {
                if (requestGeneration == generation && enabled(appContext)) {
                    scheduleSingleAlarm(appContext, alarm, predicate)
                }
            }
            onDone()
        }.start()
    }

    @OptIn(ExperimentalTime::class)
    private fun scheduleSingleAlarm(
        context: Context,
        alarm: AlarmManager,
        hasFrozenDinnerForDay: (LocalDate) -> Boolean,
    ) {
        val trigger = nextReminderTrigger(
            enabled = enabled(context),
            now = Clock.System.now(),
            tz = TimeZone.currentSystemDefault(),
            sundayTime = sundayTime(context),
            weekdayTime = weekdayTime(context),
            hasFrozenDinnerForDay = hasFrozenDinnerForDay,
        )
        if (trigger != null) {
            alarm.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                trigger.toEpochMilliseconds(),
                pending(context, REQUEST, KIND_DAILY),
            )
        }
    }

    internal fun loadFrozenDinnerPredicate(context: Context): (LocalDate) -> Boolean = try {
        loadReminderSnapshot(context).let { snapshot -> snapshot::hasFrozenDinnerFor }
    } catch (_: Exception) {
        { false }
    }

    fun cancel(context: Context) {
        synchronized(scheduleLock) {
            generation += 1
            val alarm = context.getSystemService(AlarmManager::class.java)
            alarm.cancel(pending(context, REQUEST, KIND_DAILY))
            alarm.cancel(pending(context, LEGACY_DEFROST_REQUEST, LEGACY_KIND_DEFROST))
        }
    }
    private fun pending(context: Context, request: Int, kind: String) = PendingIntent.getBroadcast(
        context, request, Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_KIND, kind),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
