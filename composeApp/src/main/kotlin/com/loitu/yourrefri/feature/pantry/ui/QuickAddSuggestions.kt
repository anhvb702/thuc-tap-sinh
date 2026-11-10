package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.pantry.logic.findIngredientByName
import kotlinx.datetime.LocalDate
import java.text.Normalizer
import java.util.Locale

internal data class QuickAddEstimateOption(
    val label: String,
    val selected: Boolean,
    val daysOnSelect: Int?,
)

internal data class QuickAddSuggestionSelection(
    val effectiveDays: Int?,
    val overrideDays: Int?,
)

internal data class QuickAddGroupOption(
    val groupId: String?,
    val label: String,
)

private data class CatalogCandidate(
    val ingredient: Ingredient,
    val catalogIndex: Int,
    val depletedAt: LocalDate?,
)

internal fun quickAddGroupOptions(): List<QuickAddGroupOption> = listOf(
    QuickAddGroupOption(null, "Tất cả"),
    QuickAddGroupOption("thit", "Thịt"),
    QuickAddGroupOption("ca", "Cá & Tôm"),
    QuickAddGroupOption("rau-la", "Rau lá"),
    QuickAddGroupOption("rau-cu", "Rau củ"),
    QuickAddGroupOption("trai-cay", "Trái cây"),
    QuickAddGroupOption("sua-trung", "Sữa & Trứng"),
    QuickAddGroupOption("khac", "Đậu & Nấm"),
)

fun quickAddSuggestions(
    state: PantryGridState,
    query: String = "",
    limit: Int = 6,
    selectedGroup: String? = null,
): List<Ingredient> {
    require(limit >= 0)
    return quickAddCandidates(state, query, selectedGroup).take(limit)
}

private fun quickAddCandidates(
    state: PantryGridState,
    query: String = "",
    selectedGroup: String? = null,
): List<Ingredient> {
    val normalizedQuery = normalizeFoodQuery(query)
    return state.ingredients.withIndex()
        .asSequence()
        .mapNotNull { indexed ->
            val ingredient = indexed.value
            val pantryItem = state.pantry[ingredient.id]
            if (pantryItem?.have == true) return@mapNotNull null
            if (selectedGroup != null && ingredient.group != selectedGroup) return@mapNotNull null
            if (normalizedQuery.isNotEmpty() && !matchesFoodQuery(ingredient.nameVi, normalizedQuery)) {
                return@mapNotNull null
            }
            CatalogCandidate(
                ingredient = ingredient,
                catalogIndex = indexed.index,
                depletedAt = pantryItem?.takeIf { !it.have }?.markedAt,
            )
        }
        .sortedWith(
            compareBy<CatalogCandidate> { if (it.depletedAt == null) 1 else 0 }
                .thenByDescending { it.depletedAt }
                .thenBy { it.catalogIndex },
        )
        .map { it.ingredient }
        .toList()
}

internal fun toggleQuickAddSelection(selectedIds: Set<String>, ingredientId: String): Set<String> =
    if (ingredientId in selectedIds) selectedIds - ingredientId else selectedIds + ingredientId

internal fun quickAddIdsToSave(
    state: PantryGridState,
    selectedIds: Set<String>,
    query: String = "",
): List<String> {
    val candidates = quickAddCandidates(state)
    if (selectedIds.isNotEmpty()) {
        return candidates.asSequence()
            .map(Ingredient::id)
            .filter { it in selectedIds }
            .toList()
    }
    return quickAddPickForSave(state, query)?.let { listOf(it.id) } ?: emptyList()
}

// Món được thêm khi xác nhận không chọn chip: khớp chính xác tên, hoặc đúng một chip còn lại.
private fun quickAddPickForSave(state: PantryGridState, query: String): Ingredient? {
    if (query.isBlank()) return null
    val exact = state.ingredients.firstOrNull { it.nameVi.equals(query.trim(), ignoreCase = true) }
    val normalized = normalizeFoodQuery(query)
    return (exact ?: state.ingredients.filter { matchesFoodQuery(it.nameVi, normalized) }.singleOrNull())
        ?.takeIf { state.pantry[it.id]?.have != true }
}

