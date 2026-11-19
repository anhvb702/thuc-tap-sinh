package com.loitu.yourrefri.ui.components

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/** Mỗi nguyên liệu trong danh mục có ảnh món hoặc icon riêng; mỗi công thức gốc có ảnh. */
class FoodAssetCoverageTest {
    private val assets = File("src/main/assets")

    private fun idsIn(file: String): List<String> =
        Regex("\"id\"\\s*:\\s*\"([^\"]+)\"").findAll(File(assets, file).readText()).map { it.groupValues[1] }.toList()

    private fun hasAsset(folder: String, id: String): Boolean =
        listOf("webp", "jpg", "png").any { File(assets, "$folder/$id.$it").isFile }

    @Test
    fun everyShelfIngredientHasPhotoOrIcon() {
        val missing = idsIn("shelf_table.json").filter { !hasAsset("food", it) && !hasAsset("icons", it) }
        assertEquals(emptyList(), missing)
    }

    @Test
    fun everyRecipeInBundleHasPhoto() {
        val missing = idsIn("recipes.json").filter { !hasAsset("recipes", it) }
        assertEquals(emptyList(), missing)
    }
}
