package com.loitu.yourrefri.ui.components

import com.loitu.yourrefri.feature.pantry.ui.FoodGlyph
import com.loitu.yourrefri.feature.pantry.ui.foodGlyphFor
import com.loitu.yourrefri.feature.pantry.ui.foodIconFor
import com.loitu.yourrefri.ui.theme.AppIcons
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class AssetCoverageTest {
    private val assets = listOf(File("src/main/assets"), File("composeApp/src/main/assets"))
        .first { it.isDirectory }

    private fun idsIn(file: String): List<String> =
        Regex("\\\"id\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
            .findAll(File(assets, file).readText())
            .map { it.groupValues[1] }
            .toList()

    private fun hasAsset(folder: String, id: String): Boolean =
        listOf("webp", "jpg", "png").any { File(assets, "$folder/$id.$it").isFile }

    @Test
    fun everyRecipeHasAnImageAsset() {
        val missing = idsIn("recipes.json").filterNot { hasAsset("recipes", it) }
        assertEquals(emptyList(), missing)
    }

    @Test
    fun everyShelfIngredientHasPhotoOrIconAsset() {
        val missing = idsIn("shelf_table.json").filterNot { hasAsset("food", it) || hasAsset("icons", it) }
        assertEquals(emptyList(), missing)
    }

    @Test
    fun customGroupsChooseSpiceAndBowlGlyphsAndIcons() {
        assertEquals(FoodGlyph.SPICE, foodGlyphFor("nuoc-mam", "gia-vi"))
        assertEquals(FoodGlyph.BOWL, foodGlyphFor("com-nguoi", "khac"))
        assertEquals(AppIcons.Spice, foodIconFor("nuoc-mam", "gia-vi"))
        assertEquals(AppIcons.Bowl, foodIconFor("com-nguoi", "khac"))
        assertEquals(FoodGlyph.TOMATO, foodGlyphFor("ca-chua", "gia-vi"))
    }
}
