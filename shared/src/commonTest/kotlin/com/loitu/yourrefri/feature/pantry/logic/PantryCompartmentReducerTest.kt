package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class PantryCompartmentReducerTest {
    @Test
    fun toggleCompartment_updatesItemAndEmitsPersistenceEffect() {
        val item = PantryItem("egg", true, LocalDate(2026, 11, 1))
        val state = reduce(
            PantryGridState(LocalDate(2026, 11, 1)),
            PantryGridAction.Loaded(listOf(Ingredient("egg", "Trứng", "food", 5)), listOf(item), LocalDate(2026, 11, 1)),
        ).state

        val update = reduce(state, PantryGridAction.ToggleCompartment("egg"))
        assertEquals(PantryCompartment.FREEZE, update.state.pantry.getValue("egg").compartment)
        assertEquals(listOf(PantryGridEffect.SaveCompartment("egg", PantryCompartment.FREEZE)), update.effects)
    }
}
