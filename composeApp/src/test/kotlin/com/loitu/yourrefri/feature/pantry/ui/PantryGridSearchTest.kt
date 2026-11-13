package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.logic.PantryCell
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PantryGridSearchTest {
    private val ingredients = listOf(
        Ingredient("ca-chua", "Cà chua", "rau-cu", 5),
        Ingredient("hanh-la", "Hành lá", "rau-la", 5),
        Ingredient("thit-bo", "Thịt bò", "thit", 2),
        Ingredient("ca-basa", "Cá basa", "ca", 2),
        Ingredient("chuoi", "Chuối", "trai-cay", 5),
        Ingredient("trung-ga", "Trứng gà", "sua-trung", 21),
        Ingredient("dau-hu", "Đậu hũ", "khac", 3),
        Ingredient("la-la", "Món lạ", "xyz", 3),
    )
    private val cells = ingredients.map { cell(it.id, it.nameVi) }

    private fun cell(id: String, name: String, have: Boolean = true) =
        PantryCell(id, name, have, 1, "cái", if (have) 3 else null, null)

    private fun List<PantryCell>.ids() = map { it.ingredientId }

    @Test
    fun search_blankQueryKeepsEverythingInOrder() {
        assertEquals(cells.ids(), filterCellsBySearch(cells, "").ids())
        assertEquals(cells.ids(), filterCellsBySearch(cells, "   ").ids())
    }

    @Test
    fun search_matchesWithoutDiacritics() {
        assertEquals(listOf("ca-chua"), filterCellsBySearch(cells, "ca chua").ids())
        assertEquals(listOf("thit-bo"), filterCellsBySearch(cells, "thit bo").ids())
        assertEquals(listOf("dau-hu"), filterCellsBySearch(cells, "dau hu").ids())
    }

    @Test
    fun search_matchesWithDiacriticsAndIgnoresCase() {
        assertEquals(listOf("ca-chua"), filterCellsBySearch(cells, "CÀ CHUA").ids())
        assertEquals(listOf("trung-ga"), filterCellsBySearch(cells, "Trứng").ids())
        assertEquals(listOf("dau-hu"), filterCellsBySearch(cells, "đậu").ids())
    }

    @Test
    fun search_noMatchGivesEmptyList() {
        assertTrue(filterCellsBySearch(cells, "xyzxyz").isEmpty())
    }

    @Test
    fun search_updatesWhileTypingPrefix() {
        assertEquals(listOf("thit-bo"), filterCellsBySearch(cells, "th").ids())
        assertEquals(listOf("ca-chua", "ca-basa"), filterCellsBySearch(cells, "ca").ids())
    }

    @Test
    fun group_putsCellsIntoDisplayCategoriesInFixedOrder() {
        val groups = groupCellsByCategory(cells, ingredients)
        assertEquals(
            listOf("Rau củ", "Thịt & cá", "Trái cây", "Sữa & trứng", "Đậu & nấm", "Khác"),
            groups.map { it.label },
        )
        assertEquals(listOf("ca-chua", "hanh-la"), groups[0].cells.ids())
        assertEquals(listOf("thit-bo", "ca-basa"), groups[1].cells.ids())
        assertEquals(listOf("la-la"), groups[5].cells.ids())
    }

    @Test
    fun group_onlyKeepsCategoriesThatHaveCells() {
        val groups = groupCellsByCategory(cells.filter { it.ingredientId == "chuoi" }, ingredients)
        assertEquals(listOf("Trái cây"), groups.map { it.label })
        assertEquals(1, groups.single().cells.size)
    }

    @Test
    fun group_keepsCatalogOrderInsideAGroupAndIsStable() {
        val reversed = cells.reversed()
        val groups = groupCellsByCategory(reversed, ingredients)
        assertEquals(listOf("hanh-la", "ca-chua"), groups.first { it.label == "Rau củ" }.cells.ids())
        assertEquals(groups, groupCellsByCategory(reversed, ingredients))
    }

    @Test
    fun group_emptyInputGivesNoGroups() {
        assertTrue(groupCellsByCategory(emptyList(), ingredients).isEmpty())
    }

    @Test
    fun group_headerShowsCount() {
        val g = groupCellsByCategory(cells, ingredients).first()
        assertEquals("Rau củ (2)", g.title)
    }

    @Test
    fun urgentStatus_usesWarmWordingAndEstimate() {
        assertEquals("Ước tính còn 2 ngày", pantryRowStatus(daysLeft = 2))
        assertEquals("Còn khoảng 5 ngày", pantryRowStatus(daysLeft = 5))
        assertEquals("Nên dùng hôm nay", pantryRowStatus(daysLeft = 0))
        assertEquals("Kiểm tra lại xem còn dùng được không", pantryRowStatus(daysLeft = -1))
    }

    @Test
    fun urgentFlag_trueOnlyUpToTwoDays() {
        assertTrue(pantryRowUrgent(have = true, daysLeft = 2))
        assertTrue(!pantryRowUrgent(have = true, daysLeft = 3))
        assertTrue(!pantryRowUrgent(have = false, daysLeft = null))
    }
}
