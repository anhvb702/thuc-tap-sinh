package com.loitu.yourrefri.feature.pantry.data

import kotlinx.datetime.LocalDate

val PANTRY_UNITS = listOf("phần", "cái", "quả", "bó", "g", "kg", "hộp", "chai")
const val DEFAULT_PANTRY_UNIT = "phần"

data class Ingredient(
    val id: String,
    val nameVi: String,
    val group: String,
    val defaultShelfDays: Int,
    // null nghĩa là người dùng chưa ghi đè; 0 là một giá trị hợp lệ, không phải "chưa có".
    val userShelfDays: Int? = null,
)

data class PantryItem(
    val ingredientId: String,
    val have: Boolean,
    val markedAt: LocalDate,
)
