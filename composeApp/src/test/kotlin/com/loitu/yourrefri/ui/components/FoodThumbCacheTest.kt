package com.loitu.yourrefri.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class FoodThumbCacheTest {
    @Test
    fun computeInSampleSize_downsamplesLargeSquareImageToPowerOfTwo() {
        assertEquals(8, computeInSampleSize(1024, 1024, 128))
        assertEquals(4, computeInSampleSize(1024, 1024, 200))
    }

    @Test
    fun computeInSampleSize_keepsSampleOneForSmallOrInvalidInputs() {
        assertEquals(1, computeInSampleSize(64, 64, 128))
        assertEquals(1, computeInSampleSize(0, 1024, 128))
        assertEquals(1, computeInSampleSize(1024, -1, 128))
        assertEquals(1, computeInSampleSize(1024, 1024, 0))
        assertEquals(1, computeInSampleSize(1024, 1024, -1))
    }

    @Test
    fun computeInSampleSize_neverUndershootsRequestedSize() {
        assertEquals(2, computeInSampleSize(500, 500, 200))
        assertEquals(2, computeInSampleSize(800, 500, 200))
        assertEquals(2, computeInSampleSize(500, 800, 200))
    }

    @Test
    fun cacheKeyFor_bucketsNearbyPixelSizesTogether() {
        val candidates = foodAssetCandidates("ca-chua")
        assertEquals(cacheKeyFor(candidates, 120), cacheKeyFor(candidates.take(1), 128))
        assertEquals(cacheKeyFor(candidates, 0), cacheKeyFor(candidates, 32))
        assertNotEquals(cacheKeyFor(candidates, 128), cacheKeyFor(candidates, 129))
    }

    @Test
    fun cacheKeyFor_separatesDifferentAssets() {
        assertNotEquals(cacheKeyFor(foodAssetCandidates("ca-chua"), 128), cacheKeyFor(foodAssetCandidates("trung"), 128))
        assertNotEquals(cacheKeyFor(foodAssetCandidates("ca-chua"), 128), cacheKeyFor(iconAssetCandidates("ca-chua"), 128))
    }

    @Test
    fun assetCandidates_rejectsPathTraversalAndBlankIds() {
        for (id in listOf("../secret", "a/b", "a\\b", "a..b", "", " \t")) {
            assertTrue(foodAssetCandidates(id).isEmpty(), "food id '$id'")
            assertTrue(recipeAssetCandidates(id).isEmpty(), "recipe id '$id'")
            assertTrue(iconAssetCandidates(id).isEmpty(), "icon id '$id'")
        }
    }
}
