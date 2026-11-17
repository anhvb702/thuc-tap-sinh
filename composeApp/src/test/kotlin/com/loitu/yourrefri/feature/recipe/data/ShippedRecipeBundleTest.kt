package com.loitu.yourrefri.feature.recipe.data

import com.loitu.yourrefri.feature.pantry.data.ShelfTable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShippedRecipeBundleTest {
    private val originalRecipeIds = setOf(
        "canh-chua-ca-basa",
        "thit-kho-trung",
        "rau-muong-xao-hanh",
    )

    private val newRecipeContracts = mapOf(
        "thit-bo-xao-hanh-tay" to RecipeContract(
            title = "Thịt bò xào hành tây",
            minutes = 15,
            required = setOf(setOf("thit-bo"), setOf("hanh-tay")),
            optional = setOf(setOf("ca-rot")),
        ),
        "canh-rau-ngot-thit-bam" to RecipeContract(
            title = "Canh rau ngót nấu thịt",
            minutes = 15,
            required = setOf(setOf("rau-ngot"), setOf("thit-heo-xay", "thit-heo")),
            optional = emptySet(),
        ),
        "dau-phu-sot-ca-chua" to RecipeContract(
            title = "Đậu phụ sốt cà chua",
            minutes = 15,
            required = setOf(setOf("dau-phu"), setOf("ca-chua"), setOf("hanh-la")),
            optional = emptySet(),
        ),
        "tom-rim-thit" to RecipeContract(
            title = "Tôm rim mặn ngọt",
            minutes = 20,
            required = setOf(setOf("tom"), setOf("hanh-la")),
            optional = setOf(setOf("thit-heo")),
        ),
        "ga-kho-khoai-tay" to RecipeContract(
            title = "Thịt gà kho khoai tây cà rốt",
            minutes = 30,
            required = setOf(setOf("thit-ga"), setOf("khoai-tay", "ca-rot")),
            optional = emptySet(),
        ),
        "canh-bi-xanh-nau-tom" to RecipeContract(
            title = "Canh bí xanh nấu tôm nấm",
            minutes = 20,
            required = setOf(setOf("bi-xanh"), setOf("tom", "thit-heo-xay")),
            optional = setOf(setOf("nam"), setOf("hanh-la")),
        ),
        "ca-hoi-ap-chao-xa-lach" to RecipeContract(
            title = "Cá hồi áp chảo trộn xà lách",
            minutes = 15,
            required = setOf(setOf("ca-hoi"), setOf("xa-lach", "cai-ngot")),
            optional = setOf(setOf("ca-chua")),
        ),
    )

    private val cookingIngredientIds = setOf(
        "thit-heo", "thit-heo-xay", "thit-bo", "thit-ga",
        "ca-basa", "ca-hoi", "tom",
        "rau-muong", "cai-ngot", "xa-lach", "rau-ngot", "hanh-la",
        "ca-rot", "khoai-tay", "ca-chua", "bi-xanh", "hanh-tay",
        "trung-ga", "dau-phu", "nam",
    )

    @Test
    fun shippedBundleContainsOriginalRecipesAndSpecifiedRecipes() {
        val recipes = shippedRecipes()

        assertTrue(recipes.size in 25..30, "Expected 25..30 family recipes, got ${recipes.size}")
        assertEquals(recipes.size, recipes.map { it.id }.toSet().size)
        assertTrue(recipes.map { it.id }.toSet().containsAll(originalRecipeIds + newRecipeContracts.keys))
    }

    @Test
    fun everyShippedRecipeHasFourNonBlankStepsAndPositiveDuration() {
        shippedRecipes().forEach { recipe ->
            assertTrue(recipe.id.isNotBlank(), "${recipe.id} must have a nonblank id")
            assertTrue(recipe.title.isNotBlank(), "${recipe.id} must have a nonblank title")
            assertEquals(4, recipe.steps.size, "${recipe.id} must have four steps")
            assertTrue(recipe.steps.all(String::isNotBlank), "${recipe.id} has a blank step")
            assertTrue(recipe.required.isNotEmpty(), "${recipe.id} must have required ingredients")
            assertTrue((recipe.minutes ?: 0) in 1..1440, "${recipe.id} must have valid minutes")
        }
    }

    @Test
    fun shippedBundleCoversBothQuickAndMediumDinnerDurations() {
        val minutes = shippedRecipes().mapNotNull { it.minutes }

        assertTrue(minutes.any { it <= 15 }, "Bundle needs a recipe at or under 15 minutes")
        assertTrue(minutes.any { it in 16..30 }, "Bundle needs a recipe from 16 to 30 minutes")
    }

    @Test
    fun shippedBundleHasMeatSeafoodAndVegetarianOptions() {
        val recipes = shippedRecipes()
        val meatIds = setOf("thit-heo", "thit-heo-xay", "thit-bo", "thit-ga", "suon-heo", "ba-chi", "uc-ga")
        val seafoodIds = setOf("ca-basa", "ca-hoi", "ca-thu", "ca-loc", "ca-dieu-hong", "tom", "muc", "ngao")
        val requiredIds = recipes.flatMap { it.required }.flatMap { it.anyOf }.toSet()

        assertTrue(requiredIds.any { it in meatIds })
        assertTrue(requiredIds.any { it in seafoodIds })
        assertTrue(recipes.any { recipe -> recipe.required.flatMap { it.anyOf }.all { it !in meatIds && it !in seafoodIds } })
    }

    @Test
    fun shippedBundleCoversEveryOriginalSavoryCatalogIngredient() {
        val coveredIds = shippedRecipes()
            .flatMap { it.required + it.optional }
            .flatMap { it.anyOf }
            .toSet()

        assertEquals(emptySet(), cookingIngredientIds - coveredIds)
    }

    @Test
    fun sevenNewRecipesMatchTheirSpecifiedTitlesDurationsAndSlots() {
        val recipesById = shippedRecipes().associateBy { it.id }

        newRecipeContracts.forEach { (id, contract) ->
            val recipe = checkNotNull(recipesById[id])
            assertEquals(contract.title, recipe.title, id)
            assertEquals(contract.minutes, recipe.minutes, id)
            assertEquals(contract.required, recipe.required.map { it.anyOf.toSet() }.toSet(), "$id required")
            assertEquals(contract.optional, recipe.optional.map { it.anyOf.toSet() }.toSet(), "$id optional")
        }
    }

    @Test
    fun shippedAssets_parseAndUseCatalogIngredients() {
        val recipes = RecipeBundle.parse(assetText("recipes.json"))
        val ingredientIds = ShelfTable.parse(assetText("shelf_table.json")).map { it.id }.toSet()

        assertTrue(recipes.isNotEmpty())
        recipes.flatMap { recipe -> recipe.required + recipe.optional }
            .flatMap { slot -> slot.anyOf }
            .forEach { ingredientId ->
                assertTrue(ingredientId in ingredientIds, "Unknown ingredient ID: $ingredientId")
            }
    }

    @Test
    fun everyShippedRecipeHasServingsPrepCookTimeAndSlotAmounts() {
        shippedRecipes().forEach { recipe ->
            assertTrue(!recipe.servings.isNullOrBlank(), "${recipe.id} missing servings")
            assertTrue(recipe.prepMinutes != null && recipe.cookMinutes != null, "${recipe.id} missing prep/cook minutes")
            assertEquals(recipe.minutes, recipe.prepMinutes!! + recipe.cookMinutes!!, "${recipe.id} minutes mismatch")
            (recipe.required + recipe.optional).forEach { slot ->
                assertTrue(!slot.amount.isNullOrBlank(), "${recipe.id} slot ${slot.anyOf} missing amount")
            }
        }
    }

    @Test
    fun waterSpinachRecipe_namesItsOnionIngredientClearly() {
        val recipe = RecipeBundle.parse(assetText("recipes.json"))
            .single { it.id == "rau-muong-xao-hanh" }

        assertEquals("Rau muống xào hành tây", recipe.title)
        assertEquals(4, recipe.steps.size)
        assertTrue("hành tây" in recipe.steps[0].lowercase())
        assertTrue("hành tây" in recipe.steps[1].lowercase())
        assertTrue(recipe.required.any { "hanh-tay" in it.anyOf })
    }

    private fun assetText(name: String): String =
        checkNotNull(javaClass.getResourceAsStream("/$name")) { "Missing shipped asset: $name" }
            .bufferedReader()
            .use { it.readText() }

    private fun shippedRecipes() = RecipeBundle.parse(assetText("recipes.json"))

    private data class RecipeContract(
        val title: String,
        val minutes: Int,
        val required: Set<Set<String>>,
        val optional: Set<Set<String>>,
    )
}
