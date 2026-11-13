package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.recipe.logic.DietGoal
import com.loitu.yourrefri.feature.recipe.logic.FamilyPreferenceTag
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklyDropdownPresentationTest {
    private fun content(): String {
        val path = "src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/WeeklyMealPlanContent.kt"
        return listOf(File(path), File("composeApp", path)).first { it.isFile }.readText()
    }

    @Test fun goalField_showsCurrentGoalLabel() {
        DietGoal.entries.forEach { assertEquals(it.labelVi, dietGoalFieldText(it)) }
    }

    @Test fun preferenceField_emptyIsGentle() {
        assertEquals("Chưa chọn, món nào cũng được", preferenceFieldText(emptySet()))
    }

    @Test fun preferenceField_joinsInEnumOrder() {
        val selected = FamilyPreferenceTag.entries.reversed().toSet()
        assertEquals(FamilyPreferenceTag.entries.joinToString(", ") { it.labelVi }, preferenceFieldText(selected))
        val subset = setOf(FamilyPreferenceTag.MEAT, FamilyPreferenceTag.SEAFOOD)
        assertEquals("${FamilyPreferenceTag.SEAFOOD.labelVi}, ${FamilyPreferenceTag.MEAT.labelVi}", preferenceFieldText(subset))
    }

    @Test fun optionDescription_namesSelection() {
        assertEquals("Cá, đã chọn", preferenceOptionDescription("Cá", true))
        assertEquals("Cá, chưa chọn", preferenceOptionDescription("Cá", false))
    }

    @Test fun weeklyContentUsesDropdownNotChipRows() {
        val source = content()
        val goalSection = source.substringAfter("Text(WEEKLY_DIET_GOAL_LABEL").substringBefore("Text(WEEKLY_FAMILY_PREFERENCE_LABEL")
        assertTrue(goalSection.contains("DietGoal.entries"))
        assertFalse(goalSection.contains("FilterChip"))
        assertTrue(source.contains("DropdownMenu"))
    }

    @Test fun dropdownAnchorsKeepMinTouchTarget() {
        val section = content().substringAfter("Text(WEEKLY_DIET_GOAL_LABEL").substringBefore("plan.forEach")
        assertTrue(Regex("heightIn\\(min = 48\\.dp\\)").findAll(section).count() >= 2)
    }
}
