package com.loitu.yourrefri.feature.settings.ui

import com.loitu.yourrefri.feature.recipe.logic.DishStyle
import com.loitu.yourrefri.feature.recipe.logic.FamilyTasteProfile
import com.loitu.yourrefri.feature.recipe.logic.TasteFlavor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsTasteSectionTest {
    @Test
    fun toggleFlavor_addsThenRemoves() {
        val added = toggleFlavor(FamilyTasteProfile(), TasteFlavor.LIGHT)
        assertEquals(setOf(TasteFlavor.LIGHT), added.flavors)
        assertEquals(FamilyTasteProfile(), toggleFlavor(added, TasteFlavor.LIGHT))
    }

    @Test
    fun toggleDishStyle_addsThenRemoves() {
        val added = toggleDishStyle(FamilyTasteProfile(), DishStyle.SOUP)
        assertEquals(setOf(DishStyle.SOUP), added.dishStyles)
        assertEquals(FamilyTasteProfile(), toggleDishStyle(added, DishStyle.SOUP))
    }

    @Test
    fun toggleAvoidIngredient_addsThenRemoves_andIgnoresBlank() {
        val added = toggleAvoidIngredient(FamilyTasteProfile(), "tom")
        assertEquals(setOf("tom"), added.avoidIngredientIds)
        assertEquals(FamilyTasteProfile(), toggleAvoidIngredient(added, "tom"))
        assertEquals(FamilyTasteProfile(), toggleAvoidIngredient(FamilyTasteProfile(), " "))
    }

    @Test
    fun summary_emptyExplainsDefault() {
        assertEquals("Chưa chọn — gợi ý cân bằng mọi món nhà", tasteProfileSummaryVi(FamilyTasteProfile()))
    }

    @Test
    fun summary_withDataIncludesTasteStyleIngredientAndKids() {
        val summary = tasteProfileSummaryVi(
            FamilyTasteProfile(
                flavors = setOf(TasteFlavor.LIGHT),
                dishStyles = setOf(DishStyle.SOUP),
                avoidIngredientIds = setOf("tom"),
                hasYoungKids = true,
            ),
        )
        assertTrue(summary.contains("thanh đạm"))
        assertTrue(summary.contains("Món canh"))
        assertTrue(summary.contains("Không ăn Tôm"))
        assertTrue(summary.contains("Nhà có trẻ nhỏ"))
    }

    @Test
    fun summary_hasNoBannedCopy() {
        val copy = tasteProfileSummaryVi(FamilyTasteProfile(TasteFlavor.entries.toSet(), hasYoungKids = true))
        val banned = listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", "điểm số", "streak") // lint-copy: rule
        banned.forEach { assertFalse(copy.contains(it, ignoreCase = true), copy) }
    }
}
