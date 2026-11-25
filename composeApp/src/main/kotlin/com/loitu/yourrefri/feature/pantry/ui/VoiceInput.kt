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
import androidx.annotation.RequiresApi

/**
 * Nhận dạng giọng nói CHỈ trên thiết bị (Android 13 trở lên), để âm thanh không rời khỏi máy.
 * Không dùng bộ nhận dạng mặc định của hệ thống vì nó có thể gửi âm thanh lên máy chủ.
 */
internal fun isVoiceInputAvailable(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && onDeviceAvailable(context)

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun onDeviceAvailable(context: Context): Boolean =
    try {
        SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
    } catch (_: Exception) {
        false
    }

internal enum class VoiceFailure { NOTHING_HEARD, UNAVAILABLE }

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

/** Tạo phiên nghe on-device; trả null khi máy không hỗ trợ (khi đó nút giọng nói đã bị ẩn). */
internal fun startVoiceSession(
    context: Context,
    onPartial: (String) -> Unit,
    onResult: (String) -> Unit,
    onFailure: (VoiceFailure) -> Unit,
    onRms: (Float) -> Unit = {},
): VoiceSession? {
    if (!isVoiceInputAvailable(context)) return null
    return OnDeviceVoiceSession(context, onPartial, onResult, onFailure, onRms)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private class OnDeviceVoiceSession(
    context: Context,
    private val onPartial: (String) -> Unit,
    private val onResult: (String) -> Unit,
    private val onFailure: (VoiceFailure) -> Unit,
    private val onRms: (Float) -> Unit,
) : VoiceSession {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var lastPartial = ""
    private var finished = false
    private var stopRequested = false

    init {
        try {
            recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            recognizer?.setRecognitionListener(
            object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {
                    if (!finished && !stopRequested) onRms(normalizeVoiceRms(rmsdB))
                }
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onEvent(eventType: Int, params: Bundle?) {}

                override fun onPartialResults(partialResults: Bundle?) {
                    firstText(partialResults)?.let {
                        lastPartial = it
                        if (!finished) onPartial(it)
                    }
                }

                override fun onResults(results: Bundle?) {
                    finish(firstText(results) ?: lastPartial)
                }

                override fun onError(error: Int) {
                    if (finished) return
                    when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> finish(lastPartial)
                        else -> {
                            finishFailure(VoiceFailure.UNAVAILABLE)
                        }
                    }
                }
            },
            )
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            recognizer?.startListening(intent)
        } catch (_: Exception) {
            recognizer = null
            finishFailure(VoiceFailure.UNAVAILABLE)
        }
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
        recognizer?.let {
            try {
                it.cancel()
                it.destroy()
            } catch (_: Exception) {
                // Đã dọn rồi: bỏ qua.
            }
        }
        recognizer = null
    }
}
