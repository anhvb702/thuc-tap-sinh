package com.loitu.yourrefri.reminder

const val NOTIFICATION_RECIPE_EXTRA = "recipe_id"

/**
 * Chọn id công thức để mở khi bấm thông báo nhắc.
 * Trả id món bữa tối nếu món còn tồn tại trong danh mục đã biết; ngược lại trả null
 * (app mở ở Trang chủ).
 */
fun notificationRecipeRequest(dinnerRecipeId: String?, knownRecipeIds: Collection<String>): String? =
    dinnerRecipeId?.takeIf { it in knownRecipeIds }
