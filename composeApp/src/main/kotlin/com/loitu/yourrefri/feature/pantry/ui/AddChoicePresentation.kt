package com.loitu.yourrefri.feature.pantry.ui

/** Hai lối thêm trong màn Tủ lạnh: nguyên liệu có sẵn, hoặc món khác ngoài danh mục. */
internal enum class AddChoice { ADD_INGREDIENT, ADD_OTHER }

internal fun addChoiceLabel(choice: AddChoice): String = when (choice) {
    AddChoice.ADD_INGREDIENT -> "Thêm nguyên liệu"
    AddChoice.ADD_OTHER -> "Khác / Thêm món"
}

/** Chặn tên rỗng hoặc trùng món đã có (không phân biệt hoa thường, bỏ khoảng trắng hai đầu). */
internal fun canAddOther(name: String, existingNames: Set<String>): Boolean {
    val key = name.trim().lowercase()
    if (key.isEmpty()) return false
    return existingNames.none { it.trim().lowercase() == key }
}
