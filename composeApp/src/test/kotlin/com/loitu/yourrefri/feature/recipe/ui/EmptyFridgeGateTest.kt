package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class EmptyFridgeGateTest {
    private val today = LocalDate(2026, 11, 19)

    @Test
    fun emptyPantryIsEmpty() {
        assertTrue(isPantryEmpty(PantryGridState(today = today)))
    }

    @Test
    fun pantryWithOnlyMissingItemsIsEmpty() {
        val state = PantryGridState(today = today, pantry = mapOf("egg" to PantryItem("egg", false, today)))
        assertTrue(isPantryEmpty(state))
    }

    @Test
    fun pantryWithOnePresentItemIsNotEmpty() {
        val state = PantryGridState(today = today, pantry = mapOf("egg" to PantryItem("egg", true, today)))
        assertFalse(isPantryEmpty(state))
    }

    @Test
    fun gateCopyAvoidsForbiddenWords() {
        val copy = listOf(EMPTY_FRIDGE_TITLE, EMPTY_FRIDGE_SUBTITLE, EMPTY_FRIDGE_SHOP_ACTION, EMPTY_FRIDGE_HOME_ACTION)
        listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên", "điểm", "streak").forEach { word -> // lint-copy: rule
            copy.forEach { text -> assertFalse(text.contains(word, ignoreCase = true), "$text chứa từ cấm: $word") }
        }
    }
}
