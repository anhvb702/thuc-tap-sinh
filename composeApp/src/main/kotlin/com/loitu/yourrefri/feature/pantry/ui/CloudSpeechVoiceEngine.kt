package com.loitu.yourrefri.feature.pantry.ui

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.CancellationException

// Prompt ngắn, tiếng Việt: chỉ yêu cầu liệt kê tên nguyên liệu nghe được.
internal const val CLOUD_VOICE_PROMPT =
    "Đây là đoạn ghi âm tiếng Việt kể tên đồ ăn. Chỉ ghi lại tên các nguyên liệu nghe được, cách nhau bằng dấu phẩy."

internal interface CloudSpeechTranscriber {
    suspend fun transcribeVietnamese(audioBytes: ByteArray, mimeType: String = "audio/wav"): String?
}

/**
 * Gọi Gemini qua Firebase AI Logic. Không có khóa API trong mã: Firebase đọc cấu hình từ google-services.json.
 * Firebase chưa khởi tạo thì trả null, không làm app crash.
 */
internal class FirebaseGeminiTranscriber(private val context: Context) : CloudSpeechTranscriber {
    override suspend fun transcribeVietnamese(audioBytes: ByteArray, mimeType: String): String? {
        if (audioBytes.isEmpty()) return null
        val model = try {
            if (FirebaseApp.getApps(context).isEmpty()) return null
            Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                modelName = "gemini-2.5-flash-lite",
                generationConfig = generationConfig {
                    temperature = 0f
                    maxOutputTokens = 64
                },
            )
        } catch (_: IllegalStateException) {
            return null
        }
        return try {
            val response = model.generateContent(
                content {
                    inlineData(audioBytes, mimeType)
                    text(CLOUD_VOICE_PROMPT)
                },
            )
            response.text?.trim()?.takeIf { it.isNotEmpty() }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }
}

/** Bọc PCM 16-bit mono thành WAV: header RIFF/WAVE 44 byte (little-endian) + dữ liệu. */
internal fun encodePcm16MonoToWav(pcmBytes: ByteArray, sampleRate: Int = 16_000): ByteArray {
    val channels = 1
    val bitsPerSample = 16
    val blockAlign = channels * bitsPerSample / 8
    val byteRate = sampleRate * blockAlign
    return ByteBuffer.allocate(WAV_HEADER_BYTES + pcmBytes.size)
        .order(ByteOrder.LITTLE_ENDIAN)
        .put("RIFF".toByteArray(Charsets.US_ASCII))
        .putInt(36 + pcmBytes.size)
        .put("WAVE".toByteArray(Charsets.US_ASCII))
        .put("fmt ".toByteArray(Charsets.US_ASCII))
        .putInt(16)
        .putShort(1) // PCM
        .putShort(channels.toShort())
        .putInt(sampleRate)
        .putInt(byteRate)
        .putShort(blockAlign.toShort())
        .putShort(bitsPerSample.toShort())
        .put("data".toByteArray(Charsets.US_ASCII))
        .putInt(pcmBytes.size)
        .put(pcmBytes)
        .array()
}

private const val WAV_HEADER_BYTES = 44
