package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import java.io.File
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

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
        assertEquals(listOf(quick, untimed, unlocked, missing), filtered())
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
        assertEquals(listOf(unlocked, missing), filtered(CookbookFilters(favoriteOnly = true)))
        assertEquals(listOf(unlocked), filtered(CookbookFilters(canCookOnly = true, favoriteOnly = true)))
    }

    @Test
    fun selectedDurationsMatchTheUnionOfZeroOneTwoOrThreeGroups() {
        val quickOnly = setOf(CookbookDurationGroup.QUICK)
        val mediumOnly = setOf(CookbookDurationGroup.MEDIUM)
        val quickAndSlow = quickOnly + CookbookDurationGroup.SLOW
        assertEquals(emptyList(), filtered(CookbookFilters(selectedDurations = emptySet())))
        assertEquals(listOf(quick), filtered(CookbookFilters(selectedDurations = quickOnly)))
        assertEquals(listOf(unlocked, missing), filtered(CookbookFilters(selectedDurations = mediumOnly)))
        assertEquals(listOf(quick, untimed), filtered(CookbookFilters(selectedDurations = quickAndSlow)))
        assertEquals(
            listOf(quick, untimed, unlocked, missing),
            filtered(CookbookFilters(selectedDurations = ALL_COOKBOOK_DURATIONS)),
        )
    }

    @Test
    fun sortsFilteredRecipesByMissingCountAscendingPreservingOrderOnTies() {
        val twoMissing = CookbookEntry(recipe("m2", "Gà rán", 25), unlocked = true, canCookNow = false, missingCount = 2)
        val readyFirst = CookbookEntry(recipe("r1", "Gà luộc", 15), unlocked = true, canCookNow = true, missingCount = 0)
        val oneMissing = CookbookEntry(recipe("m1", "Gà kho", 20), unlocked = true, canCookNow = false, missingCount = 1)
        val readySecond = CookbookEntry(recipe("r2", "Gà hấp", 35), unlocked = true, canCookNow = true, missingCount = 0)
        val customEntries = listOf(twoMissing, readyFirst, oneMissing, readySecond)
        assertEquals(
            listOf("r1", "r2", "m1", "m2"),
            filterCookbookEntries(customEntries, emptySet(), CookbookFilters(), "").map { it.recipe.id },
        )
        assertEquals(
            listOf("r1", "m1", "m2"),
            filterCookbookEntries(
                customEntries,
                emptySet(),
                CookbookFilters(selectedDurations = setOf(CookbookDurationGroup.QUICK, CookbookDurationGroup.MEDIUM)),
                "ga",
            ).map { it.recipe.id },
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

    @Test
    fun cookbookWeeklyShortcutAndRandomWheelPoolRespectReadyOnlyToggle() {
        assertEquals("Thực đơn", COOKBOOK_WEEKLY_SHORTCUT_LABEL)
        assertEquals("Mở thực đơn tuần", COOKBOOK_WEEKLY_SHORTCUT_DESC)
        assertEquals("Chỉ quay từ đồ đang có", RANDOM_WHEEL_READY_ONLY_LABEL)
        val cookable = listOf(quick)
        val all = listOf(quick, missing)
        assertEquals(cookable, randomWheelPool(cookable, all, onlyReadyIngredients = true))
        assertEquals(all, randomWheelPool(cookable, all, onlyReadyIngredients = false))
        assertEquals(emptyList(), randomWheelPool(emptyList(), all, onlyReadyIngredients = true))
    }

    @Test
    fun cookbookTitleIsMonAn() {
        assertEquals("Món ăn", COOKBOOK_SCREEN_TITLE)
    }

    @Test
    fun allQuickFilterIsActiveForDefaultAndCustomDurations() {
        val durations = listOf(
            ALL_COOKBOOK_DURATIONS,
            emptySet(),
            setOf(CookbookDurationGroup.MEDIUM),
            setOf(CookbookDurationGroup.QUICK, CookbookDurationGroup.MEDIUM),
        )
        durations.forEach {
            assertEquals(CookbookQuickFilter.ALL, activeCookbookQuickFilter(false, it))
        }
    }

    @Test
    fun favoriteQuickFilterTakesPrecedenceOverQuickDuration() {
        assertEquals(CookbookQuickFilter.FAVORITE, activeCookbookQuickFilter(true, ALL_COOKBOOK_DURATIONS))
        assertEquals(
            CookbookQuickFilter.FAVORITE,
            activeCookbookQuickFilter(true, setOf(CookbookDurationGroup.QUICK)),
        )
    }

    @Test
    fun easyQuickFilterIsActiveForQuickDurationOnly() {
        assertEquals(
            CookbookQuickFilter.EASY,
            activeCookbookQuickFilter(false, setOf(CookbookDurationGroup.QUICK)),
        )
    }

    @Test
    fun applyingAllClearsFavoritesAndRestoresAllDurations() {
        val current = CookbookFilters(true, true, setOf(CookbookDurationGroup.SLOW))
        assertEquals(
            CookbookFilters(canCookOnly = true, favoriteOnly = false, selectedDurations = ALL_COOKBOOK_DURATIONS),
            applyCookbookQuickFilter(current, CookbookQuickFilter.ALL),
        )
    }

    @Test
    fun applyingFavoriteEnablesFavoritesAndRestoresAllDurations() {
        val current = CookbookFilters(true, false, setOf(CookbookDurationGroup.QUICK))
        assertEquals(
            CookbookFilters(canCookOnly = true, favoriteOnly = true, selectedDurations = ALL_COOKBOOK_DURATIONS),
            applyCookbookQuickFilter(current, CookbookQuickFilter.FAVORITE),
        )
    }

    @Test
    fun applyingEasyClearsFavoritesAndSelectsQuickDurationOnly() {
        val current = CookbookFilters(true, true, ALL_COOKBOOK_DURATIONS)
        assertEquals(
            CookbookFilters(canCookOnly = true, favoriteOnly = false, selectedDurations = setOf(CookbookDurationGroup.QUICK)),
            applyCookbookQuickFilter(current, CookbookQuickFilter.EASY),
        )
    }

    @Test
    fun recipeScreenOmitsDuplicateWeeklyNavigation() {
        val path = "src/main/kotlin/com/loitu/yourrefri/feature/recipe/ui/RecipeScreen.kt"
        val source = listOf(File(path), File("composeApp", path)).first { it.isFile }.readText()
        assertFalse(Regex("""\bRecipeTabChips\s*\(""").containsMatchIn(source))

        val cookbookCall = assertNotNull(
            Regex("""^([ \t]*)RecipeCookbook\s*\(([\s\S]*?)^\1\)""", RegexOption.MULTILINE).find(source),
            "RecipeScreen should render the cookbook",
        )
        val arguments = cookbookCall.groupValues[2]
        assertFalse(Regex("""\bRecipeScreenMode\s*\.\s*WEEKLY\b""").containsMatchIn(arguments))
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
