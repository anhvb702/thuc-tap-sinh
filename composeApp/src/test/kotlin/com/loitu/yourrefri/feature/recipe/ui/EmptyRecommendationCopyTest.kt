package com.loitu.yourrefri.feature.recipe.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class EmptyRecommendationCopyTest {
    @Test
    fun emptyMessage_isWarmAndOffersWayOut() {
        assertEquals(
            "Mình chưa có công thức để gợi ý lúc này. Bạn vẫn xem được đồ trong tủ và hạn ước tính nhé.",
            emptyRecommendationCopy.message,
        )
    }

    @Test
    fun emptyCopy_hasNoBannedWords() {
        val copy = "${emptyRecommendationCopy.message} ${emptyRecommendationCopy.actionLabel}"
        listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn").forEach {
            assertFalse(copy.contains(it, ignoreCase = true), "Unexpected phrase: $it")
        }
    }

    @Test
    fun actionLabel_namesPantry() {
        assertEquals("Xem tủ lạnh", emptyRecommendationCopy.actionLabel)
    }

    @Test
    fun screensNoLongerUseOldSentence() {
        val sourceRoot = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
            .map { File(it, "composeApp/src/main/kotlin/com/loitu/yourrefri/feature") }
            .first { it.isDirectory }
        listOf("home/ui/HomeScreen.kt", "recipe/ui/RecipeScreen.kt").forEach { path ->
            assertFalse(File(sourceRoot, path).readText().contains("Chưa có công thức để gợi ý."), path)
        }
    }
}
