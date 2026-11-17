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
import com.loitu.yourrefri.feature.notice.logic.DailyNotice
import com.loitu.yourrefri.feature.notice.logic.defrostNamesForDay
import com.loitu.yourrefri.feature.notice.logic.planDailyNotice
import com.loitu.yourrefri.feature.pantry.data.LeftoverRepositoryImpl
import com.loitu.yourrefri.feature.pantry.data.PantryRepositoryImpl
import com.loitu.yourrefri.feature.pantry.data.ShelfTable
import com.loitu.yourrefri.feature.recipe.data.CustomRecipeRepositoryImpl
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.RecipeBundle
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanRepositoryImpl
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.filterRecipesByDiet
import com.loitu.yourrefri.feature.settings.ui.DietPrefs
import com.loitu.yourrefri.shared.db.DriverFactory
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ReminderReceiver : BroadcastReceiver() {
    @OptIn(ExperimentalTime::class)
    override fun onReceive(context: Context, intent: Intent?) {
        val forceTest = intent?.getBooleanExtra(ReminderScheduler.EXTRA_FORCE_TEST, false) == true
        if (!forceTest && !ReminderScheduler.enabled(context)) return
        val pending = goAsync()
        Thread {
            var driver: app.cash.sqldelight.db.SqlDriver? = null
            var hasFrozenDinnerForDay: ((LocalDate) -> Boolean)? = null
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
                val pantry = runBlocking { repository.observe().first() }
                val ingredients = base.map { it.copy(userShelfDays = overrides[it.id]) }
                val recipesJson = context.assets.open("recipes.json").bufferedReader().use { it.readText() }
                val customRecipes = runBlocking { CustomRecipeRepositoryImpl(db, Dispatchers.IO).observeAll().first() }
                val allRecipes = RecipeBundle.parse(recipesJson) + customRecipes
                val weeklyPlan = runBlocking { WeeklyMealPlanRepositoryImpl(db, Dispatchers.IO).observeAll().first() }
                val leftovers = runBlocking { LeftoverRepositoryImpl(db, Dispatchers.IO).observeAll().first() }
                val defrostNames = defrostNamesForDay(today, weeklyPlan, allRecipes, ingredients, pantry, leftovers)
                hasFrozenDinnerForDay = { date ->
                    defrostNamesForDay(date, weeklyPlan, allRecipes, ingredients, pantry, leftovers).isNotEmpty()
                }
                val notice = createReminderNotice(
                    pantry = pantry,
                    ingredients = ingredients,
                    today = today,
                    recipes = allRecipes,
                    dietRestriction = DietPrefs.read(context),
                    leftovers = leftovers,
                    defrostNames = defrostNames,
                    lastSent = if (forceTest) null else prefs.getString("last_sent", null)?.let(LocalDate::parse),
                    lastNudgeSent = if (forceTest) null else prefs.getString("last_nudge_sent", null)?.let(LocalDate::parse),
                    forceTest = forceTest,
                )
                val isContextualNudge = notice?.isContextualNudge == true
                val lastSentKey = "last_sent"
                val notificationId = 1730
                if (notice != null && (forceTest || prefs.getString(lastSentKey, null) != today.toString()) &&
                    (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)) {
                    val manager = context.getSystemService(NotificationManager::class.java)
                    if (Build.VERSION.SDK_INT >= 26) {
                        manager.createNotificationChannel(NotificationChannel("daily_reminder", "Nhắc nhở YourRefri", NotificationManager.IMPORTANCE_DEFAULT))
                    }
                    val shouldNotify = forceTest || prefs.edit().putString(lastSentKey, today.toString()).commit()
                    if (shouldNotify) {
                        if (!forceTest && isContextualNudge) prefs.edit().putString("last_nudge_sent", today.toString()).commit()
                        val openApp = PendingIntent.getActivity(
                            context,
                            notificationId,
                            Intent(context, MainActivity::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                        )
                        val builder = notificationBuilder(context)
                        manager.notify(notificationId, builder.setSmallIcon(R.drawable.ic_notification_fridge)
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
                    scheduleNext = { ReminderScheduler.schedule(context, hasFrozenDinnerForDay) },
                    finish = { pending.finish() },
                )
            }
        }.start()
    }
}

internal fun createReminderNotice(
    pantry: List<com.loitu.yourrefri.feature.pantry.data.PantryItem>,
    ingredients: List<com.loitu.yourrefri.feature.pantry.data.Ingredient>,
    today: LocalDate,
    recipes: List<Recipe>,
    dietRestriction: DietRestriction,
    leftovers: List<com.loitu.yourrefri.feature.pantry.data.LeftoverItem>,
    defrostNames: List<String>,
    lastSent: LocalDate?,
    lastNudgeSent: LocalDate?,
    forceTest: Boolean,
): DailyNotice? {
    val notice = planDailyNotice(
        pantry = pantry,
        ingredients = ingredients,
        today = today,
        lastSent = if (forceTest) null else lastSent,
        recipes = recipes,
        dietRestriction = dietRestriction,
        lastNudgeSent = if (forceTest) null else lastNudgeSent,
        leftovers = leftovers,
        defrostNames = defrostNames,
    )
    if (!forceTest) return notice
    return notice ?: forceTestDishSuggestion(recipes, ingredients, dietRestriction, today)
}

internal fun forceTestDishSuggestion(
    recipes: List<Recipe>,
    ingredients: List<com.loitu.yourrefri.feature.pantry.data.Ingredient>,
    dietRestriction: DietRestriction,
    today: LocalDate,
): DailyNotice? {
    val dish = filterRecipesByDiet(recipes, dietRestriction, ingredients).firstOrNull() ?: return null
    return DailyNotice(
        day = today,
        ingredientIds = emptyList(),
        title = "Gợi ý món ngon hôm nay",
        body = "Nay trời dịu nhẹ, tối nay làm món ${dish.title} là hết ý luôn nè!",
        isContextualNudge = true,
    )
}

@Suppress("DEPRECATION")
private fun notificationBuilder(context: Context): android.app.Notification.Builder =
    if (Build.VERSION.SDK_INT >= 26) android.app.Notification.Builder(context, "daily_reminder")
    else android.app.Notification.Builder(context)

class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) { ReminderScheduler.schedule(context) }
}
