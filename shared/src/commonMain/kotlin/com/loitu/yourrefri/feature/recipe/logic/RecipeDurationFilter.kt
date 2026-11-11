package com.loitu.yourrefri.feature.recipe.logic

/** Keeps matching recipes in their original order without changing the input list. */
fun <T> filterRecipesByDuration(
    recipes: List<T>,
    maxMinutesExclusive: Int?,
    durationMinutes: (T) -> Int?,
): List<T> {
    require(maxMinutesExclusive == null || maxMinutesExclusive > 0) {
        "Duration threshold must be positive"
    }
    if (maxMinutesExclusive == null) return recipes.toList()
    return recipes.filter { recipe ->
        durationMinutes(recipe)?.let { it in 1 until maxMinutesExclusive } == true
    }
}

/** Inclusive counterpart used by the user-facing "Tối đa" filters. */
fun <T> filterRecipesByDurationInclusive(
    recipes: List<T>,
    maxMinutesInclusive: Int?,
    durationMinutes: (T) -> Int?,
): List<T> {
    require(maxMinutesInclusive == null || maxMinutesInclusive > 0) {
        "Duration threshold must be positive"
    }
    if (maxMinutesInclusive == null) return recipes.toList()
    return recipes.filter { recipe ->
        durationMinutes(recipe)?.let { it in 1..maxMinutesInclusive } == true
    }
}
