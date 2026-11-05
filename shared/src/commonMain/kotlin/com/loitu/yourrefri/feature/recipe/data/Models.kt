package com.loitu.yourrefri.feature.recipe.data

// Ràng buộc nằm trong init để copy() cũng đi qua kiểm tra, không lách được.

// Một ô nguyên liệu trong công thức: chỉ cần một trong các id là đủ.
data class Slot(val anyOf: List<String>) {
    init {
        require(anyOf.isNotEmpty()) { "Slot phải có ít nhất một nguyên liệu" }
        require(anyOf.all { it.isNotBlank() }) { "Id nguyên liệu trong Slot không được rỗng" }
    }
}

data class Recipe(
    val id: String,
    val title: String,
    val steps: List<String>,
    val required: List<Slot>,
    val optional: List<Slot> = emptyList(),
) {
    init {
        require(id.isNotBlank()) { "Recipe id không được rỗng" }
        require(title.isNotBlank()) { "Recipe title không được rỗng" }
        // Giới hạn 8 bước để công thức vừa một màn hình; món không cần nguyên liệu nào sẽ luôn "nấu được" nên bắt buộc có ít nhất một Slot.
        require(steps.isNotEmpty() && steps.size <= 8) { "Recipe phải có từ 1 đến 8 bước" }
        require(steps.all { it.isNotBlank() }) { "Bước nấu không được rỗng" }
        require(required.isNotEmpty()) { "Recipe phải có ít nhất một Slot bắt buộc" }
    }
}
