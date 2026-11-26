package com.loitu.yourrefri.feature.pantry.ui

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class VoicePreFlightGateTest {
    private var now = 1_000_000L
    private var day = "2026-11-26"
    private val store = InMemoryVoiceQuotaStore()
    private val gate = VoicePreFlightGate(store, nowMs = { now }, todayKey = { day })

    private fun speech(durationMs: Long = 2_000L, activeMs: Long = 1_200L, rms: Float = 0.6f) =
        AudioCaptureMetrics(durationMs, activeMs, rms)

    // Đẩy thời gian qua giãn cách tối thiểu rồi ghi nhận một lần gọi đám mây.
    private fun attemptCloud() {
        now += MIN_CLOUD_INTERVAL_MS
        gate.recordCloudCallAttempt()
    }

    @Test
    fun tapShorterThan700msIsTooShort() {
        assertEquals(VoiceGateDecision.TOO_SHORT, gate.checkBeforeCloud(speech(durationMs = 699L)))
        assertEquals(VoiceGateDecision.ALLOW, gate.checkBeforeCloud(speech(durationMs = 700L)))
    }

    @Test
    fun silenceIsBlockedLocally() {
        assertEquals(VoiceGateDecision.SILENT, gate.checkBeforeCloud(speech(rms = 0.05f)))
        assertEquals(VoiceGateDecision.SILENT, gate.checkBeforeCloud(speech(rms = 0.11f)))
        assertEquals(VoiceGateDecision.ALLOW, gate.checkBeforeCloud(speech(rms = 0.12f)))
    }

    @Test
    fun activeSpeechUnder400msIsSilent() {
        assertEquals(VoiceGateDecision.SILENT, gate.checkBeforeCloud(speech(activeMs = 399L)))
        assertEquals(VoiceGateDecision.ALLOW, gate.checkBeforeCloud(speech(activeMs = 400L)))
    }

    @Test
    fun localVadRunsBeforeCooldownAndQuota() {
        store.writeCooldownUntilMs(now + SPAM_COOLDOWN_MS)
        assertEquals(VoiceGateDecision.TOO_SHORT, gate.checkBeforeCloud(speech(durationMs = 100L)))
    }

    @Test
    fun secondCallWithin3sIsRateLimited() {
        gate.recordCloudCallAttempt()
        now += MIN_CLOUD_INTERVAL_MS - 1
        assertEquals(VoiceGateDecision.RATE_LIMITED, gate.checkBeforeCloud(speech()))
        now += 1
        assertEquals(VoiceGateDecision.ALLOW, gate.checkBeforeCloud(speech()))
    }

    @Test
    fun threeEmptyMatchesStartA60sSpamCooldown() {
        gate.recordParseOutcome(0)
        gate.recordParseOutcome(0)
        assertEquals(VoiceGateDecision.ALLOW, gate.checkBeforeCloud(speech()))
        gate.recordParseOutcome(0)

        assertEquals(VoiceGateDecision.SPAM_COOLDOWN, gate.checkBeforeCloud(speech()))
        now += SPAM_COOLDOWN_MS - 1
        assertEquals(VoiceGateDecision.SPAM_COOLDOWN, gate.checkBeforeCloud(speech()))
        now += 1
        assertEquals(VoiceGateDecision.ALLOW, gate.checkBeforeCloud(speech()))
    }

    @Test
    fun matchedItemResetsEmptyStreak() {
        gate.recordParseOutcome(0)
        gate.recordParseOutcome(0)
        gate.recordParseOutcome(2)
        gate.recordParseOutcome(0)
        gate.recordParseOutcome(0)
        assertEquals(VoiceGateDecision.ALLOW, gate.checkBeforeCloud(speech()))
    }

    @Test
    fun cooldownTriggerResetsStreakSoNextEmptyStartsFresh() {
        repeat(MAX_CONSECUTIVE_EMPTY_MATCHES) { gate.recordParseOutcome(0) }
        now += SPAM_COOLDOWN_MS
        gate.recordParseOutcome(0)
        assertEquals(VoiceGateDecision.ALLOW, gate.checkBeforeCloud(speech()))
    }

    @Test
    fun dailyQuotaCapsAt25CloudCalls() {
        repeat(MAX_CLOUD_CALLS_PER_DAY) { attemptCloud() }
        now += MIN_CLOUD_INTERVAL_MS
        assertEquals(VoiceGateDecision.DAILY_QUOTA_REACHED, gate.checkBeforeCloud(speech()))
    }

    @Test
    fun dailyQuotaResetsOnNextDay() {
        repeat(MAX_CLOUD_CALLS_PER_DAY) { attemptCloud() }
        now += MIN_CLOUD_INTERVAL_MS
        assertEquals(VoiceGateDecision.DAILY_QUOTA_REACHED, gate.checkBeforeCloud(speech()))

        day = "2026-11-27"
        now += 5_000L
        assertEquals(VoiceGateDecision.ALLOW, gate.checkBeforeCloud(speech()))
        attemptCloud()
        now += MIN_CLOUD_INTERVAL_MS
        assertEquals(VoiceGateDecision.ALLOW, gate.checkBeforeCloud(speech()))
    }

    @Test
    fun wavHeaderAndPayloadAreWellFormed() {
        val pcm = ByteArray(10) { (it + 1).toByte() }
        val wav = encodePcm16MonoToWav(pcm)

        assertEquals(44 + pcm.size, wav.size)
        assertEquals("RIFF", String(wav, 0, 4, Charsets.US_ASCII))
        assertEquals(36 + pcm.size, readIntLe(wav, 4))
        assertEquals("WAVE", String(wav, 8, 4, Charsets.US_ASCII))
        assertEquals("fmt ", String(wav, 12, 4, Charsets.US_ASCII))
        assertEquals(16, readIntLe(wav, 16))
        assertEquals(1, readShortLe(wav, 20))
        assertEquals(1, readShortLe(wav, 22))
        assertEquals(16_000, readIntLe(wav, 24))
        assertEquals(32_000, readIntLe(wav, 28))
        assertEquals(2, readShortLe(wav, 32))
        assertEquals(16, readShortLe(wav, 34))
        assertEquals("data", String(wav, 36, 4, Charsets.US_ASCII))
        assertEquals(pcm.size, readIntLe(wav, 40))
        assertContentEquals(pcm, wav.copyOfRange(44, wav.size))
    }

    @Test
    fun wavUsesGivenSampleRate() {
        val wav = encodePcm16MonoToWav(ByteArray(4), sampleRate = 8_000)
        assertEquals(8_000, readIntLe(wav, 24))
        assertEquals(16_000, readIntLe(wav, 28))
    }

    private fun readIntLe(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 0xFF) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 3].toInt() and 0xFF) shl 24)

    private fun readShortLe(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 0xFF) or ((bytes[offset + 1].toInt() and 0xFF) shl 8)
}
