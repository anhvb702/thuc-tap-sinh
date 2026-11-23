package com.loitu.yourrefri.feature.recipe.domain.usecase

import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.logic.RandomPick
import com.loitu.yourrefri.feature.recipe.logic.pickRandomRecipe
import kotlin.random.Random

class PickRecipeRandomUseCase {
    operator fun invoke(
        cookable: List<Recipe>,
        state: PantryGridState,
        excludeId: String? = null,
        random: Random = Random.Default,
    ): RandomPick? = pickRandomRecipe(cookable, state, excludeId, random)
}
