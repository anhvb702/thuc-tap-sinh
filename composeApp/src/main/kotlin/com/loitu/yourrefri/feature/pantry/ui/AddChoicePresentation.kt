package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.customFoodName

/** Hai lối thêm trong màn Tủ lạnh: nguyên liệu có sẵn, hoặc món khác ngoài danh mục. */
internal enum class AddChoice { ADD_INGREDIENT, ADD_OTHER }

internal fun addChoiceLabel(choice: AddChoice): String = when (choice) {
    AddChoice.ADD_INGREDIENT -> "Thêm nguyên liệu"
    AddChoice.ADD_OTHER -> "Khác / Thêm món"
}

/** Tìm không thấy rồi bấm "Thêm vào tủ": tên đã điền sẵn là món mới, đi đường "Khác". */
internal fun searchToAddChoice(): AddChoice = AddChoice.ADD_OTHER

/** Đường nguyên liệu không gợi ý món tự thêm, vì chọn rồi cũng không lưu được. */
internal fun ingredientOnlySuggestions(list: List<Ingredient>): List<Ingredient> =
    list.filter { customFoodName(it.id) == null }

internal fun ingredientOnlyConfirmActions(
    state: PantryGridState,
    selectedIds: Set<String>,
    query: String,
    days: Int?,
): List<PantryGridAction> = quickAddConfirmActions(state, selectedIds, query, days).filter {
    val id = when (it) {
        is PantryGridAction.AddIngredient -> it.ingredientId
        is PantryGridAction.IncreaseQuantity -> it.ingredientId
        else -> null
    }
    id == null || customFoodName(id) == null
}

private fun nameKey(name: String): String =
    name.trim().split(Regex("\\s+")).joinToString(" ").lowercase()

/** Chặn tên rỗng hoặc trùng món đã có (không phân biệt hoa thường, bỏ khoảng trắng thừa). */
internal fun canAddOther(name: String, existingNames: Set<String>): Boolean {
    val key = nameKey(name)
    if (key.isEmpty()) return false
    return existingNames.none { nameKey(it) == key }
}
