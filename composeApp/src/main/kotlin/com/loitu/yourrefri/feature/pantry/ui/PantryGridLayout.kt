package com.loitu.yourrefri.feature.pantry.ui

/** Lưới hai cột ở cỡ chữ thường; từ 1.3 chuyển một cột để tên món và số lượng không bị cắt. */
internal fun pantryGridColumns(fontScale: Float): Int = if (fontScale >= 1.3f) 1 else 2
