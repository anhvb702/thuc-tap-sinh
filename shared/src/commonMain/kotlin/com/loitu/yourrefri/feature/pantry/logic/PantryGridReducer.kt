package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.data.DEFAULT_PANTRY_UNIT
import com.loitu.yourrefri.feature.pantry.data.PANTRY_UNITS
import kotlinx.datetime.LocalDate

// Hàm thuần, không Compose, không Clock: "hôm nay" nằm trong state và chỉ đổi qua Loaded.
// ViewModel tiêm Clock/TimeZone rồi thực hiện các effect.

data class ShelfEditor(val ingredientId: String, val selectedDays: Int?)
data class UnitEditor(val ingredientId: String, val selectedUnit: String)

data class PantryGridState(
    val today: LocalDate,
    val hourOfDay: Int = 12,
    val ingredients: List<Ingredient> = emptyList(),
    // Khóa = ingredientId.
    val pantry: Map<String, PantryItem> = emptyMap(),
    val quantities: Map<String, Int> = emptyMap(),
    val editor: ShelfEditor? = null,
    // Giá trị pantry trước lần chạm đầu tiên của từng món trong phiên; null nghĩa là lúc đó chưa có dòng.
    // Cần để chạm nhầm hai lần trả lại đúng ngày cũ, không biến thành "hôm nay".
    val sessionBase: Map<String, PantryItem?> = emptyMap(),
    val unitOverrides: Map<String, String> = emptyMap(),
    val unitEditor: UnitEditor? = null,
)

sealed interface PantryGridAction {
    data class Loaded(
        val ingredients: List<Ingredient>,
        val pantry: List<PantryItem>,
        val today: LocalDate,
        val quantityOverrides: Map<String, Int> = emptyMap(),
        val unitOverrides: Map<String, String?> = emptyMap(),
        val hourOfDay: Int = 12,
    ) : PantryGridAction

    data class Tap(val ingredientId: String) : PantryGridAction
    // null keeps the catalog estimate (and any existing user override).
    data class AddIngredient(val ingredientId: String, val shelfDays: Int?) : PantryGridAction
    data class IncreaseQuantity(val ingredientId: String) : PantryGridAction
    data class DecreaseQuantity(val ingredientId: String) : PantryGridAction
    data class ConsumeIngredient(val ingredientId: String) : PantryGridAction
    data class OpenShelfEditor(val ingredientId: String) : PantryGridAction

    // null = "theo gợi ý" (xóa ghi đè của người dùng).
    data class PickShelfDays(val days: Int?) : PantryGridAction
    data object SaveShelfEditor : PantryGridAction
    data object CancelShelfEditor : PantryGridAction
    data class OpenUnitEditor(val ingredientId: String) : PantryGridAction
    data class PickUnit(val unit: String) : PantryGridAction
    data object SaveUnitEditor : PantryGridAction
    data object CancelUnitEditor : PantryGridAction
}

sealed interface PantryGridEffect {
    // markedAt đi kèm để repository ghi lại đúng ngày khi hoàn tác, không phải lúc nào cũng là hôm nay.
    data class SaveHave(val ingredientId: String, val have: Boolean, val markedAt: LocalDate) : PantryGridEffect
    data class DeleteHave(val ingredientId: String) : PantryGridEffect
    data class SaveQuantity(val ingredientId: String, val quantity: Int) : PantryGridEffect
    data class SaveShelfDays(val ingredientId: String, val days: Int?) : PantryGridEffect
    data class SaveUnit(val ingredientId: String, val unit: String?) : PantryGridEffect
}

data class PantryGridUpdate(
    val state: PantryGridState,
    val effects: List<PantryGridEffect> = emptyList(),
)

data class PantryCell(
    val ingredientId: String,
    val nameVi: String,
    val have: Boolean,
    val quantity: Int,
    val unit: String,
    // null khi have = false: món chưa có thì không có hạn để đếm.
    val daysLeft: Int?,
    val urgency: Urgency?,
)

// Cố định, không cho nhập tự do: đủ để chọn bằng một chạm và không tràn màn hình.
val SHELF_DAY_OPTIONS: List<Int> = listOf(1, 2, 3, 4, 5, 7, 10, 14, 21, 30)

