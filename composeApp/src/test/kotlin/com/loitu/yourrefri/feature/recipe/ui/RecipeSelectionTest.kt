package com.loitu.yourrefri.feature.recipe.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RecipeSelectionTest {
    private val recommendationIds = listOf("first", "second", "third")

    @Test
    fun validRequestedId_selectsTheMatchingRecommendation() {
        assertEquals("second", resolveInitialRecipeId("second", recommendationIds))
    }

    @Test
    fun validThirdRequestedId_selectsTheThirdRecommendation() {
        assertEquals("third", resolveInitialRecipeId("third", recommendationIds))
    }

    @Test
    fun noRequestedId_defaultsToFirstRecommendation() {
        assertEquals("first", resolveInitialRecipeId(null, recommendationIds))
    }

    @Test
    fun unknownRequestedId_fallsBackToFirstRecommendation() {
        assertEquals("first", resolveInitialRecipeId("unknown", recommendationIds))
    }

    @Test
    fun emptyRecommendations_returnNoSelection() {
        assertNull(resolveInitialRecipeId("unknown", emptyList()))
    }

    @Test
    fun homeCardRequest_preservesItsRecipeId() {
        assertEquals("third", recipeRequestForHomeCard("third"))
    }

    @Test
    fun homeCardRequestResolvesAndScrollsToTheTappedRecipe() {
        val requestedId = recipeRequestForHomeCard("third")
        val resolvedId = resolveInitialRecipeId(requestedId, recommendationIds)

        assertEquals("third", resolvedId)
        assertEquals(true, shouldBringRecipeIntoView(requestedId, resolvedId, "third"))
        assertEquals(false, shouldBringRecipeIntoView(requestedId, resolvedId, "first"))
    }

    @Test
    fun generalRecipesRequest_clearsAnyPriorTarget() {
        assertNull(recipeRequestForAllRecipes())
    }

    @Test
    fun homeRequest_clearsDurationButGenericRequestPreservesDefault() {
        assertEquals(true, shouldClearDurationForRequest("second"))
        assertEquals(false, shouldClearDurationForRequest(null))
    }

    @Test
    fun scrollTarget_isOnlyTheResolvedRequestedRecipe() {
        assertEquals(true, shouldBringRecipeIntoView("third", "third", "third"))
        assertEquals(false, shouldBringRecipeIntoView("third", "third", "second"))
        assertEquals(false, shouldBringRecipeIntoView("unknown", "first", "first"))
    }
}
