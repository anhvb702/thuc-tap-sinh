package com.loitu.yourrefri.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.app.PendingIntent
import android.app.Notification
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.loitu.yourrefri.MainActivity
import com.loitu.yourrefri.R
import com.loitu.yourrefri.feature.notice.logic.DailyNotice
import com.loitu.yourrefri.feature.notice.logic.planDailyNotice
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.MealSlotKey
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.filterRecipesByDiet
import com.loitu.yourrefri.feature.settings.ui.DietPrefs
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
            var hasFrozenDinnerForDay: ((LocalDate) -> Boolean)? = null
            try {
                val prefs = context.getSharedPreferences("daily_reminder", 0)
                val tz = TimeZone.currentSystemDefault()
                val today = Clock.System.now().toLocalDateTime(tz).date
                val snapshot = loadReminderSnapshot(context.applicationContext)
                val defrostNames = snapshot.defrostNamesFor(today)
                hasFrozenDinnerForDay = snapshot::hasFrozenDinnerFor
                val notice = createReminderNotice(
                    pantry = snapshot.pantry,
                    ingredients = snapshot.ingredients,
                    today = today,
                    recipes = snapshot.recipes,
                    dietRestriction = DietPrefs.read(context),
                    leftovers = snapshot.leftovers,
                    defrostNames = defrostNames,
                    lastSent = if (forceTest) null else prefs.getString("last_sent", null)?.let(LocalDate::parse),
                    lastNudgeSent = if (forceTest) null else prefs.getString("last_nudge_sent", null)?.let(LocalDate::parse),
                    forceTest = forceTest,
                )
                val isContextualNudge = notice?.isContextualNudge == true
                val lastSentKey = "last_sent"
                val notificationId = 1730
                if (notice != null && (forceTest || prefs.getString(lastSentKey, null) != today.toString()) &&
                    NotificationManagerCompat.from(context).areNotificationsEnabled() &&
                    (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)) {
                    val manager = context.getSystemService(NotificationManager::class.java)
                    if (Build.VERSION.SDK_INT >= 26) {
                        manager.createNotificationChannel(NotificationChannel("daily_reminder", "Nhắc nhở YourRefri", NotificationManager.IMPORTANCE_DEFAULT))
                        manager.createNotificationChannel(NotificationChannel(ReminderNotificationChannel.ID, ReminderNotificationChannel.NAME, ReminderNotificationChannel.IMPORTANCE).apply {
                            setSound(null, null)
                        })
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
                        val dinnerRecipeId = snapshot.weeklyPlan.firstOrNull {
                            it.day == today && it.meal == MealSlot.DINNER && MealSlotKey(today, MealSlot.DINNER) !in snapshot.disabledMeals
                        }?.recipeId
                        val imageResourceId = dinnerRecipeId?.let { recipeId ->
                            context.resources.getIdentifier(dishImageResName(recipeId), "drawable", context.packageName).takeIf { it != 0 }
                        }
                        val notificationStyle = if (imageResourceId != null) {
                            val bitmap = BitmapFactory.decodeResource(context.resources, imageResourceId)
                            builder.setLargeIcon(bitmap)
                            Notification.BigPictureStyle().bigPicture(bitmap).setSummaryText(notice.body)
                        } else {
                            Notification.BigTextStyle().bigText(notice.body)
                        }
                        manager.notify(notificationId, builder.setSmallIcon(R.drawable.ic_notification_fridge)
                            .setContentTitle(notice.title)
                            .setContentText(notice.body.lineSequence().take(2).joinToString(" · "))
                            .setStyle(notificationStyle)
                            .setContentIntent(openApp)
                            .setAutoCancel(true).build())
                    }
                }
            } catch (_: Exception) {
                // A later scheduled reminder can retry if local data is temporarily unavailable.
            } finally {
                if (hasFrozenDinnerForDay == null) {
                    hasFrozenDinnerForDay = ReminderScheduler.loadFrozenDinnerPredicate(context.applicationContext)
                }
                completeReminderWork(
                    closeDriver = {},
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
    val dish = filterRecipesByDiet(recipes, dietRestriction, ingredients).firstOrNull()
    return DailyNotice(
        day = today,
        ingredientIds = emptyList(),
        title = "Gợi ý món ngon hôm nay",
        body = dish?.let { "Nay trời dịu nhẹ, tối nay làm món ${it.title} là hết ý luôn nè!" }
            ?: "Nay mình thử một món ngon đơn giản nhé?",
        isContextualNudge = true,
    )
}

@Suppress("DEPRECATION")
private fun notificationBuilder(context: Context): android.app.Notification.Builder =
    if (Build.VERSION.SDK_INT >= 26) android.app.Notification.Builder(context, ReminderNotificationChannel.ID)
    else android.app.Notification.Builder(context).setPriority(Notification.PRIORITY_HIGH).setSound(null)

internal object ReminderNotificationChannel {
    const val ID = "daily_reminder_v2"
    const val NAME = "Nhắc nhở YourRefri"
    const val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
}

class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pending = goAsync()
        ReminderScheduler.schedule(context, onDone = { pending.finish() })
    }
}