fun reduce(state: PantryGridState, action: PantryGridAction): PantryGridUpdate = when (action) {
    is PantryGridAction.Loaded -> PantryGridUpdate(
        state.copy(
            ingredients = action.ingredients,
            pantry = action.pantry.associateBy { it.ingredientId },
            quantities = action.quantityOverrides.filterValues { it > 1 },
            unitOverrides = action.unitOverrides.filter { (id, unit) ->
                action.ingredients.any { it.id == id } && unit != null && unit in PANTRY_UNITS && unit != DEFAULT_PANTRY_UNIT
            }.mapValues { it.value!! },
            today = action.today,
            hourOfDay = action.hourOfDay,
        ),
    )
    is PantryGridAction.Tap -> tap(state, action.ingredientId)
    is PantryGridAction.AddIngredient -> addIngredient(state, action.ingredientId, action.shelfDays)
    is PantryGridAction.IncreaseQuantity -> changeQuantity(state, action.ingredientId, 1)
    is PantryGridAction.DecreaseQuantity -> decreaseQuantity(state, action.ingredientId)
    is PantryGridAction.ConsumeIngredient -> consumeIngredient(state, action.ingredientId)
    is PantryGridAction.OpenShelfEditor -> openEditor(state, action.ingredientId)
    is PantryGridAction.PickShelfDays -> pickDays(state, action.days)
    PantryGridAction.SaveShelfEditor -> saveEditor(state)
    PantryGridAction.CancelShelfEditor -> PantryGridUpdate(state.copy(editor = null))
    is PantryGridAction.OpenUnitEditor -> openUnitEditor(state, action.ingredientId)
    is PantryGridAction.PickUnit -> pickUnit(state, action.unit)
    PantryGridAction.SaveUnitEditor -> saveUnitEditor(state)
    PantryGridAction.CancelUnitEditor -> cancelUnitEditor(state)
}

private fun addIngredient(state: PantryGridState, id: String, days: Int?): PantryGridUpdate {
    if ((days != null && days !in SHELF_DAY_OPTIONS) || state.pantry[id]?.have == true) {
        return PantryGridUpdate(state)
    }
    val ingredients = if (state.ingredients.any { it.id == id }) {
        state.ingredients
    } else {
        val name = customFoodName(id) ?: return PantryGridUpdate(state)
        state.ingredients + Ingredient(id, name, CUSTOM_FOOD_GROUP, CUSTOM_FOOD_DEFAULT_SHELF_DAYS)
    }
    val added = state.copy(
        ingredients = ingredients,
        pantry = state.pantry + (id to PantryItem(id, true, state.today)),
        sessionBase = state.sessionBase - id,
    )
    if (days == null) {
        return PantryGridUpdate(
            added,
            listOf(PantryGridEffect.SaveHave(id, true, state.today)),
        )
    }
    val savedEstimate = saveEditor(
        pickDays(openEditor(added, id).state, days).state,
    )
    return savedEstimate.copy(
        effects = listOf(PantryGridEffect.SaveHave(id, true, state.today)) + savedEstimate.effects,
    )
}

private fun openUnitEditor(state: PantryGridState, id: String): PantryGridUpdate {
    if (state.ingredients.none { it.id == id }) return PantryGridUpdate(state)
    return PantryGridUpdate(state.copy(unitEditor = UnitEditor(id, state.unitOverrides[id] ?: DEFAULT_PANTRY_UNIT)))
}

private fun pickUnit(state: PantryGridState, unit: String): PantryGridUpdate {
    val editor = state.unitEditor ?: return PantryGridUpdate(state)
    if (unit !in PANTRY_UNITS) return PantryGridUpdate(state)
    return PantryGridUpdate(state.copy(unitEditor = editor.copy(selectedUnit = unit)))
}

private fun saveUnitEditor(state: PantryGridState): PantryGridUpdate {
    val editor = state.unitEditor ?: return PantryGridUpdate(state)
    val unit = editor.selectedUnit.takeUnless { it == DEFAULT_PANTRY_UNIT }
    val overrides = state.unitOverrides.toMutableMap().apply {
        if (unit == null) remove(editor.ingredientId) else put(editor.ingredientId, unit)
    }
    return PantryGridUpdate(state.copy(unitOverrides = overrides, unitEditor = null), listOf(PantryGridEffect.SaveUnit(editor.ingredientId, unit)))
}

private fun cancelUnitEditor(state: PantryGridState): PantryGridUpdate {
    if (state.unitEditor == null) return PantryGridUpdate(state)
    return PantryGridUpdate(state.copy(unitEditor = null))
}

private fun changeQuantity(state: PantryGridState, id: String, amount: Int): PantryGridUpdate {
    if (state.ingredients.none { it.id == id }) return PantryGridUpdate(state)
    val current = state.quantities[id] ?: 1
    if (amount > 0 && current > Int.MAX_VALUE - amount) return PantryGridUpdate(state)
    val quantity = current + amount
    val quantities = state.quantities.toMutableMap().apply {
        if (quantity == 1) remove(id) else put(id, quantity)
    }
    return PantryGridUpdate(
        state.copy(quantities = quantities),
        listOf(PantryGridEffect.SaveQuantity(id, quantity)),
    )
}

