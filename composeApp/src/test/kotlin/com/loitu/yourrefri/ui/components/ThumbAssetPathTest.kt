package com.loitu.yourrefri.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThumbAssetPathTest {
    @Test
    fun foodCandidatesTryWebpThenJpgThenPng() {
        assertEquals(
            listOf("food/ca-chua.webp", "food/ca-chua.jpg", "food/ca-chua.png"),
            foodAssetCandidates("ca-chua"),
        )
    }

    @Test
    fun recipeCandidatesUseTheRecipesFolder() {
        assertEquals(
            listOf("recipes/canh-chua-ca-loc.webp", "recipes/canh-chua-ca-loc.jpg", "recipes/canh-chua-ca-loc.png"),
            recipeAssetCandidates("canh-chua-ca-loc"),
        )
    }

    @Test
    fun unsafeIdsNeverEscapeTheAssetFolder() {
        listOf("../secret", "a/b", "a\\b", "").forEach { id ->
            assertTrue(foodAssetCandidates(id).isEmpty(), "food id '$id'")
            assertTrue(recipeAssetCandidates(id).isEmpty(), "recipe id '$id'")
        }
    }
}
