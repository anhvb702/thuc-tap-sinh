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
    val unit = measure.unit.trim()
    if (unit.equals("quả", ignoreCase = true) && scaled > 0.0 && scaled < 10.0) {
        val quarters = (scaled * 4.0).roundToLong()
        val whole = quarters / 4
        val rem = (quarters % 4).toInt()
        if (whole >= 1L && rem != 0) {
            return "khoảng $whole–${whole + 1} $unit"
        }
    }
    val amountText = formatMeasureAmount(scaled)
    return if (amountText.isEmpty()) "" else "$amountText $unit"
}

fun formatSlotMeasure(slot: Slot, ingredientId: String, baseServings: Int, targetServings: Int): String? {
    val measure = slot.measures[ingredientId] ?: return null
    return formatScaledMeasure(measure, baseServings, targetServings).ifEmpty { null }
}

fun measureEstimateNote(servings: Int): String =
    "Định lượng ước tính cho khoảng ${servings.coerceAtLeast(1)} người"

/** Ghi chú gia vị trung thực khi người dùng đổi khẩu phần khác chuẩn của công thức. */
fun seasoningEstimateNote(baseServings: Int, targetServings: Int): String {
    val base = baseServings.coerceAtLeast(1)
    val target = targetServings.coerceAtLeast(1)
    return if (target == base) {
        "Tham khảo cho khoảng $base người · nêm theo khẩu vị"
    } else {
        "Khẩu phần chuẩn $base người · tự gia giảm khi nấu $target người"
    }
}
