package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TasteProfileTest {
    private fun recipe(
        id: String,
        title: String,
        vararg required: String,
        optional: List<String> = emptyList(),
    ) = Recipe(
        id = id,
        title = title,
        steps = listOf("Nấu đến khi chín"),
        required = required.map { Slot(listOf(it)) },
        optional = optional.map { Slot(listOf(it)) },
    )

    @Test
    fun profileIsEmptyOnlyWithoutAnyPreference() {
        assertTrue(FamilyTasteProfile().isEmpty)
        assertFalse(FamilyTasteProfile(flavors = setOf(TasteFlavor.LIGHT)).isEmpty)
        assertFalse(FamilyTasteProfile(avoidIngredientIds = setOf("tomato")).isEmpty)
        assertFalse(FamilyTasteProfile(hasYoungKids = true).isEmpty)
    }

    @Test
    fun inferDishStylesAcceptsVietnameseDiacriticsAndPlainText() {
        val dish = recipe("dish", "Cá kho tộ với rau xào", "ca", "rau")
        assertEquals(
            setOf(DishStyle.BRAISED, DishStyle.STIR_FRY),
            inferDishStyles(dish),
        )
        assertEquals(setOf(DishStyle.SOUP), inferDishStyles(recipe("soup", "Sup ga", "ga")))
        assertEquals(setOf(DishStyle.FRIED), inferDishStyles(recipe("fried", "Trứng ran", "trung")))
        assertEquals(setOf(DishStyle.STEAMED_BOILED), inferDishStyles(recipe("steam", "Rau hap", "rau")))
    }

    @Test
    fun inferFlavorsUsesTitleAndIngredientIds() {
        val dish = recipe("dish", "Rau luộc sốt", "ot", optional = listOf("tieu"))
        assertEquals(setOf(TasteFlavor.LIGHT, TasteFlavor.SAVORY, TasteFlavor.SPICY), inferFlavors(dish))
        assertTrue(TasteFlavor.SWEET in inferFlavors(recipe("sweet", "Cá chua ngọt", "ca")))
        assertTrue(TasteFlavor.SAVORY in inferFlavors(recipe("savory", "Đậu rang", "dau")))
    }

    @Test
    fun avoidedIngredientOnlyChecksRequiredSlots() {
        val optionalAllergen = recipe("optional", "Canh rau", "rau", optional = listOf("milk"))
        val requiredAllergen = recipe("required", "Canh rau", "milk")
        val profile = FamilyTasteProfile(avoidIngredientIds = setOf("milk"))

        assertFalse(isAvoidedByTaste(optionalAllergen, profile))
        assertTrue(isAvoidedByTaste(requiredAllergen, profile))
    }

    @Test
    fun youngKidsAvoidSpicyDishes() {
        val spicy = recipe("spicy", "Gà xào ớt", "chicken")
        val mild = recipe("mild", "Gà hấp", "chicken")

        assertTrue(isAvoidedByTaste(spicy, FamilyTasteProfile(hasYoungKids = true)))
        assertFalse(isAvoidedByTaste(mild, FamilyTasteProfile(hasYoungKids = true)))
        assertFalse(isAvoidedByTaste(spicy, FamilyTasteProfile()))
    }

    @Test
    fun affinityAddsStyleFlavorAndKidFriendlyPoints() {
        val soup = recipe("soup", "Canh rau", "rau")
        val profile = FamilyTasteProfile(
            flavors = setOf(TasteFlavor.LIGHT),
            dishStyles = setOf(DishStyle.SOUP),
            hasYoungKids = true,
        )

        assertEquals(6, tasteAffinityScore(soup, profile))
        assertEquals(Int.MIN_VALUE, tasteAffinityScore(recipe("hot", "Mì cay", "mi"), FamilyTasteProfile(hasYoungKids = true)))
    }

    @Test
    fun sortingPrefersTasteAndKeepsEqualScoresStable() {
        val plain = recipe("plain", "Cơm", "rice")
        val soup = recipe("soup", "Canh rau", "rau")
        val secondPlain = recipe("plain-2", "Cơm", "rice")
        val profile = FamilyTasteProfile(flavors = setOf(TasteFlavor.LIGHT), dishStyles = setOf(DishStyle.SOUP))

        assertEquals(listOf("soup", "plain", "plain-2"), sortRecipesByTaste(listOf(plain, soup, secondPlain), profile).map { it.id })
        assertEquals(listOf("plain", "soup", "plain-2"), sortRecipesByTaste(listOf(plain, soup, secondPlain), FamilyTasteProfile()).map { it.id })
    }

    @Test
    fun sortingRemovesAvoidedRecipesAndFallsBackWhenAllAreAvoided() {
        val safe = recipe("safe", "Canh rau", "rau")
        val unsafe = recipe("unsafe", "Canh rau", "peanut")
        val profile = FamilyTasteProfile(avoidIngredientIds = setOf("peanut"))

        assertEquals(listOf("safe"), sortRecipesByTaste(listOf(unsafe, safe), profile).map { it.id })
        assertEquals(
            listOf("unsafe", "unsafe-2"),
            sortRecipesByTaste(listOf(unsafe, recipe("unsafe-2", "Món peanut", "peanut")), profile).map { it.id },
        )
    }
}
