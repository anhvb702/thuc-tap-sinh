package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.recipe.data.Recipe

/** Chế độ ăn người dùng chọn; lưu bằng [storageValue] trên máy. */
enum class DietRestriction(val storageValue: String, val labelVi: String, val hintVi: String) {
    NONE("none", "Không kiêng", "Gợi ý mọi món"),
    NO_SEAFOOD("no_seafood", "Không hải sản", "Bỏ món có cá, tôm"),
    NO_RED_MEAT("no_red_meat", "Không thịt đỏ", "Bỏ món có thịt heo, bò"),
    VEGETARIAN("vegetarian", "Ăn chay", "Không thịt, cá, tôm; vẫn có trứng, sữa");

    companion object {
        fun fromStorage(value: String?): DietRestriction = entries.firstOrNull { it.storageValue == value } ?: NONE
    }
}

/** Nhóm nguyên liệu theo id, định nghĩa một chỗ duy nhất (khớp shelf_table: nhóm "thit" và "ca"). */
internal object DietIngredients {
    val redMeat = setOf("thit-heo", "thit-heo-xay", "thit-bo")
    val meat = redMeat + "thit-ga"
    val seafood = setOf("ca-basa", "ca-hoi", "tom")

    /** Món tự thêm chưa có id quen thuộc thì dựa vào nhóm của nguyên liệu. */
    private const val GROUP_MEAT = "thit"
    private const val GROUP_SEAFOOD = "ca"

    fun isBanned(id: String, restriction: DietRestriction, groupById: Map<String, String>): Boolean {
        val group = groupById[id]
        val isSeafood = id in seafood || (id !in meat && group == GROUP_SEAFOOD)
        val isRed = id in redMeat || (id !in meat && group == GROUP_MEAT)
        val isMeat = id in meat || (id !in seafood && group == GROUP_MEAT)
        return when (restriction) {
            DietRestriction.NONE -> false
            DietRestriction.NO_SEAFOOD -> isSeafood
            DietRestriction.NO_RED_MEAT -> isRed
            DietRestriction.VEGETARIAN -> isMeat || isSeafood
        }
    }
}

/**
 * Loại món có nguyên liệu bắt buộc bị kiêng. Một ô còn ít nhất một lựa chọn hợp lệ thì vẫn giữ món.
 * Nguyên liệu tùy chọn không làm món bị loại. Giữ nguyên thứ tự.
 */
fun filterRecipesByDiet(
    recipes: List<Recipe>,
    restriction: DietRestriction,
    ingredients: List<Ingredient> = emptyList(),
): List<Recipe> {
    if (restriction == DietRestriction.NONE) return recipes
    val groups = ingredients.associate { it.id to it.group }
    return recipes.filter { recipe ->
        recipe.required.all { slot -> slot.anyOf.any { !DietIngredients.isBanned(it, restriction, groups) } }
    }
}

/** True khi chế độ ăn làm một danh sách có món thành rỗng; giao diện nên báo nhẹ thay vì để trống. */
fun isDietFilterTooStrict(
    recipes: List<Recipe>,
    restriction: DietRestriction,
    ingredients: List<Ingredient> = emptyList(),
): Boolean = recipes.isNotEmpty() && filterRecipesByDiet(recipes, restriction, ingredients).isEmpty()
