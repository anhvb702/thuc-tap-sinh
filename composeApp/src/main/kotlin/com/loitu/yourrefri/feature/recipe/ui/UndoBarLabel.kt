package com.loitu.yourrefri.feature.recipe.ui

/** Chữ thanh hoàn tác sau khi nấu, theo Figma 12: "Đã lưu · Hoàn tác (5s)". Số giây không xuống dưới 0. */
internal fun undoBarLabel(secondsLeft: Int): String =
    "Đã lưu · Hoàn tác (${secondsLeft.coerceAtLeast(0)}s)"
