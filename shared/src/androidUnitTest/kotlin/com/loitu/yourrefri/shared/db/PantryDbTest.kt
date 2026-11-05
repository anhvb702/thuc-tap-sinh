package com.loitu.yourrefri.shared.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Chạy trên JVM (androidUnitTest) với SQLite in-memory: mỗi test một DB mới, không chạm file thật.
// Ngày là chuỗi ISO cố định, không đọc giờ hệ thống.
class PantryDbTest {

    private lateinit var driver: SqlDriver
    private lateinit var db: YourRefriDatabase

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

    private val pantry get() = db.pantryEntityQueries
    private val shelf get() = db.userShelfEntityQueries
    private val cook get() = db.cookLogEntityQueries

    private fun insertCook(
        recipeId: String,
        cookedOn: String,
        saved: List<String> = emptyList(),
        photoUri: String? = null,
    ): Long {
        cook.insert(recipeId = recipeId, cookedOn = cookedOn, photoUri = photoUri, savedIngredientIds = saved)
        return cook.lastInsertRowId().executeAsOne()
    }

    // ---------------- PantryEntity ----------------

    @Test
    fun pantry_upsertThenSelectById_returnsSameValues() {
        pantry.upsert(ingredientId = "egg", have = true, markedAt = "2026-11-01")

        assertEquals(PantryEntity("egg", true, "2026-11-01"), pantry.selectById("egg").executeAsOneOrNull())
    }

    @Test
    fun pantry_selectById_unknownId_isNull() {
        pantry.upsert(ingredientId = "egg", have = true, markedAt = "2026-11-01")

        assertNull(pantry.selectById("fish").executeAsOneOrNull())
    }

    @Test
    fun pantry_haveFalse_roundTripsAsFalse() {
        pantry.upsert(ingredientId = "egg", have = false, markedAt = "2026-11-01")

        assertEquals(false, pantry.selectById("egg").executeAsOne().have)
    }

    @Test
    fun pantry_upsertSameId_overwrites_andKeepsSingleRow() {
        pantry.upsert(ingredientId = "egg", have = true, markedAt = "2026-11-01")
        pantry.upsert(ingredientId = "egg", have = false, markedAt = "2026-11-04")

        assertEquals(listOf(PantryEntity("egg", false, "2026-11-04")), pantry.selectAll().executeAsList())
    }

    @Test
    fun pantry_updateHave_changesOnlyHave_keepsMarkedAt() {
        pantry.upsert(ingredientId = "egg", have = true, markedAt = "2026-11-01")

        pantry.updateHave(have = false, ingredientId = "egg")

        assertEquals(PantryEntity("egg", false, "2026-11-01"), pantry.selectById("egg").executeAsOne())
    }

    @Test
    fun pantry_updateHave_unknownId_insertsNothing() {
        pantry.updateHave(have = false, ingredientId = "egg")

        assertTrue(pantry.selectAll().executeAsList().isEmpty())
    }

    @Test
    fun pantry_updateHave_touchesOnlyTargetRow() {
        pantry.upsert(ingredientId = "egg", have = true, markedAt = "2026-11-01")
        pantry.upsert(ingredientId = "rice", have = true, markedAt = "2026-11-02")

        pantry.updateHave(have = false, ingredientId = "egg")

        assertEquals(PantryEntity("rice", true, "2026-11-02"), pantry.selectById("rice").executeAsOne())
    }

    @Test
    fun pantry_selectAll_isOrderedByIngredientId() {
        pantry.upsert(ingredientId = "rice", have = true, markedAt = "2026-11-01")
        pantry.upsert(ingredientId = "egg", have = false, markedAt = "2026-11-02")
        pantry.upsert(ingredientId = "pork", have = true, markedAt = "2026-11-03")

        assertEquals(listOf("egg", "pork", "rice"), pantry.selectAll().executeAsList().map { it.ingredientId })
    }

    @Test
    fun pantry_deleteById_removesOnlyThatRow() {
        pantry.upsert(ingredientId = "egg", have = true, markedAt = "2026-11-01")
        pantry.upsert(ingredientId = "rice", have = true, markedAt = "2026-11-02")

        pantry.deleteById("egg")

        assertEquals(listOf("rice"), pantry.selectAll().executeAsList().map { it.ingredientId })
    }

