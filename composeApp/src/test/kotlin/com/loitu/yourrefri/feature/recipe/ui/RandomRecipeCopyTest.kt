package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RandomRecipeCopyTest {
    private val banned = listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên")
    private val pork = Recipe("thit-kho", "Thịt kho", listOf("kho"), listOf(Slot(listOf("thit-heo"))))
    private val veg = Recipe("rau-xao", "Rau xào", listOf("xào"), listOf(Slot(listOf("rau-muong"))))
    private val ingredients = listOf(
        Ingredient("thit-heo", "Thịt heo", "thit", 3),
        Ingredient("rau-muong", "Rau muống", "rau-la", 2),
    )

    @Test
    fun outsideDiet_trueWhenRecipeIsFilteredOut() {
        assertTrue(isOutsideDiet(pork, DietRestriction.VEGETARIAN, ingredients))
    }

    @Test
    fun outsideDiet_falseWhenRecipeFitsOrNoRestriction() {
        assertFalse(isOutsideDiet(veg, DietRestriction.VEGETARIAN, ingredients))
        assertFalse(isOutsideDiet(pork, DietRestriction.NONE, ingredients))
    }

    @Test
    fun copyIsExactAndAvoidsBannedWords() {
        assertEquals("Nấu ngẫu nhiên", RandomRecipeCopy.button)
        assertEquals("Món này cần thêm vài thứ nữa nè", RandomRecipeCopy.needMore)
        assertEquals("Món này ngoài chế độ ăn của bạn", RandomRecipeCopy.outsideDiet)
        assertEquals("Thử món khác", RandomRecipeCopy.tryAnother)
        assertEquals("Chưa có món khác để thử", RandomRecipeCopy.noOther)
        assertEquals("Quay lại", RandomRecipeCopy.back)
        assertEquals("Xem món này", RandomRecipeCopy.open)
        listOf(
            RandomRecipeCopy.button, RandomRecipeCopy.needMore, RandomRecipeCopy.outsideDiet,
            RandomRecipeCopy.tryAnother, RandomRecipeCopy.noOther, RandomRecipeCopy.back, RandomRecipeCopy.open,
        ).forEach { text -> banned.forEach { w -> assertFalse(text.contains(w, ignoreCase = true), "$text chứa $w") } }
    }
}
