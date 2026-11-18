package com.loitu.yourrefri.feature.recipe.ui

/** Nhãn ngày theo Figma 05: "Thứ 2 · 16.11" (ngày.tháng, hai chữ số). */
internal fun dayChipLabel(dayName: String, day: Int, month: Int): String =
    "$dayName · ${day.toString().padStart(2, '0')}.${month.toString().padStart(2, '0')}"
