package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.recipe.data.Measure
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlin.math.roundToLong

fun scaleMeasureAmount(baseAmount: Double, baseServings: Int, targetServings: Int): Double {
    if (baseServings <= 0 || targetServings <= 0 || !baseAmount.isFinite() || baseAmount <= 0.0) return 0.0
    if (baseServings == targetServings) return baseAmount
    return baseAmount * (targetServings.toDouble() / baseServings.toDouble())
}

fun formatMeasureAmount(amount: Double): String {
    if (!amount.isFinite() || amount <= 0.0) return ""
    if (amount >= 10.0) return amount.roundToLong().toString()
    val quarters = (amount * 4.0).roundToLong()
    if (quarters <= 0L) return "1/4"
    val whole = quarters / 4
    val rem = (quarters % 4).toInt()
    return if (whole == 0L) {
        when (rem) {
            1 -> "1/4"
            2 -> "1/2"
            else -> "3/4"
        }
    } else {
        when (rem) {
            0 -> "$whole"
            1 -> "$whole,25"
            2 -> "$whole,5"
            else -> "$whole,75"
        }
    }
}

fun formatScaledMeasure(measure: Measure, baseServings: Int, targetServings: Int): String {
    val scaled = scaleMeasureAmount(measure.amount, baseServings, targetServings)
    val amountText = formatMeasureAmount(scaled)
    return if (amountText.isEmpty()) "" else "$amountText ${measure.unit.trim()}"
}

fun formatSlotMeasure(slot: Slot, ingredientId: String, baseServings: Int, targetServings: Int): String? {
    val measure = slot.measures[ingredientId] ?: return null
    return formatScaledMeasure(measure, baseServings, targetServings).ifEmpty { null }
}

fun measureEstimateNote(servings: Int): String =
    "Định lượng ước tính cho khoảng ${servings.coerceAtLeast(1)} người"
