package com.loitu.yourrefri.feature.recipe.data

import com.loitu.yourrefri.feature.pantry.data.ShelfTable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShippedRecipeBundleTest {
    @Test
    fun parserReadsStepMinutesAndKeepsLegacyJsonCompatible() {
        val withStepMinutes = """
            [{"id":"a","title":"A","steps":["Rửa","Nấu"],"required":[{"anyOf":["x"]}],"stepMinutes":[null,7]}]
        """.trimIndent()
        val legacy = """
            [{"id":"b","title":"B","steps":["Rửa"],"required":[{"anyOf":["x"]}]}]
        """.trimIndent()

        assertEquals(listOf(null, 7), RecipeBundle.parse(withStepMinutes).single().stepMinutes)
        assertEquals(emptyList(), RecipeBundle.parse(legacy).single().stepMinutes)
    }
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
        "trung-xao-ca-chua" to RecipeContract(
            title = "Trứng xào cà chua",
            minutes = 12,
            required = setOf(setOf("trung-ga"), setOf("ca-chua")),
            optional = setOf(setOf("hanh-la")),
        ),
        "canh-cai-dau-phu" to RecipeContract(
            title = "Canh cải đậu phụ",
            minutes = 15,
            required = setOf(setOf("cai-ngot"), setOf("dau-phu")),
            optional = emptySet(),
        ),
        "ga-ap-chao" to RecipeContract(
            title = "Thịt gà áp chảo",
            minutes = 20,
            required = setOf(setOf("thit-ga")),
            optional = setOf(setOf("hanh-la")),
        ),
        "canh-bi-thit-bam" to RecipeContract(
            title = "Canh bí xanh nấu thịt bằm",
            minutes = 20,
            required = setOf(setOf("bi-xanh"), setOf("thit-heo-xay", "thit-heo")),
            optional = setOf(setOf("hanh-la")),
        ),
        "rau-muong-xao-toi" to RecipeContract(
            title = "Rau muống xào tỏi",
            minutes = 10,
            required = setOf(setOf("rau-muong"), setOf("toi")),
            optional = emptySet(),
        ),
        "suon-xao-chua-ngot" to RecipeContract(
            title = "Sườn xào chua ngọt",
            minutes = 30,
            required = setOf(setOf("suon-heo"), setOf("ca-chua")),
            optional = setOf(setOf("hanh-la"), setOf("nuoc-mam"), setOf("dau-an")),
        ),
        "muc-xao-can-toi" to RecipeContract(
            title = "Mực xào dưa leo",
            minutes = 15,
            required = setOf(setOf("muc"), setOf("dua-leo")),
            optional = setOf(setOf("hanh-tay"), setOf("toi"), setOf("dau-an")),
        ),
        "cai-thia-xao-nam" to RecipeContract(
            title = "Cải thìa xào nấm",
            minutes = 12,
            required = setOf(setOf("cai-thia"), setOf("nam")),
            optional = setOf(setOf("toi"), setOf("dau-an")),
        ),
        "canh-suon-bi-xanh" to RecipeContract(
            title = "Canh sườn bí xanh",
            minutes = 30,
            required = setOf(setOf("suon-heo"), setOf("bi-xanh")),
            optional = setOf(setOf("hanh-la"), setOf("nuoc-mam")),
        ),
        "trung-chien-hanh-la" to RecipeContract(
            title = "Trứng chiên hành lá",
            minutes = 10,
            required = setOf(setOf("trung-ga"), setOf("hanh-la")),
            optional = setOf(setOf("nuoc-mam"), setOf("dau-an")),
        ),
    )

    private val cookingIngredientIds = setOf(
        "thit-heo", "thit-heo-xay", "thit-bo", "thit-ga", "suon-heo",
        "ca-basa", "ca-hoi", "tom", "muc",
        "rau-muong", "cai-ngot", "xa-lach", "rau-ngot", "hanh-la", "cai-thia",
        "ca-rot", "khoai-tay", "ca-chua", "bi-xanh", "hanh-tay", "dua-leo",
        "trung-ga", "dau-phu", "nam",
    )

    @Test
    fun shippedBundleKeepsThreeOriginalRecipesAndAddsTwelveSpecifiedRecipes() {
        val recipes = shippedRecipes()

        // Đổi từ 15 sang 20 có chủ đích: người dùng yêu cầu thêm 6 nguyên liệu và 5 món mới kèm ảnh.
        assertEquals(20, recipes.size)
        assertEquals(originalRecipeIds + newRecipeContracts.keys, recipes.map { it.id }.toSet())
    }

    @Test
    fun everyShippedRecipeHasFourNonBlankStepsAndPositiveDuration() {
        shippedRecipes().forEach { recipe ->
            assertEquals(4, recipe.steps.size, "${recipe.id} must have four steps")
            assertTrue(recipe.steps.all(String::isNotBlank), "${recipe.id} has a blank step")
            assertTrue((recipe.minutes ?: 0) > 0, "${recipe.id} must have positive minutes")
        }
    }

    @Test
    fun shippedBundleCoversBothQuickAndMediumDinnerDurations() {
        val minutes = shippedRecipes().mapNotNull { it.minutes }

        assertTrue(minutes.any { it <= 15 }, "Bundle needs a recipe at or under 15 minutes")
        assertTrue(minutes.any { it in 16..30 }, "Bundle needs a recipe from 16 to 30 minutes")
    }

    @Test
    fun shippedBundleCoversEverySavoryCatalogIngredient() {
        val coveredIds = shippedRecipes()
            .flatMap { it.required + it.optional }
            .flatMap { it.anyOf }
            .toSet()

        assertEquals(emptySet(), cookingIngredientIds - coveredIds)
    }

    @Test
    fun twelveNewRecipesMatchTheirSpecifiedTitlesDurationsAndSlots() {
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
