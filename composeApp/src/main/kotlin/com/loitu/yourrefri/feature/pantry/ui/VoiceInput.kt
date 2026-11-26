package com.loitu.yourrefri.feature.pantry.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

internal enum class VoiceEngineChoice { ON_DEVICE, PLATFORM, NONE }

internal enum class VoiceFailure { NOTHING_HEARD, LANGUAGE_PACK_MISSING, UNAVAILABLE }

internal fun chooseVoiceEngine(
    sdkInt: Int,
    onDeviceAvailable: Boolean,
    platformAvailable: Boolean,
): VoiceEngineChoice = when {
    sdkInt >= Build.VERSION_CODES.TIRAMISU && onDeviceAvailable -> VoiceEngineChoice.ON_DEVICE
    platformAvailable -> VoiceEngineChoice.PLATFORM
    else -> VoiceEngineChoice.NONE
}

internal fun isLanguagePackError(errorCode: Int): Boolean =
    errorCode == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE ||
        errorCode == SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED

internal fun shouldFallbackToPlatform(
    engine: VoiceEngineChoice,
    errorCode: Int,
    platformAvailable: Boolean,
    alreadyFallenBack: Boolean,
): Boolean {
    if (engine != VoiceEngineChoice.ON_DEVICE || !platformAvailable || alreadyFallenBack) return false
    return isLanguagePackError(errorCode) || when (errorCode) {
        SpeechRecognizer.ERROR_SERVER_DISCONNECTED,
        SpeechRecognizer.ERROR_CLIENT,
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> true
        else -> false
    }
}

internal fun classifyVoiceError(errorCode: Int): VoiceFailure = when {
    errorCode == SpeechRecognizer.ERROR_NO_MATCH || errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
        VoiceFailure.NOTHING_HEARD
    isLanguagePackError(errorCode) -> VoiceFailure.LANGUAGE_PACK_MISSING
    else -> VoiceFailure.UNAVAILABLE
}

internal fun isVoiceInputAvailable(context: Context): Boolean =
    availableVoiceEngine(context) != VoiceEngineChoice.NONE

private fun availableVoiceEngine(context: Context): VoiceEngineChoice =
    chooseVoiceEngine(Build.VERSION.SDK_INT, onDeviceAvailable(context), platformAvailable(context))

private fun onDeviceAvailable(context: Context): Boolean =
    try {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
    } catch (_: Exception) {
        false
    }

private fun platformAvailable(context: Context): Boolean =
    try {
        SpeechRecognizer.isRecognitionAvailable(context)
    } catch (_: Exception) {
        false
    }

internal interface VoiceSession {
    /** Dừng nói: gửi kết quả đã nghe được cho onResult. */
    fun stop()

    /** Hủy và dọn bộ nhận dạng; gọi lại nhiều lần cũng an toàn. */
    fun destroy()
}

internal fun normalizeVoiceRms(rmsdB: Float): Float =
    if (rmsdB.isFinite()) ((rmsdB + 2f) / 12f).coerceIn(0f, 1f) else 0f

internal fun voiceWaveBarHeights(normalizedLevel: Float, animatorsEnabled: Boolean): List<Float> {
    if (!animatorsEnabled) return List(5) { 0.45f }
    val amplitude = 0.25f + 0.75f * normalizedLevel.coerceIn(0f, 1f)
    return listOf(0.45f, 0.78f, 1f, 0.78f, 0.45f).map { it * amplitude }
}

internal fun startVoiceSession(
    context: Context,
    onPartial: (String) -> Unit,
    onResult: (String) -> Unit,
    onFailure: (VoiceFailure) -> Unit,
    onRms: (Float) -> Unit = {},
): VoiceSession? {
    val engine = availableVoiceEngine(context)
    if (engine == VoiceEngineChoice.NONE) return null
    return ManagedVoiceSession(context, engine, onPartial, onResult, onFailure, onRms)
}

