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

// Từ miền Bắc/Nam khác nhau cho cùng một món (đã bỏ dấu, chữ thường). Thứ tự không quan trọng: khớp cụm dài nhất trước.
private val REGIONAL_SYNONYMS: Map<String, String> = mapOf(
    "dau hu" to "dau phu",
    "tau hu" to "dau phu",
    "thit lon" to "thit heo",
    "thit bam" to "thit heo xay",
    "lon xay" to "thit heo xay",
    "heo xay" to "thit heo xay",
    // Giữ nguyên cụm đã đúng: nếu không có dòng này, "heo xay" bên trong sẽ bị thêm "thit" lần nữa.
    "thit heo xay" to "thit heo xay",
    "ba roi" to "thit ba chi",
    "gio lua" to "cha lua",
    "dua chuot" to "dua leo",
    "sup lo xanh" to "bong cai xanh",
    "sup lo" to "bong cai xanh",
    "dau co ve" to "dau que",
    "mi chinh" to "bot ngot",
    "xi dau" to "nuoc tuong",
    "ca qua" to "ca loc",
    "ca chuoi" to "ca loc",
    "cu hanh tim" to "hanh tim",
)

// Một lần quét, không thay chồng: cụm đã thay không bị thay tiếp. (?<!\S) và (?!\S) = khớp đúng từ.
private val REGIONAL_SYNONYM_PATTERN: Regex = Regex(
    "(?<!\\S)(" + REGIONAL_SYNONYMS.keys.sortedByDescending { it.length }
        .joinToString("|") { Regex.escape(it) } + ")(?!\\S)",
)

/** Đổi từ miền sang tên chuẩn. Đầu vào đã chuẩn hóa (không dấu, chữ thường, một khoảng trắng). */
internal fun applyRegionalSynonyms(normalizedPhrase: String): String =
    REGIONAL_SYNONYM_PATTERN.replace(normalizedPhrase) { REGIONAL_SYNONYMS.getValue(it.value) }

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
    // Đổi từ miền trước khi khớp (cụm "ba rọi" phải thành "thịt ba chỉ" trước khi bỏ số đếm "ba").
    // Không đổi gì thì giữ nguyên token gốc, để hành vi khớp có dấu như trước.
    val normalizedJoined = segment.joinToString(" ") { normalizeFoodQuery(it) }
    val replaced = applyRegionalSynonyms(normalizedJoined)
    val words = if (replaced == normalizedJoined) segment else replaced.split(' ')
    // Thử nguyên cụm trước (tránh nhầm "nấm" với số "năm"), rồi cụm đã bỏ số lượng/đơn vị.
    quickAddMatch(ingredients, words.joinToString(" "))?.let { return listOf(it.id) }
    val stripped = words.dropWhile { token ->
        val n = normalizeFoodQuery(token)
        n in LEADING_NOISE || n.all(Char::isDigit)
    }
    if (stripped.isNotEmpty() && stripped.size != words.size) {
        quickAddMatch(ingredients, stripped.joinToString(" "))?.let { return listOf(it.id) }
    }
    return scanForNames(stripped.ifEmpty { words }, ingredients)
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
