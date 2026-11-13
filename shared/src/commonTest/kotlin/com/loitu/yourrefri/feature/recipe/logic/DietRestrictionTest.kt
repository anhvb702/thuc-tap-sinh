package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DietRestrictionTest {
    private fun recipe(id: String, vararg slots: List<String>, optional: List<String> = emptyList()) = Recipe(
        id = id, title = id, steps = listOf("Nấu."),
        required = slots.map { Slot(it) },
        optional = optional.map { Slot(listOf(it)) },
    )

    private val pork = recipe("pork", listOf("thit-heo"), listOf("hanh-la"))
    private val beef = recipe("beef", listOf("thit-bo"))
    private val chicken = recipe("chicken", listOf("thit-ga"))
    private val fish = recipe("fish", listOf("ca-basa"))
    private val shrimp = recipe("shrimp", listOf("tom"))
    private val egg = recipe("egg", listOf("trung-ga"), listOf("hanh-la"))
    private val tofu = recipe("tofu", listOf("dau-phu"), listOf("nam"))
    private val porkOrTofu = recipe("porkOrTofu", listOf("thit-heo", "dau-phu"))
    private val all = listOf(pork, beef, chicken, fish, shrimp, egg, tofu, porkOrTofu)

    private fun ids(r: DietRestriction, list: List<Recipe> = all) =
        filterRecipesByDiet(list, r).map { it.id }

    @Test
    fun none_keepsEverythingInOrder() = assertEquals(all.map { it.id }, ids(DietRestriction.NONE))

    @Test
    fun noSeafood_dropsFishAndShrimpOnly() =
        assertEquals(listOf("pork", "beef", "chicken", "egg", "tofu", "porkOrTofu"), ids(DietRestriction.NO_SEAFOOD))

    @Test
    fun noRedMeat_dropsPorkAndBeefButKeepsChickenAndFish() =
        assertEquals(listOf("chicken", "fish", "shrimp", "egg", "tofu", "porkOrTofu"), ids(DietRestriction.NO_RED_MEAT))

    @Test
    fun vegetarian_keepsEggDairyTofuOnly() =
        assertEquals(listOf("egg", "tofu", "porkOrTofu"), ids(DietRestriction.VEGETARIAN))

    @Test
    fun slotWithAllowedAlternative_isKept() =
        assertTrue(porkOrTofu in filterRecipesByDiet(all, DietRestriction.VEGETARIAN))

    @Test
    fun optionalIngredientsDoNotExcludeRecipe() {
        val withOptionalShrimp = recipe("opt", listOf("trung-ga"), optional = listOf("tom"))
        assertEquals(listOf("opt"), ids(DietRestriction.VEGETARIAN, listOf(withOptionalShrimp)))
    }

    @Test
    fun customIngredientUsesGroupFromIngredients() {
        val custom = recipe("custom", listOf("mon-tu-them"))
        val ingredients = listOf(Ingredient("mon-tu-them", "Xúc xích", "thit", 5))
        assertTrue(filterRecipesByDiet(listOf(custom), DietRestriction.VEGETARIAN, ingredients).isEmpty())
        assertEquals(1, filterRecipesByDiet(listOf(custom), DietRestriction.VEGETARIAN).size)
    }

    @Test
    fun customMeatGroupIsRedMeat_customFishGroupIsSeafood_knownChickenStaysAllowed() {
        val sausage = recipe("sausage", listOf("xuc-xich"))
        val squid = recipe("squid", listOf("muc"))
        val ingredients = listOf(
            Ingredient("xuc-xich", "Xúc xích", "thit", 5),
            Ingredient("muc", "Mực", "ca", 2),
            Ingredient("thit-ga", "Thịt gà", "thit", 2),
        )
        assertTrue(filterRecipesByDiet(listOf(sausage), DietRestriction.NO_RED_MEAT, ingredients).isEmpty())
        assertEquals(1, filterRecipesByDiet(listOf(squid), DietRestriction.NO_RED_MEAT, ingredients).size)
        assertTrue(filterRecipesByDiet(listOf(squid), DietRestriction.NO_SEAFOOD, ingredients).isEmpty())
        assertEquals(1, filterRecipesByDiet(listOf(chicken), DietRestriction.NO_RED_MEAT, ingredients).size)
    }

    @Test
    fun everyShelfTableMeatOrFishIdIsClassified() {
        listOf("thit-heo", "thit-heo-xay", "thit-bo", "thit-ga", "ca-basa", "ca-hoi", "tom").forEach {
            assertTrue(DietIngredients.meat.contains(it) || DietIngredients.seafood.contains(it), it)
        }
        assertTrue(DietIngredients.redMeat.containsAll(listOf("thit-heo", "thit-heo-xay", "thit-bo")))
        assertFalse(DietIngredients.redMeat.contains("thit-ga"))
    }

    @Test
    fun tooStrict_onlyWhenFilterEmptiesNonEmptyList() {
        val meatOnly = listOf(pork, beef)
        assertTrue(isDietFilterTooStrict(meatOnly, DietRestriction.VEGETARIAN))
        assertFalse(isDietFilterTooStrict(meatOnly, DietRestriction.NONE))
        assertFalse(isDietFilterTooStrict(all, DietRestriction.VEGETARIAN))
        assertFalse(isDietFilterTooStrict(emptyList(), DietRestriction.VEGETARIAN))
    }

    @Test
    fun storage_roundTripAndDefault() {
        DietRestriction.entries.forEach { assertEquals(it, DietRestriction.fromStorage(it.storageValue)) }
        assertEquals(DietRestriction.NONE, DietRestriction.fromStorage(null))
        assertEquals(DietRestriction.NONE, DietRestriction.fromStorage("la-hoac"))
        assertEquals(DietRestriction.entries.size, DietRestriction.entries.map { it.storageValue }.toSet().size)
    }

    @Test
    fun labels_matchAgreedCopy() {
        assertEquals("Không kiêng", DietRestriction.NONE.labelVi)
        assertEquals("Không hải sản", DietRestriction.NO_SEAFOOD.labelVi)
        assertEquals("Không thịt đỏ", DietRestriction.NO_RED_MEAT.labelVi)
        assertEquals("Ăn chay", DietRestriction.VEGETARIAN.labelVi)
    }
}
