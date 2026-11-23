package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class RecipeCookbookLogicTest {
    private val today = LocalDate(2026, 11, 23)
    private val pantry = PantryGridState(
        today = today,
        pantry = mapOf(
            "hanh-tay" to PantryItem("hanh-tay", true, today),
            "toi" to PantryItem("toi", false, today),
        ),
    )
    private val quick = recipe("quick", "Đậu phụ xào hành", 15)
    private val missing = recipe("missing", "Canh cà chua", 16, required = listOf(Slot(listOf("toi"))))
    private val untimed = recipe("untimed", "Trứng xào hành", null)
    private val unlocked = recipe("unlocked", "Cá hồi sốt chanh", 20, mystery = true)
    private val locked = recipe("locked", "Bò lúc lắc", 10, mystery = true)
    private val entries get() = buildCookbookEntries(listOf(locked, quick, missing, untimed, unlocked), pantry, setOf(unlocked.id))

    @Test
    fun countsMissingRequiredSlotsUsingAvailableAlternativesAndIgnoringOptionalSlots() {
        val mixed = quick.copy(required = listOf(quick.required.single(), Slot(listOf("toi", "gung")), Slot(listOf("ca-chua"))))
        assertEquals(
            listOf(CookbookEntry(quick, true, true, 0), CookbookEntry(mixed, true, false, 2)),
            buildCookbookEntries(listOf(quick, mixed), pantry, emptySet()),
        )
    }

    @Test
    fun allListsUnlockedFirstAndMysteryListsLockedFirst() {
        assertEquals(listOf(quick, missing, untimed, unlocked, locked), filtered(RecipeCookbookFilter.ALL))
        assertEquals(listOf(locked, unlocked), filtered(RecipeCookbookFilter.MYSTERY))
        assertEquals(listOf(false, true), entries.filter { it.recipe.isMystery }.map { it.unlocked })
    }

    @Test
    fun canCookAndFavoriteOnlyShowUnlockedRecipes() {
        assertEquals(listOf(quick, untimed, unlocked), filtered(RecipeCookbookFilter.CAN_COOK))
        assertEquals(listOf(missing, unlocked), filtered(RecipeCookbookFilter.FAVORITE))
    }

    @Test
    fun quickIncludesExactlyFifteenMinutesAndExcludesUnknownAndLocked() {
        assertEquals(listOf(quick), filtered(RecipeCookbookFilter.QUICK))
    }

    @Test
    fun searchCombinesWithFilterAndOnlyMatchesPublicLabelsForLockedRecipes() {
        assertEquals(listOf(quick), filtered(RecipeCookbookFilter.ALL, "  DAU   PHU "))
        assertEquals(emptyList(), filtered(RecipeCookbookFilter.FAVORITE, "dau phu"))
        assertEquals(listOf(unlocked), filtered(RecipeCookbookFilter.MYSTERY, "CA HOI"))
        assertEquals(emptyList(), filtered(RecipeCookbookFilter.ALL, "bo luc lac"))
        assertEquals(listOf(locked), filtered(RecipeCookbookFilter.ALL, "mon bi mat"))
    }

    private fun filtered(filter: RecipeCookbookFilter, query: String = "") =
        filterCookbookEntries(entries, setOf(locked.id, missing.id, unlocked.id), filter, query).map { it.recipe }

    private fun recipe(
        id: String,
        title: String,
        minutes: Int?,
        mystery: Boolean = false,
        required: List<Slot> = listOf(Slot(listOf("toi", "hanh-tay"))),
    ) = Recipe(
        id = id, title = title, steps = listOf("Nấu chín."), required = required,
        optional = listOf(Slot(listOf("nuoc-mam"))), minutes = minutes, isMystery = mystery,
    )
}
