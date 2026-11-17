package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.logic.DietRestriction
import com.loitu.yourrefri.feature.recipe.logic.RecipeRecommendation
import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import com.loitu.yourrefri.feature.recipe.logic.filterRecipesByDiet
import com.loitu.yourrefri.feature.recipe.logic.recommendRecipes
import com.loitu.yourrefri.ui.NavTab
import com.loitu.yourrefri.ui.dietAdjustmentTab
import kotlinx.datetime.LocalDate
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DailyEmptyDietPresentationTest {

    private val today = LocalDate(2026, 11, 16)

    private fun vegRecipe(id: String = "veg-tofu", minutes: Int? = 20) = Recipe(
        id = id,
        title = "Đậu phụ sốt cà chua",
        steps = listOf("Rán đậu", "Sốt cà chua"),
        required = listOf(Slot(listOf("dau-phu"))),
        minutes = minutes,
    )

    private fun meatRecipe(id: String = "beef-stir", minutes: Int? = 25) = Recipe(
        id = id,
        title = "Thịt bò xào",
        steps = listOf("Ướp thịt", "Xào nhanh"),
        required = listOf(Slot(listOf("thit-bo"))),
        minutes = minutes,
    )

    private fun activeSource(relative: String): String {
        val roots = generateSequence(File(System.getProperty("user.dir") ?: ".")) { it.parentFile }
        val file = roots.map { File(it, "composeApp/src/main/kotlin/com/loitu/yourrefri/$relative") }
            .first { it.isFile }
        return file.readLines()
            .filterNot { it.trim().startsWith("//") }
            .joinToString("\n")
    }

    // M5-UT-01: Mọi chế độ ăn nghiêm ngặt làm rỗng danh sách -> EMPTY_DIET + CTA "Điều chỉnh chế độ ăn"
    @Test
    fun strictDietEmptiesRecipesYieldsEmptyDietForAllNonNoneRestrictions() {
        val strictRestrictions = listOf(
            DietRestriction.NO_SEAFOOD,
            DietRestriction.NO_RED_MEAT,
            DietRestriction.VEGETARIAN,
        )
        val banned = listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn")

        strictRestrictions.forEach { restriction ->
            val contentState = resolveDailyRecipeContentState(
                dietFilteredRecipes = emptyList(),
                recommendations = emptyList(),
                durationFilteredRecommendations = emptyList(),
                dietRestriction = restriction,
            )
            val presentation = dailyHeroEmptyPresentation(
                dietFilteredRecipes = emptyList(),
                dietRestriction = restriction,
            )

            assertEquals(DailyRecipeContentState.EMPTY_DIET, contentState, "Expected EMPTY_DIET for $restriction")
            assertEquals(DailyRecipeEmptyState.EMPTY_DIET, presentation.state, "Expected EMPTY_DIET presentation for $restriction")
            assertEquals(emptyDietRecommendationCopy.message, presentation.message)
            assertEquals("Điều chỉnh chế độ ăn", presentation.actionLabel)
            banned.forEach { word ->
                assertFalse(presentation.message.lowercase().contains(word), "Copy contains banned word '$word'")
            }
        }
    }

    // M5-UT-02: Không kiêng (NONE) + danh sách rỗng -> PANTRY_EMPTY + không hiện nút "Điều chỉnh chế độ ăn"
    @Test
    fun noDietRestrictionWithEmptyRecipesYieldsPantryEmptyWithoutDietAction() {
        val contentState = resolveDailyRecipeContentState(
            dietFilteredRecipes = emptyList(),
            recommendations = emptyList(),
            durationFilteredRecommendations = emptyList(),
            dietRestriction = DietRestriction.NONE,
        )
        val presentation = dailyHeroEmptyPresentation(
            dietFilteredRecipes = emptyList(),
            dietRestriction = DietRestriction.NONE,
        )

        assertEquals(DailyRecipeContentState.PANTRY_EMPTY, contentState)
        assertEquals(DailyRecipeEmptyState.PANTRY_EMPTY, presentation.state)
        assertEquals(HERO_EMPTY_MESSAGE, presentation.message)
        assertNull(presentation.actionLabel)
    }

    // M5-UT-03: Có món hợp lệ sau lọc chế độ ăn nhưng recommendations rỗng -> PANTRY_EMPTY (không đổ lỗi chế độ ăn)
    @Test
    fun nonEmptyDietFilteredRecipesWithEmptyRecommendationsYieldsPantryEmpty() {
        val compliant = listOf(vegRecipe())

        val contentState = resolveDailyRecipeContentState(
            dietFilteredRecipes = compliant,
            recommendations = emptyList(),
            durationFilteredRecommendations = emptyList(),
            dietRestriction = DietRestriction.VEGETARIAN,
        )
        val presentation = dailyHeroEmptyPresentation(
            dietFilteredRecipes = compliant,
            dietRestriction = DietRestriction.VEGETARIAN,
        )

        assertEquals(DailyRecipeContentState.PANTRY_EMPTY, contentState)
        assertEquals(DailyRecipeEmptyState.PANTRY_EMPTY, presentation.state)
        assertEquals(HERO_EMPTY_MESSAGE, presentation.message)
        assertNull(presentation.actionLabel)
    }

    // M5-UT-04: Ưu tiên nhánh empty recommendations (EMPTY_DIET/PANTRY_EMPTY) trước DURATION_EMPTY
    @Test
    fun emptyRecommendationsTakePrecedenceOverDurationEmptyFilter() {
        val slowVeg = vegRecipe(minutes = 45)
        val rec = RecipeRecommendation(slowVeg, RecommendationReason.OTHER)

        // (a) Có recommendations nhưng bộ lọc thời gian loại hết -> DURATION_EMPTY
        val durationEmptyState = resolveDailyRecipeContentState(
            dietFilteredRecipes = listOf(slowVeg),
            recommendations = listOf(rec),
            durationFilteredRecommendations = emptyList(),
            dietRestriction = DietRestriction.VEGETARIAN,
        )
        assertEquals(DailyRecipeContentState.DURATION_EMPTY, durationEmptyState)

        // (b) Cả recommendations và durationFiltered đều rỗng khi chế độ ăn lọc hết -> EMPTY_DIET (không rơi vào DURATION_EMPTY)
        val emptyDietState = resolveDailyRecipeContentState(
            dietFilteredRecipes = emptyList(),
            recommendations = emptyList(),
            durationFilteredRecommendations = emptyList(),
            dietRestriction = DietRestriction.VEGETARIAN,
        )
        assertEquals(DailyRecipeContentState.EMPTY_DIET, emptyDietState)

        val emptyPantryState = resolveDailyRecipeContentState(
            dietFilteredRecipes = emptyList(),
            recommendations = emptyList(),
            durationFilteredRecommendations = emptyList(),
            dietRestriction = DietRestriction.NONE,
        )
        assertEquals(DailyRecipeContentState.PANTRY_EMPTY, emptyPantryState)
    }

    // M5-UT-05: Luồng phục hồi khi nới chế độ ăn từ VEGETARIAN -> NavTab.SETTINGS -> NONE
    @Test
    fun dietRelaxationRecoversFromEmptyDietToRecommendationsViaSettingsTab() {
        val catalog = listOf(meatRecipe())
        val pantryState = PantryGridState(today = today)

        // Bước 1: Lọc ăn chay loại hết món mặn -> EMPTY_DIET
        val strictFiltered = filterRecipesByDiet(catalog, DietRestriction.VEGETARIAN)
        val strictRecs = recommendRecipes(strictFiltered, pantryState)
        val step1State = resolveDailyRecipeContentState(
            dietFilteredRecipes = strictFiltered,
            recommendations = strictRecs,
            durationFilteredRecommendations = strictRecs,
            dietRestriction = DietRestriction.VEGETARIAN,
        )
        val step1Presentation = dailyHeroEmptyPresentation(
            dietFilteredRecipes = strictFiltered,
            dietRestriction = DietRestriction.VEGETARIAN,
        )
        assertEquals(DailyRecipeContentState.EMPTY_DIET, step1State)
        assertEquals("Điều chỉnh chế độ ăn", step1Presentation.actionLabel)

        // Bước 2: CTA điều hướng đúng tab Cài đặt
        assertEquals(NavTab.SETTINGS, dietAdjustmentTab())

        // Bước 3: Nới về NONE -> có món gợi ý trở lại
        val relaxedFiltered = filterRecipesByDiet(catalog, DietRestriction.NONE)
        val relaxedRecs = recommendRecipes(relaxedFiltered, pantryState)
        val step3State = resolveDailyRecipeContentState(
            dietFilteredRecipes = relaxedFiltered,
            recommendations = relaxedRecs,
            durationFilteredRecommendations = relaxedRecs,
            dietRestriction = DietRestriction.NONE,
        )
        assertEquals(DailyRecipeContentState.RECOMMENDATIONS, step3State)
        assertTrue(relaxedRecs.isNotEmpty())
    }

    // M5-UT-06: Kiểm tra wiring giữa RecipeScreen.kt và MainScaffold.kt
    @Test
    fun recipeScreenAndMainScaffoldWireDailyEmptyDietAndAdjustDietCallback() {
        val recipeScreen = activeSource("feature/recipe/ui/RecipeScreen.kt")
        val mainScaffold = activeSource("ui/MainScaffold.kt")

        val weeklyCall = recipeScreen.substringAfter("WeeklyMealPlanSection(").substringBefore("RecipeScreenMode.SHOPPING")
        val heroEmptyCall = recipeScreen.substringAfter("HeroEmptyCard(").substringBefore("DailyRecipeContentState.DURATION_EMPTY")
        val heroEmptyBody = recipeScreen.substringAfter("private fun HeroEmptyCard(")
        val mainScaffoldRecipeCall = mainScaffold.substringAfter("else RecipeScreen(").substringBefore("NavTab.SETTINGS")

        assertTrue(recipeScreen.contains("onAdjustDiet: () -> Unit = {}"), "RecipeScreen must declare onAdjustDiet")
        assertTrue(recipeScreen.contains("resolveDailyRecipeContentState("), "RecipeScreen must call resolveDailyRecipeContentState")
        assertTrue(recipeScreen.contains("dailyHeroEmptyPresentation("), "RecipeScreen must call dailyHeroEmptyPresentation")
        assertTrue(
            Regex("""onAdjustDiet\s*=\s*onAdjustDiet""").containsMatchIn(weeklyCall),
            "RecipeScreen must forward onAdjustDiet to WeeklyMealPlanSection",
        )
        assertTrue(
            Regex("""onAdjustDiet\s*=\s*onAdjustDiet""").containsMatchIn(heroEmptyCall),
            "RecipeScreen must forward onAdjustDiet to HeroEmptyCard",
        )
        assertTrue(
            Regex("""TextButton\([\s\S]*?onClick\s*=\s*onAdjustDiet""").containsMatchIn(heroEmptyBody),
            "HeroEmptyCard TextButton must invoke onAdjustDiet",
        )
        assertTrue(
            Regex("""dietFilteredEmpty\s*=\s*dietTooStrict""").containsMatchIn(mainScaffoldRecipeCall),
            "MainScaffold must pass dietFilteredEmpty = dietTooStrict to RecipeScreen",
        )
        assertTrue(
            Regex("""onAdjustDiet\s*=\s*\{\s*currentTab\s*=\s*dietAdjustmentTab\(\)\s*\}""").containsMatchIn(mainScaffoldRecipeCall),
            "MainScaffold must pass onAdjustDiet = { currentTab = dietAdjustmentTab() } to RecipeScreen",
        )
    }
}
