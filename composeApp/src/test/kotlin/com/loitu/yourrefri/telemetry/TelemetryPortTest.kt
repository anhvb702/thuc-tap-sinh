package com.loitu.yourrefri.telemetry

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TelemetryPortTest {
    @Test
    fun sanitizeTelemetryParam_normalizesSpecialCharsAndTruncatesTo40() {
        assertEquals("home_fridge_2", sanitizeTelemetryParam("__HOME!!! Fridge__2--"))
        assertEquals("abcdefghijklmnopqrstuvwxyz0123456789abcd", sanitizeTelemetryParam("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ABCDE"))
        assertEquals("home", sanitizeTelemetryParam("HomeScreen", maxLength = 4))
    }

    @Test
    fun sanitizeTelemetryParam_blankFallsBackToUnknown() {
        assertEquals("unknown", sanitizeTelemetryParam(" \t\n "))
        assertEquals("unknown", sanitizeTelemetryParam("?!___"))
    }

    @Test
    fun guardedTelemetryPort_dropsEventsWhenBackendNotReady() {
        val backend = RecordingBackend(ready = false)

        backend.port.track(TelemetryEvent.ScreenViewed("Home"))
        backend.port.track(TelemetryEvent.NonFatalError("Storage", IllegalStateException()))
        backend.port.setCollectionEnabled(false)

        assertEquals(emptyList(), backend.analytics)
        assertEquals(emptyList(), backend.errors)
        assertEquals(emptyList(), backend.collectionChanges)
    }

    @Test
    fun guardedTelemetryPort_dropsEventsWhenCollectionDisabled() {
        val backend = RecordingBackend()

        backend.port.setCollectionEnabled(false)
        backend.port.track(TelemetryEvent.ScreenViewed("Home"))
        backend.port.track(TelemetryEvent.NonFatalError("Storage", IllegalStateException()))

        assertEquals(emptyList(), backend.analytics)
        assertEquals(emptyList(), backend.errors)
        assertEquals(listOf(false), backend.collectionChanges)

        backend.port.setCollectionEnabled(true)
        backend.port.track(TelemetryEvent.ScreenViewed("Home"))

        assertEquals(listOf(false, true), backend.collectionChanges)
        assertEquals(listOf(TelemetryPayload("screen_view", mapOf("screen_name" to "home"))), backend.analytics)
    }

    @Test
    fun guardedTelemetryPort_routesAnalyticsAndNonFatalErrorSeparately() {
        val backend = RecordingBackend()
        val failure = IllegalStateException("Write failed")

        backend.port.track(TelemetryEvent.ScreenViewed("Meal Planner"))
        backend.port.track(TelemetryEvent.FeatureUsed("Quick Add", "From Fridge"))
        backend.port.track(TelemetryEvent.NonFatalError("Storage / Write", failure))

        assertEquals(
            listOf(
                TelemetryPayload("screen_view", mapOf("screen_name" to "meal_planner")),
                TelemetryPayload("feature_used", mapOf("feature" to "quick_add", "detail" to "from_fridge")),
            ),
            backend.analytics,
        )
        assertEquals(listOf("storage_write" to "[storage_write] IllegalStateException"), backend.errors.map { it.first to it.second.message })
    }

    @Test
    fun toTelemetryPayload_omitsBlankDetailAndReturnsNullForError() {
        val expected = TelemetryPayload("feature_used", mapOf("feature" to "quick_add"))
        assertEquals(expected, toTelemetryPayload(TelemetryEvent.FeatureUsed("Quick Add", " \t")))
        assertEquals(expected, toTelemetryPayload(TelemetryEvent.FeatureUsed("Quick Add")))
        assertNull(toTelemetryPayload(TelemetryEvent.NonFatalError("Storage", IllegalStateException())))
    }

    @Test
    fun guardedTelemetryPort_containsBackendFailures() {
        val port = GuardedTelemetryPort(
            isBackendReady = { true },
            logAnalytics = { error("Analytics unavailable") },
            recordError = { _, _ -> error("Crash reporting unavailable") },
            applyCollectionEnabled = { error("Collection setting unavailable") },
        )

        port.track(TelemetryEvent.ScreenViewed("Home"))
        port.track(TelemetryEvent.NonFatalError("Storage", IllegalStateException()))
        port.setCollectionEnabled(false)
    }

    @Test
    fun sanitizeNonFatalThrowable_stripsOriginalMessageAndPreservesStackTrace() {
        val original = IllegalStateException("Family recipe", IllegalArgumentException("Fridge contents"))
        val sanitized = sanitizeNonFatalThrowable("Storage / Write", original)
        assertEquals("[storage_write] IllegalStateException", sanitized.message)
        assertContentEquals(original.stackTrace, sanitized.stackTrace)
        assertNull(sanitized.cause)
    }

    @Test
    fun noOpTelemetryPort_ignoresTrackAndToggleSafely() {
        NoOpTelemetryPort.setCollectionEnabled(false)
        NoOpTelemetryPort.track(TelemetryEvent.ScreenViewed("Home"))
    }

    @Test
    fun guardedTelemetryPort_remembersDisabledFlagBeforeBackendReady() {
        val backend = RecordingBackend(ready = false)
        backend.port.setCollectionEnabled(false)
        backend.ready = true
        backend.port.track(TelemetryEvent.ScreenViewed("Home"))
        assertEquals(emptyList(), backend.analytics)
    }

    private class RecordingBackend(var ready: Boolean = true) {
        val analytics = mutableListOf<TelemetryPayload>()
        val errors = mutableListOf<Pair<String, Throwable>>()
        val collectionChanges = mutableListOf<Boolean>()

        val port = GuardedTelemetryPort(
            isBackendReady = { ready },
            logAnalytics = { analytics.add(it) },
            recordError = { tag, throwable -> errors.add(tag to throwable) },
            applyCollectionEnabled = { collectionChanges.add(it) },
        )
    }
}
