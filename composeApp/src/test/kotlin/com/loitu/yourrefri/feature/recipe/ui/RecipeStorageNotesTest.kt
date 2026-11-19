package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecipeStorageNotesTest {
    private val today = LocalDate(2026, 11, 19)
    private val ingredients = listOf(
        Ingredient("thit-heo", "Thịt heo", "thit", 14),
        Ingredient("ca-hoi", "Cá hồi", "ca", 7),
        Ingredient("cai-xanh", "Cải xanh", "rau-cu", 5),
    )
    private val recipe = Recipe(
        id = "meal",
        title = "Món nhà mình",
        steps = listOf("Sơ chế"),
        required = listOf(Slot(listOf("thit-heo")), Slot(listOf("cai-xanh"))),
        optional = listOf(Slot(listOf("ca-hoi")), Slot(listOf("thit-heo"))),
    )

    @Test
    fun ingredientZoneLabelSeparatesFreezerAndFridge() {
        assertEquals("Ngăn đông · Cần rã đông trước", ingredientZoneLabel("thit-heo"))
        assertEquals("Ngăn mát", ingredientZoneLabel("cai-xanh"))
    }

    @Test
    fun freezerIngredientNamesIncludesPresentRequiredAndOptionalNamesOnce() {
        val state = PantryGridState(
            today = today,
            ingredients = ingredients,
            pantry = mapOf(
                "thit-heo" to PantryItem("thit-heo", true, today),
                "ca-hoi" to PantryItem("ca-hoi", true, today),
                "cai-xanh" to PantryItem("cai-xanh", true, today),
            ),
        )

        assertEquals(listOf("Thịt heo", "Cá hồi"), freezerIngredientNames(recipe, state))
    }

    @Test
    fun defrostNoticeTextIsNullWhenNothingNeedsDefrosting() {
        assertNull(defrostNoticeText(emptyList()))
    }

    @Test
    fun defrostNoticeTextNamesOneOrTwoFreezerIngredientsWithoutBannedCopy() {
        val notice = defrostNoticeText(listOf("Thịt heo", "Cá hồi"))
        val oneIngredientNotice = defrostNoticeText(listOf("Thịt heo"))

        assertEquals(
            "Lưu ý rã đông: Thịt heo, Cá hồi đang ở ngăn đông — nên rã đông trước khi nấu nhé.",
            notice,
        )
        assertTrue(notice!!.contains("Thịt heo"))
        assertTrue(notice.contains("Cá hồi"))
        assertTrue(oneIngredientNotice!!.contains("Thịt heo"))
        listOf(
            "bạn" + " quên",
            "lãng" + " phí",
            "bỏ" + " lỡ",
            "hết" + " hạn",
            "stre" + "ak",
            "điểm" + " số",
        ).forEach { banned ->
            assertFalse(notice.contains(banned, ignoreCase = true), "Không được chứa từ cấm: $banned")
        }
    }
}
