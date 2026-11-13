package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.MealSlotKey
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlinx.datetime.minus

enum class DietGoal(val labelVi: String) {
    BALANCED("Cân bằng gia đình"), LIGHT("Thanh đạm · Nhiều rau"),
    RICH_PROTEIN("Đậm đà · Đủ đạm"), QUICK("Nấu nhanh ≤ 20 phút"),
}

enum class FamilyPreferenceTag(val labelVi: String) {
    KID_FAVORITE("Món dễ ăn (trứng, gà, khoai)"), SEAFOOD("Cá & Tôm"),
    VEGGIE_TOFU("Rau, Đậu & Nấm"), MEAT("Thịt heo, bò, gà"),
}

data class PlannedDayMeal(
    val dayIndex: Int,
    val dayLabelVi: String,
    val recipe: Recipe,
    val matchReasonVi: String,
    val sideRecipe: Recipe? = null,
    val mealTypeVi: String = "Bữa cơm chính",
    val mealSlot: MealSlot = MealSlot.DINNER,
) {
    val dishes: List<Recipe>
        get() = listOfNotNull(recipe, sideRecipe)
}

data class ShoppingListItem(
    val ingredient: Ingredient,
    val mealCount: Int,
    val alreadyInPantry: Boolean,
    val availableQuantity: Int = if (alreadyInPantry) mealCount else 0,
    val missingQuantity: Int = if (alreadyInPantry) 0 else mealCount,
)

private val days = listOf("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật")
private val lightIds = setOf("rau-muong", "rau-ngot", "cai-ngot", "xa-lach", "bi-xanh", "dau-phu", "nam", "ca-basa", "ca-hoi")
private val proteinIds = setOf("thit-heo", "thit-heo-xay", "thit-bo", "thit-ga", "ca-basa", "ca-hoi", "tom", "trung-ga")
private val kidIds = setOf("trung-ga", "thit-ga", "khoai-tay", "khoai")
private val seaIds = setOf("ca-basa", "ca-hoi", "tom")
private val veggieIds = lightIds - setOf("ca-basa", "ca-hoi")
private val meatIds = setOf("thit-heo", "thit-heo-xay", "thit-bo", "thit-ga")
private val soupOrVeggiePrefixes = listOf("canh-", "rau-")

private fun Recipe.ids() = required.flatMap { it.anyOf }.toSet()

internal fun Recipe.isSoupOrVeggieSide(): Boolean {
    val lowerTitle = title.lowercase()
    return soupOrVeggiePrefixes.any { id.startsWith(it) } ||
        lowerTitle.startsWith("canh ") ||
        lowerTitle.startsWith("rau ")
}

private fun FamilyPreferenceTag.ingredientIds(): Set<String> = when (this) {
    FamilyPreferenceTag.KID_FAVORITE -> kidIds
    FamilyPreferenceTag.SEAFOOD -> seaIds
    FamilyPreferenceTag.VEGGIE_TOFU -> veggieIds
    FamilyPreferenceTag.MEAT -> meatIds
}

