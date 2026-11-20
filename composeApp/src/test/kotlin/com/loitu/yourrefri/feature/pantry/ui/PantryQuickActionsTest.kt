package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PantryQuickActionsTest {
    private val today = LocalDate(2026, 11, 19)
    private val state = PantryGridState(
        today = today,
        ingredients = listOf(Ingredient("thit-heo", "Thịt heo", "thit", 3), Ingredient("trung-ga", "Trứng gà", "sua-trung", 7)),
        pantry = mapOf("thit-heo" to PantryItem("thit-heo", true, today)),
    )

    @Test fun combo_increasesPresent_addsMissing_skipsUnknown() {
        val combo = GroceryCombo("x", "X", "X", listOf("thit-heo", "trung-ga", "khong-co"))
        assertEquals(
            listOf(PantryGridAction.IncreaseQuantity("thit-heo"), PantryGridAction.AddIngredient("trung-ga", null)),
            comboActionsFor(combo, state),
        )
    }

    @Test fun clearAll_marksEveryPresentItem_once() {
        val all = state.copy(
            ingredients = state.ingredients + Ingredient("tom", "Tôm", "hai-san", 2),
            pantry = state.pantry + ("tom" to PantryItem("tom", true, today)) +
                ("trung-ga" to PantryItem("trung-ga", false, today)),
        )
        assertEquals(
            listOf(PantryGridAction.Tap("thit-heo"), PantryGridAction.Tap("tom")),
            clearAllPantryActions(all),
        )
    }

    @Test fun copy_hasNoProductBannedWords() {
        val copy = (GROCERY_COMBOS.flatMap { listOf(it.titleVi, it.subtitleVi) } + LEFTOVER_PRESETS + "Đã dọn món khỏi tủ" + "Hoàn tác").joinToString(" ").lowercase()
        listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", "điểm số", "streak").forEach { assertFalse(copy.contains(it), it) } // lint-copy: rule
        assertTrue(copy.contains("hoàn tác"))
    }
}
