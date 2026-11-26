package com.loitu.yourrefri.feature.pantry.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.google.firebase.FirebaseApp
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt
import kotlin.time.TimeSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal enum class VoiceEngineChoice { ON_DEVICE, PLATFORM, CLOUD_FALLBACK, NONE }

internal enum class VoiceFailure { NOTHING_HEARD, LANGUAGE_PACK_MISSING, TOO_SHORT, COOLDOWN, QUOTA_REACHED, UNAVAILABLE }

internal fun chooseVoiceEngine(
    sdkInt: Int,
    onDeviceAvailable: Boolean,
    platformAvailable: Boolean,
    cloudFallbackEnabled: Boolean = false,
): VoiceEngineChoice = when {
    sdkInt >= Build.VERSION_CODES.TIRAMISU && onDeviceAvailable -> VoiceEngineChoice.ON_DEVICE
    platformAvailable -> VoiceEngineChoice.PLATFORM
    cloudFallbackEnabled -> VoiceEngineChoice.CLOUD_FALLBACK
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
    availableVoiceEngine(context) != VoiceEngineChoice.NONE || isCloudVoiceFallbackAvailable(context)

internal fun isCloudVoiceFallbackAvailable(context: Context): Boolean =
    try {
        FirebaseApp.getApps(context).isNotEmpty()
    } catch (_: Exception) {
        false
    }

private fun availableVoiceEngine(context: Context, cloudFallbackEnabled: Boolean = false): VoiceEngineChoice =
    chooseVoiceEngine(
        Build.VERSION.SDK_INT, onDeviceAvailable(context), platformAvailable(context), cloudFallbackEnabled,
    )

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

internal fun mapGateDecisionToFailure(decision: VoiceGateDecision): VoiceFailure? = when (decision) {
    VoiceGateDecision.ALLOW -> null
    VoiceGateDecision.TOO_SHORT -> VoiceFailure.TOO_SHORT
    VoiceGateDecision.SILENT -> VoiceFailure.NOTHING_HEARD
    VoiceGateDecision.RATE_LIMITED, VoiceGateDecision.SPAM_COOLDOWN -> VoiceFailure.COOLDOWN
    VoiceGateDecision.DAILY_QUOTA_REACHED -> VoiceFailure.QUOTA_REACHED
}

internal fun pcm16NormalizedRms(samples: ShortArray, count: Int): Float {
    if (count <= 0) return 0f
    var sumSquares = 0.0
    for (index in 0 until count) {
        val sample = samples[index].toDouble()
        sumSquares += sample * sample
    }
    val rms = sqrt(sumSquares / count).toFloat() / 32768f
    return (rms * 4f).coerceIn(0f, 1f)
}

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
    gate: VoicePreFlightGate? = null,
    transcriber: CloudSpeechTranscriber? = null,
    onCloudRecording: (Boolean) -> Unit = {},
): VoiceSession? {
    val startCloudFallback: (() -> VoiceSession)? = gate?.takeIf { isCloudVoiceFallbackAvailable(context) }?.let { cloudGate ->
        {
            val cloudTranscriber = transcriber ?: FirebaseGeminiTranscriber(context.applicationContext)
            CloudFallbackVoiceSession(cloudGate, cloudTranscriber, onCloudRecording, onRms, onResult, onFailure)
        }
    }
    return when (val engine = availableVoiceEngine(context, cloudFallbackEnabled = startCloudFallback != null)) {
        VoiceEngineChoice.NONE -> null
        VoiceEngineChoice.CLOUD_FALLBACK -> startCloudFallback?.invoke()
        VoiceEngineChoice.ON_DEVICE, VoiceEngineChoice.PLATFORM ->
            ManagedVoiceSession(context, engine, onPartial, onResult, onFailure, onRms, startCloudFallback)
    }
}