fun buildWeeklyMealPlan(
    recipes: List<Recipe>, state: PantryGridState, goal: DietGoal = DietGoal.BALANCED,
    preferences: Set<FamilyPreferenceTag> = emptySet(), swappedOffsets: Map<Int, Int> = emptyMap(),
): List<PlannedDayMeal> {
    val unique = recipes.distinctBy { it.id }
    if (unique.isEmpty()) return emptyList()
    val cells = cellsOf(state).associateBy { it.ingredientId }
    val available = cells.values.filter { it.have }.mapTo(HashSet()) { it.ingredientId }
    val urgent = cells.values.filter { it.have && it.daysLeft in 0..2 }.mapTo(HashSet()) { it.ingredientId }
    fun score(recipe: Recipe, day: Int): Int {
        val ids = recipe.ids()
        val pantryScore = recipe.required.count { slot -> slot.anyOf.any { it in urgent } } * 100 +
            if (recipe.required.all { slot -> slot.anyOf.any { it in available } }) 50 else 0
        val goalScore = when (goal) {
            DietGoal.BALANCED -> 10
            DietGoal.QUICK -> if (recipe.minutes != null && recipe.minutes <= 20) 40 else 0
            DietGoal.LIGHT -> if (ids.any { it in lightIds }) 40 else 0
            DietGoal.RICH_PROTEIN -> if (ids.any { it in proteinIds }) 40 else 0
        }
        val preferenceScore = preferences.sumOf { tag ->
            if (ids.any { it in tag.ingredientIds() }) 20 else 0
        }
        return (if (day <= 1) pantryScore else 0) + goalScore + preferenceScore
    }
    fun rankedFrom(pool: List<Recipe>, day: Int, excluded: Set<String>) = pool.withIndex()
        .filter { it.value.id !in excluded }
        .sortedWith(compareByDescending<IndexedValue<Recipe>> { score(it.value, day) }.thenBy { it.index })
        .map { it.value }

    val mainPool = unique.filterNot { it.isSoupOrVeggieSide() }.ifEmpty { unique }
    val sidePool = unique.filter { it.isSoupOrVeggieSide() }
    val hasTwoCourseCatalog = mainPool.size >= 2 && sidePool.isNotEmpty() && mainPool.any { main -> sidePool.any { it.id != main.id } }

    fun pickSeries(pool: List<Recipe>): MutableList<Recipe> {
        val picks = mutableListOf<Recipe>()
        repeat(7) { day ->
            val candidates = rankedFrom(pool, day, setOfNotNull(picks.lastOrNull()?.id))
            if (candidates.isEmpty()) return@repeat
            picks += candidates.firstOrNull { c -> picks.none { it.id == c.id } } ?: candidates.first()
        }
        swappedOffsets.toSortedMap().forEach { (day, rawOffset) ->
            val offset = rawOffset.coerceAtLeast(0)
            if (day !in picks.indices || offset == 0) return@forEach
            val options = rankedFrom(pool, day, setOfNotNull(picks.getOrNull(day - 1)?.id))
            if (options.isEmpty()) return@forEach
            val start = options.indexOfFirst { it.id == picks[day].id }.coerceAtLeast(0)
            picks[day] = options[(start + offset) % options.size]
            var next = day + 1
            while (next < picks.size && picks[next].id == picks[next - 1].id) {
                val avoid = setOfNotNull(picks[next - 1].id, picks.getOrNull(next + 1)?.id)
                picks[next] = rankedFrom(pool, next, avoid).firstOrNull()
                    ?: rankedFrom(pool, next, setOf(picks[next - 1].id)).firstOrNull()
                    ?: picks[next]
                next++
            }
        }
        return picks
    }

    val mainPicks = pickSeries(if (hasTwoCourseCatalog) mainPool else unique)
    val sidePicks = if (hasTwoCourseCatalog) {
        val sides = mutableListOf<Recipe?>()
        repeat(mainPicks.size) { day ->
            val mainId = mainPicks[day].id
            val priorSideId = sides.lastOrNull()?.id
            val candidates = rankedFrom(sidePool, day, setOfNotNull(mainId, priorSideId))
                .ifEmpty { rankedFrom(sidePool, day, setOf(mainId)) }
            if (candidates.isEmpty()) {
                sides += null
            } else {
                val unseen = candidates.filter { c -> sides.none { it?.id == c.id } }.ifEmpty { candidates }
                val offset = swappedOffsets[day]?.coerceAtLeast(0) ?: 0
                sides += unseen[offset % unseen.size]
            }
        }
        sides
    } else {
        List(mainPicks.size) { null }
    }

    val chosen = mutableListOf<PlannedDayMeal>()
    mainPicks.forEachIndexed { day, selected ->
        val side = sidePicks.getOrNull(day)
        val combinedIds = selected.ids() + (side?.ids() ?: emptySet())
        val reason = when {
            day <= 1 && combinedIds.any { it in urgent } -> "Nên dùng sớm nguyên liệu đang có"
            goal == DietGoal.QUICK && selected.minutes != null && selected.minutes <= 20 -> "Nấu trong khoảng ${selected.minutes} phút"
            goal == DietGoal.LIGHT && combinedIds.any { it in lightIds } -> "Có rau, đậu, nấm hoặc cá"
            goal == DietGoal.RICH_PROTEIN && combinedIds.any { it in proteinIds } -> "Có nguyên liệu giàu đạm"
            preferences.any { tag -> combinedIds.any { it in tag.ingredientIds() } } -> "Hợp với lựa chọn của gia đình"
            side != null -> "Đủ mâm: 1 món mặn + 1 món canh/rau"
            else -> "Gợi ý cân bằng cho bữa cơm gia đình"
        }
        chosen += PlannedDayMeal(day, days[day], selected, reason, sideRecipe = side)
    }
    return chosen
}

