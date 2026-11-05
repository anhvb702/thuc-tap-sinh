package com.loitu.yourrefri.feature.cooklog.data

import kotlinx.datetime.LocalDate

// id do DB cấp; không ràng buộc ở đây vì nấu xong mà không ghi nhận đồ nào vẫn hợp lệ.
data class CookLog(
    val id: Long,
    val recipeId: String,
    val cookedOn: LocalDate,
    val photoUri: String? = null,
    val savedIngredientIds: List<String>,
)
