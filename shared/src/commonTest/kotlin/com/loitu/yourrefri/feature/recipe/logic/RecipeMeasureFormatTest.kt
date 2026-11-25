package com.loitu.yourrefri.feature.recipe.logic

import com.loitu.yourrefri.feature.recipe.data.Measure
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class RecipeMeasureFormatTest {
    @Test
    fun scaleMeasureAmountScalesLinearlyAndGuardsInvalidInputs() {
        assertEquals(250.0, scaleMeasureAmount(250.0, 2, 2), 0.0001)
        assertEquals(500.0, scaleMeasureAmount(250.0, 2, 4), 0.0001)
        assertEquals(125.0, scaleMeasureAmount(250.0, 2, 1), 0.0001)
        assertEquals(1.5, scaleMeasureAmount(1.0, 2, 3), 0.0001)

        assertEquals(0.0, scaleMeasureAmount(250.0, 0, 2), 0.0001)
        assertEquals(0.0, scaleMeasureAmount(250.0, 2, 0), 0.0001)
        assertEquals(0.0, scaleMeasureAmount(-10.0, 2, 4), 0.0001)
        assertEquals(0.0, scaleMeasureAmount(Double.NaN, 2, 4), 0.0001)
    }

    @Test
    fun formatMeasureAmountFormatsFractionsDecimalsIntegersAndEmptyInvalid() {
        assertEquals("1/4", formatMeasureAmount(0.25))
        assertEquals("1/2", formatMeasureAmount(0.5))
        assertEquals("3/4", formatMeasureAmount(0.75))
        assertEquals("1", formatMeasureAmount(1.0))
        assertEquals("1,25", formatMeasureAmount(1.25))
        assertEquals("1,5", formatMeasureAmount(1.5))
        assertEquals("1,75", formatMeasureAmount(1.75))
        assertEquals("2", formatMeasureAmount(2.0))
        assertEquals("10", formatMeasureAmount(10.0))
        assertEquals("250", formatMeasureAmount(250.0))
        assertEquals("125", formatMeasureAmount(125.4))
        assertEquals("126", formatMeasureAmount(125.6))

        assertEquals("", formatMeasureAmount(0.0))
        assertEquals("", formatMeasureAmount(-1.5))
        assertEquals("", formatMeasureAmount(Double.NaN))
        assertEquals("", formatMeasureAmount(Double.POSITIVE_INFINITY))
    }

    @Test
    fun formatScaledMeasureAndSlotMeasureRespectIngredientChoiceAndServings() {
        assertEquals("500 g", formatScaledMeasure(Measure(250.0, "g"), 2, 4))
        assertEquals("1/2 củ", formatScaledMeasure(Measure(1.0, "củ"), 2, 1))
        assertEquals("1,5 quả", formatScaledMeasure(Measure(1.0, "quả"), 2, 3))

        val slot = Slot(
            anyOf = listOf("thit-heo", "thit-heo-xay"),
            measures = mapOf(
                "thit-heo" to Measure(300.0, "g"),
                "thit-heo-xay" to Measure(250.0, "g"),
            ),
        )
        assertEquals("450 g", formatSlotMeasure(slot, "thit-heo", 2, 3))
        assertEquals("250 g", formatSlotMeasure(slot, "thit-heo-xay", 2, 2))
        assertNull(formatSlotMeasure(slot, "khong-co", 2, 2))
    }

    @Test
    fun estimateNoteClampsLowerBoundAndAvoidsForbiddenWords() {
        assertEquals("Định lượng ước tính cho khoảng 2 người", measureEstimateNote(2))
        assertEquals("Định lượng ước tính cho khoảng 1 người", measureEstimateNote(0))
        val low = measureEstimateNote(3).lowercase()
        listOf("bạn quên", "lãng phí", "bỏ lỡ", "hết hạn").forEach { forbidden -> // lint-copy: rule
            assertFalse(forbidden in low)
        }
    }
}