fun buildWeeklyShoppingList(
    plan: List<PlannedDayMeal>,
    state: PantryGridState,
    preferences: Set<FamilyPreferenceTag> = emptySet(),
    includeLunchMealPrep: Boolean = false,
    disabledMeals: List<MealSlotKey> = emptyList(),
    weekStart: LocalDate = state.today.minus(DatePeriod(days = state.today.dayOfWeek.ordinal)),
): List<ShoppingListItem> {
    val preferredIds = preferences.flatMapTo(HashSet()) { it.ingredientIds() }
    val counts = linkedMapOf<String, Int>()
    val portionMultiplier = if (includeLunchMealPrep) 2 else 1
    val disabled = disabledMeals.toSet()
    plan.forEach { meal ->
        if (MealSlotKey(weekStart.plus(DatePeriod(days = meal.dayIndex)), meal.mealSlot) in disabled) return@forEach
        meal.dishes.forEach { dish ->
            dish.required.forEach { slot ->
                val selected = slot.anyOf.firstOrNull { state.pantry[it]?.have == true }
                    ?: slot.anyOf.firstOrNull { it in preferredIds }
                    ?: slot.anyOf.first()
                counts[selected] = (counts[selected] ?: 0) + portionMultiplier
            }
        }
    }
    val catalog = state.ingredients.associateBy { it.id }
    val order = state.ingredients.mapIndexed { index, ingredient -> ingredient.id to index }.toMap()
    return counts.mapNotNull { (id, count) ->
        catalog[id]?.let { ingredient ->
            val have = state.pantry[id]?.have == true
            val currentQty = if (have) (state.quantities[id] ?: 1) else 0
            val missingQty = (count - currentQty).coerceAtLeast(0)
            ShoppingListItem(
                ingredient = ingredient,
                mealCount = count,
                alreadyInPantry = have,
                availableQuantity = currentQty,
                missingQuantity = missingQty,
            )
        }
    }.sortedWith(compareBy<ShoppingListItem> { it.alreadyInPantry && it.missingQuantity == 0 }.thenBy { order[it.ingredient.id] ?: Int.MAX_VALUE })
}

fun shoppingIdsToStock(items: List<ShoppingListItem>, uncheckedIds: Set<String> = emptySet()): List<String> =
    items.filter { (!it.alreadyInPantry || it.missingQuantity > 0) && it.ingredient.id !in uncheckedIds }.map { it.ingredient.id }

fun shoppingQuantitiesToStock(items: List<ShoppingListItem>, uncheckedIds: Set<String> = emptySet()): Map<String, Int> =
    items.filter { (!it.alreadyInPantry || it.missingQuantity > 0) && it.ingredient.id !in uncheckedIds }
        .associate { it.ingredient.id to it.missingQuantity.coerceAtLeast(1) }
