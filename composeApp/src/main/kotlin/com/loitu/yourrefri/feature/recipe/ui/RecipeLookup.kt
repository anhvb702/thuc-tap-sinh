package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.data.Recipe

/**
 * Tìm món theo id: ưu tiên danh sách đã lọc chế độ ăn, không có thì tìm trong danh sách đầy đủ.
 * Cần vì "Nấu ngẫu nhiên" và "Nấu lại" có thể mở món nằm ngoài chế độ ăn; mọi màn mở món phải tìm cùng một cách.
 */
internal fun findRecipeById(id: String?, visible: List<Recipe>, all: List<Recipe>): Recipe? {
    if (id == null) return null
    return visible.firstOrNull { it.id == id } ?: all.firstOrNull { it.id == id }
}
