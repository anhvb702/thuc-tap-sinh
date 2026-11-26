package com.loitu.yourrefri.telemetry

import android.content.Context
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

internal fun toAnalyticsBundlePairs(payload: TelemetryPayload): List<Pair<String, String>> =
    payload.params.entries.sortedBy { it.key }.map { it.key to it.value }

fun createTelemetryPort(context: Context): TelemetryPort {
    val appContext = context.applicationContext
    return GuardedTelemetryPort(
        isBackendReady = { runCatching { FirebaseApp.getApps(appContext).isNotEmpty() }.getOrDefault(false) },
        logAnalytics = { payload ->
            val bundle = Bundle().apply {
                toAnalyticsBundlePairs(payload).forEach { (key, value) -> putString(key, value) }
            }
            FirebaseAnalytics.getInstance(appContext).logEvent(payload.name, bundle)
        },
        recordError = { tag, sanitized ->
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey("error_tag", tag)
                recordException(sanitized)
            }
        },
        applyCollectionEnabled = { enabled ->
            FirebaseAnalytics.getInstance(appContext).setAnalyticsCollectionEnabled(enabled)
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled)
        },
    )
}
