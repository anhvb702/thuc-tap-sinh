package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.cellsOf
import com.loitu.yourrefri.feature.recipe.data.Recipe

enum class DietGoal(val labelVi: String) {
    BALANCED("Cân bằng gia đình"), LIGHT("Thanh đạm · Nhiều rau"),
    RICH_PROTEIN("Đậm đà · Đủ đạm"), QUICK("Nấu nhanh ≤ 20 phút"),
}

enum class FamilyPreferenceTag(val labelVi: String) {
    KID_FAVORITE("Món dễ ăn (trứng, gà, khoai)"), SEAFOOD("Cá & Tôm"),
    VEGGIE_TOFU("Rau, Đậu & Nấm"), MEAT("Thịt heo, bò, gà"),
}

data class PlannedDayMeal(val dayIndex: Int, val dayLabelVi: String, val recipe: Recipe, val matchReasonVi: String)
data class ShoppingListItem(val ingredient: Ingredient, val mealCount: Int, val alreadyInPantry: Boolean)

private val days = listOf("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật")
private val lightIds = setOf("rau-muong", "rau-ngot", "cai-ngot", "xa-lach", "bi-xanh", "dau-phu", "nam", "ca-basa", "ca-hoi")
private val proteinIds = setOf("thit-heo", "thit-heo-xay", "thit-bo", "thit-ga", "ca-basa", "ca-hoi", "tom", "trung-ga")
private val kidIds = setOf("trung-ga", "thit-ga", "khoai")
private val seaIds = setOf("ca-basa", "ca-hoi", "tom")
private val veggieIds = lightIds - setOf("ca-basa", "ca-hoi")
private val meatIds = setOf("thit-heo", "thit-heo-xay", "thit-bo", "thit-ga")

private fun Recipe.ids() = required.flatMap { it.anyOf }.toSet()

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
            val group = when (tag) {
                FamilyPreferenceTag.KID_FAVORITE -> kidIds
                FamilyPreferenceTag.SEAFOOD -> seaIds
                FamilyPreferenceTag.VEGGIE_TOFU -> veggieIds
                FamilyPreferenceTag.MEAT -> meatIds
            }
            if (ids.any { it in group }) 20 else 0
        }
        return (if (day <= 1) pantryScore else 0) + goalScore + preferenceScore
    }
    fun ranked(day: Int, excluded: Set<String>) = unique.withIndex().filter { it.value.id !in excluded }
        .sortedWith(compareByDescending<IndexedValue<Recipe>> { score(it.value, day) }.thenBy { it.index })
        .map { it.value }
    // Base plan first; swaps are applied afterwards so one swap never reshuffles unrelated days.
    val picks = mutableListOf<Recipe>()
    repeat(7) { day ->
        val candidates = ranked(day, setOfNotNull(picks.lastOrNull()?.id))
        if (candidates.isEmpty()) return@repeat
        picks += candidates.firstOrNull { c -> picks.none { it.id == c.id } } ?: candidates.first()
    }
    swappedOffsets.toSortedMap().forEach { (day, rawOffset) ->
        val offset = rawOffset.coerceAtLeast(0)
        if (day !in picks.indices || offset == 0) return@forEach
        val options = ranked(day, setOfNotNull(picks.getOrNull(day - 1)?.id))
        val start = options.indexOfFirst { it.id == picks[day].id }.coerceAtLeast(0)
        picks[day] = options[(start + offset) % options.size]
        var next = day + 1
        while (next < picks.size && picks[next].id == picks[next - 1].id) {
            val avoid = setOfNotNull(picks[next - 1].id, picks.getOrNull(next + 1)?.id)
            picks[next] = ranked(next, avoid).firstOrNull() ?: ranked(next, setOf(picks[next - 1].id)).first()
            next++
        }
    }
    val chosen = mutableListOf<PlannedDayMeal>()
    picks.forEachIndexed { day, selected ->
        val reason = when {
            day <= 1 && selected.ids().any { it in urgent } -> "Nên dùng sớm nguyên liệu đang có"
            goal == DietGoal.QUICK && selected.minutes != null && selected.minutes <= 20 -> "Nấu trong khoảng ${selected.minutes} phút"
            goal == DietGoal.LIGHT && selected.ids().any { it in lightIds } -> "Có rau, đậu, nấm hoặc cá"
            goal == DietGoal.RICH_PROTEIN && selected.ids().any { it in proteinIds } -> "Có nguyên liệu giàu đạm"
            preferences.any { tag -> selected.ids().any { id -> when (tag) {
                FamilyPreferenceTag.KID_FAVORITE -> id in kidIds
                FamilyPreferenceTag.SEAFOOD -> id in seaIds
                FamilyPreferenceTag.VEGGIE_TOFU -> id in veggieIds
                FamilyPreferenceTag.MEAT -> id in meatIds
            } } } -> "Hợp với lựa chọn của gia đình"
            else -> "Gợi ý cân bằng cho bữa cơm gia đình"
        }
        chosen += PlannedDayMeal(day, days[day], selected, reason)
    }
    return chosen
}

fun buildWeeklyShoppingList(plan: List<PlannedDayMeal>, state: PantryGridState): List<ShoppingListItem> {
    val counts = linkedMapOf<String, Int>()
    plan.forEach { meal -> meal.recipe.required.forEach { slot ->
        val selected = slot.anyOf.firstOrNull { state.pantry[it]?.have == true } ?: slot.anyOf.first()
        counts[selected] = (counts[selected] ?: 0) + 1
    } }
    val catalog = state.ingredients.associateBy { it.id }
    val order = state.ingredients.mapIndexed { index, ingredient -> ingredient.id to index }.toMap()
    return counts.mapNotNull { (id, count) ->
        catalog[id]?.let { ingredient -> ShoppingListItem(ingredient, count, state.pantry[id]?.have == true) }
    }.sortedWith(compareBy<ShoppingListItem> { it.alreadyInPantry }.thenBy { order[it.ingredient.id] ?: Int.MAX_VALUE })
}

fun shoppingIdsToStock(items: List<ShoppingListItem>, uncheckedIds: Set<String> = emptySet()): List<String> =
    items.filter { !it.alreadyInPantry && it.ingredient.id !in uncheckedIds }.map { it.ingredient.id }
