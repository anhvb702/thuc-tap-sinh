package com.loitu.yourrefri.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.loitu.yourrefri.feature.notice.logic.defrostNamesForDay
import com.loitu.yourrefri.feature.notice.logic.nextReminderTrigger
import com.loitu.yourrefri.feature.pantry.data.LeftoverRepositoryImpl
import com.loitu.yourrefri.feature.pantry.data.PantryRepositoryImpl
import com.loitu.yourrefri.feature.pantry.data.ShelfTable
import com.loitu.yourrefri.feature.recipe.data.CustomRecipeRepositoryImpl
import com.loitu.yourrefri.feature.recipe.data.RecipeBundle
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanRepositoryImpl
import com.loitu.yourrefri.feature.settings.ui.coerceWeekdayTime
import com.loitu.yourrefri.feature.settings.ui.minutesOf
import com.loitu.yourrefri.feature.settings.ui.sundayTimeOf
import com.loitu.yourrefri.feature.settings.ui.weekdayTimeOf
import com.loitu.yourrefri.shared.db.DriverFactory
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object ReminderScheduler {
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
    ) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        // Hủy báo thức rã đông cũ (nếu còn từ bản trước) để luôn giữ đúng 1 thông báo/ngày (Q4).
        alarm.cancel(pending(context, LEGACY_DEFROST_REQUEST, LEGACY_KIND_DEFROST))
        if (!enabled(context)) {
            alarm.cancel(pending(context, REQUEST, KIND_DAILY))
            return
        }
        if (hasFrozenDinnerForDay != null) {
            scheduleSingleAlarm(context, alarm, hasFrozenDinnerForDay)
            return
        }
        val appContext = context.applicationContext
        Thread {
            val predicate = loadFrozenDinnerPredicate(appContext)
            scheduleSingleAlarm(appContext, alarm, predicate)
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

    @OptIn(ExperimentalTime::class)
    private fun loadFrozenDinnerPredicate(context: Context): (LocalDate) -> Boolean {
        var driver: app.cash.sqldelight.db.SqlDriver? = null
        return try {
            val tz = TimeZone.currentSystemDefault()
            val openedDriver = DriverFactory(context).create()
            driver = openedDriver
            val db = createDatabase(openedDriver)
            val pantryRepo = PantryRepositoryImpl(db, Clock.System, tz, Dispatchers.IO)
            val base = ShelfTable.parse(context.assets.open("shelf_table.json").bufferedReader().use { it.readText() })
            val overrides = runBlocking { pantryRepo.observeShelfOverrides().first() }
            val pantry = runBlocking { pantryRepo.observe().first() }
            val ingredients = base.map { it.copy(userShelfDays = overrides[it.id]) }
            val recipesJson = context.assets.open("recipes.json").bufferedReader().use { it.readText() }
            val customRecipes = runBlocking { CustomRecipeRepositoryImpl(db, Dispatchers.IO).observeAll().first() }
            val allRecipes = RecipeBundle.parse(recipesJson) + customRecipes
            val weeklyPlan = runBlocking { WeeklyMealPlanRepositoryImpl(db, Dispatchers.IO).observeAll().first() }
            val leftovers = runBlocking { LeftoverRepositoryImpl(db, Dispatchers.IO).observeAll().first() }
            val predicate: (LocalDate) -> Boolean = { date ->
                defrostNamesForDay(date, weeklyPlan, allRecipes, ingredients, pantry, leftovers).isNotEmpty()
            }
            predicate
        } catch (_: Exception) {
            { false }
        } finally {
            try {
                driver?.close()
            } catch (_: Exception) {
            }
        }
    }

    fun cancel(context: Context) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        alarm.cancel(pending(context, REQUEST, KIND_DAILY))
        alarm.cancel(pending(context, LEGACY_DEFROST_REQUEST, LEGACY_KIND_DEFROST))
    }
    private fun pending(context: Context, request: Int, kind: String) = PendingIntent.getBroadcast(
        context, request, Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_KIND, kind),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
