package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.recipe.data.Recipe

enum class TasteFlavor(val labelVi: String) {
    SAVORY("Đậm đà"),
    LIGHT("Thanh đạm"),
    SWEET("Hơi ngọt"),
    SPICY("Thích cay"),
}

enum class DishStyle(val labelVi: String) {
    BRAISED("Món kho"),
    SOUP("Món canh"),
    STIR_FRY("Món xào"),
    FRIED("Món chiên/rán"),
    STEAMED_BOILED("Hấp/luộc"),
}

data class FamilyTasteProfile(
    val flavors: Set<TasteFlavor> = emptySet(),
    val dishStyles: Set<DishStyle> = emptySet(),
    val avoidIngredientIds: Set<String> = emptySet(),
    val hasYoungKids: Boolean = false,
) {
    val isEmpty: Boolean
        get() = flavors.isEmpty() &&
            dishStyles.isEmpty() &&
            avoidIngredientIds.isEmpty() &&
            !hasYoungKids
}

fun inferDishStyles(recipe: Recipe): Set<DishStyle> {
    val text = recipeText(recipe)
    return buildSet {
        if (text.hasTerm("kho")) add(DishStyle.BRAISED)
        if (text.hasTerm("canh") || text.hasTerm("sup")) add(DishStyle.SOUP)
        if (text.hasTerm("xao")) add(DishStyle.STIR_FRY)
        if (text.hasTerm("chien") || text.hasTerm("ran")) add(DishStyle.FRIED)
        if (text.hasTerm("luoc") || text.hasTerm("hap")) add(DishStyle.STEAMED_BOILED)
    }
}

fun inferFlavors(recipe: Recipe): Set<TasteFlavor> {
    val text = recipeText(recipe)
    return buildSet {
        if (text.hasTerm("kho") || text.hasTerm("sot") || text.hasTerm("rang")) add(TasteFlavor.SAVORY)
        if (text.hasTerm("canh") || text.hasTerm("luoc") || text.hasTerm("hap")) add(TasteFlavor.LIGHT)
        if (text.hasTerm("chua ngot") || text.hasTerm("rim") || text.hasTerm("kho tau")) add(TasteFlavor.SWEET)
        if (text.hasTerm("cay") || text.hasTerm("ot") || text.hasTerm("sa te") || text.hasTerm("tieu")) add(TasteFlavor.SPICY)
    }
}

fun isAvoidedByTaste(recipe: Recipe, profile: FamilyTasteProfile): Boolean {
    val requiredIngredientIds = recipe.required.flatMap { it.anyOf }
    return requiredIngredientIds.any { it in profile.avoidIngredientIds } ||
        (profile.hasYoungKids && TasteFlavor.SPICY in inferFlavors(recipe))
}

fun tasteAffinityScore(recipe: Recipe, profile: FamilyTasteProfile): Int {
    if (isAvoidedByTaste(recipe, profile)) return Int.MIN_VALUE

    val styles = inferDishStyles(recipe)
    val flavors = inferFlavors(recipe)
    return styles.count { it in profile.dishStyles } * 3 +
        flavors.count { it in profile.flavors } * 2 +
        if (profile.hasYoungKids && TasteFlavor.LIGHT in flavors) 1 else 0
}

fun sortRecipesByTaste(recipes: List<Recipe>, profile: FamilyTasteProfile): List<Recipe> {
    val allowed = recipes.filterNot { isAvoidedByTaste(it, profile) }
    if (allowed.isEmpty()) return recipes

    return allowed.mapIndexed { index, recipe -> index to recipe }
        .sortedWith(compareByDescending<Pair<Int, Recipe>> { tasteAffinityScore(it.second, profile) }
            .thenBy { it.first })
        .map { it.second }
}

private fun recipeText(recipe: Recipe): String = buildString {
    append(recipe.title)
    recipe.required.forEach { slot -> slot.anyOf.forEach { append(' ').append(it) } }
    recipe.optional.forEach { slot -> slot.anyOf.forEach { append(' ').append(it) } }
}.lowercase().withoutVietnameseMarks()

private fun String.hasTerm(term: String): Boolean {
    var start = indexOf(term)
    while (start >= 0) {
        val end = start + term.length
        val startsAtBoundary = start == 0 || !this[start - 1].isLetter()
        val endsAtBoundary = end == length || !this[end].isLetter()
        if (startsAtBoundary && endsAtBoundary) return true
        start = indexOf(term, start + 1)
    }
    return false
}

private fun String.withoutVietnameseMarks(): String = buildString(length) {
    for (character in this@withoutVietnameseMarks) {
        append(
            when (character) {
                'à', 'á', 'ả', 'ã', 'ạ', 'ă', 'ằ', 'ắ', 'ẳ', 'ẵ', 'ặ', 'â', 'ầ', 'ấ', 'ẩ', 'ẫ', 'ậ' -> 'a'
                'è', 'é', 'ẻ', 'ẽ', 'ẹ', 'ê', 'ề', 'ế', 'ể', 'ễ', 'ệ' -> 'e'
                'ì', 'í', 'ỉ', 'ĩ', 'ị' -> 'i'
                'ò', 'ó', 'ỏ', 'õ', 'ọ', 'ô', 'ồ', 'ố', 'ổ', 'ỗ', 'ộ', 'ơ', 'ờ', 'ớ', 'ở', 'ỡ', 'ợ' -> 'o'
                'ù', 'ú', 'ủ', 'ũ', 'ụ', 'ư', 'ừ', 'ứ', 'ử', 'ữ', 'ự' -> 'u'
                'ỳ', 'ý', 'ỷ', 'ỹ', 'ỵ' -> 'y'
                'đ' -> 'd'
                else -> character
            },
        )
    }
}
