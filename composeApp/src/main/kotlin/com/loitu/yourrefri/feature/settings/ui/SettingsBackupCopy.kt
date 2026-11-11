package com.loitu.yourrefri.feature.settings.ui

internal object SettingsBackupCopy {
    const val privacy =
        "Tủ lạnh và các công thức có sẵn dùng được không cần kết nối mạng. " +
            "Dữ liệu chỉ lưu trên điện thoại này, không tự đồng bộ."

    val transferSteps = listOf(
        "Lưu bản sao thành tệp trên điện thoại cũ.",
        "Tự chuyển tệp sang điện thoại mới.",
        "Trên máy mới, mở YourRefri và chọn tệp bản sao.",
    )

    const val backupContents =
        "Bản sao lưu có lịch sử nấu nhưng không có ảnh đính kèm."

    const val exportLabel = "Lưu bản sao vào tệp"
    const val importLabel = "Mở tệp bản sao để khôi phục"
    const val exportSuccess = "Đã lưu tệp bản sao YourRefri trên thiết bị."
    const val restoreFailure = "Chưa thể đọc tệp bản sao YourRefri. Hãy chọn tệp hợp lệ."
    const val restoreLabel = "Khôi phục"
    const val restoreTitle = "Thay dữ liệu hiện tại?"
    const val restoreWarning =
        "Bản sao lưu sẽ thay thế thực phẩm, hạn dùng, số lượng đã chỉnh và lịch sử nấu trên điện thoại này."
    const val cancelRestoreLabel = "Để sau"
}
