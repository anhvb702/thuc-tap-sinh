package com.loitu.yourrefri.feature.pantry.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Hạn trong bảng là ƯỚC TÍNH để nhắc "nên dùng trước", không phải lời khuyên an toàn thực phẩm.
object ShelfTable {
    val GROUPS: Set<String> = setOf("thit", "ca", "rau-la", "rau-cu", "trai-cay", "sua-trung", "khac")

    private const val MIN_SHELF_DAYS = 1
    private const val MAX_SHELF_DAYS = 365

    // Json mặc định: không lenient, không coerceInputValues, nên null hay thiếu trường đều bị từ chối.
    // Kiểu DTO tách riêng để Ingredient không phải mang annotation serialization.
    @Serializable
    private data class Row(
        val id: String,
        val nameVi: String,
        val group: String,
        val defaultShelfDays: Int,
    )

    // SerializationException là lớp con của IllegalArgumentException, nên JSON hỏng cũng ra đúng loại lỗi.
    // Mọi lỗi dữ liệu đều nêu id hoặc group sai để người duyệt bảng biết phải sửa dòng nào.
    fun parse(json: String): List<Ingredient> {
        val rows = Json.decodeFromString<List<Row>>(json)
        val seenIds = HashSet<String>()
        return rows.map { row ->
            require(row.id.isNotBlank()) { "id không được rỗng" }
            require(seenIds.add(row.id)) { "id trùng: ${row.id}" }
            require(row.nameVi.isNotBlank()) { "nameVi rỗng ở ${row.id}" }
            require(row.defaultShelfDays in MIN_SHELF_DAYS..MAX_SHELF_DAYS) {
                "defaultShelfDays=${row.defaultShelfDays} ngoài $MIN_SHELF_DAYS..$MAX_SHELF_DAYS ở ${row.id}"
            }
            require(row.group in GROUPS) { "group '${row.group}' không hợp lệ ở ${row.id}" }
            Ingredient(
                id = row.id,
                nameVi = row.nameVi,
                group = row.group,
                defaultShelfDays = row.defaultShelfDays,
                userShelfDays = null,
            )
        }
    }
}
