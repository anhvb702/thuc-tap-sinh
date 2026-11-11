package com.loitu.yourrefri.feature.pantry.data

import java.io.File

internal actual fun readShelfTableJson(): String {
    // Gradle chạy unit test với thư mục làm việc là thư mục module (shared/); dự phòng khi chạy từ gốc repo.
    val file = listOf("src/commonMain/resources/shelf_table.json", "shared/src/commonMain/resources/shelf_table.json")
        .map(::File).firstOrNull { it.isFile }
        ?: error("Không tìm thấy shelf_table.json")
    return file.readText(Charsets.UTF_8)
}
