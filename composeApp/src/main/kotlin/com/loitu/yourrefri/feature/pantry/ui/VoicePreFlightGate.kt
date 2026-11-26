package com.loitu.yourrefri.feature.pantry.ui

import android.content.Context

internal const val MIN_RECORDING_MS = 700L
internal const val MIN_ACTIVE_SPEECH_MS = 400L
internal const val MIN_SPEECH_RMS = 0.12f
internal const val MIN_CLOUD_INTERVAL_MS = 3_000L
internal const val MAX_CONSECUTIVE_EMPTY_MATCHES = 3
internal const val SPAM_COOLDOWN_MS = 60_000L
internal const val MAX_CLOUD_CALLS_PER_DAY = 25

internal enum class VoiceGateDecision { ALLOW, TOO_SHORT, SILENT, RATE_LIMITED, SPAM_COOLDOWN, DAILY_QUOTA_REACHED }

internal data class AudioCaptureMetrics(val durationMs: Long, val activeSpeechMs: Long, val peakNormalizedRms: Float)

internal interface VoiceQuotaStore {
    fun readDate(): String?
    fun readDailyCount(): Int
    fun writeDaily(date: String, count: Int)
    fun readLastCallMs(): Long
    fun writeLastCallMs(ms: Long)
    fun readEmptyStreak(): Int
    fun writeEmptyStreak(count: Int)
    fun readCooldownUntilMs(): Long
    fun writeCooldownUntilMs(ms: Long)
}

internal class SharedPrefsVoiceQuotaStore(context: Context) : VoiceQuotaStore {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun readDate(): String? = prefs.getString(KEY_DATE, null)
    override fun readDailyCount(): Int = prefs.getInt(KEY_DAILY_COUNT, 0)
    override fun writeDaily(date: String, count: Int) {
        prefs.edit().putString(KEY_DATE, date).putInt(KEY_DAILY_COUNT, count).apply()
    }
    override fun readLastCallMs(): Long = prefs.getLong(KEY_LAST_CALL_MS, 0L)
    override fun writeLastCallMs(ms: Long) {
        prefs.edit().putLong(KEY_LAST_CALL_MS, ms).apply()
    }
    override fun readEmptyStreak(): Int = prefs.getInt(KEY_EMPTY_RUN_COUNT, 0)
    override fun writeEmptyStreak(count: Int) {
        prefs.edit().putInt(KEY_EMPTY_RUN_COUNT, count).apply()
    }
    override fun readCooldownUntilMs(): Long = prefs.getLong(KEY_COOLDOWN_UNTIL_MS, 0L)
    override fun writeCooldownUntilMs(ms: Long) {
        prefs.edit().putLong(KEY_COOLDOWN_UNTIL_MS, ms).apply()
    }

    private companion object {
        const val PREFS_NAME = "voice_quota"
        const val KEY_DATE = "date"
        const val KEY_DAILY_COUNT = "daily_count"
        const val KEY_LAST_CALL_MS = "last_call_ms"
        const val KEY_EMPTY_RUN_COUNT = "empty_run_count"
        const val KEY_COOLDOWN_UNTIL_MS = "cooldown_until_ms"
    }
}

internal class InMemoryVoiceQuotaStore : VoiceQuotaStore {
    private var date: String? = null
    private var dailyCount = 0
    private var lastCallMs = 0L
    private var emptyStreak = 0
    private var cooldownUntilMs = 0L

    override fun readDate(): String? = date
    override fun readDailyCount(): Int = dailyCount
    override fun writeDaily(date: String, count: Int) {
        this.date = date
        dailyCount = count
    }
    override fun readLastCallMs(): Long = lastCallMs
    override fun writeLastCallMs(ms: Long) {
        lastCallMs = ms
    }
    override fun readEmptyStreak(): Int = emptyStreak
    override fun writeEmptyStreak(count: Int) {
        emptyStreak = count
    }
    override fun readCooldownUntilMs(): Long = cooldownUntilMs
    override fun writeCooldownUntilMs(ms: Long) {
        cooldownUntilMs = ms
    }
}

/**
 * Chặn cuộc gọi đám mây khi không cần thiết: 3 tầng gồm VAD cục bộ (ghi âm quá ngắn/im lặng),
 * chống spam (cooldown sau nhiều lần không khớp món, giãn cách giữa hai lần gọi), và hạn mức theo ngày.
 */
internal class VoicePreFlightGate(
    private val store: VoiceQuotaStore,
    private val nowMs: () -> Long = { System.currentTimeMillis() },
    private val todayKey: () -> String,
) {
    fun checkBeforeCloud(metrics: AudioCaptureMetrics): VoiceGateDecision {
        // Tầng 1: VAD cục bộ, không tốn mạng.
        if (metrics.durationMs < MIN_RECORDING_MS) return VoiceGateDecision.TOO_SHORT
        if (metrics.peakNormalizedRms < MIN_SPEECH_RMS || metrics.activeSpeechMs < MIN_ACTIVE_SPEECH_MS) {
            return VoiceGateDecision.SILENT
        }
        val now = nowMs()
        // Tầng 2: chống spam.
        val cooldownUntil = store.readCooldownUntilMs()
        if (now < cooldownUntil) {
            if (cooldownUntil - now <= SPAM_COOLDOWN_MS) return VoiceGateDecision.SPAM_COOLDOWN
            // Đồng hồ bị lùi: cooldown xa hơn mức tối đa thì không hợp lệ, xóa đi.
            store.writeCooldownUntilMs(0L)
        }
        // Chỉ giãn cách khi lần gọi trước nằm trong quá khứ; nếu đồng hồ lùi thì bỏ qua.
        val lastCall = store.readLastCallMs()
        if (lastCall in 1..now && now - lastCall < MIN_CLOUD_INTERVAL_MS) return VoiceGateDecision.RATE_LIMITED
        // Tầng 3: hạn mức ngày.
        if (dailyCount() >= MAX_CLOUD_CALLS_PER_DAY) return VoiceGateDecision.DAILY_QUOTA_REACHED
        return VoiceGateDecision.ALLOW
    }

    fun recordCloudCallAttempt() {
        val count = dailyCount() + 1
        store.writeDaily(todayKey(), count)
        store.writeLastCallMs(nowMs())
    }

    fun recordParseOutcome(matchedItemCount: Int) {
        if (matchedItemCount > 0) {
            store.writeEmptyStreak(0)
            return
        }
        val streak = store.readEmptyStreak() + 1
        if (streak >= MAX_CONSECUTIVE_EMPTY_MATCHES) {
            store.writeCooldownUntilMs(nowMs() + SPAM_COOLDOWN_MS)
            store.writeEmptyStreak(0)
        } else {
            store.writeEmptyStreak(streak)
        }
    }

    private fun dailyCount(): Int =
        if (store.readDate() == todayKey()) store.readDailyCount() else 0
}
