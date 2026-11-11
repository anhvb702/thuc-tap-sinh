@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)

package com.loitu.yourrefri.feature.pantry.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.loitu.yourrefri.shared.db.YourRefriDatabase
import com.loitu.yourrefri.shared.db.createDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// Đồng hồ giả: thời điểm cố định do test đặt. Năm 2026-03 cố ý khác ngày thật để lộ code lén dùng Clock.System.
private class FakeClock(var current: Instant) : Clock {
    override fun now(): Instant = current
}

class PantryRepositoryTest {

    private lateinit var driver: SqlDriver
    private lateinit var db: YourRefriDatabase

    private val utc: TimeZone = TimeZone.UTC
    private val plus7: TimeZone = UtcOffset(hours = 7).asTimeZone()
    private val minus5: TimeZone = UtcOffset(hours = -5).asTimeZone()

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        YourRefriDatabase.Schema.create(driver)
        db = createDatabase(driver)
    }

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    private fun d(iso: String): LocalDate = LocalDate.parse(iso)
    private fun at(iso: String): Instant = Instant.parse(iso)

    private fun TestScope.repo(clock: Clock, timeZone: TimeZone = utc): PantryRepositoryImpl =
        PantryRepositoryImpl(db, clock, timeZone, StandardTestDispatcher(testScheduler))

    @Test
    fun deleteHave_removesOnlyPantryRow() = runTest {
        val repository = repo(FakeClock(at("2026-03-10T12:00:00Z")))
        repository.setHave("egg", true)
        repository.setShelfDays("egg", 8)
        repository.setQuantity("egg", 3)
        repository.setUnit("egg", "quả")

        repository.deleteHave("egg")

        assertTrue(repository.observe().first().isEmpty())
        assertEquals(8L, db.userShelfEntityQueries.selectById("egg").executeAsOne())
        assertEquals(3L, db.pantryQuantityEntityQueries.selectById("egg").executeAsOne())
        assertEquals("quả", db.pantryUnitEntityQueries.selectById("egg").executeAsOne())
    }

    // ---------------- setHave(true): markedAt lấy từ Clock + TimeZone được tiêm ----------------

    @Test
    fun setHave_true_newItem_marksDateFromInjectedClock() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T05:00:00Z")))

        repo.setHave("egg", true)

        assertEquals(listOf(PantryItem("egg", true, d("2026-03-10"))), repo.observe().first())
    }

    @Test
    fun setHave_true_at2330Utc_withUtcPlus7_marksNextDay() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T23:30:00Z")), plus7)

        repo.setHave("egg", true)

        assertEquals(listOf(PantryItem("egg", true, d("2026-03-11"))), repo.observe().first())
    }

    @Test
    fun setHave_true_at2330Utc_withUtc_marksSameDay() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T23:30:00Z")), utc)

        repo.setHave("egg", true)

        assertEquals(listOf(PantryItem("egg", true, d("2026-03-10"))), repo.observe().first())
    }

    @Test
    fun setHave_true_at0300Utc_withUtcMinus5_marksPreviousDay() = runTest {
        val repo = repo(FakeClock(at("2026-03-11T03:00:00Z")), minus5)

        repo.setHave("egg", true)

        assertEquals(listOf(PantryItem("egg", true, d("2026-03-10"))), repo.observe().first())
    }

    @Test
    fun setHave_true_newYearsEveUtc_withUtcPlus7_marksJan1() = runTest {
        val repo = repo(FakeClock(at("2026-12-31T17:00:00Z")), plus7)

        repo.setHave("egg", true)

        assertEquals(listOf(PantryItem("egg", true, d("2027-01-01"))), repo.observe().first())
    }

    @Test
    fun setHave_true_storesIsoDateString_inDb() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T23:30:00Z")), plus7)

        repo.setHave("egg", true)

        assertEquals("2026-03-11", db.pantryEntityQueries.selectById("egg").executeAsOne().markedAt)
    }

    // ---------------- setHave(true) lần nữa / setHave(false) ----------------

    @Test
    fun setHave_trueAgain_updatesMarkedAtToNewDate() = runTest {
        val clock = FakeClock(at("2026-03-10T05:00:00Z"))
        val repo = repo(clock)
        repo.setHave("egg", true)

        clock.current = clock.current + 2.days
        repo.setHave("egg", true)

        assertEquals(listOf(PantryItem("egg", true, d("2026-03-12"))), repo.observe().first())
    }

    @Test
    fun setHave_false_keepsRow_withHaveFalse_andOldMarkedAt() = runTest {
        val clock = FakeClock(at("2026-03-10T05:00:00Z"))
        val repo = repo(clock)
        repo.setHave("egg", true)

        clock.current = clock.current + 3.days
        repo.setHave("egg", false)

        assertEquals(listOf(PantryItem("egg", false, d("2026-03-10"))), repo.observe().first())
    }

    @Test
    fun setHave_trueAfterFalse_marksHaveWithNewDate() = runTest {
        val clock = FakeClock(at("2026-03-10T05:00:00Z"))
        val repo = repo(clock)
        repo.setHave("egg", true)
        clock.current = clock.current + 1.days
        repo.setHave("egg", false)

        clock.current = clock.current + 3.days
        repo.setHave("egg", true)

        assertEquals(listOf(PantryItem("egg", true, d("2026-03-14"))), repo.observe().first())
    }

    @Test
    fun setHave_false_unknownId_createsNoRow() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T05:00:00Z")))

        repo.setHave("egg", false)

        assertTrue(repo.observe().first().isEmpty())
    }

    @Test
    fun setHave_changesOnlyTargetItem() = runTest {
        val clock = FakeClock(at("2026-03-10T05:00:00Z"))
        val repo = repo(clock)
        repo.setHave("egg", true)
        repo.setHave("rice", true)

        clock.current = clock.current + 2.days
        repo.setHave("egg", false)

        assertEquals(
            listOf(PantryItem("egg", false, d("2026-03-10")), PantryItem("rice", true, d("2026-03-10"))),
            repo.observe().first(),
        )
    }

    @Test
    fun observe_isSortedByIngredientId() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T05:00:00Z")))
        repo.setHave("rice", true)
        repo.setHave("egg", true)
        repo.setHave("pork", true)

        assertEquals(listOf("egg", "pork", "rice"), repo.observe().first().map { it.ingredientId })
    }

    @Test
    fun observe_emitsAgain_whenPantryChanges() = runTest {
        val clock = FakeClock(at("2026-03-10T05:00:00Z"))
        val repo = repo(clock)
        val seen = mutableListOf<List<PantryItem>>()
        backgroundScope.launch { repo.observe().collect { seen += it } }
        runCurrent()
        assertEquals(emptyList<PantryItem>(), seen.single())

        repo.setHave("egg", true)
        runCurrent()
        assertEquals(listOf(PantryItem("egg", true, d("2026-03-10"))), seen.last())

        clock.current = clock.current + 1.days
        repo.setHave("egg", false)
        runCurrent()
        assertEquals(listOf(PantryItem("egg", false, d("2026-03-10"))), seen.last())
    }

    // ---------------- setHave(..., markedAt): khôi phục ngày cũ khi chạm nhầm ----------------

    @Test
    fun setHave_true_withExplicitMarkedAt_writesThatPastDate() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T05:00:00Z")))

        repo.setHave("egg", true, markedAt = d("2026-03-02"))

        assertEquals(listOf(PantryItem("egg", true, d("2026-03-02"))), repo.observe().first())
    }

    @Test
    fun setHave_true_explicitMarkedAt_ignoresClockAndTimeZone() = runTest {
        // Clock + UTC+7 sẽ ra 2026-03-11; ngày truyền vào khác hẳn để lộ code lén dùng Clock.
        val repo = repo(FakeClock(at("2026-03-10T23:30:00Z")), plus7)

        repo.setHave("egg", true, markedAt = d("2026-02-27"))

        assertEquals(listOf(PantryItem("egg", true, d("2026-02-27"))), repo.observe().first())
        assertEquals("2026-02-27", db.pantryEntityQueries.selectById("egg").executeAsOne().markedAt)
    }

    @Test
    fun setHave_true_explicitNullMarkedAt_stillUsesClockAndTimeZone() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T23:30:00Z")), plus7)

        repo.setHave("egg", true, markedAt = null)

        assertEquals(listOf(PantryItem("egg", true, d("2026-03-11"))), repo.observe().first())
    }

    @Test
    fun setHave_haveThenOutThenHave_withOldMarkedAt_restoresOriginalDate() = runTest {
        val clock = FakeClock(at("2026-03-10T05:00:00Z"))
        val repo = repo(clock)
        repo.setHave("egg", true)
        val original = repo.observe().first().single().markedAt

        clock.current = clock.current + 2.days
        repo.setHave("egg", false)
        repo.setHave("egg", true, markedAt = original)

        assertEquals(listOf(PantryItem("egg", true, d("2026-03-10"))), repo.observe().first())
    }

    @Test
    fun setHave_false_withMarkedAt_onExistingItem_overwritesMarkedAt() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T05:00:00Z")))
        repo.setHave("egg", true)

        repo.setHave("egg", false, markedAt = d("2026-03-05"))

        assertEquals(listOf(PantryItem("egg", false, d("2026-03-05"))), repo.observe().first())
    }

    @Test
    fun setHave_false_explicitNullMarkedAt_keepsOldMarkedAt() = runTest {
        val clock = FakeClock(at("2026-03-10T05:00:00Z"))
        val repo = repo(clock)
        repo.setHave("egg", true)

        clock.current = clock.current + 4.days
        repo.setHave("egg", false, markedAt = null)

        assertEquals(listOf(PantryItem("egg", false, d("2026-03-10"))), repo.observe().first())
    }

    @Test
    fun setHave_false_withMarkedAt_unknownId_createsNoRow() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T05:00:00Z")))

        repo.setHave("egg", false, markedAt = d("2026-03-05"))

        assertTrue(repo.observe().first().isEmpty())
    }

    // ---------------- setShelfDays / observeShelfOverrides ----------------

    @Test
    fun setShelfDays_isRemembered() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T05:00:00Z")))

        repo.setShelfDays("egg", 4)

        assertEquals(mapOf("egg" to 4), repo.observeShelfOverrides().first())
    }

    @Test
    fun setShelfDays_again_overwritesPreviousValue() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T05:00:00Z")))
        repo.setShelfDays("egg", 4)

        repo.setShelfDays("egg", 9)

        assertEquals(mapOf("egg" to 9), repo.observeShelfOverrides().first())
    }

    @Test
    fun setShelfDays_zero_isKeptAsOverride() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T05:00:00Z")))

        repo.setShelfDays("egg", 0)

        assertEquals(mapOf("egg" to 0), repo.observeShelfOverrides().first())
    }

    @Test
    fun setShelfDays_null_removesOnlyThatOverride() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T05:00:00Z")))
        repo.setShelfDays("egg", 4)
        repo.setShelfDays("rice", 30)

        repo.setShelfDays("egg", null)

        assertEquals(mapOf("rice" to 30), repo.observeShelfOverrides().first())
    }

    @Test
    fun setShelfDays_null_withoutOverride_isNoOp() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T05:00:00Z")))

        repo.setShelfDays("egg", null)

        assertTrue(repo.observeShelfOverrides().first().isEmpty())
    }

    @Test
    fun setShelfDays_negative_throws_andKeepsPreviousValue() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T05:00:00Z")))
        repo.setShelfDays("egg", 4)

        assertFailsWith<IllegalArgumentException> { repo.setShelfDays("egg", -1) }

        assertEquals(mapOf("egg" to 4), repo.observeShelfOverrides().first())
    }

    @Test
    fun setShelfDays_survivesMarkOut_andMarkHaveAgain() = runTest {
        val clock = FakeClock(at("2026-03-10T05:00:00Z"))
        val repo = repo(clock)
        repo.setShelfDays("egg", 4)
        repo.setHave("egg", true)

        repo.setHave("egg", false)
        clock.current = clock.current + 5.days
        repo.setHave("egg", true)

        assertEquals(mapOf("egg" to 4), repo.observeShelfOverrides().first())
    }

    @Test
    fun setShelfDays_withoutPantryRow_doesNotCreatePantryItem() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T05:00:00Z")))

        repo.setShelfDays("fish", 2)

        assertEquals(mapOf("fish" to 2), repo.observeShelfOverrides().first())
        assertTrue(repo.observe().first().isEmpty())
    }

    @Test
    fun setShelfDays_doesNotChangeHaveOrMarkedAt() = runTest {
        val clock = FakeClock(at("2026-03-10T05:00:00Z"))
        val repo = repo(clock)
        repo.setHave("egg", true)

        clock.current = clock.current + 2.days
        repo.setShelfDays("egg", 4)

        assertEquals(listOf(PantryItem("egg", true, d("2026-03-10"))), repo.observe().first())
    }

    @Test
    fun observeShelfOverrides_emitsAgain_whenOverrideChanges() = runTest {
        val repo = repo(FakeClock(at("2026-03-10T05:00:00Z")))
        val seen = mutableListOf<Map<String, Int>>()
        backgroundScope.launch { repo.observeShelfOverrides().collect { seen += it } }
        runCurrent()
        assertEquals(emptyMap<String, Int>(), seen.single())

        repo.setShelfDays("egg", 4)
        runCurrent()
        assertEquals(mapOf("egg" to 4), seen.last())

        repo.setShelfDays("egg", null)
        runCurrent()
        assertEquals(emptyMap<String, Int>(), seen.last())
    }
}
