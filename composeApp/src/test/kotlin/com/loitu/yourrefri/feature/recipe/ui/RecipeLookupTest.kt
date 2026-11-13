package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class RecipeLookupTest {
    private fun recipe(id: String) = Recipe(id, id, listOf("nấu"), listOf(Slot(listOf("trung"))))

    private val inDiet = recipe("rau-xao")
    private val outOfDiet = recipe("thit-kho")

    @Test
    fun prefersTheFilteredList() {
        val duplicate = recipe("rau-xao")
        assertSame(inDiet, findRecipeById("rau-xao", visible = listOf(inDiet), all = listOf(duplicate, inDiet)))
    }

    @Test
    fun fallsBackToUnfilteredListForRecipesOutsideTheDiet() {
        assertEquals("thit-kho", findRecipeById("thit-kho", visible = listOf(inDiet), all = listOf(inDiet, outOfDiet))?.id)
    }

    @Test
    fun unknownIdReturnsNull() {
        assertNull(findRecipeById("khong-co", visible = listOf(inDiet), all = listOf(inDiet, outOfDiet)))
    }

    @Test
    fun nullIdReturnsNull() {
        assertNull(findRecipeById(null, visible = listOf(inDiet), all = listOf(inDiet)))
    }
}
