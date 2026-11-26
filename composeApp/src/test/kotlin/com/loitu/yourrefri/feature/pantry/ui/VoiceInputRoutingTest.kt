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
}
