package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.recipe.data.MealSlot

/**
 * Bữa người dùng đang nghĩ tới theo giờ trong ngày: từ 5 giờ đến trước 14 giờ là bữa trưa, còn lại là bữa tối.
 * Khuya và rạng sáng (0-4 giờ) tính là bữa tối. Giờ lấy từ đồng hồ được tiêm vào, không gọi giờ hệ thống ở đây.
 */
fun defaultMealFor(hourOfDay: Int): MealSlot = if (hourOfDay in 5..13) MealSlot.LUNCH else MealSlot.DINNER