private class ManagedVoiceSession(
    private val context: Context,
    initialEngine: VoiceEngineChoice,
    private val onPartial: (String) -> Unit,
    private val onResult: (String) -> Unit,
    private val onFailure: (VoiceFailure) -> Unit,
    private val onRms: (Float) -> Unit,
) : VoiceSession {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var activeEngine = initialEngine
    private var didFallback = false
    private var lastPartial = ""
    private var finished = false
    private var stopRequested = false

    init {
        // Let the caller store the session before startup can report a failure.
        mainHandler.post { if (!finished && !stopRequested) startListening(initialEngine) }
    }

    private fun startListening(engine: VoiceEngineChoice) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, engine == VoiceEngineChoice.ON_DEVICE)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            val nextRecognizer = when (engine) {
                VoiceEngineChoice.ON_DEVICE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                    } else {
                        SpeechRecognizer.createSpeechRecognizer(context)
                    }
                }
                VoiceEngineChoice.PLATFORM -> SpeechRecognizer.createSpeechRecognizer(context)
                VoiceEngineChoice.NONE -> return
            }
            recognizer = nextRecognizer
            nextRecognizer.setRecognitionListener(
                object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {
                        if (isActive(engine) && !stopRequested) onRms(normalizeVoiceRms(rmsdB))
                    }
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}

                    override fun onPartialResults(partialResults: Bundle?) {
                        if (!isActive(engine)) return
                        val text = firstText(partialResults) ?: return
                        lastPartial = text
                        onPartial(text)
                    }

                    override fun onResults(results: Bundle?) {
                        if (isActive(engine)) finish(firstText(results) ?: lastPartial)
                    }

                    override fun onError(error: Int) {
                        handleError(engine, error, intent)
                    }
                },
            )
            nextRecognizer.startListening(intent)
        } catch (_: Exception) {
            handleError(engine, SpeechRecognizer.ERROR_CLIENT, intent)
        }
    }

    private fun isActive(engine: VoiceEngineChoice): Boolean =
        !finished && engine == activeEngine

    private fun handleError(engine: VoiceEngineChoice, error: Int, intent: Intent) {
        if (!isActive(engine)) return
        val failure = classifyVoiceError(error)
        if (failure == VoiceFailure.NOTHING_HEARD) {
            finish(lastPartial)
            return
        }
        if (engine == VoiceEngineChoice.ON_DEVICE && isLanguagePackError(error) &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            try {
                recognizer?.triggerModelDownload(intent)
            } catch (_: Exception) {
                // The recognizer may not support downloads.
            }
        }
        if (shouldFallbackToPlatform(activeEngine, error, platformAvailable(context), didFallback)) {
            didFallback = true
            activeEngine = VoiceEngineChoice.PLATFORM
            // Defer teardown until the current recognizer callback has returned.
            mainHandler.post {
                destroyNow()
                if (!finished) {
                    if (stopRequested) finish(lastPartial) else startListening(VoiceEngineChoice.PLATFORM)
                }
            }
            return
        }
        finishFailure(failure)
    }

    private fun finish(text: String) {
        if (finished) return
        finished = true
        if (text.isBlank()) onFailure(VoiceFailure.NOTHING_HEARD) else onResult(text)
        mainHandler.post { destroyNow() }
    }

    private fun finishFailure(failure: VoiceFailure) {
        if (finished) return
        finished = true
        onFailure(failure)
        mainHandler.post { destroyNow() }
    }

    private fun firstText(bundle: Bundle?): String? =
        bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.takeIf { it.isNotBlank() }

    override fun stop() {
        stopRequested = true
        try {
            recognizer?.stopListening()
        } catch (_: Exception) {
        }
        mainHandler.postDelayed({ if (!finished) finish(lastPartial) }, 1_500)
    }

    override fun destroy() {
        finished = true
        mainHandler.post { destroyNow() }
    }

    private fun destroyNow() {
        val currentRecognizer = recognizer ?: return
        recognizer = null
        try {
            currentRecognizer.cancel()
        } catch (_: Exception) {
            // Attempt destruction even if cancellation fails.
        }
        try {
            currentRecognizer.destroy()
        } catch (_: Exception) {
            // The service may have already disconnected.
        }
    }
}