    // ---------------- UserShelfEntity (hạn sửa tay, bảng riêng) ----------------

    @Test
    fun userShelf_upsertThenSelectById_returnsDays() {
        shelf.upsert(ingredientId = "egg", userShelfDays = 4L)

        assertEquals(4L, shelf.selectById("egg").executeAsOneOrNull())
    }

    @Test
    fun userShelf_selectById_unknownId_isNull() {
        assertNull(shelf.selectById("egg").executeAsOneOrNull())
    }

    @Test
    fun userShelf_zero_isStoredAsZero_notNull() {
        shelf.upsert(ingredientId = "egg", userShelfDays = 0L)

        assertEquals(0L, shelf.selectById("egg").executeAsOneOrNull())
    }

    @Test
    fun userShelf_upsertSameId_overwrites_andKeepsSingleRow() {
        shelf.upsert(ingredientId = "egg", userShelfDays = 4L)
        shelf.upsert(ingredientId = "egg", userShelfDays = 9L)

        assertEquals(listOf(UserShelfEntity("egg", 9L)), shelf.selectAll().executeAsList())
    }

    @Test
    fun userShelf_selectAll_isOrderedByIngredientId() {
        shelf.upsert(ingredientId = "rice", userShelfDays = 30L)
        shelf.upsert(ingredientId = "egg", userShelfDays = 4L)

        assertEquals(
            listOf(UserShelfEntity("egg", 4L), UserShelfEntity("rice", 30L)),
            shelf.selectAll().executeAsList(),
        )
    }

    @Test
    fun userShelf_isRemembered_afterMarkedOut_andMarkedHaveAgain() {
        shelf.upsert(ingredientId = "egg", userShelfDays = 4L)
        pantry.upsert(ingredientId = "egg", have = true, markedAt = "2026-11-01")

        pantry.updateHave(have = false, ingredientId = "egg")
        pantry.upsert(ingredientId = "egg", have = true, markedAt = "2026-11-08")

        assertEquals(4L, shelf.selectById("egg").executeAsOneOrNull())
    }

    @Test
    fun userShelf_isRemembered_afterPantryRowDeleted() {
        shelf.upsert(ingredientId = "egg", userShelfDays = 4L)
        pantry.upsert(ingredientId = "egg", have = true, markedAt = "2026-11-01")

        pantry.deleteById("egg")

        assertEquals(4L, shelf.selectById("egg").executeAsOneOrNull())
    }

    @Test
    fun userShelf_canExistWithoutPantryRow_andDoesNotCreateOne() {
        shelf.upsert(ingredientId = "fish", userShelfDays = 2L)

        assertEquals(2L, shelf.selectById("fish").executeAsOneOrNull())
        assertTrue(pantry.selectAll().executeAsList().isEmpty())
    }

    @Test
    fun userShelf_deleteById_removesOnlyThatOverride() {
        shelf.upsert(ingredientId = "egg", userShelfDays = 4L)
        shelf.upsert(ingredientId = "rice", userShelfDays = 30L)

        shelf.deleteById("egg")

        assertNull(shelf.selectById("egg").executeAsOneOrNull())
        assertEquals(30L, shelf.selectById("rice").executeAsOneOrNull())
    }

    // ---------------- CookLogEntity ----------------

    @Test
    fun cookLog_insert_assignsIncreasingIds() {
        val first = insertCook("omelette", "2026-11-01")
        val second = insertCook("fried-rice", "2026-11-01")

        assertTrue(first > 0L, "id đầu tiên phải dương, thực tế $first")
        assertTrue(second > first, "id sau ($second) phải lớn hơn id trước ($first)")
    }

    @Test
    fun cookLog_autoincrement_doesNotReuseDeletedId() {
        insertCook("omelette", "2026-11-01")
        val second = insertCook("fried-rice", "2026-11-02")
        cook.deleteById(second)

        val third = insertCook("pho", "2026-11-03")

        assertTrue(third > second, "AUTOINCREMENT không được dùng lại id đã xóa ($second), thực tế $third")
    }

    @Test
    fun cookLog_insertThenSelectAll_returnsAllFields() {
        val id = insertCook("omelette", "2026-11-01", saved = listOf("egg", "scallion"), photoUri = "content://photos/7")

        assertEquals(
            listOf(CookLogEntity(id, "omelette", "2026-11-01", "content://photos/7", listOf("egg", "scallion"))),
            cook.selectAll().executeAsList(),
        )
    }

