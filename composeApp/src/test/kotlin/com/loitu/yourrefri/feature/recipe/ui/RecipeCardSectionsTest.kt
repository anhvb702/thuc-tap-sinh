package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.RecipeRecommendation
import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import kotlin.test.Test
import kotlin.test.assertEquals

class RecipeCardSectionsTest {
    private fun rec(id: String, reason: RecommendationReason) = RecipeRecommendation(
        recipe = Recipe(id = id, title = id, steps = listOf("bước 1"), required = listOf(com.loitu.yourrefri.feature.recipe.data.Slot(listOf("egg"))), minutes = 10),
        reason = reason,
    )

    @Test
    fun useSoonGetsSectionOne() {
        assertEquals("01 · DÙNG ĐỒ NÊN DÙNG TRƯỚC", recipeCardSections(listOf(rec("a", RecommendationReason.USE_SOON)))[0].first)
    }

    @Test
    fun readyGetsSectionTwoAndQuickGetsSectionThree() {
        val sections = recipeCardSections(listOf(rec("b", RecommendationReason.READY), rec("c", RecommendationReason.QUICK)))
        assertEquals("02 · NẤU ĐƯỢC NGAY", sections[0].first)
        assertEquals("03 · MỘT MÓN NHANH", sections[1].first)
    }

    @Test
    fun keepsOrderWithinEachSection() {
        val sections = recipeCardSections(listOf(rec("x", RecommendationReason.USE_SOON), rec("y", RecommendationReason.USE_SOON)))
        assertEquals(listOf("x", "y"), sections.map { it.second.recipe.id })
    }
}
