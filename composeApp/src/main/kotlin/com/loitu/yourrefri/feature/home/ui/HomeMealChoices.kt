package com.loitu.yourrefri.feature.home.ui

import com.loitu.yourrefri.feature.recipe.data.MealSlot

/** Các nút bữa trên Trang chủ: chỉ trưa và tối (bữa sáng đã bỏ). */
internal fun homeMealChoices(): List<MealSlot> = listOf(MealSlot.LUNCH, MealSlot.DINNER)

/** Bữa đang chọn trên Trang chủ; lựa chọn bữa sáng đã lưu trước đây được đưa về trưa. */
internal fun homeMealChoice(meal: MealSlot): MealSlot = if (meal == MealSlot.BREAKFAST) MealSlot.LUNCH else meal
