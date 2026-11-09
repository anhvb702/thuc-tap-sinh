package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PANTRY_UNITS
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PantryUnitReducerTest {
    private val today = LocalDate(2026, 11, 6)
    private val units = listOf("phần", "cái", "quả", "bó", "g", "kg", "hộp", "chai")
    private val ingredients = listOf(
        Ingredient("egg", "Trứng", "khac", 7),
        Ingredient("milk", "Sữa", "khac", 3),
    )

    private fun loaded(overrides: Map<String, String?> = emptyMap()) = reduce(
        PantryGridState(today),
        PantryGridAction.Loaded(ingredients, emptyList(), today, unitOverrides = overrides),
    ).state

    @Test
    fun unitOptions_areTheFixedAllowedUnits() {
        assertEquals(units, PANTRY_UNITS)
    }

    @Test
    fun cells_showSavedUnitOrDefaultPhan() {
        val cells = cellsOf(loaded(mapOf("milk" to "chai"))).associateBy { it.ingredientId }
        assertEquals("phần", cells.getValue("egg").unit)
        assertEquals("chai", cells.getValue("milk").unit)
    }

    @Test
    fun openUnitEditor_usesDefaultOrExistingOverride() {
        val start = loaded(mapOf("milk" to "chai"))
        assertEquals(UnitEditor("egg", "phần"), reduce(start, PantryGridAction.OpenUnitEditor("egg")).state.unitEditor)
        assertEquals(UnitEditor("milk", "chai"), reduce(start, PantryGridAction.OpenUnitEditor("milk")).state.unitEditor)
    }

    @Test
    fun openUnitEditor_unknownIngredient_isIgnored() {
        val start = loaded()
        val update = reduce(start, PantryGridAction.OpenUnitEditor("unknown"))
        assertEquals(start, update.state)
        assertEquals(emptyList(), update.effects)
    }

    @Test
    fun pickUnit_acceptsAllowedValueAndRejectsUnknownValue() {
        val opened = reduce(loaded(), PantryGridAction.OpenUnitEditor("egg")).state
        val accepted = reduce(opened, PantryGridAction.PickUnit("kg"))
        assertEquals(UnitEditor("egg", "kg"), accepted.state.unitEditor)
        assertEquals(emptyList(), accepted.effects)

        val rejected = reduce(accepted.state, PantryGridAction.PickUnit("lon"))
        assertEquals(accepted.state, rejected.state)
        assertEquals(emptyList(), rejected.effects)
    }

    @Test
    fun saveUnitEditor_updatesOverrideAndEmitsSaveEffect() {
        val opened = reduce(loaded(), PantryGridAction.OpenUnitEditor("egg")).state
        val picked = reduce(opened, PantryGridAction.PickUnit("quả")).state
        val saved = reduce(picked, PantryGridAction.SaveUnitEditor)
        assertEquals(mapOf("egg" to "quả"), saved.state.unitOverrides)
        assertNull(saved.state.unitEditor)
        assertEquals(listOf(PantryGridEffect.SaveUnit("egg", "quả")), saved.effects)
    }

    @Test
    fun saveDefaultUnit_removesOverrideAndPersistsNull() {
        val opened = reduce(loaded(mapOf("egg" to "quả")), PantryGridAction.OpenUnitEditor("egg")).state
        val picked = reduce(opened, PantryGridAction.PickUnit("phần")).state
        val saved = reduce(picked, PantryGridAction.SaveUnitEditor)
        assertEquals(emptyMap(), saved.state.unitOverrides)
        assertEquals(listOf(PantryGridEffect.SaveUnit("egg", null)), saved.effects)
    }

    @Test
    fun cancelUnitEditor_discardsSelectionWithoutEffect() {
        val start = loaded(mapOf("egg" to "quả"))
        val opened = reduce(start, PantryGridAction.OpenUnitEditor("egg")).state
        val picked = reduce(opened, PantryGridAction.PickUnit("kg")).state
        val cancelled = reduce(picked, PantryGridAction.CancelUnitEditor)
        assertEquals(start, cancelled.state)
        assertEquals(emptyList(), cancelled.effects)
    }
}
