package com.loitu.yourrefri.reminder

/**
 * Ảnh món đóng gói trong app cho popup nhắc ăn uống. Thông báo hiện chưa gắn với một món cụ thể,
 * nên ảnh luân phiên theo ngày; chọn món theo nguyên liệu sắp hết hạn là bước sau.
 */
object ReminderPicture {
    val ASSETS: List<String> = listOf(
        "notify/trung-xao-ca-chua.jpg",
        "notify/ga-ap-chao.jpg",
        "notify/canh-cai-dau-phu.jpg",
        "notify/dau-phu-sot-ca.jpg",
    )

    /** day: số ngày trong năm (1..366); cùng ngày luôn ra cùng ảnh. */
    fun assetFor(day: Int): String = ASSETS[Math.floorMod(day - 1, ASSETS.size)]
}
