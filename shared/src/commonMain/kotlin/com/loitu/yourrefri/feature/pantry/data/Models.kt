package com.loitu.yourrefri.feature.pantry.data

import kotlinx.datetime.LocalDate

val PANTRY_UNITS = listOf("phần", "cái", "quả", "bó", "g", "kg", "hộp", "chai")
const val DEFAULT_PANTRY_UNIT = "phần"

enum class IngredientKind(val jsonValue: String) {
    FRESH("fresh"),
    SEASONING("seasoning"),
    READY("ready"),
    ;

    companion object {
        fun fromJson(value: String?): IngredientKind =
            entries.firstOrNull { it.jsonValue == value } ?: FRESH
    }
}

data class Ingredient(
    val id: String,
    val nameVi: String,
    val group: String,
    val defaultShelfDays: Int,
    // null nghĩa là người dùng chưa ghi đè; 0 là một giá trị hợp lệ, không phải "chưa có".
    val userShelfDays: Int? = null,
    val kind: IngredientKind = IngredientKind.FRESH,
    val freezerShelfDays: Int? = null,
)

data class PantryItem(
    val ingredientId: String,
    val have: Boolean,
    val markedAt: LocalDate,
    val compartment: PantryCompartment = PantryCompartment.CHILL,
)

enum class PantryCompartment { CHILL, FREEZE }
