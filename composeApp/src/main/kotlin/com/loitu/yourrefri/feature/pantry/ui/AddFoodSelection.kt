package com.loitu.yourrefri.feature.pantry.ui

/** Nhãn nút xác nhận màn "Thêm nguyên liệu" (Figma 11): chữ "nguyên liệu", không còn "món". */
internal fun addSelectionLabel(count: Int): String {
    require(count >= 0)
    return if (count == 0) "Thêm nguyên liệu vào tủ" else "Thêm $count nguyên liệu vào tủ"
}

/** Mục "Không thấy món bạn cần?" (Figma 11) chỉ hiện khi người dùng đã gõ và không có món nào khớp. */
internal fun notFoundSectionVisible(query: String, matchCount: Int): Boolean {
    require(matchCount >= 0)
    return query.isNotBlank() && matchCount == 0
}
