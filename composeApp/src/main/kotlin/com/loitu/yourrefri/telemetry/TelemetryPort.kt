package com.loitu.yourrefri.telemetry

sealed interface TelemetryEvent {
    data class ScreenViewed(val screen: String) : TelemetryEvent
    data class FeatureUsed(val feature: String, val detail: String? = null) : TelemetryEvent
    data class NonFatalError(val tag: String, val throwable: Throwable) : TelemetryEvent
}

interface TelemetryPort {
    fun track(event: TelemetryEvent)
    fun setCollectionEnabled(enabled: Boolean)
}

object NoOpTelemetryPort : TelemetryPort {
    override fun track(event: TelemetryEvent) = Unit
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}

internal data class TelemetryPayload(val name: String, val params: Map<String, String>)

private val invalidTelemetryChars = Regex("[^a-z0-9_]")
private val repeatedUnderscores = Regex("_+")

internal fun sanitizeTelemetryParam(raw: String, maxLength: Int = 40): String =
    raw.lowercase()
        .replace(invalidTelemetryChars, "_")
        .replace(repeatedUnderscores, "_")
        .trim('_')
        .take(maxLength)
        .ifEmpty { "unknown" }

internal fun sanitizeNonFatalThrowable(tag: String, throwable: Throwable): Throwable =
    RuntimeException("[${sanitizeTelemetryParam(tag)}] ${throwable::class.simpleName ?: "Throwable"}")
        .apply { stackTrace = throwable.stackTrace }

internal fun toTelemetryPayload(event: TelemetryEvent): TelemetryPayload? = when (event) {
    is TelemetryEvent.ScreenViewed ->
        TelemetryPayload("screen_view", mapOf("screen_name" to sanitizeTelemetryParam(event.screen)))
    is TelemetryEvent.FeatureUsed ->
        TelemetryPayload("feature_used", buildMap {
            put("feature", sanitizeTelemetryParam(event.feature))
            event.detail?.takeIf { it.isNotBlank() }?.let { put("detail", sanitizeTelemetryParam(it)) }
        })
    is TelemetryEvent.NonFatalError -> null
}

internal class GuardedTelemetryPort(
    private val isBackendReady: () -> Boolean,
    private val logAnalytics: (TelemetryPayload) -> Unit,
    private val recordError: (String, Throwable) -> Unit,
    private val applyCollectionEnabled: (Boolean) -> Unit = {},
) : TelemetryPort {
    @Volatile private var collectionEnabled: Boolean = true

    override fun setCollectionEnabled(enabled: Boolean) {
        collectionEnabled = enabled
        if (isBackendReady()) {
            runCatching { applyCollectionEnabled(enabled) }
        }
    }

    override fun track(event: TelemetryEvent) {
        if (!collectionEnabled || !isBackendReady()) return

        if (event is TelemetryEvent.NonFatalError) {
            runCatching { recordError(sanitizeTelemetryParam(event.tag), sanitizeNonFatalThrowable(event.tag, event.throwable)) }
        } else {
            toTelemetryPayload(event)?.let { payload -> runCatching { logAnalytics(payload) } }
        }
    }
}
