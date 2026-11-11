package com.loitu.yourrefri.feature.pantry.ui

internal data class PantryCardCopy(
    val statusLabel: String,
    val absentHint: String?,
    val showEstimatedStatus: Boolean,
    val quantityHint: String?,
)

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
