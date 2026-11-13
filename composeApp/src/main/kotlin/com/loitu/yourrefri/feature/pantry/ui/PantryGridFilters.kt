package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.logic.PantryCell

internal data class PantryGridFilters(
    val storageTab: StorageTab = StorageTab.ALL,
    val presentOnly: Boolean = false,
) {
    fun showAllItems() = PantryGridFilters()
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
private val CATEGORY_ORDER = listOf("rau-cu", "thit", "trai-cay", "sua-trung", "khac-dau-nam", "khac")

private fun categoryKeyOf(group: String?): String = when (group) {
    "rau-cu", "rau-la" -> "rau-cu"
    "thit", "ca" -> "thit"
    "trai-cay" -> "trai-cay"
    "sua-trung" -> "sua-trung"
    "khac" -> "khac-dau-nam"
    else -> "khac"
}

private fun categoryLabelOf(key: String): String = when (key) {
    "rau-cu" -> "Rau củ"
    "thit" -> "Thịt & cá"
    "trai-cay" -> "Trái cây"
    "sua-trung" -> "Sữa & trứng"
    "khac-dau-nam" -> "Đậu & nấm"
    else -> "Khác"
}

/** Gom món theo nhóm, thứ tự nhóm cố định, thứ tự món trong nhóm giữ như danh sách đầu vào; bỏ nhóm rỗng. */
internal fun groupCellsByCategory(
    cells: List<PantryCell>,
    ingredients: List<Ingredient>,
): List<PantryCategoryGroup> {
    val groupById = ingredients.associate { it.id to it.group }
    val byKey = cells.groupBy { categoryKeyOf(groupById[it.ingredientId]) }
    return CATEGORY_ORDER.mapNotNull { key ->
        byKey[key]?.takeIf { it.isNotEmpty() }?.let { PantryCategoryGroup(key, categoryLabelOf(key), it) }
    }
}

internal const val URGENT_DAYS = 2

internal fun pantryRowUrgent(have: Boolean, daysLeft: Int?): Boolean =
    have && daysLeft != null && daysLeft <= URGENT_DAYS

/** Dòng trạng thái dưới tên món; luôn là ước tính và không dùng lời nặng nề. */
internal fun pantryRowStatus(daysLeft: Int): String = when {
    daysLeft < 0 -> "Kiểm tra lại xem còn dùng được không"
    daysLeft == 0 -> "Nên dùng hôm nay"
    daysLeft <= URGENT_DAYS -> "Ước tính còn $daysLeft ngày"
    else -> "Còn khoảng $daysLeft ngày"
}
