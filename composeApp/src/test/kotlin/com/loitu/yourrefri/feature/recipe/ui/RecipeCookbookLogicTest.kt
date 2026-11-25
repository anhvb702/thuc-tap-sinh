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
    fun lockedMysteryRecipesAreHiddenFromTheDefaultListAndSearch() {
        assertEquals(listOf(false, true), entries.filter { it.recipe.isMystery }.map { it.unlocked })
        assertEquals(listOf(quick, missing, untimed, unlocked), filtered())
        assertEquals(emptyList(), filtered(query = "bo luc lac"))
        assertEquals(emptyList(), filtered(query = "mon bi mat"))
    }

    @Test
    fun newlyUnlockedMysteryRecipeAppearsInTheListAndMatchesItsRealTitle() {
        val afterUnlock = buildCookbookEntries(listOf(locked, unlocked), pantry, setOf(locked.id, unlocked.id))
        assertEquals(listOf(locked, unlocked), filtered(source = afterUnlock))
        assertEquals(listOf(locked), filtered(query = "bo luc lac", source = afterUnlock))
    }

    @Test
    fun durationGroupsIncludeTheirUpperBoundAndPutUnknownMinutesInSlow() {
        assertEquals(CookbookDurationGroup.QUICK, cookbookDurationOf(15))
        assertEquals(CookbookDurationGroup.MEDIUM, cookbookDurationOf(16))
        assertEquals(CookbookDurationGroup.MEDIUM, cookbookDurationOf(30))
        assertEquals(CookbookDurationGroup.SLOW, cookbookDurationOf(31))
        assertEquals(CookbookDurationGroup.SLOW, cookbookDurationOf(null))
    }

    @Test
    fun togglingADurationRemovesOrAddsOnlyThatGroup() {
        val quickAndSlow = setOf(CookbookDurationGroup.QUICK, CookbookDurationGroup.SLOW)
        val slowOnly = setOf(CookbookDurationGroup.SLOW)
        assertEquals(slowOnly, toggleCookbookDuration(quickAndSlow, CookbookDurationGroup.QUICK))
        assertEquals(quickAndSlow, toggleCookbookDuration(slowOnly, CookbookDurationGroup.QUICK))
    }

    @Test
    fun presenceSwitchLabelsReflectBothStates() {
        assertEquals("Đủ nguyên liệu nấu ngay", cookbookPresenceSwitchLabel(true))
        assertEquals("Tất cả món ăn", cookbookPresenceSwitchLabel(false))
    }

    @Test
    fun canCookOnlyAndFavoriteOnlyWorkIndependentlyAndCombined() {
        assertEquals(listOf(quick, untimed, unlocked), filtered(CookbookFilters(canCookOnly = true)))
        assertEquals(listOf(missing, unlocked), filtered(CookbookFilters(favoriteOnly = true)))
        assertEquals(listOf(unlocked), filtered(CookbookFilters(canCookOnly = true, favoriteOnly = true)))
    }

    @Test
    fun selectedDurationsMatchTheUnionOfZeroOneTwoOrThreeGroups() {
        val quickOnly = setOf(CookbookDurationGroup.QUICK)
        val mediumOnly = setOf(CookbookDurationGroup.MEDIUM)
        val quickAndSlow = quickOnly + CookbookDurationGroup.SLOW
        assertEquals(emptyList(), filtered(CookbookFilters(selectedDurations = emptySet())))
        assertEquals(listOf(quick), filtered(CookbookFilters(selectedDurations = quickOnly)))
        assertEquals(listOf(missing, unlocked), filtered(CookbookFilters(selectedDurations = mediumOnly)))
        assertEquals(listOf(quick, untimed), filtered(CookbookFilters(selectedDurations = quickAndSlow)))
        assertEquals(
            listOf(quick, missing, untimed, unlocked),
            filtered(CookbookFilters(selectedDurations = ALL_COOKBOOK_DURATIONS)),
        )
    }

    @Test
    fun durationBitmaskRoundTripsEverySubsetAndIgnoresUnknownBits() {
        val subsets = listOf(
            emptySet(),
            setOf(CookbookDurationGroup.QUICK),
            setOf(CookbookDurationGroup.MEDIUM, CookbookDurationGroup.SLOW),
            ALL_COOKBOOK_DURATIONS,
        )
        subsets.forEach { subset ->
            assertEquals(subset, subset.toDurationMask().toCookbookDurations(), subset.toString())
        }
        assertEquals(setOf(CookbookDurationGroup.QUICK), (1 or (1 shl 9)).toCookbookDurations())
    }

    @Test
    fun combinedFiltersRequireIngredientsFavoriteDurationAndNormalizedTitle() {
        val noIngredients = missing.copy(title = "Cá kho cà chua")
        val notFavorite = quick.copy(title = "Cá kho hành")
        val wrongDuration = untimed.copy(title = "Cá cơm kho")
        val wrongTitle = recipe("wrong-title", "Thịt kho trứng", 20)
        val matching = unlocked.copy(title = "Cá hồi kho")
        val lockedMatch = locked.copy(title = "Cá bống kho")
        val source = buildCookbookEntries(
            listOf(noIngredients, notFavorite, wrongDuration, wrongTitle, matching, lockedMatch),
            pantry,
            setOf(matching.id),
        )
        val favorites = setOf(noIngredients.id, wrongDuration.id, wrongTitle.id, matching.id, lockedMatch.id)
        val filters = CookbookFilters(
            canCookOnly = true,
            favoriteOnly = true,
            selectedDurations = setOf(CookbookDurationGroup.QUICK, CookbookDurationGroup.MEDIUM),
        )
        assertEquals(listOf(matching), filtered(filters, "  CA   KHO ", source, favorites))
    }

    @Test
    fun resetDurationsKeepsSwitchesAndResetAllRestoresDefaults() {
        val filters = CookbookFilters(canCookOnly = true, favoriteOnly = true, selectedDurations = emptySet())
        assertEquals(CookbookFilters(canCookOnly = true, favoriteOnly = true), filters.resetDurations())
        assertEquals(CookbookFilters(), filters.resetAll())
    }

    private fun filtered(
        filters: CookbookFilters = CookbookFilters(),
        query: String = "",
        source: List<CookbookEntry> = entries,
        favoriteIds: Set<String> = setOf(locked.id, missing.id, unlocked.id),
    ) = filterCookbookEntries(source, favoriteIds, filters, query).map { it.recipe }

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