internal fun quickAddConfirmLabel(selectedCount: Int): String {
    require(selectedCount >= 0)
    return if (selectedCount > 1) "Thêm $selectedCount món vào tủ" else "Thêm vào tủ"
}

fun quickAddEstimateDays(ingredient: Ingredient?): Int? = ingredient?.let {
    it.userShelfDays ?: it.defaultShelfDays
}

internal fun quickAddSelectionDays(ingredient: Ingredient?): Int? = quickAddEstimateDays(ingredient)

internal fun quickAddSuggestionSelection(ingredient: Ingredient): QuickAddSuggestionSelection =
    QuickAddSuggestionSelection(
        effectiveDays = ingredient.userShelfDays ?: ingredient.defaultShelfDays,
        overrideDays = null,
    )

internal fun quickAddCustomDays(
    ingredient: Ingredient?,
    options: List<Int> = listOf(2, 3, 7, 14),
): List<Int> {
    val effectiveDays = quickAddEstimateDays(ingredient)
    return options.filter { it != effectiveDays }
}

internal fun quickAddMatch(ingredients: List<Ingredient>, query: String): Ingredient? {
    findIngredientByName(ingredients, query)?.let { return it }
    val normalized = normalizeFoodQuery(query)
    if (normalized.isEmpty()) return null
    return ingredients.filter { matchesFoodQuery(it.nameVi, normalized) }.singleOrNull()
}

internal fun quickAddEstimateOption(ingredient: Ingredient?, selectedDays: Int?): QuickAddEstimateOption? {
    val suggestedDays = quickAddSelectionDays(ingredient) ?: return null
    return QuickAddEstimateOption(
        label = "Theo gợi ý · $suggestedDays ngày",
        selected = selectedDays == null,
        daysOnSelect = null,
    )
}

internal fun quickAddQueryMessage(
    state: PantryGridState,
    query: String,
    afterAttempt: Boolean = false,
): String? {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isEmpty()) return null

    val normalized = normalizeFoodQuery(normalizedQuery)
    val matchingIngredients = state.ingredients.filter { matchesFoodQuery(it.nameVi, normalized) }
    if (matchingIngredients.isEmpty()) return "Không có món phù hợp trong danh sách. Bạn thử tên khác nhé."
    val uniqueMatch = quickAddMatch(state.ingredients, normalizedQuery)
    if (uniqueMatch != null && state.pantry[uniqueMatch.id]?.have == true) {
        return "Món này đã có trong tủ rồi."
    }
    if (afterAttempt && quickAddPickForSave(state, normalizedQuery) == null) {
        val chipCount = quickAddCandidates(state, normalizedQuery).size
        if (chipCount > 1) return "Có vài món phù hợp, bạn chạm chọn 1 món gợi ý bên trên nhé."
        if (chipCount == 1) return "Bạn chạm món gợi ý bên trên để thêm nhé."
    }
    return null
}

// Chạm chip chỉ bật/tắt trạng thái chọn; ô tìm kiếm giữ nguyên để chọn thêm chip khác.
internal fun quickAddQueryAfterChipTap(
    currentQuery: String,
    selectedIdsAfterTap: Set<String>,
    ingredient: Ingredient,
): String = currentQuery

private fun normalizeFoodQuery(value: String): String = Normalizer
    .normalize(value.lowercase(Locale.ROOT).replace('đ', 'd'), Normalizer.Form.NFD)
    .replace(Regex("\\p{Mn}+"), "")
    .trim()
    .split(Regex("\\s+"))
    .filter(String::isNotEmpty)
    .joinToString(" ")

private fun matchesFoodQuery(name: String, normalizedQuery: String): Boolean {
    val normalizedName = normalizeFoodQuery(name)
    if (normalizedName.contains(normalizedQuery)) return true
    val queryWords = normalizedQuery.split(' ')
    val nameWords = normalizedName.split(' ')
    return queryWords.all { queryWord -> nameWords.any { it.startsWith(queryWord) } }
}
