package com.loitu.yourrefri.feature.pantry.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VoiceInputRoutingTest {
    @Test
    fun android13AndNewerPreferOnDeviceWhenAvailable() {
        for (sdk in listOf(33, 34)) {
            assertEquals(VoiceEngineChoice.ON_DEVICE, chooseVoiceEngine(sdk, true, true))
            assertEquals(VoiceEngineChoice.ON_DEVICE, chooseVoiceEngine(sdk, true, false))
            assertEquals(VoiceEngineChoice.PLATFORM, chooseVoiceEngine(sdk, false, true))
            assertEquals(VoiceEngineChoice.NONE, chooseVoiceEngine(sdk, false, false))
        }
    }

    @Test
    fun android12UsesOnlyPlatformRecognition() {
        assertEquals(VoiceEngineChoice.PLATFORM, chooseVoiceEngine(32, true, true))
        assertEquals(VoiceEngineChoice.PLATFORM, chooseVoiceEngine(32, false, true))
        assertEquals(VoiceEngineChoice.NONE, chooseVoiceEngine(32, true, false))
        assertEquals(VoiceEngineChoice.NONE, chooseVoiceEngine(32, false, false))
    }

    @Test
    fun cloudFallbackComesAfterBothAndroidEngines() {
        assertEquals(VoiceEngineChoice.ON_DEVICE, chooseVoiceEngine(33, true, true, cloudFallbackEnabled = true))
        assertEquals(VoiceEngineChoice.PLATFORM, chooseVoiceEngine(33, false, true, cloudFallbackEnabled = true))
        assertEquals(VoiceEngineChoice.CLOUD_FALLBACK, chooseVoiceEngine(33, false, false, cloudFallbackEnabled = true))
    }

    @Test
    fun gateDecisionsMapToVoiceFailures() {
        assertEquals(null, mapGateDecisionToFailure(VoiceGateDecision.ALLOW))
        assertEquals(VoiceFailure.TOO_SHORT, mapGateDecisionToFailure(VoiceGateDecision.TOO_SHORT))
        assertEquals(VoiceFailure.NOTHING_HEARD, mapGateDecisionToFailure(VoiceGateDecision.SILENT))
        assertEquals(VoiceFailure.COOLDOWN, mapGateDecisionToFailure(VoiceGateDecision.RATE_LIMITED))
        assertEquals(VoiceFailure.COOLDOWN, mapGateDecisionToFailure(VoiceGateDecision.SPAM_COOLDOWN))
        assertEquals(VoiceFailure.QUOTA_REACHED, mapGateDecisionToFailure(VoiceGateDecision.DAILY_QUOTA_REACHED))
    }

    @Test
    fun emptyAndSilentPcmHaveZeroRms() {
        assertEquals(0f, pcm16NormalizedRms(shortArrayOf(), 0))
        assertEquals(0f, pcm16NormalizedRms(shortArrayOf(4096), -1))
        assertEquals(0f, pcm16NormalizedRms(shortArrayOf(0, 0, Short.MAX_VALUE), 2))
    }

    @Test
    fun midLevelPcmIsBoostedAboveTheSpeechThreshold() {
        val samples = shortArrayOf(0, -2048, Short.MAX_VALUE)
        val rms = pcm16NormalizedRms(samples, 2)
        assertEquals(0.1767767f, rms, absoluteTolerance = 0.0001f)
        assertTrue(rms > 0.12f)
    }

    @Test
    fun fullScalePcmRmsIsClampedToOne() {
        assertEquals(1f, pcm16NormalizedRms(shortArrayOf(Short.MIN_VALUE, Short.MAX_VALUE), 2))
    }

    @Test
    fun onlyMissingOrUnsupportedLanguageErrorsIdentifyAMissingPack() {
        for (error in 1..15) {
            assertEquals(error == 12 || error == 13, isLanguagePackError(error), "Error $error")
        }
    }

    @Test
    fun fallbackRequiresAnEligibleErrorFromOnDeviceAndAnUnusedAvailablePlatformEngine() {
        val fallbackErrors = listOf(5, 8, 11, 12, 13)
        for (error in 1..15) {
            assertEquals(
                error in fallbackErrors,
                shouldFallbackToPlatform(VoiceEngineChoice.ON_DEVICE, error, true, false),
                "Error $error",
            )
        }
        for (error in fallbackErrors) {
            assertFalse(shouldFallbackToPlatform(VoiceEngineChoice.ON_DEVICE, error, false, false))
            assertFalse(shouldFallbackToPlatform(VoiceEngineChoice.ON_DEVICE, error, true, true))
            assertFalse(shouldFallbackToPlatform(VoiceEngineChoice.PLATFORM, error, true, false))
            assertFalse(shouldFallbackToPlatform(VoiceEngineChoice.NONE, error, true, false))
        }
    }

    @Test
    fun speechErrorsAreClassifiedForTheUserMessage() {
        assertEquals(VoiceFailure.NOTHING_HEARD, classifyVoiceError(6))
        assertEquals(VoiceFailure.NOTHING_HEARD, classifyVoiceError(7))
        assertEquals(VoiceFailure.LANGUAGE_PACK_MISSING, classifyVoiceError(12))
        assertEquals(VoiceFailure.LANGUAGE_PACK_MISSING, classifyVoiceError(13))
        for (error in listOf(1, 2, 3, 4, 5, 8, 9, 10, 11, 14, 15, -1)) {
            assertEquals(VoiceFailure.UNAVAILABLE, classifyVoiceError(error), "Error $error")
        }
    }

    @Test
    fun missingLanguagePackMessageIsNonBlankAndAvoidsForbiddenCopy() {
        val message = VOICE_LANGUAGE_PACK_MISSING
        assertTrue(message.isNotBlank())
        val forbiddenWords = listOf(
            "bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", "streak", "chuỗi ngày liên tiếp", // lint-copy: rule
            "điểm số", "xếp hạng", "so với người khác", "đáng xấu hổ", // lint-copy: rule
        )
        for (word in forbiddenWords) {
            assertFalse(message.contains(word, ignoreCase = true), "Forbidden copy: $word")
        }
    }

    @Test
    fun cloudFallbackMessagesAreNonBlankAndAvoidForbiddenCopy() {
        val forbiddenWords = listOf(
            "bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", "streak", "chuỗi ngày liên tiếp", // lint-copy: rule
            "điểm số", "xếp hạng", "so với người khác", "đáng xấu hổ", // lint-copy: rule
        )
        for (message in listOf(VOICE_TOO_SHORT, VOICE_COOLDOWN, VOICE_QUOTA_REACHED)) {
            assertTrue(message.isNotBlank())
            for (word in forbiddenWords) {
                assertFalse(message.contains(word, ignoreCase = true), "Forbidden copy: $word")
            }
        }
    }
}
