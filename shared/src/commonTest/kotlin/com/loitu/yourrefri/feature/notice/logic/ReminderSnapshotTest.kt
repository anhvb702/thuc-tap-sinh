@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.loitu.yourrefri.feature.notice.logic

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.LeftoverItem
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.MealSlotKey
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanEntry
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Spec: docs/tests/R6-01a.md
class ReminderSnapshotTest {
    private val today = LocalDate(2026, 11, 13)
    private val dinnerRecipe = Recipe("dinner", "Cá kho", listOf("Nấu"), listOf(Slot(listOf("ca-hoi"))))
    private val ingredients = listOf(
        Ingredient("ca-hoi", "Cá hồi", "ca", 2),
        Ingredient("custom-chicken", "Gà", "thit", 3),
    )

    private fun dinner(day: LocalDate = today, recipeId: String = "dinner") =
        WeeklyMealPlanEntry(day, MealSlot.DINNER, recipeId, 2)

    private fun frozenFish(day: LocalDate = today) =
        PantryItem("ca-hoi", true, day, PantryCompartment.FREEZE)

    @Test
    fun bữaTốiDùngĐồNgănĐông_trảVềTênĐồ() {
        val snapshot = ReminderSnapshot(
            weeklyPlan = listOf(dinner()),
            recipes = listOf(dinnerRecipe),
            ingredients = ingredients,
            pantry = listOf(frozenFish()),
        )

        assertEquals(listOf("Cá hồi"), snapshot.defrostNamesFor(today))
        assertTrue(snapshot.hasFrozenDinnerFor(today))
    }

    @Test
    fun ngàyKhôngCóBữaTốiĐãXếp_trảVềRỗng() {
        val snapshot = ReminderSnapshot(
            weeklyPlan = emptyList(),
            recipes = listOf(dinnerRecipe),
            ingredients = ingredients,
            pantry = listOf(frozenFish()),
        )

        assertEquals(emptyList(), snapshot.defrostNamesFor(today))
        assertFalse(snapshot.hasFrozenDinnerFor(today))
    }

    @Test
    fun bữaTốiBịTắt_khôngTínhRãĐông() {
        val snapshot = ReminderSnapshot(
            weeklyPlan = listOf(dinner()),
            recipes = listOf(dinnerRecipe),
            ingredients = ingredients,
            pantry = listOf(frozenFish()),
            disabledMeals = listOf(MealSlotKey(today, MealSlot.DINNER)),
        )

        assertEquals(emptyList(), snapshot.defrostNamesFor(today))
        assertFalse(snapshot.hasFrozenDinnerFor(today))
    }

    @Test
    fun mónTựThêmCóNguyênLiệuNgănĐông_đượcTínhNhư_mónBundle() {
        val customRecipe = Recipe("custom-1", "Gà om", listOf("Nấu"), listOf(Slot(listOf("custom-chicken"))))
        val snapshot = ReminderSnapshot(
            weeklyPlan = listOf(dinner(recipeId = "custom-1")),
            recipes = listOf(dinnerRecipe) + customRecipe,
            ingredients = ingredients,
            pantry = listOf(PantryItem("custom-chicken", true, today, PantryCompartment.FREEZE)),
        )

        assertEquals(listOf("Gà"), snapshot.defrostNamesFor(today))
        assertTrue(snapshot.hasFrozenDinnerFor(today))
    }

    @Test
    fun leftoverNgănĐôngCùngMónBữaTối_đượcTínhQuaSnapshot() {
        // Nguyên liệu không ở ngăn đông, nên chỉ đồ thừa ngăn đông mới đưa món này vào danh sách.
        val snapshot = ReminderSnapshot(
            weeklyPlan = listOf(dinner()),
            recipes = listOf(dinnerRecipe),
            ingredients = ingredients,
            pantry = listOf(PantryItem("ca-hoi", true, today, PantryCompartment.CHILL)),
            leftovers = listOf(LeftoverItem("lo-freeze", "Cá kho", PantryCompartment.FREEZE, today, 14)),
        )

        val expected = defrostNamesForDay(
            day = today,
            weeklyPlan = listOf(dinner()),
            recipes = listOf(dinnerRecipe),
            ingredients = ingredients,
            pantry = listOf(PantryItem("ca-hoi", true, today, PantryCompartment.CHILL)),
            leftovers = listOf(LeftoverItem("lo-freeze", "Cá kho", PantryCompartment.FREEZE, today, 14)),
        )
        assertEquals(listOf("Cá kho"), expected)
        assertEquals(expected, snapshot.defrostNamesFor(today))
    }

    @Test
    fun chủNhậtCóRãĐông_vẫnGiữMốcĐiChợCủaChủNhật() {
        // Chủ Nhật 2026-11-15: có rã đông, nhưng mốc đi chợ (10:00) không bị ghi đè bởi 08:00 rã đông.
        val sunday = LocalDate(2026, 11, 15)
        val snapshot = ReminderSnapshot(
            weeklyPlan = listOf(dinner(day = sunday)),
            recipes = listOf(dinnerRecipe),
            ingredients = ingredients,
            pantry = listOf(frozenFish(day = sunday)),
        )
        assertTrue(snapshot.hasFrozenDinnerFor(sunday))

        val trigger = nextReminderTrigger(
            enabled = true,
            now = Instant.parse("2026-11-14T18:00:00Z"),
            tz = TimeZone.UTC,
            sundayTime = LocalTime(10, 0),
            weekdayTime = LocalTime(16, 0),
            hasFrozenDinnerForDay = snapshot::hasFrozenDinnerFor,
        )

        assertEquals(Instant.parse("2026-11-15T10:00:00Z"), trigger)
    }
}
