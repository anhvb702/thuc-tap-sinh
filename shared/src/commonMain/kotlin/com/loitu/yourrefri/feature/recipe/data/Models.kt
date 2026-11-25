package com.loitu.yourrefri.feature.recipe.data

// Ràng buộc nằm trong init để copy() cũng đi qua kiểm tra, không lách được.

// Một ô nguyên liệu trong công thức: chỉ cần một trong các id là đủ.
data class Measure(val amount: Double, val unit: String) {
    init {
        require(amount.isFinite() && amount > 0.0) { "Measure amount phải hữu hạn và lớn hơn 0" }
        require(unit.isNotBlank()) { "Measure unit không được rỗng" }
    }
}

// Gia vị ghi dạng chữ tự do, ví dụ "1 thìa cà phê".
data class Seasoning(val name: String, val amountText: String) {
    init {
        require(name.isNotBlank()) { "Tên gia vị không được rỗng" }
        require(amountText.isNotBlank()) { "Định lượng gia vị không được rỗng" }
    }
}

data class Slot(val anyOf: List<String>, val measures: Map<String, Measure> = emptyMap()) {
    init {
        require(anyOf.isNotEmpty()) { "Slot phải có ít nhất một nguyên liệu" }
        require(anyOf.all { it.isNotBlank() }) { "Id nguyên liệu trong Slot không được rỗng" }
        val allowed = anyOf.toSet()
        require(measures.keys.all { it in allowed }) { "Khóa measures phải nằm trong anyOf của Slot" }
    }
}

data class Recipe(
    val id: String,
    val title: String,
    val steps: List<String>,
    val required: List<Slot>,
    val optional: List<Slot> = emptyList(),
    val minutes: Int? = null,
    val stepMinutes: List<Int?> = emptyList(),
    val isMystery: Boolean = false,
    val servings: Int = 2,
    val seasonings: List<Seasoning> = emptyList(),
) {
    init {
        require(servings in 1..20) { "Recipe servings phải nằm trong 1..20" }
        require(id.isNotBlank()) { "Recipe id không được rỗng" }
        require(title.isNotBlank()) { "Recipe title không được rỗng" }
        // Giới hạn 8 bước để công thức vừa một màn hình; món không cần nguyên liệu nào sẽ luôn "nấu được" nên bắt buộc có ít nhất một Slot.
        require(steps.isNotEmpty() && steps.size <= 8) { "Recipe phải có từ 1 đến 8 bước" }
        require(steps.all { it.isNotBlank() }) { "Bước nấu không được rỗng" }
        require(required.isNotEmpty()) { "Recipe phải có ít nhất một Slot bắt buộc" }
        require(minutes == null || minutes in 1..1440) { "Recipe minutes phải nằm trong 1..1440" }
    }
}
