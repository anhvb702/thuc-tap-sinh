package com.loitu.yourrefri.feature.recipe.domain

import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.domain.usecase.BuildRecipeCatalogUseCase
import com.loitu.yourrefri.feature.recipe.domain.usecase.PickRecipeRandomUseCase
import kotlinx.datetime.LocalDate
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class BuildRecipeCatalogUseCaseTest {
    private val today = LocalDate(2026, 1, 10)
    private val state = PantryGridState(
        today = today,
        pantry = mapOf("rice" to PantryItem("rice", true, today)),
    )

    private fun recipe(id: String, minutes: Int? = null, ingredient: String = "rice") = Recipe(
        id = id,
        title = id,
        steps = listOf("cook"),
        required = listOf(Slot(listOf(ingredient))),
        minutes = minutes,
    )

    @Test
    fun durationFilter_isInclusive() {
        val result = BuildRecipeCatalogUseCase()(
            recipes = listOf(recipe("at-limit", 30), recipe("over-limit", 31)),
            randomPool = null,
            state = state,
            maxDurationMinutes = 30,
        )

        assertEquals(listOf("at-limit"), result.visibleRecipes.map { it.recipe.id })
    }

    @Test
    fun randomPool_containsOnlyCookableRecipes() {
        val result = BuildRecipeCatalogUseCase()(
            recipes = emptyList(),
            randomPool = listOf(recipe("ready"), recipe("missing", ingredient = "egg")),
            state = state,
        )

        assertEquals(listOf("ready"), result.cookableRandomPool.map { it.id })
    }

    @Test
    fun randomPick_excludesPreviousRecipeWhenAnotherCookableRecipeExists() {
        val pool = listOf(recipe("first"), recipe("second"))
        val pick = PickRecipeRandomUseCase()(pool, state, excludeId = "first", random = Random(1))

        assertNotEquals("first", pick?.recipe?.id)
    }
}
