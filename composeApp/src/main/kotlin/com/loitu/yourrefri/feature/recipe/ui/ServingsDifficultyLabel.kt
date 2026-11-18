package com.loitu.yourrefri.feature.recipe.ui

/**
 * Dòng "2 người · Dễ nấu" theo Figma 07.
 * Công thức chưa có số người và độ khó trong dữ liệu: mặc định 2 người (như các màn Figma khác),
 * "Dễ nấu" khi nấu từ 15 phút trở xuống. Quy tắc này là ruling, không phải dữ liệu gốc.
 */
internal fun servingsDifficultyLabel(minutes: Int?, people: Int = 2): String {
    val base = "$people người"
    return if (minutes != null && minutes <= 15) "$base · Dễ nấu" else base
}