    @Test
    fun cookLog_selectAll_newestCookedOnFirst_sameDayLaterInsertFirst() {
        val a = insertCook("a", "2026-10-30")
        val b = insertCook("b", "2026-11-02")
        val c = insertCook("c", "2026-09-30")
        val d = insertCook("d", "2026-11-02")
        val e = insertCook("e", "2026-10-01")

        assertEquals(listOf(d, b, a, e, c), cook.selectAll().executeAsList().map { it.id })
    }

    @Test
    fun cookLog_photoUri_null_roundTripsAsNull() {
        insertCook("omelette", "2026-11-01", photoUri = null)

        assertNull(cook.selectAll().executeAsOne().photoUri)
    }

    @Test
    fun cookLog_savedIngredientIds_preservesOrderAndDuplicates() {
        val saved = listOf("pork", "egg", "scallion", "egg")
        insertCook("braised-pork", "2026-11-01", saved = saved)

        assertEquals(saved, cook.selectAll().executeAsOne().savedIngredientIds)
    }

    @Test
    fun cookLog_savedIngredientIds_empty_roundTripsAsEmptyList() {
        insertCook("omelette", "2026-11-01", saved = emptyList())

        assertEquals(emptyList<String>(), cook.selectAll().executeAsOne().savedIngredientIds)
    }

    @Test
    fun cookLog_savedIngredientIds_single_roundTrips() {
        insertCook("omelette", "2026-11-01", saved = listOf("egg"))

        assertEquals(listOf("egg"), cook.selectAll().executeAsOne().savedIngredientIds)
    }

    @Test
    fun cookLog_selectSince_isInclusive_andNewestFirst() {
        insertCook("old", "2026-10-29")
        val boundary = insertCook("boundary", "2026-10-30")
        val mid = insertCook("mid", "2026-11-02")
        val latest = insertCook("latest", "2026-11-05")

        assertEquals(listOf(latest, mid, boundary), cook.selectSince("2026-10-30").executeAsList().map { it.id })
    }

    @Test
    fun cookLog_selectSince_comparesAcrossMonthAndYear() {
        insertCook("sep", "2026-09-30")
        val oct = insertCook("oct", "2026-10-01")
        val jan = insertCook("jan", "2027-01-02")

        assertEquals(listOf(jan, oct), cook.selectSince("2026-10-01").executeAsList().map { it.id })
    }

    @Test
    fun cookLog_selectSince_nothingRecent_isEmpty() {
        insertCook("old", "2026-10-01")

        assertTrue(cook.selectSince("2026-11-01").executeAsList().isEmpty())
    }

    @Test
    fun cookLog_selectRecipeIdsSince_isDistinct_sorted_andInclusive() {
        insertCook("pho", "2026-10-28")
        insertCook("omelette", "2026-11-03")
        insertCook("fried-rice", "2026-11-01")
        insertCook("omelette", "2026-11-01")

        assertEquals(listOf("fried-rice", "omelette"), cook.selectRecipeIdsSince("2026-11-01").executeAsList())
    }

    // ---------------- IdListAdapter (định dạng lưu savedIngredientIds) ----------------

    @Test
    fun idListAdapter_encode_joinsWithComma_inOrder() {
        assertEquals("pork,egg", IdListAdapter.encode(listOf("pork", "egg")))
        assertEquals("", IdListAdapter.encode(emptyList()))
    }

    @Test
    fun idListAdapter_decode_emptyString_isEmptyList() {
        assertEquals(emptyList<String>(), IdListAdapter.decode(""))
        assertEquals(listOf("pork", "egg"), IdListAdapter.decode("pork,egg"))
    }

    @Test
    fun idListAdapter_rejectsIdContainingComma() {
        assertFailsWith<IllegalArgumentException> { IdListAdapter.encode(listOf("egg", "a,b")) }
    }

    @Test
    fun idListAdapter_rejectsEmptyId() {
        // [""] sẽ mã hóa thành "" rồi đọc lại thành [] nên phải bị chặn từ lúc ghi.
        assertFailsWith<IllegalArgumentException> { IdListAdapter.encode(listOf("")) }
        assertFailsWith<IllegalArgumentException> { IdListAdapter.encode(listOf("egg", "")) }
    }
}
