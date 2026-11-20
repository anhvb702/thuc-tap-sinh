package com.loitu.yourrefri.feature.home.domain.usecase

import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.RandomRecipePick
import com.loitu.yourrefri.feature.recipe.logic.pickRandomRecipe
import kotlin.random.Random

class PickHomeRandomRecipeUseCase(
    private val random: Random = Random.Default,
) {
    operator fun invoke(
        cookable: List<Recipe>,
        state: PantryGridState,
        excludeId: String? = null,
    ): RandomRecipePick? = pickRandomRecipe(cookable, state, excludeId = excludeId, random = random)
}
