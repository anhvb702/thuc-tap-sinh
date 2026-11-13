package com.loitu.yourrefri.feature.pantry.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class FoodGlyphMappingTest {
    @Test
    fun knownIngredientsMapToTheirGroup() {
        assertEquals(FoodGlyph.MEAT, foodGlyphFor("thit-heo"))
        assertEquals(FoodGlyph.MEAT, foodGlyphFor("thit-ga"))
        assertEquals(FoodGlyph.FISH, foodGlyphFor("ca-basa"))
        assertEquals(FoodGlyph.FISH, foodGlyphFor("tom"))
        assertEquals(FoodGlyph.TOMATO, foodGlyphFor("ca-chua"))
        assertEquals(FoodGlyph.EGG, foodGlyphFor("trung-ga"))
        assertEquals(FoodGlyph.MILK, foodGlyphFor("sua-tuoi"))
        assertEquals(FoodGlyph.TOFU, foodGlyphFor("dau-phu"))
        assertEquals(FoodGlyph.LEAF, foodGlyphFor("rau-muong"))
    }

    @Test
    fun unknownIngredientFallsBackToLeaf() {
        assertEquals(FoodGlyph.LEAF, foodGlyphFor("mon-la-chua-co"))
    }

    @Test
    fun emojiMappingStaysAvailableForExistingContract() {
        assertEquals("🍅", foodEmojiFor("ca-chua"))
    }
}
