package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PantryCompartmentReducerTest {
    @Test
    fun toggleCompartment_updatesItemAndEmitsPersistenceEffect() {
        val item = PantryItem("meat", true, LocalDate(2026, 11, 1))
        val state = reduce(
            PantryGridState(LocalDate(2026, 11, 1)),
            PantryGridAction.Loaded(
                listOf(Ingredient("meat", "Thịt", "food", 3, freezerShelfDays = 30)),
                listOf(item),
                LocalDate(2026, 11, 1),
            ),
        ).state

        val update = reduce(state, PantryGridAction.ToggleCompartment("meat"))
        assertEquals(PantryCompartment.FREEZE, update.state.pantry.getValue("meat").compartment)
        assertEquals(listOf(PantryGridEffect.SaveCompartment("meat", PantryCompartment.FREEZE)), update.effects)
    }

    @Test
    fun toggleCompartment_withoutFreezerShelfDays_staysChillWithNoEffects() {
        val item = PantryItem("egg", true, LocalDate(2026, 11, 1))
        val state = reduce(
            PantryGridState(LocalDate(2026, 11, 1)),
            PantryGridAction.Loaded(
                listOf(Ingredient("egg", "Trứng", "food", 5, freezerShelfDays = null)),
                listOf(item),
                LocalDate(2026, 11, 1),
            ),
        ).state

        val update = reduce(state, PantryGridAction.ToggleCompartment("egg"))
        assertEquals(PantryCompartment.CHILL, update.state.pantry.getValue("egg").compartment)
        assertEquals(emptyList(), update.effects)
    }

    @Test
    fun toggleCompartment_resetsMarkedAtToTodayAndClearsUserShelfDays() {
        val today = LocalDate(2026, 11, 5)
        val oldDate = LocalDate(2026, 11, 1)
        val item = PantryItem("meat", true, oldDate, PantryCompartment.CHILL)
        val state = reduce(
            PantryGridState(today),
            PantryGridAction.Loaded(
                listOf(Ingredient("meat", "Thịt", "food", 3, userShelfDays = 7, freezerShelfDays = 30)),
                listOf(item),
                today,
            ),
        ).state

        val toFreeze = reduce(state, PantryGridAction.ToggleCompartment("meat"))
        assertEquals(PantryCompartment.FREEZE, toFreeze.state.pantry.getValue("meat").compartment)
        assertEquals(today, toFreeze.state.pantry.getValue("meat").markedAt)
        assertNull(toFreeze.state.ingredients.single().userShelfDays)
        assertEquals(
            listOf(
                PantryGridEffect.SaveCompartment("meat", PantryCompartment.FREEZE),
                PantryGridEffect.SaveHave("meat", true, today),
                PantryGridEffect.SaveShelfDays("meat", null),
            ),
            toFreeze.effects,
        )

        val nextDay = LocalDate(2026, 11, 10)
        val backToChill = reduce(
            toFreeze.state.copy(
                today = nextDay,
                ingredients = toFreeze.state.ingredients.map { it.copy(userShelfDays = 14) },
            ),
            PantryGridAction.ToggleCompartment("meat"),
        )
        assertEquals(PantryCompartment.CHILL, backToChill.state.pantry.getValue("meat").compartment)
        assertEquals(nextDay, backToChill.state.pantry.getValue("meat").markedAt)
        assertNull(backToChill.state.ingredients.single().userShelfDays)
        assertEquals(
            listOf(
                PantryGridEffect.SaveCompartment("meat", PantryCompartment.CHILL),
                PantryGridEffect.SaveHave("meat", true, nextDay),
                PantryGridEffect.SaveShelfDays("meat", null),
            ),
            backToChill.effects,
        )
    }
}