private fun decreaseQuantity(state: PantryGridState, id: String): PantryGridUpdate {
    if (state.ingredients.none { it.id == id }) return PantryGridUpdate(state)
    return if ((state.quantities[id] ?: 1) <= 1) {
        tap(state, id)
    } else {
        changeQuantity(state, id, -1)
    }
}

private fun consumeIngredient(state: PantryGridState, id: String): PantryGridUpdate {
    if (state.pantry[id]?.have != true) return PantryGridUpdate(state)
    return decreaseQuantity(state, id)
}

private fun tap(state: PantryGridState, id: String): PantryGridUpdate {
    if (state.ingredients.none { it.id == id }) return PantryGridUpdate(state)

    val current = state.pantry[id]
    val newHave = !(current?.have ?: false)
    val base = if (state.sessionBase.containsKey(id)) state.sessionBase[id] else current

    // Chạm lại về đúng trạng thái đầu phiên: khôi phục nguyên dòng cũ, kể cả ngày đánh dấu.
    if (newHave == (base?.have ?: false)) {
        val pantry = if (base == null) state.pantry - id else state.pantry + (id to base)
        return PantryGridUpdate(
            state.copy(pantry = pantry, sessionBase = state.sessionBase - id),
            listOfNotNull(
                if (base == null) PantryGridEffect.DeleteHave(id)
                else PantryGridEffect.SaveHave(id, newHave, base.markedAt),
            ),
        )
    }

    val sessionBase = if (state.sessionBase.containsKey(id)) state.sessionBase else state.sessionBase + (id to base)
    // Bật lại: ghi ngày hôm nay. Tắt: giữ ngày đánh dấu cũ để biết đồ đã mua từ bao giờ.
    val item = if (newHave) {
        PantryItem(ingredientId = id, have = true, markedAt = state.today)
    } else {
        checkNotNull(current).copy(have = false)
    }
    return PantryGridUpdate(
        state.copy(pantry = state.pantry + (id to item), sessionBase = sessionBase),
        listOf(PantryGridEffect.SaveHave(id, newHave, item.markedAt)),
    )
}

private fun openEditor(state: PantryGridState, id: String): PantryGridUpdate {
    // Mở với lựa chọn hiện tại của người dùng (null nếu chưa ghi đè), không phải defaultShelfDays.
    val ingredient = state.ingredients.firstOrNull { it.id == id } ?: return PantryGridUpdate(state)
    return PantryGridUpdate(state.copy(editor = ShelfEditor(ingredient.id, ingredient.userShelfDays)))
}

private fun pickDays(state: PantryGridState, days: Int?): PantryGridUpdate {
    val editor = state.editor ?: return PantryGridUpdate(state)
    if (days != null && days !in SHELF_DAY_OPTIONS) return PantryGridUpdate(state)
    return PantryGridUpdate(state.copy(editor = editor.copy(selectedDays = days)))
}

private fun saveEditor(state: PantryGridState): PantryGridUpdate {
    val editor = state.editor ?: return PantryGridUpdate(state)
    val ingredients = state.ingredients.map {
        if (it.id == editor.ingredientId) it.copy(userShelfDays = editor.selectedDays) else it
    }
    return PantryGridUpdate(
        state.copy(ingredients = ingredients, editor = null),
        listOf(PantryGridEffect.SaveShelfDays(editor.ingredientId, editor.selectedDays)),
    )
}

// Thứ tự ô theo đúng bảng nguyên liệu; không sắp xếp lại để người dùng nhớ vị trí.
fun cellsOf(state: PantryGridState): List<PantryCell> = state.ingredients.map { ingredient ->
    val item = state.pantry[ingredient.id]
    if (item != null && item.have) {
        val left = daysLeft(item, ingredient, state.today)
        PantryCell(
            ingredient.id,
            ingredient.nameVi,
            have = true,
            quantity = state.quantities[ingredient.id] ?: 1,
            unit = state.unitOverrides[ingredient.id] ?: DEFAULT_PANTRY_UNIT,
            daysLeft = left,
            urgency = urgencyOf(left),
        )
    } else {
        PantryCell(
            ingredient.id,
            ingredient.nameVi,
            have = false,
            quantity = state.quantities[ingredient.id] ?: 1,
            unit = state.unitOverrides[ingredient.id] ?: DEFAULT_PANTRY_UNIT,
            daysLeft = null,
            urgency = null,
        )
    }
}
