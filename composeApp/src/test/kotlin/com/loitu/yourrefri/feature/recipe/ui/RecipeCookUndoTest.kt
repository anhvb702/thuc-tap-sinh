package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.reduce
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.logic.RecommendationReason
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class RecipeCookUndoTest {
    private val today = LocalDate(2026, 11, 8)
    private val older = LocalDate(2026, 11, 5)
    private val ingredients = listOf(
        Ingredient("pork", "Thịt heo", "thit", defaultShelfDays = 14),
        Ingredient("egg", "Trứng gà", "sua-trung", defaultShelfDays = 21),
        Ingredient("rice", "Gạo", "khac", defaultShelfDays = 30),
    )

    private fun state(quantities: Map<String, Int> = emptyMap()) = PantryGridState(
        today = today,
        ingredients = ingredients,
        pantry = mapOf(
            "pork" to PantryItem("pork", true, older),
            "egg" to PantryItem("egg", true, today),
        ),
        quantities = quantities,
    )

    private fun apply(s: PantryGridState, actions: List<PantryGridAction>) =
        actions.fold(s) { acc, a -> reduce(acc, a).state }

    @Test
    fun quantityAboveOneUsesIncreaseAndSingleUsesTap() {
        val before = state(mapOf("pork" to 3))
        val actions = cookUndoActions(before, listOf("pork", "egg"))
        assertEquals(
            listOf(PantryGridAction.IncreaseQuantity("pork"), PantryGridAction.Tap("egg")),
            actions,
        )
    }

    @Test
    fun duplicateIdsProduceASingleUndoAction() {
        assertEquals(listOf(PantryGridAction.Tap("egg")), cookUndoActions(state(), listOf("egg", "egg")))
    }

    @Test
    fun ingredientsNotInPantryAreSkipped() {
        val before = state()
        assertEquals(emptyList(), cookUndoActions(before, listOf("rice", "unknown")))
        assertEquals(emptyList(), cookUndoActions(before, emptyList()))
    }

    @Test
    fun undoRestoresPantryAfterConsumingWithMixedQuantities() {
        val before = state(mapOf("pork" to 2))
        val ids = listOf("pork", "egg")
        val cooked = ids.fold(before) { acc, id -> reduce(acc, PantryGridAction.ConsumeIngredient(id)).state }
        val restored = apply(cooked, cookUndoActions(before, ids))
        assertEquals(before.pantry, restored.pantry)
        assertEquals(before.quantities, restored.quantities)
    }

    @Test
    fun undoKeepsOriginalMarkedDateForSingleItem() {
        val before = state()
        val cooked = reduce(before, PantryGridAction.ConsumeIngredient("pork")).state
        val restored = apply(cooked, cookUndoActions(before, listOf("pork")))
        assertEquals(older, restored.pantry.getValue("pork").markedAt)
        assertEquals(true, restored.pantry.getValue("pork").have)
    }

    @Test
    fun chipsMarkHaveWhenAnyAlternativeIsInPantry() {
        val recipe = Recipe(
            id = "r", title = "R", steps = listOf("a"),
            required = listOf(Slot(listOf("rice", "pork")), Slot(listOf("rice"))),
        )
        val chips = recipeIngredientChips(recipe, state())
        assertEquals(
            listOf(RecipeIngredientChip("Thịt heo", true), RecipeIngredientChip("Gạo", false)),
            chips,
        )
    }

    @Test
    fun chipsFallbackToIdWhenIngredientUnknown() {
        val recipe = Recipe("r", "R", listOf("a"), listOf(Slot(listOf("mystery"))))
        assertEquals(listOf(RecipeIngredientChip("mystery", false)), recipeIngredientChips(recipe, state()))
    }

    @Test
    fun missingChipTextIsGentle() {
        assertEquals("chưa có", missingChipSuffix(false))
        assertEquals(null, missingChipSuffix(true))
    }

    @Test
    fun stepTitleIsNumbered() {
        assertEquals("Bước 1", stepTitle(0))
        assertEquals("Bước 4", stepTitle(3))
    }

    @Test
    fun undoSnackbarMessageIsWarm() {
        assertEquals("Đã cập nhật tủ. Chúc cả nhà ngon miệng", cookDoneMessage(hasUpdates = true))
        assertEquals("Chúc cả nhà ngon miệng", cookDoneMessage(hasUpdates = false))
    }

    @Test
    fun reasonPillColorsMatchHomeStyle() {
        assertEquals(
            com.loitu.yourrefri.ui.theme.TerracottaSoft to com.loitu.yourrefri.ui.theme.TerracottaText,
            reasonPillColors(RecommendationReason.USE_SOON),
        )
        assertEquals(
            com.loitu.yourrefri.ui.theme.WarmBeigeSoft to com.loitu.yourrefri.ui.theme.WarmBeigeText,
            reasonPillColors(RecommendationReason.QUICK),
        )
        assertEquals(
            com.loitu.yourrefri.ui.theme.SoftGreen to com.loitu.yourrefri.ui.theme.DeepCharcoal,
            reasonPillColors(RecommendationReason.READY),
        )
        assertEquals(reasonPillColors(RecommendationReason.READY), reasonPillColors(RecommendationReason.OTHER))
    }

    @Test
    fun segmentLabelsAreFixed() {
        assertEquals(listOf("Hôm nay", "Cả tuần"), recipeSegmentLabels())
    }
}
