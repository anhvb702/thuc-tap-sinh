package com.loitu.yourrefri.feature.pantry.ui

internal data class PantryCardCopy(
    val statusLabel: String,
    val absentHint: String?,
    val showEstimatedStatus: Boolean,
    val quantityHint: String?,
)

internal const val UNDO_LABEL = "Hoàn tác"

// Bấm "−" khi chỉ còn 1 (hoặc 0) thì món được đánh dấu hết, nên cần cho người dùng đường lui.
internal fun decreaseMarksOut(quantity: Int): Boolean = quantity <= 1

// Chạm vào món đang có sẽ đánh dấu hết; chạm món chưa có là thêm vào tủ, không cần hoàn tác.
internal fun tapMarksOut(have: Boolean): Boolean = have

internal fun markedOutMessage(name: String): String = "Đã đánh dấu hết $name"

internal fun pantryCardCopy(have: Boolean, quantity: Int): PantryCardCopy =
    if (have) {
        PantryCardCopy(
            statusLabel = "Có trong tủ",
            absentHint = null,
            showEstimatedStatus = true,
            quantityHint = if (quantity == 1) "− để đánh dấu hết" else null,
        )
    } else {
        PantryCardCopy(
            statusLabel = "Chưa có",
            absentHint = "Chạm để thêm vào tủ",
            showEstimatedStatus = false,
            quantityHint = null,
        )
    }

/** Chữ của bảng "Hôm nay mua gì về?" (thêm món vào tủ). */
internal object AddFoodSheetCopy {
    const val title = "Hôm nay mua gì về?"
    const val subtitle = "Chạm để chọn, hạn dùng ước tính sẽ tự điền."
    const val estimateHeader = "Hạn dùng ước tính"
    const val closeLabel = "Đóng"
    const val fabLabel = "Thêm món"
    const val searchPlaceholder = "Tìm món, ví dụ: thịt bò"
    const val alreadyPresent = "Món này đã có trong tủ rồi."
    const val alreadyPresentWithIncrease = "Món này đã có trong tủ. Bấm bên dưới nếu vừa mua thêm 1."
}

// Chữ lớn thì lưới ô chạm giảm còn 2 cột để tên món không bị cắt.
internal fun quickAddGridColumns(fontScale: Float): Int = if (fontScale >= 1.3f) 2 else 3

internal fun quickAddTileDescription(name: String, selected: Boolean): String =
    if (selected) "$name, đã chọn" else "$name, chưa chọn"
