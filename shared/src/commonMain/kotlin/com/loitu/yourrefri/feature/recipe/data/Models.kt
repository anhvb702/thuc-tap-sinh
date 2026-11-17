package com.loitu.yourrefri.feature.recipe.data

// Ràng buộc nằm trong init để copy() cũng đi qua kiểm tra, không lách được.

// Một ô nguyên liệu trong công thức: chỉ cần một trong các id là đủ.
data class Slot(val anyOf: List<String>, val amount: String? = null) {
    init {
        require(anyOf.isNotEmpty()) { "Slot phải có ít nhất một nguyên liệu" }
        require(anyOf.all { it.isNotBlank() }) { "Id nguyên liệu trong Slot không được rỗng" }
        require(amount == null || amount.isNotBlank()) { "Định lượng nguyên liệu trong Slot không được rỗng" }
    }
}

data class Recipe(
    val id: String,
    val title: String,
    val steps: List<String>,
    val required: List<Slot>,
    val optional: List<Slot> = emptyList(),
    val minutes: Int? = null,
    val servings: String? = null,
    val prepMinutes: Int? = null,
    val cookMinutes: Int? = null,
) {
    init {
        require(id.isNotBlank()) { "Recipe id không được rỗng" }
        require(title.isNotBlank()) { "Recipe title không được rỗng" }
        // Giới hạn 8 bước để công thức vừa một màn hình (món tự thêm có thể 0 bước theo Q26); món không cần nguyên liệu nào sẽ luôn "nấu được" nên bắt buộc có ít nhất một Slot.
        require(steps.size <= 8) { "Recipe phải có tối đa 8 bước" }
        require(steps.all { it.isNotBlank() }) { "Bước nấu không được rỗng" }
        require(required.isNotEmpty()) { "Recipe phải có ít nhất một Slot bắt buộc" }
        require(minutes == null || minutes in 1..1440) { "Recipe minutes phải nằm trong 1..1440" }
        require(servings == null || servings.isNotBlank()) { "Recipe servings không được rỗng" }
        require(prepMinutes == null || prepMinutes in 0..1440) { "Recipe prepMinutes phải nằm trong 0..1440" }
        require(cookMinutes == null || cookMinutes in 0..1440) { "Recipe cookMinutes phải nằm trong 0..1440" }
    }
}
