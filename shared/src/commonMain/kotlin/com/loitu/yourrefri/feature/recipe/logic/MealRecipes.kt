package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.Recipe

/** Món "nhanh" cho bữa trưa: nấu khoảng 20 phút trở xuống. Món chưa rõ thời gian không tính là nhanh. */
const val LUNCH_QUICK_MINUTES = 20

private const val MIN_QUICK_FOR_LUNCH = 3

/**
 * Danh sách công thức đưa vào gợi ý theo bữa. Bữa tối giữ nguyên. Bữa trưa ưu tiên món nhanh:
 * đủ món nhanh thì chỉ dùng món nhanh, chưa đủ thì món nhanh đứng trước, phần còn lại giữ nguyên thứ tự.
 * Chỉ đổi đầu vào của `recommendRecipes`, không đổi cách chấm gợi ý.
 */
fun recipesForMeal(recipes: List<Recipe>, meal: MealSlot): List<Recipe> {
    if (meal == MealSlot.DINNER) return recipes
    val (quick, others) = recipes.partition { (it.minutes ?: Int.MAX_VALUE) <= LUNCH_QUICK_MINUTES }
    return when {
        quick.isEmpty() -> recipes
        quick.size >= MIN_QUICK_FOR_LUNCH -> quick
        else -> quick + others
    }
}
