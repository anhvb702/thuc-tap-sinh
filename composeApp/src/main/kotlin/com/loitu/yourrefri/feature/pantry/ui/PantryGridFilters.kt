package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.logic.MAX_PANTRY_QUANTITY
import com.loitu.yourrefri.feature.pantry.logic.PantryCell

internal data class PantryGridFilters(
    val storageTab: StorageTab = StorageTab.ALL,
    val presentOnly: Boolean = false,
    val selectedCategories: Set<PantryCategory> = ALL_PANTRY_CATEGORIES,
) {
    fun showAllItems() = PantryGridFilters()
}

internal enum class PantryCategory(val labelVi: String) {
    UNPROCESSED("Chưa chế biến"),
    PROCESSED("Đã chế biến"),
    SPICE("Gia vị"),
}

internal val ALL_PANTRY_CATEGORIES: Set<PantryCategory> = PantryCategory.entries.toSet()

private val leftoverNames = listOf("đồ ăn thừa", "leftover", "thừa", "cơm nguội")
private val leftoverWord = Regex("(?<!\\p{L})dư(?!\\p{L})")
private val drinkNames = listOf("sữa", "nước", "trà", "cà phê", "nước ép")
private val spiceNames = listOf("muối", "đường", "tiêu", "nước mắm", "nước tương", "dầu ăn", "dầu hào", "bột ngọt")

internal fun pantryCategoryOf(cell: PantryCell): PantryCategory {
    val kind = cell.kind.lowercase()
    val group = cell.group?.lowercase()
    val name = cell.nameVi.lowercase()
    return when {
        kind == "leftover" || group == "leftover" || group == "do-an-thua" || kind == "ready" ||
            leftoverNames.any(name::contains) || leftoverWord.containsMatchIn(name) ||
            (group == "sua-trung" && drinkNames.any(name::contains)) ->
            PantryCategory.PROCESSED
        kind == "seasoning" || group == "gia-vi" || spiceNames.any(name::contains) -> PantryCategory.SPICE
        else -> PantryCategory.UNPROCESSED
    }
}

internal fun togglePantryCategory(selected: Set<PantryCategory>, category: PantryCategory): Set<PantryCategory> =
    if (category in selected) selected - category else selected + category

internal fun filterByCategories(cells: List<PantryCell>, selected: Set<PantryCategory>): List<PantryCell> = when {
    selected.isEmpty() -> emptyList()
    selected.size == PantryCategory.entries.size -> cells
    else -> cells.filter { pantryCategoryOf(it) in selected }
}

internal fun pantryStatusSwitchLabel(presentOnly: Boolean): String =
    if (presentOnly) "Đang có trong tủ" else "Tất cả nguyên liệu"

internal fun parseDirectQuantityInput(raw: String, maxQuantity: Int = MAX_PANTRY_QUANTITY): Int? {
    val digits = raw.trim()
    if (digits.isEmpty() || digits.any { it !in '0'..'9' }) return null
    return digits.toIntOrNull()?.coerceAtMost(maxQuantity) ?: maxQuantity
}

internal enum class PantryGridEmptyState {
    NONE,
    CATALOG_EMPTY,
    NO_PRESENT_ITEMS,
    NO_PRESENT_IN_LOCATION,
    NO_ITEMS_IN_LOCATION,
    NO_SOON_ITEMS,
}

internal fun pantryGridEmptyState(
    catalogSize: Int,
    visibleSize: Int,
    filters: PantryGridFilters,
): PantryGridEmptyState = when {
    catalogSize == 0 -> PantryGridEmptyState.CATALOG_EMPTY
    visibleSize > 0 -> PantryGridEmptyState.NONE
    filters.storageTab == StorageTab.SOON -> PantryGridEmptyState.NO_SOON_ITEMS
    filters.presentOnly && filters.storageTab == StorageTab.ALL -> PantryGridEmptyState.NO_PRESENT_ITEMS
    filters.presentOnly -> PantryGridEmptyState.NO_PRESENT_IN_LOCATION
    filters.storageTab != StorageTab.ALL -> PantryGridEmptyState.NO_ITEMS_IN_LOCATION
    else -> PantryGridEmptyState.NONE
}

/** Lọc theo tên, không phân biệt dấu và hoa thường; ô tìm trống thì giữ nguyên danh sách và thứ tự. */
internal fun filterCellsBySearch(cells: List<PantryCell>, query: String): List<PantryCell> {
    val normalized = normalizeFoodQuery(query)
    if (normalized.isEmpty()) return cells
    return cells.filter { matchesFoodQuery(it.nameVi, normalized) }
}

/** Một nhóm hiển thị trên màn Tủ lạnh, ví dụ "Rau củ (6)". */
internal data class PantryCategoryGroup(
    val key: String,
    val label: String,
    val cells: List<PantryCell>,
) {
    val title: String get() = "$label (${cells.size})"
}

// Thứ tự nhóm cố định; nhóm lấy từ cùng trường `group` của bảng nguyên liệu như bộ chip "Hôm nay mua gì về?".
private val CATEGORY_ORDER = listOf("rau-cu", "thit", "trai-cay", "sua-trung", "khac-dau-nam", "gia-vi", "khac")

private fun categoryKeyOf(cell: PantryCell, group: String?): String = when {
    group == "gia-vi" || pantryCategoryOf(cell) == PantryCategory.SPICE -> "gia-vi"
    group == "rau-cu" || group == "rau-la" -> "rau-cu"
    group == "thit" || group == "ca" -> "thit"
    group == "trai-cay" -> "trai-cay"
    group == "sua-trung" -> "sua-trung"
    group == "khac" -> "khac-dau-nam"
    else -> "khac"
}

private fun categoryLabelOf(key: String): String = when (key) {
    "rau-cu" -> "Rau củ"
    "thit" -> "Thịt & cá"
    "trai-cay" -> "Trái cây"
    "sua-trung" -> "Sữa & trứng"
    "khac-dau-nam" -> "Đậu & nấm"
    "gia-vi" -> "Gia vị"
    else -> "Khác"
}

/** Gom món theo nhóm, thứ tự nhóm cố định, thứ tự món trong nhóm giữ như danh sách đầu vào; bỏ nhóm rỗng. */
internal fun groupCellsByCategory(
    cells: List<PantryCell>,
    ingredients: List<Ingredient>,
): List<PantryCategoryGroup> {
    val groupById = ingredients.associate { it.id to it.group }
    val byKey = cells.groupBy { categoryKeyOf(it, groupById[it.ingredientId]) }
    return CATEGORY_ORDER.mapNotNull { key ->
        byKey[key]?.takeIf { it.isNotEmpty() }?.let { PantryCategoryGroup(key, categoryLabelOf(key), it) }
    }
}

internal const val URGENT_DAYS = 2

internal fun pantryRowUrgent(have: Boolean, daysLeft: Int?): Boolean =
    have && daysLeft != null && daysLeft <= URGENT_DAYS

internal fun estimateDaysText(daysLeft: Int): String = when {
    daysLeft < 0 -> "Đã qua ${-daysLeft} ngày"
    daysLeft == 0 -> "Nên dùng hôm nay"
    else -> "Còn $daysLeft ngày"
}

/** Dòng trạng thái dưới tên món; luôn là ước tính và không dùng lời nặng nề. */
internal fun pantryRowStatus(daysLeft: Int): String = estimateDaysText(daysLeft)
