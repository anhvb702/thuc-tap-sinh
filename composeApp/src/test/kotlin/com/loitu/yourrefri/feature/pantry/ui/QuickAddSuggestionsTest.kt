package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class QuickAddSuggestionsTest {
    private val today = LocalDate(2026, 11, 7)
    private val ingredients = listOf(
        Ingredient("egg", "Trứng gà", "khac", 7),
        Ingredient("pork", "Thịt heo", "thit", 30),
        Ingredient("rice", "Gạo", "khac", 30),
        Ingredient("tomato", "Cà chua", "rau-cu", 7),
    )

    @Test
    fun suggestionsExcludePresentButAllowPreviouslyDepletedAndUntrackedItems() {
        val state = PantryGridState(
            today = today,
            ingredients = ingredients,
            pantry = mapOf(
                "egg" to PantryItem("egg", true, today),
                "pork" to PantryItem("pork", false, today),
            ),
        )

        assertEquals(listOf("pork", "rice", "tomato"), quickAddSuggestions(state).map { it.id })
    }

    @Test
    fun suggestionsPreserveCatalogOrderAndRespectLimit() {
        val state = PantryGridState(today = today, ingredients = ingredients)

        assertEquals(listOf("egg", "pork"), quickAddSuggestions(state, limit = 2).map { it.id })
        assertEquals(emptyList(), quickAddSuggestions(state, limit = 0))
    }

    @Test
    fun suggestionsUseSixItemDefaultLimit() {
        val expandedCatalog = ingredients + (7..10).map { index ->
            Ingredient("extra$index", "Món $index", "khac", 7)
        }

        assertEquals(6, quickAddSuggestions(PantryGridState(today = today, ingredients = expandedCatalog)).size)
    }

    @Test
    fun suggestionsReturnEmptyWhenCatalogEmptyOrAllItemsPresent() {
        val allPresent = ingredients.associate { it.id to PantryItem(it.id, true, today) }
        assertEquals(emptyList(), quickAddSuggestions(PantryGridState(today = today, ingredients = ingredients, pantry = allPresent)))
        assertEquals(emptyList(), quickAddSuggestions(PantryGridState(today = today)))
    }

    @Test
    fun suggestionsFilterByTypedQueryWithoutChangingPantryState() {
        val state = PantryGridState(today = today, ingredients = ingredients)

        assertEquals(listOf("tomato"), quickAddSuggestions(state, query = "  cà chua ").map { it.id })
        assertEquals(emptyMap(), state.pantry)
    }

    @Test
    fun queryFilteringKeepsCatalogOrderAndAppliesLimitAfterFiltering() {
        val expanded = ingredients + Ingredient("tomato2", "Cà chua bi", "rau-cu", 7)
        val state = PantryGridState(today = today, ingredients = expanded)

        assertEquals(listOf("tomato", "tomato2"), quickAddSuggestions(state, query = "cà chua", limit = 2).map { it.id })
    }

    @Test
    fun estimateUsesSavedUserChoiceBeforeCatalogDefault() {
        assertEquals(7, quickAddEstimateDays(ingredients.first()))
        assertEquals(4, quickAddEstimateDays(ingredients.first().copy(userShelfDays = 4)))
        assertNull(quickAddEstimateDays(null))
    }

    @Test
    fun selectingSuggestionStartsWithItsSavedOrCatalogEstimate() {
        assertEquals(7, quickAddSelectionDays(ingredients.first()))
        assertEquals(4, quickAddSelectionDays(ingredients.first().copy(userShelfDays = 4)))
        assertNull(quickAddSelectionDays(null))
    }

    @Test
    fun suggestedEstimateOptionCanRestoreCatalogOrUserEstimateAfterCustomChoice() {
        val ingredient = ingredients.first()
        val option = quickAddEstimateOption(ingredient, selectedDays = 3)

        assertEquals("Theo gợi ý · 7 ngày", option?.label)
        assertEquals(false, option?.selected)
        assertNull(option?.daysOnSelect)
        assertEquals(true, quickAddEstimateOption(ingredient, selectedDays = null)?.selected)
        assertEquals("Theo gợi ý · 4 ngày", quickAddEstimateOption(ingredient.copy(userShelfDays = 4), 2)?.label)
        assertNull(quickAddEstimateOption(null, selectedDays = 3))
    }

    @Test
    fun queryMessageDistinguishesUnknownFromAlreadyPresentAndAmbiguousMatches() {
        val state = PantryGridState(
            today = today,
            ingredients = ingredients + listOf(
                Ingredient("milk_alt", "Sữa hạt", "khac", 5),
                Ingredient("milk_fresh", "Sữa tươi", "khac", 5),
            ),
            pantry = mapOf("egg" to PantryItem("egg", true, today)),
        )

        assertNull(quickAddQueryMessage(state, "  "))
        assertTrue(quickAddQueryMessage(state, "Bắp cải")!!.contains("không có", ignoreCase = true))
        assertTrue(quickAddQueryMessage(state, "Trứng gà")!!.contains("đã có trong tủ"))
        assertNull(quickAddQueryMessage(state, "Sữa"))
        assertEquals(listOf("milk_alt", "milk_fresh"), quickAddSuggestions(state, "Sữa").map { it.id })
        assertEquals(
            "Có vài món phù hợp, bạn chạm chọn 1 món gợi ý bên trên nhé.",
            quickAddQueryMessage(state, "Sữa", afterAttempt = true),
        )
    }

    // Contract UX-05 (quyết định 2026-11-10, theo khuyến nghị Advisor và Brainstormer): chạm chip chỉ bật/tắt
    // trạng thái chọn; không đổi ô tìm kiếm, để người dùng chọn tiếp các chip khác.
    @Test
    fun chipTapNeverChangesSearchQuery() {
        val chicken = Ingredient("chicken", "Thịt gà", "thit", 3)
        assertEquals("thit", quickAddQueryAfterChipTap("thit", setOf("chicken"), chicken))
        assertEquals("thit", quickAddQueryAfterChipTap("thit", setOf("chicken", "pork"), chicken))
        assertEquals("thit", quickAddQueryAfterChipTap("thit", emptySet(), chicken))
    }

    // Contract UX-04/UX-05: không xác nhận khi truy vấn còn nhiều chip, kể cả khi chỉ một món bắt đầu bằng truy vấn.
    @Test
    fun confirmWithoutChipDoesNotPickOneOfSeveralSuggestions() {
        val today = LocalDate(2026, 11, 10)
        val state = PantryGridState(
            today = today,
            ingredients = listOf(
                Ingredient("cam", "Cam", "trai-cay", 7),
                Ingredient("carrot", "Cà rốt", "rau-cu", 14),
                Ingredient("salmon", "Cá hồi", "ca", 3),
            ),
        )

        assertEquals(emptyList(), quickAddIdsToSave(state, emptySet(), "ca"))
        assertEquals(listOf("cam"), quickAddIdsToSave(state, emptySet(), "Cam"))
        assertEquals(
            "Có vài món phù hợp, bạn chạm chọn 1 món gợi ý bên trên nhé.",
            quickAddQueryMessage(state, "ca", afterAttempt = true),
        )
    }

    // Contract UX-04: thông báo "có vài món" chỉ khi còn từ hai chip gợi ý; một chip không báo "có vài món".
    @Test
    fun ambiguityMessageNeedsAtLeastTwoSuggestionChips() {
        val today = LocalDate(2026, 11, 10)
        val pork = Ingredient("pork", "Thịt heo", "thit", 7)
        val porkMince = Ingredient("pork2", "Thịt heo xay", "thit", 3)
        val chicken = Ingredient("chicken", "Thịt gà", "thit", 3)
        val oneChip = PantryGridState(
            today = today,
            ingredients = listOf(pork, porkMince, chicken),
            pantry = mapOf(
                "pork" to PantryItem("pork", true, today),
                "pork2" to PantryItem("pork2", true, today),
            ),
        )
        val twoChips = PantryGridState(today = today, ingredients = listOf(pork, porkMince, chicken))

        assertEquals(
            "Bạn chạm món gợi ý bên trên để thêm nhé.",
            quickAddQueryMessage(oneChip, "thit", afterAttempt = true),
        )
        assertEquals(
            "Có vài món phù hợp, bạn chạm chọn 1 món gợi ý bên trên nhé.",
            quickAddQueryMessage(twoChips, "thit", afterAttempt = true),
        )
    }

    @Test
    fun suggestionSelectionUsesEffectiveEstimateWithoutSavingAnOverride() {
        val catalogDefault = quickAddSuggestionSelection(ingredients.first())
        val savedUserDefault = quickAddSuggestionSelection(ingredients.first().copy(userShelfDays = 4))

        assertEquals(7, catalogDefault.effectiveDays)
        assertNull(catalogDefault.overrideDays)
        assertEquals(4, savedUserDefault.effectiveDays)
        assertNull(savedUserDefault.overrideDays)
    }

    @Test
    fun customDayChoicesOmitTheEffectiveSuggestedEstimate() {
        assertEquals(listOf(2, 3, 14), quickAddCustomDays(ingredients.first()))
        assertEquals(listOf(2, 7, 14), quickAddCustomDays(ingredients.first().copy(userShelfDays = 3)))
        assertEquals(listOf(2, 3, 7), quickAddCustomDays(ingredients.first().copy(userShelfDays = 14)))
    }

    @Test
    fun suggestionsPrioritizeRecentlyDepletedThenKeepCatalogOrderForDateTies() {
        val catalog = listOf(
            Ingredient("tomato", "Cà chua", "rau-cu", 5),
            Ingredient("old-pork", "Thịt heo", "thit", 3),
            Ingredient("new-beef", "Thịt bò", "thit", 2),
            Ingredient("new-chicken", "Thịt gà", "thit", 2),
            Ingredient("present-fish", "Cá basa", "ca", 2),
        )
        val state = PantryGridState(
            today = today,
            ingredients = catalog,
            pantry = mapOf(
                "old-pork" to PantryItem("old-pork", false, LocalDate(2026, 11, 5)),
                "new-beef" to PantryItem("new-beef", false, LocalDate(2026, 11, 6)),
                "new-chicken" to PantryItem("new-chicken", false, LocalDate(2026, 11, 6)),
                "present-fish" to PantryItem("present-fish", true, today),
                "orphan" to PantryItem("orphan", false, today),
            ),
        )

        assertEquals(
            listOf("new-beef", "new-chicken", "old-pork", "tomato"),
            quickAddSuggestions(state).map { it.id },
        )
    }

    @Test
    fun suggestionsFilterByGroupAndAccentlessQueryTogether() {
        val catalog = listOf(
            Ingredient("pork", "Thịt heo", "thit", 2),
            Ingredient("beef", "Thịt bò", "thit", 2),
            Ingredient("fish", "Cá basa", "ca", 2),
        )
        val state = PantryGridState(today = today, ingredients = catalog)

        assertEquals(
            listOf("pork", "beef"),
            quickAddSuggestions(state, query = "thit", selectedGroup = "thit").map { it.id },
        )
        assertEquals(
            listOf("fish"),
            quickAddSuggestions(state, query = "ca", selectedGroup = "ca").map { it.id },
        )
        assertEquals(
            emptyList(),
            quickAddSuggestions(state, query = "thit", selectedGroup = "ca").map { it.id },
        )
    }

    @Test
    fun groupOptionsExposeAllOneTouchCategoriesInProductOrder() {
        assertEquals(
            listOf(
                null to "Tất cả",
                "thit" to "Thịt",
                "ca" to "Cá & Tôm",
                "rau-la" to "Rau lá",
                "rau-cu" to "Rau củ",
                "trai-cay" to "Trái cây",
                "sua-trung" to "Sữa & Trứng",
                "khac" to "Đậu & Nấm",
            ),
            quickAddGroupOptions().map { it.groupId to it.label },
        )
    }

    @Test
    fun selectionToggleAddsIndependentIdsAndRemovesOnlyTappedId() {
        val first = toggleQuickAddSelection(emptySet(), "pork")
        val second = toggleQuickAddSelection(first, "beef")

        assertEquals(setOf("pork"), first)
        assertEquals(setOf("pork", "beef"), second)
        assertEquals(setOf("beef"), toggleQuickAddSelection(second, "pork"))
        assertEquals(emptySet(), toggleQuickAddSelection(setOf("pork"), "pork"))
    }

    @Test
    fun saveIdsKeepsCandidateOrderAndSkipsPresentOrUnknownSelections() {
        val state = PantryGridState(
            today = today,
            ingredients = listOf(
                Ingredient("tomato", "Cà chua", "rau-cu", 5),
                Ingredient("old-pork", "Thịt heo", "thit", 3),
                Ingredient("new-beef", "Thịt bò", "thit", 2),
                Ingredient("new-chicken", "Thịt gà", "thit", 2),
                Ingredient("present-fish", "Cá basa", "ca", 2),
            ),
            pantry = mapOf(
                "old-pork" to PantryItem("old-pork", false, LocalDate(2026, 11, 5)),
                "new-beef" to PantryItem("new-beef", false, LocalDate(2026, 11, 6)),
                "new-chicken" to PantryItem("new-chicken", false, LocalDate(2026, 11, 6)),
                "present-fish" to PantryItem("present-fish", true, today),
            ),
        )

        assertEquals(
            listOf("new-beef", "old-pork", "tomato"),
            quickAddIdsToSave(state, setOf("tomato", "old-pork", "new-beef", "present-fish", "missing")),
        )
    }

    @Test
    fun saveIdsFallsBackToOneUniqueTypedMatchButNotAnAmbiguousQuery() {
        val catalog = ingredients + listOf(
            Ingredient("milk_alt", "Sữa hạt", "sua-trung", 5),
            Ingredient("milk_fresh", "Sữa tươi", "sua-trung", 5),
        )
        val state = PantryGridState(today = today, ingredients = catalog)

        assertEquals(listOf("tomato"), quickAddIdsToSave(state, emptySet(), query = "ca chua"))
        assertEquals(emptyList(), quickAddIdsToSave(state, emptySet(), query = "sua"))
    }

    @Test
    fun confirmLabelAddsCountOnlyForMultipleSelections() {
        assertEquals("Thêm vào tủ", quickAddConfirmLabel(0))
        assertEquals("Thêm vào tủ", quickAddConfirmLabel(1))
        assertEquals("Thêm 2 món vào tủ", quickAddConfirmLabel(2))
        assertEquals("Thêm 4 món vào tủ", quickAddConfirmLabel(4))
    }
}
