package com.loitu.yourrefri.reminder

/**
 * Đăng nhắc hằng ngày rồi mới ghi mốc "đã gửi". Nếu đăng lỗi thì mốc không được ghi,
 * nên ngày đó vẫn được thử lại. Nút thử ngay không bao giờ ghi mốc.
 */
fun deliverDailyReminder(isTest: Boolean, post: () -> Unit, markSent: () -> Unit) {
    post()
    if (!isTest) markSent()
}
