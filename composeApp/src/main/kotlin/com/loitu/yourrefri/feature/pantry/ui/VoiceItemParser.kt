package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient

internal const val VOICE_NOT_HEARD = "Mình chưa nghe rõ, bạn thử nói lại hoặc chọn thủ công nhé"

// Từ nối giữa các món (đã bỏ dấu).
private val SEPARATORS = setOf("va", "voi", "cung", ",")

// Từ đứng đầu cụm cần bỏ: lời dẫn, số đếm, đơn vị nói kèm (đã bỏ dấu).
private val LEADING_NOISE = setOf(
    "toi", "minh", "them", "mua", "co", "vua",
    "mot", "hai", "ba", "bon", "tu", "nam", "sau", "bay", "tam", "chin", "muoi", "chuc", "vai", "it", "nhieu",
    "qua", "trai", "cu", "cai", "con", "bo", "goi", "hop", "chai", "lon", "vi", "bap", "mieng", "tep", "nhanh", "mo",
    "kg", "ky", "can", "lang", "gram", "g", "lit", "ml",
)

/** Chữ nghe được, gọn khoảng trắng, để hiển thị "Mình nghe được: ...". */
internal fun voiceHeardText(transcript: String): String =
    transcript.trim().split(Regex("\\s+")).filter(String::isNotEmpty).joinToString(" ")

/**
 * Tách câu nói thành danh sách id món trong danh mục. Chỉ trả món khớp rõ ràng (một món duy nhất), không trùng id,
 * giữ thứ tự nói. Không tự thêm vào tủ: người dùng xác nhận ở bảng thêm món.
 */
internal fun parseVoiceItems(transcript: String, ingredients: List<Ingredient>): List<String> {
    val tokens = voiceHeardText(transcript.replace(Regex("[,;.]"), " , ")).split(' ').filter(String::isNotEmpty)
    if (tokens.isEmpty()) return emptyList()

    val segments = mutableListOf<List<String>>()
    var current = mutableListOf<String>()
    for (token in tokens) {
        if (normalizeFoodQuery(token) in SEPARATORS) {
            if (current.isNotEmpty()) segments += current
            current = mutableListOf()
        } else {
            current += token
        }
    }
    if (current.isNotEmpty()) segments += current

    val result = LinkedHashSet<String>()
    for (segment in segments) result += idsOfSegment(segment, ingredients)
    return result.toList()
}

private fun idsOfSegment(segment: List<String>, ingredients: List<Ingredient>): List<String> {
    // Thử nguyên cụm trước (tránh nhầm "nấm" với số "năm"), rồi cụm đã bỏ số lượng/đơn vị.
    quickAddMatch(ingredients, segment.joinToString(" "))?.let { return listOf(it.id) }
    val stripped = segment.dropWhile { token ->
        val n = normalizeFoodQuery(token)
        n in LEADING_NOISE || n.all(Char::isDigit)
    }
    if (stripped.isNotEmpty() && stripped.size != segment.size) {
        quickAddMatch(ingredients, stripped.joinToString(" "))?.let { return listOf(it.id) }
    }
    return scanForNames(stripped.ifEmpty { segment }, ingredients)
}

// Không có từ nối: dò từ trái sang phải, ưu tiên tên dài nhất khớp trọn vẹn.
private fun scanForNames(segment: List<String>, ingredients: List<Ingredient>): List<String> {
    val words = segment.map(::normalizeFoodQuery)
    val names = ingredients
        .map { it.id to normalizeFoodQuery(it.nameVi).split(' ') }
        .sortedByDescending { it.second.size }
    val found = mutableListOf<String>()
    var i = 0
    while (i < words.size) {
        val hit = names.firstOrNull { (_, name) ->
            i + name.size <= words.size && name.indices.all { words[i + it] == name[it] }
        }
        if (hit != null) {
            found += hit.first
            i += hit.second.size
        } else {
            i++
        }
    }
    return found
}
