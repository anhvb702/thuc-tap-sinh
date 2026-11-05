package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import kotlinx.datetime.LocalDate

// Hàm thuần, không Compose, không Clock: "hôm nay" nằm trong state và chỉ đổi qua Loaded.
// ViewModel tiêm Clock/TimeZone rồi thực hiện các effect.

data class ShelfEditor(val ingredientId: String, val selectedDays: Int?)

data class PantryGridState(
    val today: LocalDate,
    val ingredients: List<Ingredient> = emptyList(),
    // Khóa = ingredientId.
    val pantry: Map<String, PantryItem> = emptyMap(),
    val editor: ShelfEditor? = null,
    // Giá trị pantry trước lần chạm đầu tiên của từng món trong phiên; null nghĩa là lúc đó chưa có dòng.
    // Cần để chạm nhầm hai lần trả lại đúng ngày cũ, không biến thành "hôm nay".
    val sessionBase: Map<String, PantryItem?> = emptyMap(),
)

sealed interface PantryGridAction {
    data class Loaded(
        val ingredients: List<Ingredient>,
        val pantry: List<PantryItem>,
        val today: LocalDate,
    ) : PantryGridAction

    data class Tap(val ingredientId: String) : PantryGridAction
    data class OpenShelfEditor(val ingredientId: String) : PantryGridAction

    // null = "theo gợi ý" (xóa ghi đè của người dùng).
    data class PickShelfDays(val days: Int?) : PantryGridAction
    data object SaveShelfEditor : PantryGridAction
    data object CancelShelfEditor : PantryGridAction
}

sealed interface PantryGridEffect {
    // markedAt đi kèm để repository ghi lại đúng ngày khi hoàn tác, không phải lúc nào cũng là hôm nay.
    data class SaveHave(val ingredientId: String, val have: Boolean, val markedAt: LocalDate) : PantryGridEffect
    data class SaveShelfDays(val ingredientId: String, val days: Int?) : PantryGridEffect
}

data class PantryGridUpdate(
    val state: PantryGridState,
    val effects: List<PantryGridEffect> = emptyList(),
)

data class PantryCell(
    val ingredientId: String,
    val nameVi: String,
    val have: Boolean,
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
            today = action.today,
        ),
    )
    is PantryGridAction.Tap -> tap(state, action.ingredientId)
    is PantryGridAction.OpenShelfEditor -> openEditor(state, action.ingredientId)
    is PantryGridAction.PickShelfDays -> pickDays(state, action.days)
    PantryGridAction.SaveShelfEditor -> saveEditor(state)
    PantryGridAction.CancelShelfEditor -> PantryGridUpdate(state.copy(editor = null))
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
            listOf(PantryGridEffect.SaveHave(id, newHave, base?.markedAt ?: state.today)),
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
        PantryCell(ingredient.id, ingredient.nameVi, have = true, daysLeft = left, urgency = urgencyOf(left))
    } else {
        PantryCell(ingredient.id, ingredient.nameVi, have = false, daysLeft = null, urgency = null)
    }
}
