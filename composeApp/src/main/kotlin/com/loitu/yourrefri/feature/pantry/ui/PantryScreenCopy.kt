package com.loitu.yourrefri.feature.pantry.ui

internal object PantryScreenCopy {
    const val pantryHint = "Chạm món để thêm vào tủ hoặc đổi trạng thái. Hạn dùng là ước tính."
    const val addFoodDescription = "Thêm thực phẩm vào tủ"
}

internal fun shelfEditorStatusText(selectedDays: Int?): String? =
    if (selectedDays == 0) "Đang áp dụng: ước tính dùng trong ngày" else null
