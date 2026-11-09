package com.loitu.yourrefri.feature.pantry.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction.CancelShelfEditor
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction.CancelUnitEditor
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction.AddIngredient
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction.ConsumeIngredient
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction.Loaded
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction.OpenUnitEditor
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction.OpenShelfEditor
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction.IncreaseQuantity
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction.DecreaseQuantity
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction.PickShelfDays
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction.PickUnit
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction.SaveShelfEditor
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction.SaveUnitEditor
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction.Tap
import com.loitu.yourrefri.feature.pantry.logic.PantryGridEffect.SaveHave
import com.loitu.yourrefri.feature.pantry.logic.PantryGridEffect.SaveShelfDays
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PantryGridReducerTest {

    private fun d(iso: String): LocalDate = LocalDate.parse(iso)

    private val today = d("2026-11-05")

    private fun ing(id: String, days: Int, user: Int? = null) =
        Ingredient(id = id, nameVi = "Tên $id", group = "khac", defaultShelfDays = days, userShelfDays = user)

    private fun have(id: String, markedAt: String) = PantryItem(ingredientId = id, have = true, markedAt = d(markedAt))
    private fun notHave(id: String, markedAt: String) = PantryItem(ingredientId = id, have = false, markedAt = d(markedAt))

    // Cố ý không theo thứ tự chữ cái để bắt code tự sắp xếp lại lưới.
    private fun table(): List<Ingredient> = listOf(
        ing("thit-heo", 2), ing("thit-bo", 2), ing("thit-ga", 2), ing("ca-hoi", 2), ing("tom", 1),
        ing("rau-muong", 2), ing("cai-ngot", 3), ing("ca-rot", 14), ing("khoai-tay", 21), ing("ca-chua", 5),
        ing("trung-ga", 21), ing("sua-tuoi", 3),
    )

    private fun loaded(
        pantry: List<PantryItem> = emptyList(),
        ingredients: List<Ingredient> = table(),
        day: LocalDate = today,
    ): PantryGridState = reduce(PantryGridState(today = day), Loaded(ingredients, pantry, day)).state

    private fun run(start: PantryGridState, actions: List<PantryGridAction>): Pair<PantryGridState, List<PantryGridEffect>> {
        var state = start
        val effects = mutableListOf<PantryGridEffect>()
        for (a in actions) {
            val update = reduce(state, a)
            state = update.state
            effects += update.effects
        }
        return state to effects
    }

    private fun cell(state: PantryGridState, id: String): PantryCell = cellsOf(state).single { it.ingredientId == id }

    @Test
    fun decreaseQuantityKeepsCookedItemPresentWhenMoreThanOneRemains() {
        val start = loaded(pantry = listOf(have("trung-ga", "2026-11-05"))).copy(
            quantities = mapOf("trung-ga" to 3),
        )

        val result = reduce(start, DecreaseQuantity("trung-ga"))

        assertEquals(2, result.state.quantities["trung-ga"])
        assertTrue(result.state.pantry.getValue("trung-ga").have)
    }

    @Test
    fun decreaseQuantityAtOneMarksCookedItemAbsentWithoutZeroQuantity() {
        val start = loaded(pantry = listOf(have("trung-ga", "2026-11-05")))

        val result = reduce(start, DecreaseQuantity("trung-ga"))

        assertFalse(result.state.pantry.getValue("trung-ga").have)
        assertNull(result.state.quantities["trung-ga"])
    }

    @Test
    fun legacyDecreaseQuantityKeepsItsExistingAbsentRowBehavior() {
        val start = loaded(pantry = listOf(notHave("trung-ga", "2026-11-05")))

        val result = reduce(start, DecreaseQuantity("trung-ga"))

        assertTrue(result.state.pantry.getValue("trung-ga").have)
        assertEquals(listOf(SaveHave("trung-ga", true, today)), result.effects)
    }

    @Test
    fun consumeIngredient_doesNotRestoreItemMadeAbsentByEarlierQueuedAction() {
        val start = loaded(pantry = listOf(have("trung-ga", "2026-11-05")))
        val absent = reduce(start, Tap("trung-ga"))

        val result = reduce(absent.state, ConsumeIngredient("trung-ga"))

        assertEquals(absent.state, result.state)
        assertTrue(result.effects.isEmpty())
    }

    @Test
    fun consumeIngredient_withNoPantryRowDoesNothing() {
        val start = loaded()

        val result = reduce(start, ConsumeIngredient("trung-ga"))

        assertEquals(start, result.state)
        assertTrue(result.effects.isEmpty())
    }

    @Test
    fun consumeIngredient_atOneMarksPresentIngredientAbsent() {
        val start = loaded(pantry = listOf(have("trung-ga", "2026-11-05")))

        val result = reduce(start, ConsumeIngredient("trung-ga"))

        assertFalse(result.state.pantry.getValue("trung-ga").have)
        assertEquals(listOf(SaveHave("trung-ga", false, today)), result.effects)
    }

    @Test
    fun consumeIngredient_aboveOneDecreasesOneAndKeepsItemPresent() {
        val start = loaded(pantry = listOf(have("trung-ga", "2026-11-05"))).copy(
            quantities = mapOf("trung-ga" to 3),
        )

        val result = reduce(start, ConsumeIngredient("trung-ga"))

        assertEquals(2, result.state.quantities["trung-ga"])
        assertTrue(result.state.pantry.getValue("trung-ga").have)
        assertEquals(listOf(PantryGridEffect.SaveQuantity("trung-ga", 2)), result.effects)
    }

    @Test
    fun consumeIngredient_unknownIdDoesNothing() {
        val start = loaded(pantry = listOf(have("trung-ga", "2026-11-05")))

        val result = reduce(start, ConsumeIngredient("unknown"))

        assertEquals(start, result.state)
        assertTrue(result.effects.isEmpty())
    }

    private fun userTapCount(actions: List<PantryGridAction>): Int = actions.count { it !is Loaded }

    // --- Loaded ---

    @Test
    fun loaded_setsIngredientsPantryAndToday_withoutEffects() {
        val pantry = listOf(have("trung-ga", "2026-11-01"), notHave("tom", "2026-10-20"))
        val update = reduce(PantryGridState(today = d("2026-01-01")), Loaded(table(), pantry, today))
        assertEquals(table(), update.state.ingredients)
        assertEquals(mapOf("trung-ga" to pantry[0], "tom" to pantry[1]), update.state.pantry)
        assertEquals(today, update.state.today)
        assertNull(update.state.editor)
        assertEquals(emptyList(), update.effects)
    }

    // --- 1 chạm đổi có/hết ---

    @Test
    fun tap_absentItem_marksHaveToday_andEmitsSave() {
        val update = reduce(loaded(), Tap("rau-muong"))
        assertEquals(have("rau-muong", "2026-11-05"), update.state.pantry["rau-muong"])
        assertEquals(listOf<PantryGridEffect>(SaveHave("rau-muong", true, today)), update.effects)
    }

    @Test
    fun tap_notHaveItem_marksHaveWithToday_notOldDate() {
        val update = reduce(loaded(listOf(notHave("tom", "2026-10-01"))), Tap("tom"))
        assertEquals(have("tom", "2026-11-05"), update.state.pantry["tom"])
        assertEquals(listOf<PantryGridEffect>(SaveHave("tom", true, today)), update.effects)
    }

    @Test
    fun tap_haveItem_marksNotHave_keepsMarkedAt_andEmitsSave() {
        val update = reduce(loaded(listOf(have("trung-ga", "2026-11-01"))), Tap("trung-ga"))
        assertEquals(notHave("trung-ga", "2026-11-01"), update.state.pantry["trung-ga"])
        assertEquals(listOf<PantryGridEffect>(SaveHave("trung-ga", false, d("2026-11-01"))), update.effects)
    }

    @Test
    fun tap_changesOnlyTheTappedItem() {
        val start = loaded(listOf(have("trung-ga", "2026-11-01"), notHave("tom", "2026-10-01")))
        val after = reduce(start, Tap("ca-rot")).state
        assertEquals(start.pantry - "ca-rot", after.pantry - "ca-rot")
        assertEquals(start.ingredients, after.ingredients)
    }

    // Không có đường thêm món ngoài bảng: id lạ (vd. gõ tay) bị bỏ qua.
    @Test
    fun tap_unknownId_isIgnored() {
        val start = loaded(listOf(have("trung-ga", "2026-11-01")))
        val update = reduce(start, Tap("banh-mi-tu-go"))
        assertEquals(start, update.state)
        assertEquals(emptyList(), update.effects)
    }

    // --- chạm nhầm 2 lần trở về như cũ ---

    @Test
    fun doubleTap_haveItem_restoresExactStateAndOriginalDate() {
        val start = loaded(listOf(have("trung-ga", "2026-11-01")))
        val (end, effects) = run(start, listOf(Tap("trung-ga"), Tap("trung-ga")))
        assertEquals(start, end)
        assertEquals(
            listOf<PantryGridEffect>(
                SaveHave("trung-ga", false, d("2026-11-01")),
                SaveHave("trung-ga", true, d("2026-11-01")),
            ),
            effects,
        )
    }

    @Test
    fun doubleTap_notHaveItem_restoresExactStateAndOriginalDate() {
        val start = loaded(listOf(notHave("tom", "2026-10-01")))
        val (end, effects) = run(start, listOf(Tap("tom"), Tap("tom")))
        assertEquals(start, end)
        assertEquals(
            listOf<PantryGridEffect>(
                SaveHave("tom", true, today),
                SaveHave("tom", false, d("2026-10-01")),
            ),
            effects,
        )
    }

    @Test
    fun doubleTap_absentItem_restoresExactState() {
        val start = loaded()
        val (end, effects) = run(start, listOf(Tap("ca-chua"), Tap("ca-chua")))
        assertEquals(start, end)
        assertFalse("ca-chua" in end.pantry)
        assertEquals(
            listOf<PantryGridEffect>(SaveHave("ca-chua", true, today), PantryGridEffect.DeleteHave("ca-chua")),
            effects,
        )
    }

    @Test
    fun tripleTap_equalsSingleTap() {
        val start = loaded(listOf(have("trung-ga", "2026-11-01")))
        val once = reduce(start, Tap("trung-ga")).state
        val thrice = run(start, listOf(Tap("trung-ga"), Tap("trung-ga"), Tap("trung-ga"))).first
        assertEquals(once, thrice)
    }

    // ViewModel nạp lại dữ liệu từ repository sau mỗi lần lưu; chạm lại vẫn phải trả về ngày cũ.
    @Test
    fun doubleTap_withRepositoryEchoInBetween_restoresOriginalDate() {
        val start = loaded(listOf(have("trung-ga", "2026-11-01")))
        val (end, effects) = run(
            start,
            listOf(
                Tap("trung-ga"),
                Loaded(table(), listOf(notHave("trung-ga", "2026-11-01")), today),
                Tap("trung-ga"),
            ),
        )
        assertEquals(have("trung-ga", "2026-11-01"), end.pantry["trung-ga"])
        assertEquals(SaveHave("trung-ga", true, d("2026-11-01")), effects.last())
    }

    // --- hiển thị hạn ước tính qua daysLeft/urgencyOf ---

    @Test
    fun cells_followTableOrder_andShowHaveFlag() {
        val state = loaded(listOf(have("tom", "2026-11-05"), notHave("thit-bo", "2026-11-01")))
        val cells = cellsOf(state)
        assertEquals(table().map { it.id }, cells.map { it.ingredientId })
        assertEquals(table().map { it.nameVi }, cells.map { it.nameVi })
        assertEquals(setOf("tom"), cells.filter { it.have }.map { it.ingredientId }.toSet())
    }

    @Test
    fun cells_notHave_hasNoDaysLeftOrUrgency() {
        val state = loaded(listOf(notHave("thit-bo", "2026-11-04")))
        assertNull(cell(state, "thit-bo").daysLeft)
        assertNull(cell(state, "thit-bo").urgency)
        assertNull(cell(state, "ca-rot").daysLeft)
        assertNull(cell(state, "ca-rot").urgency)
    }

    @Test
    fun cells_daysLeftAndUrgency_matchShelfFunctions() {
        val ingredients = table().map { if (it.id == "ca-rot") it.copy(userShelfDays = 3) else it }
        val pantry = listOf(
            have("thit-heo", "2026-11-04"), // 2 ngày → còn 1, SOON
            have("khoai-tay", "2026-11-01"), // 21 ngày → còn 17, OK
            have("tom", "2026-11-01"), // 1 ngày → còn -3, EXPIRED
            have("ca-rot", "2026-11-01"), // người dùng sửa 3 (mặc định 14) → còn -1, EXPIRED
        )
        val state = loaded(pantry, ingredients)
        val expected = mapOf(
            "thit-heo" to (1 to Urgency.SOON),
            "khoai-tay" to (17 to Urgency.OK),
            "tom" to (-3 to Urgency.EXPIRED),
            "ca-rot" to (-1 to Urgency.EXPIRED),
        )
        for ((id, pair) in expected) {
            val c = cell(state, id)
            val item = pantry.single { it.ingredientId == id }
            val ingredient = ingredients.single { it.id == id }
            assertEquals(pair.first, c.daysLeft, "daysLeft của $id")
            assertEquals(pair.second, c.urgency, "urgency của $id")
            assertEquals(daysLeft(item, ingredient, today), c.daysLeft)
            assertEquals(urgencyOf(daysLeft(item, ingredient, today)), c.urgency)
        }
    }

    @Test
    fun cells_useTodayFromState() {
        val pantry = listOf(have("khoai-tay", "2026-11-01"))
        assertEquals(17, cell(loaded(pantry, day = d("2026-11-05")), "khoai-tay").daysLeft)
        assertEquals(12, cell(loaded(pantry, day = d("2026-11-10")), "khoai-tay").daysLeft)
    }

    @Test
    fun cells_afterTap_showFullShelfDays() {
        val state = reduce(loaded(), Tap("cai-ngot")).state
        assertEquals(3, cell(state, "cai-ngot").daysLeft)
        assertEquals(Urgency.OK, cell(state, "cai-ngot").urgency)
    }

    // --- sửa hạn ≤3 chạm: mở → chọn → lưu ---

    @Test
    fun editShelf_openPickSave_inThreeTaps_overridesUserShelfDays() {
        val start = loaded(listOf(have("khoai-tay", "2026-11-01")))
        val days = SHELF_DAY_OPTIONS.first { it != 21 }
        val actions = listOf(OpenShelfEditor("khoai-tay"), PickShelfDays(days), SaveShelfEditor)
        val (end, effects) = run(start, actions)

        assertTrue(userTapCount(actions) <= 3)
        assertEquals(days, end.ingredients.single { it.id == "khoai-tay" }.userShelfDays)
        assertEquals(21, end.ingredients.single { it.id == "khoai-tay" }.defaultShelfDays)
        assertNull(end.editor)
        assertEquals(listOf<PantryGridEffect>(SaveShelfDays("khoai-tay", days)), effects)
        val expectedLeft = daysLeft(have("khoai-tay", "2026-11-01"), ing("khoai-tay", 21, days), today)
        assertEquals(expectedLeft, cell(end, "khoai-tay").daysLeft)
    }

    @Test
    fun editShelf_changesOnlyThatIngredient_andNotThePantry() {
        val start = loaded(listOf(have("khoai-tay", "2026-11-01"), have("tom", "2026-11-04")))
        val end = run(start, listOf(OpenShelfEditor("khoai-tay"), PickShelfDays(SHELF_DAY_OPTIONS.first()), SaveShelfEditor)).first
        assertEquals(start.pantry, end.pantry)
        assertEquals(start.ingredients.filter { it.id != "khoai-tay" }, end.ingredients.filter { it.id != "khoai-tay" })
        assertEquals(start.ingredients.map { it.id }, end.ingredients.map { it.id })
    }

    @Test
    fun openEditor_preselectsCurrentUserShelfDays_orNullWhenNone() {
        val ingredients = table().map { if (it.id == "ca-rot") it.copy(userShelfDays = 5) else it }
        val state = loaded(ingredients = ingredients)
        assertEquals(ShelfEditor("ca-rot", 5), reduce(state, OpenShelfEditor("ca-rot")).state.editor)
        assertEquals(ShelfEditor("khoai-tay", null), reduce(state, OpenShelfEditor("khoai-tay")).state.editor)
    }

    @Test
    fun openEditor_emitsNoEffect_andChangesNothingElse() {
        val start = loaded(listOf(have("khoai-tay", "2026-11-01")))
        val update = reduce(start, OpenShelfEditor("khoai-tay"))
        assertEquals(emptyList(), update.effects)
        assertEquals(start.copy(editor = ShelfEditor("khoai-tay", null)), update.state)
    }

    @Test
    fun openEditor_unknownId_isIgnored() {
        val start = loaded()
        val update = reduce(start, OpenShelfEditor("banh-mi-tu-go"))
        assertEquals(start, update.state)
        assertEquals(emptyList(), update.effects)
    }

    @Test
    fun pickShelfDays_updatesSelectionOnly() {
        val opened = reduce(loaded(), OpenShelfEditor("khoai-tay")).state
        val days = SHELF_DAY_OPTIONS.last()
        val update = reduce(opened, PickShelfDays(days))
        assertEquals(ShelfEditor("khoai-tay", days), update.state.editor)
        assertEquals(opened.ingredients, update.state.ingredients)
        assertEquals(emptyList(), update.effects)
    }

    @Test
    fun pickShelfDays_valueNotInFixedOptions_isIgnored() {
        val opened = reduce(loaded(), OpenShelfEditor("khoai-tay")).state
        val notOffered = listOf(0, -1, 366, (1..365).first { it !in SHELF_DAY_OPTIONS })
        for (days in notOffered) {
            val update = reduce(opened, PickShelfDays(days))
            assertEquals(opened, update.state, "PickShelfDays($days) phải bị bỏ qua")
            assertEquals(emptyList(), update.effects)
        }
    }

    @Test
    fun pickNull_thenSave_resetsToSuggestedShelfDays() {
        val ingredients = table().map { if (it.id == "ca-rot") it.copy(userShelfDays = 5) else it }
        val (end, effects) = run(
            loaded(ingredients = ingredients),
            listOf(OpenShelfEditor("ca-rot"), PickShelfDays(null), SaveShelfEditor),
        )
        assertNull(end.ingredients.single { it.id == "ca-rot" }.userShelfDays)
        assertEquals(listOf<PantryGridEffect>(SaveShelfDays("ca-rot", null)), effects)
    }

    @Test
    fun cancelEditor_discardsSelection_withoutEffects() {
        val start = loaded(listOf(have("khoai-tay", "2026-11-01")))
        val (end, effects) = run(
            start,
            listOf(OpenShelfEditor("khoai-tay"), PickShelfDays(SHELF_DAY_OPTIONS.first()), CancelShelfEditor),
        )
        assertEquals(start, end)
        assertEquals(emptyList(), effects)
    }

    @Test
    fun pickAndSave_withoutOpenEditor_areIgnored() {
        val start = loaded(listOf(have("khoai-tay", "2026-11-01")))
        val (end, effects) = run(start, listOf(PickShelfDays(SHELF_DAY_OPTIONS.first()), SaveShelfEditor, CancelShelfEditor))
        assertEquals(start, end)
        assertEquals(emptyList(), effects)
    }

    @Test
    fun shelfDayOptions_areFixedSortedAndFitOneScreen() {
        assertTrue(SHELF_DAY_OPTIONS.isNotEmpty())
        assertTrue(SHELF_DAY_OPTIONS.size <= 12, "Tối đa 12 lựa chọn để chọn bằng 1 chạm")
        assertEquals(SHELF_DAY_OPTIONS.sorted().distinct(), SHELF_DAY_OPTIONS)
        assertTrue(SHELF_DAY_OPTIONS.all { it in 1..365 })
        assertTrue(SHELF_DAY_OPTIONS.containsAll(listOf(1, 2, 3, 7)))
    }

    // --- kịch bản 10 món: mỗi món 1 chạm từ lưới, ngưỡng 3 chạm/món ---

    private val tenIds = listOf(
        "thit-heo", "ca-hoi", "rau-muong", "cai-ngot", "ca-rot",
        "khoai-tay", "ca-chua", "trung-ga", "sua-tuoi", "tom",
    )

    @Test
    fun addTenItems_takesOneTapEach() {
        val actions = tenIds.map { Tap(it) }
        val (end, effects) = run(loaded(), actions)

        assertEquals(10, userTapCount(actions))
        assertTrue(userTapCount(actions) <= 3 * tenIds.size)
        for (id in tenIds) {
            assertEquals(have(id, "2026-11-05"), end.pantry[id], "món $id")
        }
        assertEquals(tenIds.map { SaveHave(it, true, today) }, effects)
        assertEquals(tenIds.toSet(), cellsOf(end).filter { it.have }.map { it.ingredientId }.toSet())
    }

    @Test
    fun addTenItems_withTwoShelfEdits_staysWithinThreeTapsPerItem() {
        val days = SHELF_DAY_OPTIONS.first()
        val actions: List<PantryGridAction> = tenIds.map { Tap(it) } +
            listOf(OpenShelfEditor("ca-rot"), PickShelfDays(days), SaveShelfEditor) +
            listOf(OpenShelfEditor("trung-ga"), PickShelfDays(days), SaveShelfEditor)
        val (end, effects) = run(loaded(), actions)

        assertEquals(16, userTapCount(actions))
        assertTrue(userTapCount(actions) <= 3 * tenIds.size)
        assertEquals(10, cellsOf(end).count { it.have })
        assertEquals(days, end.ingredients.single { it.id == "ca-rot" }.userShelfDays)
        assertEquals(days, end.ingredients.single { it.id == "trung-ga" }.userShelfDays)
        assertEquals(12, effects.size)
    }

    // --- không có hành động nhập chữ tự do ---

    // `when` không có else: thêm một Action mới (vd. gõ tên đồ) sẽ làm test không biên dịch, buộc phải xem lại spec.
    private fun freeTextOf(action: PantryGridAction): String? = when (action) {
        is Loaded -> null
        is Tap -> null
        is AddIngredient -> null
        is IncreaseQuantity -> null
        is DecreaseQuantity -> null
        is ConsumeIngredient -> null
        is OpenShelfEditor -> null
        is PickShelfDays -> null
        SaveShelfEditor -> null
        CancelShelfEditor -> null
        is OpenUnitEditor -> null
        is PickUnit -> null
        SaveUnitEditor -> null
        CancelUnitEditor -> null
    }

    @Test
    fun actions_haveNoFreeTextInput() {
        val all: List<PantryGridAction> = listOf(
            Loaded(table(), emptyList(), today),
            Tap("tom"),
            AddIngredient("tom", SHELF_DAY_OPTIONS.first()),
            IncreaseQuantity("tom"),
            DecreaseQuantity("tom"),
            ConsumeIngredient("tom"),
            OpenShelfEditor("tom"),
            PickShelfDays(SHELF_DAY_OPTIONS.first()),
            PickShelfDays(null),
            SaveShelfEditor,
            CancelShelfEditor,
            OpenUnitEditor("tom"),
            PickUnit("quả"),
            SaveUnitEditor,
            CancelUnitEditor,
        )
        assertTrue(all.all { freeTextOf(it) == null })
    }

    // --- reducer thuần: tất định, không đổi state đầu vào ---

    private fun fixture(): PantryGridState {
        val ingredients = table().map { if (it.id == "ca-rot") it.copy(userShelfDays = 5) else it }
        val base = loaded(listOf(have("trung-ga", "2026-11-01"), notHave("tom", "2026-10-01")), ingredients)
        return reduce(base, OpenShelfEditor("ca-rot")).state
    }

    private val sampleActions: List<PantryGridAction> = listOf(
        Loaded(table(), listOf(have("thit-bo", "2026-11-04")), d("2026-11-06")),
        Tap("trung-ga"),
        Tap("tom"),
        Tap("ca-chua"),
        OpenShelfEditor("khoai-tay"),
        PickShelfDays(null),
        SaveShelfEditor,
        CancelShelfEditor,
    )

    @Test
    fun reduce_doesNotMutateInputState() {
        for (action in sampleActions) {
            val input = fixture()
            reduce(input, action)
            assertEquals(fixture(), input, "state đầu vào bị đổi bởi $action")
        }
    }

    @Test
    fun reduce_isDeterministic() {
        for (action in sampleActions) {
            assertEquals(reduce(fixture(), action), reduce(fixture(), action), "kết quả khác nhau cho $action")
        }
    }
}
