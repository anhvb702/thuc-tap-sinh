package com.loitu.yourrefri.feature.recipe.ui

import com.loitu.yourrefri.feature.notice.logic.defrostNamesForDay
import com.loitu.yourrefri.feature.notice.logic.nextDailyTrigger
import com.loitu.yourrefri.feature.notice.logic.planDailyNotice
import com.loitu.yourrefri.feature.notice.logic.planDefrostNotice
import com.loitu.yourrefri.feature.pantry.data.Ingredient
import com.loitu.yourrefri.feature.pantry.data.LeftoverItem
import com.loitu.yourrefri.feature.pantry.data.LeftoverRepository
import com.loitu.yourrefri.feature.pantry.data.PantryCompartment
import com.loitu.yourrefri.feature.pantry.data.PantryItem
import com.loitu.yourrefri.feature.pantry.data.PantryRepository
import com.loitu.yourrefri.feature.pantry.logic.PantryGridAction
import com.loitu.yourrefri.feature.pantry.ui.PantryGridViewModel
import com.loitu.yourrefri.feature.recipe.data.ExtraShoppingItem
import com.loitu.yourrefri.feature.recipe.data.ExtraShoppingRepository
import com.loitu.yourrefri.feature.recipe.data.MealSlot
import com.loitu.yourrefri.feature.recipe.data.MealSlotKey
import com.loitu.yourrefri.feature.recipe.data.Recipe
import com.loitu.yourrefri.feature.recipe.data.Slot
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanEntry
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanRepository
import com.loitu.yourrefri.feature.recipe.data.WeeklyMealPlanSettings
import com.loitu.yourrefri.feature.recipe.logic.buildWeeklyMealSchedule
import com.loitu.yourrefri.feature.recipe.logic.buildWeeklyShoppingList
import com.loitu.yourrefri.feature.recipe.logic.shoppingIdsToStock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class WeeklyLoopIntegrationTest {
    private val monday = LocalDate(2026, 11, 9)
    private val sunday = LocalDate(2026, 11, 15)
    private val tz = TimeZone.of("UTC+07:00")

    private val ingredients = listOf(
        Ingredient(id = "thit-heo", nameVi = "Thịt heo", group = "thit", defaultShelfDays = 3, freezerShelfDays = 30),
        Ingredient(id = "egg", nameVi = "Trứng gà", group = "trung", defaultShelfDays = 14, freezerShelfDays = 30),
        Ingredient(id = "tomato", nameVi = "Cà chua", group = "rau", defaultShelfDays = 5, freezerShelfDays = 30),
        Ingredient(id = "garlic", nameVi = "Tỏi", group = "gia-vi", defaultShelfDays = 30, freezerShelfDays = 60),
    )

    private val recipes = listOf(
        Recipe(
            id = "thit_kho",
            title = "Thịt kho tàu",
            steps = listOf("Kho thịt với trứng"),
            required = listOf(Slot(listOf("thit-heo")), Slot(listOf("egg"))),
            minutes = 30,
        ),
        Recipe(
            id = "trung_chien",
            title = "Trứng chiên cà chua",
            steps = listOf("Chiên trứng với cà chua"),
            required = listOf(Slot(listOf("egg")), Slot(listOf("tomato"))),
            minutes = 15,
        ),
    )

    @Test
    fun smartWeekendLoop_presetPersistDisableMealExtraShoppingAndStocking() = runTest {
        val clock = FixedClock(Instant.parse("2026-11-09T02:00:00Z")) // Monday 09:00 UTC+7
        val pantryRepo = FakePantryRepository()
        val leftoverRepo = FakeLeftoverRepository()
        val extraRepo = FakeExtraShoppingRepository()
        val weeklyRepo = FakeWeeklyMealPlanRepository()

        val pantryVm = PantryGridViewModel(
            repository = pantryRepo,
            ingredients = ingredients,
            clock = clock,
            timeZone = tz,
            scope = backgroundScope,
            leftoverRepository = leftoverRepo,
            extraShoppingRepository = extraRepo,
        )
        val weeklyVm = WeeklyMealPlanViewModel(
            repository = weeklyRepo,
            clock = clock,
            timeZone = tz,
            scope = backgroundScope,
            extraShoppingRepository = extraRepo,
        )
        runCurrent()

        // 1. Apply ONE_MEAL then TWO_MEALS preset and persist schedule
        weeklyVm.dispatch(WeeklyMealPlanAction.ApplyMealPreset(WeeklyMealPreset.ONE_MEAL))
        runCurrent()
        assertEquals(7, weeklyVm.state.value.disabledMeals.size)
        assertTrue(weeklyVm.state.value.disabledMeals.all { it.slot == MealSlot.LUNCH })

        weeklyVm.dispatch(WeeklyMealPlanAction.ApplyMealPreset(WeeklyMealPreset.TWO_MEALS))
        runCurrent()
        assertTrue(weeklyVm.state.value.disabledMeals.isEmpty())

        weeklyVm.syncSchedule(recipes, pantryVm.state.value)
        runCurrent()
        assertEquals(14, weeklyVm.state.value.savedEntries.size)
        assertEquals(14, weeklyRepo.entries.value.size)

        // 2. Disable Monday dinner: schedule retains the recipe, shopping list omits exclusive ingredients
        val schedule = buildWeeklyMealSchedule(
            recipes = recipes,
            state = pantryVm.state.value,
            goal = weeklyVm.state.value.goal,
            preferences = weeklyVm.state.value.preferences,
            swappedOffsets = weeklyVm.state.value.swappedOffsets,
            includeLunchMealPrep = weeklyVm.state.value.includeLunchMealPrep,
        )
        val shoppingBeforeDisable = buildWeeklyShoppingList(
            plan = schedule,
            state = pantryVm.state.value,
            disabledMeals = weeklyVm.state.value.disabledMeals,
            weekStart = weeklyVm.state.value.weekStart,
        )
        val mondayDinnerId = weeklyVm.state.value.savedEntries
            .first { it.day == monday && it.meal == MealSlot.DINNER }
            .recipeId

        weeklyVm.dispatch(WeeklyMealPlanAction.ToggleMealSlot(0, MealSlot.DINNER))
        runCurrent()

        assertEquals(
            mondayDinnerId,
            weeklyVm.state.value.savedEntries.first { it.day == monday && it.meal == MealSlot.DINNER }.recipeId,
        )
        val shoppingAfterDisable = buildWeeklyShoppingList(
            plan = schedule,
            state = pantryVm.state.value,
            disabledMeals = weeklyVm.state.value.disabledMeals,
            weekStart = weeklyVm.state.value.weekStart,
        )
        val totalQtyBefore = shoppingBeforeDisable.sumOf { it.mealCount }
        val totalQtyAfter = shoppingAfterDisable.sumOf { it.mealCount }
        assertTrue(totalQtyAfter < totalQtyBefore)

        // 3. Add "Cần mua thêm" item from pantry and stock combined weekly + extra shopping
        pantryVm.addToExtraShopping("garlic")
        runCurrent()
        assertEquals(listOf("garlic"), weeklyVm.state.value.extraShoppingItems.map { it.ingredientId })

        val weeklyIds = shoppingIdsToStock(shoppingAfterDisable, weeklyVm.state.value.uncheckedIds)
        val combinedIds = combinedWeeklyStockIds(weeklyIds, weeklyVm.state.value.extraShoppingItems)
        assertTrue("garlic" in combinedIds)

        val suggestedById = shoppingAfterDisable.associate { it.ingredient.id to suggestedWeeklyCompartment(it) }
        val stockWithCompartments = weeklyStockCompartments(combinedIds, suggestedById)
        stockWithCompartments.forEach { (id, targetCompartment) ->
            val current = pantryVm.state.value.pantry[id]
            if (current?.have == true) {
                pantryVm.dispatch(PantryGridAction.IncreaseQuantity(id))
            } else {
                pantryVm.dispatch(PantryGridAction.AddIngredient(id, null))
            }
            if ((current?.compartment ?: PantryCompartment.CHILL) != targetCompartment) {
                pantryVm.dispatch(PantryGridAction.ToggleCompartment(id))
            }
        }
        weeklyVm.clearExtraShoppingItems()
        runCurrent()

        assertTrue(weeklyVm.state.value.extraShoppingItems.isEmpty())
        combinedIds.distinct().forEach { id ->
            val item = pantryVm.state.value.pantry[id]
            assertNotNull(item)
            assertTrue(item.have)
            val expectedCompartment = suggestedById[id] ?: PantryCompartment.CHILL
            assertEquals(expectedCompartment, item.compartment)
        }
    }

    @Test
    fun smartWeekendLoop_defrostNoticeSundayMergeAnd0800Shift_forFrozenIngredientAndLeftover() = runTest {
        val clock = FixedClock(Instant.parse("2026-11-09T00:30:00Z")) // Monday 07:30 UTC+7
        val pantryRepo = FakePantryRepository(
            initialPantry = listOf(
                PantryItem(
                    ingredientId = "thit-heo",
                    have = true,
                    markedAt = monday,
                    compartment = PantryCompartment.FREEZE,
                ),
            ),
        )
        val leftoverRepo = FakeLeftoverRepository()
        val weeklyRepo = FakeWeeklyMealPlanRepository()
        weeklyRepo.save(WeeklyMealPlanEntry(day = monday, meal = MealSlot.DINNER, recipeId = "thit_kho", portions = 1))
        weeklyRepo.save(WeeklyMealPlanEntry(day = sunday, meal = MealSlot.DINNER, recipeId = "thit_kho", portions = 1))

        val pantryVm = PantryGridViewModel(
            repository = pantryRepo,
            ingredients = ingredients,
            clock = clock,
            timeZone = tz,
            scope = backgroundScope,
            leftoverRepository = leftoverRepo,
        )
        val weeklyVm = WeeklyMealPlanViewModel(
            repository = weeklyRepo,
            clock = clock,
            timeZone = tz,
            scope = backgroundScope,
        )
        runCurrent()

        // Weekday frozen ingredient -> defrostNamesForDay, planDefrostNotice, planDailyNotice, 08:00 shift
        val weekdayDefrostNames = defrostNamesForDay(
            day = monday,
            weeklyPlan = weeklyVm.state.value.savedEntries,
            recipes = recipes,
            ingredients = ingredients,
            pantry = pantryVm.state.value.pantry.values.toList(),
            leftovers = pantryVm.leftovers.value,
        )
        assertEquals(listOf("Thịt heo"), weekdayDefrostNames)

        val defrostNotice = planDefrostNotice(
            today = monday,
            weeklyPlan = weeklyVm.state.value.savedEntries,
            recipes = recipes,
            ingredients = ingredients,
            pantry = pantryVm.state.value.pantry.values.toList(),
            leftovers = pantryVm.leftovers.value,
        )
        assertNotNull(defrostNotice)
        assertTrue("Thịt heo" in defrostNotice.body)

        val weekdayNotice = planDailyNotice(
            pantry = pantryVm.state.value.pantry.values.toList(),
            ingredients = ingredients,
            today = monday,
            lastSent = null,
            defrostNames = weekdayDefrostNames,
        )
        assertNotNull(weekdayNotice)
        assertEquals("Chuẩn bị rã đông", weekdayNotice.title)
        assertTrue("Thịt heo" in weekdayNotice.body)

        val shiftedTrigger = nextDailyTrigger(
            enabled = true,
            now = clock.now(),
            tz = tz,
            sundayTime = LocalTime(8, 0),
            weekdayTime = LocalTime(16, 0),
            hasFrozenDinnerForDay = { day ->
                defrostNamesForDay(
                    day = day,
                    weeklyPlan = weeklyVm.state.value.savedEntries,
                    recipes = recipes,
                    ingredients = ingredients,
                    pantry = pantryVm.state.value.pantry.values.toList(),
                    leftovers = pantryVm.leftovers.value,
                ).isNotEmpty()
            },
        )
        assertEquals(Instant.parse("2026-11-09T01:00:00Z"), shiftedTrigger) // 08:00 UTC+7

        // Sunday frozen leftover -> merged Sunday shopping + defrost notice
        pantryVm.addLeftover(title = "Thịt kho tàu", compartment = PantryCompartment.FREEZE, shelfDays = 14)
        runCurrent()
        assertEquals(1, pantryVm.leftovers.value.size)

        val sundayDefrostNames = defrostNamesForDay(
            day = sunday,
            weeklyPlan = weeklyVm.state.value.savedEntries,
            recipes = recipes,
            ingredients = ingredients,
            pantry = emptyList(),
            leftovers = pantryVm.leftovers.value,
        )
        assertEquals(listOf("Thịt kho tàu"), sundayDefrostNames)

        val sundayNotice = planDailyNotice(
            pantry = emptyList(),
            ingredients = ingredients,
            today = sunday,
            lastSent = null,
            sundayShopping = true,
            leftovers = pantryVm.leftovers.value,
            defrostNames = sundayDefrostNames,
        )
        assertNotNull(sundayNotice)
        assertEquals("Cuối tuần rồi nè", sundayNotice.title)
        assertTrue("đi chợ" in sundayNotice.body)
        assertTrue("Thịt kho tàu" in sundayNotice.body)
        assertFalse(sundayNotice.isContextualNudge)
    }

    private class FixedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private class FakePantryRepository(
        initialPantry: List<PantryItem> = emptyList(),
        private val defaultDate: LocalDate = LocalDate(2026, 11, 9),
    ) : PantryRepository {
        private val items = MutableStateFlow(initialPantry)
        private val overrides = MutableStateFlow<Map<String, Int>>(emptyMap())
        private val quantities = MutableStateFlow<Map<String, Int>>(emptyMap())
        private val units = MutableStateFlow<Map<String, String>>(emptyMap())

        override fun observe(): Flow<List<PantryItem>> = items
        override fun observeShelfOverrides(): Flow<Map<String, Int>> = overrides
        override fun observeQuantities(): Flow<Map<String, Int>> = quantities
        override fun observeUnits(): Flow<Map<String, String>> = units

        override suspend fun setCompartment(ingredientId: String, compartment: PantryCompartment) {
            val current = items.value.firstOrNull { it.ingredientId == ingredientId } ?: return
            items.value = items.value.filterNot { it.ingredientId == ingredientId } + current.copy(compartment = compartment)
        }

        override suspend fun setHave(ingredientId: String, have: Boolean, markedAt: LocalDate?) {
            val existing = items.value.firstOrNull { it.ingredientId == ingredientId }
            val updated = PantryItem(
                ingredientId = ingredientId,
                have = have,
                markedAt = markedAt ?: existing?.markedAt ?: defaultDate,
                compartment = existing?.compartment ?: PantryCompartment.CHILL,
            )
            items.value = items.value.filterNot { it.ingredientId == ingredientId } + updated
        }

        override suspend fun deleteHave(ingredientId: String) {
            items.value = items.value.filterNot { it.ingredientId == ingredientId }
        }

        override suspend fun setShelfDays(ingredientId: String, days: Int?) {
            overrides.value = if (days == null) overrides.value - ingredientId else overrides.value + (ingredientId to days)
        }

        override suspend fun setQuantity(ingredientId: String, quantity: Int) {
            quantities.value = quantities.value + (ingredientId to quantity)
        }

        override suspend fun setUnit(ingredientId: String, unit: String?) {
            units.value = if (unit == null) units.value - ingredientId else units.value + (ingredientId to unit)
        }
    }

    private class FakeLeftoverRepository : LeftoverRepository {
        private val items = MutableStateFlow<List<LeftoverItem>>(emptyList())
        private var nextId = 1

        override fun observeAll(): Flow<List<LeftoverItem>> = items

        override suspend fun add(
            title: String,
            compartment: PantryCompartment,
            markedAt: LocalDate,
            shelfDays: Int,
        ): LeftoverItem {
            val item = LeftoverItem(
                id = "leftover-${nextId++}",
                title = title,
                compartment = compartment,
                markedAt = markedAt,
                shelfDays = shelfDays,
            )
            upsert(item)
            return item
        }

        override suspend fun upsert(item: LeftoverItem) {
            items.value = items.value.filterNot { it.id == item.id } + item
        }

        override suspend fun delete(id: String) {
            items.value = items.value.filterNot { it.id == id }
        }
    }

    private class FakeExtraShoppingRepository : ExtraShoppingRepository {
        private val items = MutableStateFlow<List<ExtraShoppingItem>>(emptyList())

        override fun observeAll(): Flow<List<ExtraShoppingItem>> = items

        override suspend fun add(ingredientId: String, addedAt: LocalDate) {
            items.value = items.value.filterNot { it.ingredientId == ingredientId } + ExtraShoppingItem(ingredientId, addedAt)
        }

        override suspend fun remove(ingredientId: String) {
            items.value = items.value.filterNot { it.ingredientId == ingredientId }
        }

        override suspend fun clear() {
            items.value = emptyList()
        }
    }

    private class FakeWeeklyMealPlanRepository : WeeklyMealPlanRepository {
        val saved = MutableStateFlow<WeeklyMealPlanSettings?>(null)
        val disabledMeals = MutableStateFlow<List<MealSlotKey>>(emptyList())
        val entries = MutableStateFlow<List<WeeklyMealPlanEntry>>(emptyList())

        override fun observeDisabledMeals(): Flow<List<MealSlotKey>> = disabledMeals

        override suspend fun setMealEnabled(day: LocalDate, slot: MealSlot, enabled: Boolean) {
            val key = MealSlotKey(day, slot)
            disabledMeals.value = if (enabled) {
                disabledMeals.value.filterNot { it == key }
            } else {
                (disabledMeals.value + key).distinct()
            }
        }

        override fun observeAll(): Flow<List<WeeklyMealPlanEntry>> = entries

        override suspend fun save(entry: WeeklyMealPlanEntry) {
            entries.value = entries.value.filterNot { it.day == entry.day && it.meal == entry.meal } + entry
        }

        override fun observeSettings(): Flow<WeeklyMealPlanSettings?> = saved

        override suspend fun saveSettings(settings: WeeklyMealPlanSettings) {
            saved.value = settings
        }
    }
}