private class ManagedVoiceSession(
    private val context: Context,
    initialEngine: VoiceEngineChoice,
    private val onPartial: (String) -> Unit,
    private val onResult: (String) -> Unit,
    private val onFailure: (VoiceFailure) -> Unit,
    private val onRms: (Float) -> Unit,
    private var startCloudFallback: (() -> VoiceSession)?,
) : VoiceSession {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var activeEngine = initialEngine
    private var didFallback = false
    private var lastPartial = ""
    private var finished = false
    private var stopRequested = false
    private var cloudSession: VoiceSession? = null

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
                VoiceEngineChoice.CLOUD_FALLBACK, VoiceEngineChoice.NONE -> return
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
        if (startCloudFallback != null &&
            (failure == VoiceFailure.UNAVAILABLE || failure == VoiceFailure.LANGUAGE_PACK_MISSING)
        ) {
            // Release the Android recognizer outside its callback before opening AudioRecord.
            mainHandler.post {
                destroyNow()
                cloudSession = startCloudFallback?.invoke()
            }
            return
        }
        onFailure(failure)
        mainHandler.post { destroyNow() }
    }

    private fun firstText(bundle: Bundle?): String? =
        bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.takeIf { it.isNotBlank() }

    override fun stop() {
        if (finished) {
            cloudSession?.stop()
            return
        }
        stopRequested = true
        try {
            recognizer?.stopListening()
        } catch (_: Exception) {
        }
        mainHandler.postDelayed({ if (!finished) finish(lastPartial) }, 1_500)
    }

    override fun destroy() {
        finished = true
        startCloudFallback = null
        cloudSession?.destroy()
        cloudSession = null
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

private class CloudFallbackVoiceSession(
    gate: VoicePreFlightGate,
    transcriber: CloudSpeechTranscriber,
    onCloudRecording: (Boolean) -> Unit,
    private val onRms: (Float) -> Unit,
    onResult: (String) -> Unit,
    onFailure: (VoiceFailure) -> Unit,
) : VoiceSession {
    @Volatile
    private var stopRequested = false

    private val job = CoroutineScope(Dispatchers.Main).launch {
        val transcript = try {
            onCloudRecording(true)
            val capture = withContext(Dispatchers.IO) { recordAudio() }
            onCloudRecording(false)
            ensureActive()
            // Keep the quota check and attempt together on the main thread.
            val failure = mapGateDecisionToFailure(gate.checkBeforeCloud(capture.metrics))
            if (failure != null) {
                onFailure(failure)
                return@launch
            }
            gate.recordCloudCallAttempt()
            withContext(Dispatchers.IO) {
                transcriber.transcribeVietnamese(encodePcm16MonoToWav(capture.pcmBytes, SAMPLE_RATE))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            onFailure(VoiceFailure.UNAVAILABLE)
            return@launch
        }
        if (transcript.isNullOrBlank()) {
            gate.recordParseOutcome(0)
            onFailure(if (transcript == null) VoiceFailure.UNAVAILABLE else VoiceFailure.NOTHING_HEARD)
        } else {
            onResult(transcript)
        }
    }

    override fun stop() {
        stopRequested = true
    }

    override fun destroy() {
        stopRequested = true
        job.cancel()
    }

    @SuppressLint("MissingPermission") // The screen requests RECORD_AUDIO before starting the session.
    private suspend fun recordAudio(): Capture {
        val channel = AudioFormat.CHANNEL_IN_MONO
        val format = AudioFormat.ENCODING_PCM_16BIT
        val minBufferBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE, channel, format)
        check(minBufferBytes > 0)
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC, SAMPLE_RATE, channel, format,
            maxOf(minBufferBytes, BUFFER_SAMPLES * Short.SIZE_BYTES),
        )
        try {
            check(recorder.state == AudioRecord.STATE_INITIALIZED)
            recorder.startRecording()
            try {
                check(recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING)
                return readPcm(recorder)
            } finally {
                recorder.stop()
            }
        } finally {
            recorder.release()
        }
    }

    private suspend fun readPcm(recorder: AudioRecord): Capture {
        val maxSamples = (MAX_RECORDING_MS * SAMPLE_RATE / 1_000).toInt()
        val pcm = ByteBuffer.allocate(maxSamples * Short.SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)
        val samples = ShortArray(BUFFER_SAMPLES)
        val started = TimeSource.Monotonic.markNow()
        var totalSamples = 0
        var activeSamples = 0
        var peakRms = 0f
        while (!stopRequested && totalSamples < maxSamples && started.elapsedNow().inWholeMilliseconds < MAX_RECORDING_MS) {
            val requested = minOf(samples.size, maxSamples - totalSamples)
            val count = recorder.read(samples, 0, requested, AudioRecord.READ_NON_BLOCKING)
            check(count >= 0)
            if (count == 0) {
                delay(20)
                continue
            }
            val rms = pcm16NormalizedRms(samples, count)
            peakRms = maxOf(peakRms, rms)
            if (rms >= MIN_SPEECH_RMS) activeSamples += count
            for (index in 0 until count) pcm.putShort(samples[index])
            totalSamples += count
            withContext(Dispatchers.Main) { if (!stopRequested) onRms(rms) }
        }
        val metrics = AudioCaptureMetrics(
            durationMs = totalSamples * 1_000L / SAMPLE_RATE,
            activeSpeechMs = activeSamples * 1_000L / SAMPLE_RATE,
            peakNormalizedRms = peakRms,
        )
        return Capture(pcm.array().copyOf(pcm.position()), metrics)
    }

    private data class Capture(val pcmBytes: ByteArray, val metrics: AudioCaptureMetrics)

    private companion object {
        const val SAMPLE_RATE = 16_000
        const val BUFFER_SAMPLES = 320
        const val MAX_RECORDING_MS = 12_000L
    }
}
