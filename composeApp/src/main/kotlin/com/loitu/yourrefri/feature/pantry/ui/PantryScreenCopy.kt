package com.loitu.yourrefri.feature.pantry.ui

internal object PantryScreenCopy {
    const val pantryHint = "Chạm để thêm hoặc đổi trạng thái. Hạn là ước tính."
    const val addFoodDescription = "Thêm thực phẩm vào tủ"
    const val soonEmpty = "Tủ đang ổn, chưa có món nào cần dùng sớm"
    const val cookFromSoonAction = "Nấu gì từ các món này?"

    fun soonChipLabel(count: Int): String = "Nên dùng sớm · $count"
}

internal fun shelfEditorStatusText(selectedDays: Int?): String? =
    if (selectedDays == 0) "Đang áp dụng: ước tính dùng trong ngày" else null
