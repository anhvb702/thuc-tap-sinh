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
        "thit-ba-chi-rang-chay-canh" to RecipeContract(
            title = "Thịt ba chỉ rang cháy cạnh",
            minutes = 20,
            required = setOf(setOf("ba-chi")),
            optional = setOf(setOf("hanh-la"), setOf("nuoc-mam")),
        ),
        "ca-thu-sot-ca-chua" to RecipeContract(
            title = "Cá thu sốt cà chua",
            minutes = 25,
            required = setOf(setOf("ca-thu"), setOf("ca-chua")),
            optional = setOf(setOf("hanh-la"), setOf("nuoc-mam"), setOf("dau-an")),
        ),
        "ga-kho-gung" to RecipeContract(
            title = "Gà kho gừng",
            minutes = 30,
            required = setOf(setOf("thit-ga"), setOf("gung")),
            optional = setOf(setOf("nuoc-mam"), setOf("dau-an")),
        ),
        "bo-xao-bong-cai" to RecipeContract(
            title = "Thịt bò xào bông cải xanh",
            minutes = 15,
            required = setOf(setOf("thit-bo"), setOf("bong-cai")),
            optional = setOf(setOf("toi"), setOf("nuoc-tuong"), setOf("dau-an")),
        ),
        "dau-que-xao-thit-bo" to RecipeContract(
            title = "Đậu que xào thịt bò",
            minutes = 15,
            required = setOf(setOf("dau-que"), setOf("thit-bo")),
            optional = setOf(setOf("toi"), setOf("nuoc-tuong"), setOf("dau-an")),
        ),
        "canh-muop-nau-tom" to RecipeContract(
            title = "Canh mướp nấu tôm",
            minutes = 15,
            required = setOf(setOf("muop"), setOf("tom")),
            optional = setOf(setOf("hanh-la"), setOf("nuoc-mam")),
        ),
        "gia-do-xao-dau-phu" to RecipeContract(
            title = "Giá đỗ xào đậu phụ",
            minutes = 12,
            required = setOf(setOf("gia-do"), setOf("dau-phu")),
            optional = setOf(setOf("hanh-la"), setOf("nuoc-tuong"), setOf("dau-an")),
        ),
        "cha-lua-kho-tieu" to RecipeContract(
            title = "Chả lụa kho tiêu",
            minutes = 15,
            required = setOf(setOf("cha-lua")),
            optional = setOf(setOf("nuoc-mam"), setOf("nuoc-tuong"), setOf("hanh-la")),
        ),
        "tom-rim-thit-ba-chi" to RecipeContract(
            title = "Tôm rim thịt ba chỉ",
            minutes = 25,
            required = setOf(setOf("tom"), setOf("ba-chi")),
            optional = setOf(setOf("hanh-la"), setOf("nuoc-mam")),
        ),
        "canh-ca-chua-trung" to RecipeContract(
            title = "Canh cà chua trứng",
            minutes = 10,
            required = setOf(setOf("ca-chua"), setOf("trung-ga")),
            optional = setOf(setOf("hanh-la"), setOf("nuoc-mam"), setOf("chanh")),
        ),
    )

    private val cookingIngredientIds = setOf(
        "thit-heo", "thit-heo-xay", "thit-bo", "thit-ga", "suon-heo", "ba-chi", "cha-lua",
        "ca-basa", "ca-hoi", "tom", "muc", "ca-thu",
        "rau-muong", "cai-ngot", "xa-lach", "rau-ngot", "hanh-la", "cai-thia", "gia-do",
        "ca-rot", "khoai-tay", "ca-chua", "bi-xanh", "hanh-tay", "dua-leo", "bong-cai", "dau-que", "muop", "gung",
        "trung-ga", "dau-phu", "nam",
    )

    private val mysteryRecipeIds = setOf(
        "bo-luc-lac-khoai-tay",
        "suon-xao-chua-ngot-bi-mat",
        "tom-rim-toi-chay-canh",
        "ca-hoi-ap-chao-sot-chanh",
        "ga-kho-gung-nam",
        "muc-xao-bong-cai",
    )

    @Test
    fun shippedBundleKeepsThreeOriginalRecipesAndAddsTwelveSpecifiedRecipes() {
        val recipes = shippedRecipes()
        val standard = recipes.filter { !it.isMystery }
        val mystery = recipes.filter { it.isMystery }

        // 30 món mở sẵn + 6 món bí mật mới theo yêu cầu tính năng Cook-to-Unlock.
        assertEquals(36, recipes.size)
        assertEquals(30, standard.size)
        assertEquals(6, mystery.size)
        assertEquals(originalRecipeIds + newRecipeContracts.keys, standard.map { it.id }.toSet())
        assertEquals(mysteryRecipeIds, mystery.map { it.id }.toSet())
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

    @Test
    fun firstTwelveRecipesHaveCompleteMeasuresAndSeasonings() {
        val expectedIds = listOf(
            "canh-chua-ca-basa", "thit-kho-trung", "rau-muong-xao-hanh", "thit-bo-xao-hanh-tay",
            "canh-rau-ngot-thit-bam", "dau-phu-sot-ca-chua", "tom-rim-thit", "ga-kho-khoai-tay",
            "canh-bi-xanh-nau-tom", "ca-hoi-ap-chao-xa-lach", "trung-xao-ca-chua", "canh-cai-dau-phu",
        )
        val recipes = shippedRecipes().take(expectedIds.size)
        assertCompleteMeasuresAndSeasonings(recipes, expectedIds)
    }

    @Test
    fun firstTwentyFourRecipesHaveCompleteMeasuresAndSeasonings() {
        val expectedIds = listOf(
            "canh-chua-ca-basa", "thit-kho-trung", "rau-muong-xao-hanh", "thit-bo-xao-hanh-tay",
            "canh-rau-ngot-thit-bam", "dau-phu-sot-ca-chua", "tom-rim-thit", "ga-kho-khoai-tay",
            "canh-bi-xanh-nau-tom", "ca-hoi-ap-chao-xa-lach", "trung-xao-ca-chua", "canh-cai-dau-phu",
            "ga-ap-chao", "canh-bi-thit-bam", "rau-muong-xao-toi", "suon-xao-chua-ngot",
            "muc-xao-can-toi", "cai-thia-xao-nam", "canh-suon-bi-xanh", "trung-chien-hanh-la",
            "thit-ba-chi-rang-chay-canh", "ca-thu-sot-ca-chua", "ga-kho-gung", "bo-xao-bong-cai",
        )
        assertCompleteMeasuresAndSeasonings(shippedRecipes().take(expectedIds.size), expectedIds)
    }

    @Test
    fun everyShippedRecipeHasCompleteMeasuresAndSeasonings() {
        val recipes = shippedRecipes()
        assertEquals(36, recipes.size)
        assertCompleteMeasuresAndSeasonings(recipes, recipes.map { it.id })
        recipes.forEach { recipe ->
            recipe.seasonings.forEach { seasoning ->
                assertTrue(seasoning.amountText.isNotBlank(), "${recipe.id}: ${seasoning.name} needs amountText")
            }
        }
        assertEquals(6, recipes.count { it.isMystery })
    }

    private fun assertCompleteMeasuresAndSeasonings(recipes: List<Recipe>, expectedIds: List<String>) {
        val forbiddenCopy = listOf(
            "bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", "streak", "chuỗi ngày liên tiếp", // lint-copy: rule
            "điểm số", "xếp hạng", "so với người khác", "đáng xấu hổ", // lint-copy: rule
        )
        assertEquals(expectedIds, recipes.map { it.id })
        recipes.forEach { recipe ->
            assertEquals(2, recipe.servings, recipe.id)
            val slots = recipe.required + recipe.optional
            slots.forEach { slot ->
                slot.anyOf.forEach { ingredientId ->
                    val measure = checkNotNull(slot.measures[ingredientId]) {
                        "${recipe.id} is missing a measure for $ingredientId"
                    }
                    assertTrue(measure.amount > 0.0, "${recipe.id}: $ingredientId must have a positive amount")
                    assertTrue(measure.unit.isNotBlank(), "${recipe.id}: $ingredientId must have a unit")
                }
            }
            assertTrue(recipe.seasonings.isNotEmpty(), "${recipe.id} needs seasonings")

            val copy = listOf(recipe.title) + recipe.steps +
                slots.flatMap { slot -> slot.measures.values.map { it.unit } } +
                recipe.seasonings.flatMap { listOf(it.name, it.amountText) }
            copy.forEach { label ->
                assertTrue(forbiddenCopy.none { label.contains(it, ignoreCase = true) }, "${recipe.id}: $label")
            }
        }
    }
}
