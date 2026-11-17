package com.loitu.yourrefri.feature.pantry.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

// Nội dung shelf_table.json thật; actual cho Android host test đọc file trong shared/src/commonMain/resources.
internal expect fun readShelfTableJson(): String

class ShelfTableTest {

    private fun entry(id: String, nameVi: String = "Món $id", group: String = "khac", days: String = "3"): String =
        """{"id":"$id","nameVi":"$nameVi","group":"$group","defaultShelfDays":$days}"""

    private fun array(vararg entries: String): String = entries.joinToString(prefix = "[", postfix = "]", separator = ",")

    // Mục hỏng luôn đặt sau một mục hợp lệ để bắt code chỉ kiểm mục đầu tiên.
    private fun withValidFirst(bad: String): String = array(entry("trung-ga", "Trứng gà", "sua-trung", "21"), bad)

    private fun assertRejected(json: String, mustMention: String? = null) {
        val e = assertFailsWith<IllegalArgumentException> { ShelfTable.parse(json) }
        if (mustMention != null) {
            assertTrue(
                e.message.orEmpty().contains(mustMention),
                "Thông báo lỗi phải nêu '$mustMention', nhận được: ${e.message}",
            )
        }
    }

    // --- tập group đã chốt ---

    @Test
    fun groups_isExactlyTheAgreedSet() {
        assertEquals(
            setOf("thit", "ca", "rau-la", "rau-cu", "trai-cay", "sua-trung", "khac"),
            ShelfTable.GROUPS,
        )
    }

    // --- JSON hợp lệ ---

    @Test
    fun parse_validSmallJson_returnsIngredientsInFileOrder() {
        val json = array(
            entry("rau-muong", "Rau muống", "rau-la", "2"),
            entry("trung-ga", "Trứng gà", "sua-trung", "21"),
            entry("ca-hoi", "Cá hồi", "ca", "2"),
        )
        assertEquals(
            listOf(
                Ingredient(id = "rau-muong", nameVi = "Rau muống", group = "rau-la", defaultShelfDays = 2, userShelfDays = null),
                Ingredient(id = "trung-ga", nameVi = "Trứng gà", group = "sua-trung", defaultShelfDays = 21, userShelfDays = null),
                Ingredient(id = "ca-hoi", nameVi = "Cá hồi", group = "ca", defaultShelfDays = 2, userShelfDays = null),
            ),
            ShelfTable.parse(json),
        )
    }

    @Test
    fun parse_toleratesWhitespaceAndNewlines() {
        val json = """
            [
              { "id": "ca-rot", "nameVi": "Cà rốt", "group": "rau-cu", "defaultShelfDays": 14 }
            ]
        """.trimIndent()
        assertEquals(listOf(Ingredient("ca-rot", "Cà rốt", "rau-cu", 14)), ShelfTable.parse(json))
    }

    @Test
    fun parse_emptyArray_returnsEmptyList() {
        assertEquals(emptyList(), ShelfTable.parse("[]"))
    }

    @Test
    fun parse_everyAgreedGroup_isAccepted() {
        val groups = listOf("thit", "ca", "rau-la", "rau-cu", "trai-cay", "sua-trung", "khac")
        val json = array(*groups.mapIndexed { i, g -> entry("mon-$i", group = g) }.toTypedArray())
        assertEquals(groups, ShelfTable.parse(json).map { it.group })
    }

    @Test
    fun parse_shelfDays1_isValid() {
        assertEquals(1, ShelfTable.parse(withValidFirst(entry("tom", "Tôm", "ca", "1")))[1].defaultShelfDays)
    }

    @Test
    fun parse_shelfDays365_isValid() {
        assertEquals(365, ShelfTable.parse(withValidFirst(entry("muoi", "Muối", "khac", "365")))[1].defaultShelfDays)
    }

    // --- kind & freezerShelfDays ---

    @Test
    fun parse_parsesKindAndFreezerShelfDays() {
        val json = array(
            """{"id":"thit-heo","nameVi":"Thịt heo","group":"thit","defaultShelfDays":2,"kind":"fresh","freezerShelfDays":60}""",
            """{"id":"nuoc-mam","nameVi":"Nước mắm","group":"khac","defaultShelfDays":30,"kind":"seasoning"}""",
            """{"id":"sua-tuoi","nameVi":"Sữa tươi","group":"sua-trung","defaultShelfDays":3,"kind":"ready"}""",
        )
        val parsed = ShelfTable.parse(json)
        assertEquals(IngredientKind.FRESH, parsed[0].kind)
        assertEquals(60, parsed[0].freezerShelfDays)
        assertEquals(IngredientKind.SEASONING, parsed[1].kind)
        assertEquals(null, parsed[1].freezerShelfDays)
        assertEquals(IngredientKind.READY, parsed[2].kind)
        assertEquals(null, parsed[2].freezerShelfDays)
    }

    @Test
    fun parse_unknownOrMissingKind_defaultsToFresh() {
        val json = array(
            entry("ca-rot", "Cà rốt", "rau-cu", "14"),
            """{"id":"ca-chua","nameVi":"Cà chua","group":"rau-cu","defaultShelfDays":5,"kind":"other"}""",
        )
        val parsed = ShelfTable.parse(json)
        assertEquals(IngredientKind.FRESH, parsed[0].kind)
        assertEquals(null, parsed[0].freezerShelfDays)
        assertEquals(IngredientKind.FRESH, parsed[1].kind)
    }

    @Test
    fun parse_freezerShelfDaysOutOfRange_fails_andNamesTheId() {
        assertRejected(
            withValidFirst("""{"id":"thit-heo","nameVi":"Thịt heo","group":"thit","defaultShelfDays":2,"freezerShelfDays":0}"""),
            mustMention = "thit-heo",
        )
        assertRejected(
            withValidFirst("""{"id":"thit-heo","nameVi":"Thịt heo","group":"thit","defaultShelfDays":2,"freezerShelfDays":366}"""),
            mustMention = "thit-heo",
        )
    }

    // --- id ---

    @Test
    fun parse_duplicateId_fails_andNamesTheId() {
        val json = array(
            entry("trung-ga", "Trứng gà", "sua-trung", "21"),
            entry("ca-hoi", "Cá hồi", "ca", "2"),
            entry("ca-hoi", "Cá hồi phi lê", "ca", "2"),
        )
        assertRejected(json, mustMention = "ca-hoi")
    }

    @Test
    fun parse_emptyId_fails() {
        assertRejected(withValidFirst(entry("", "Không tên", "khac", "3")))
    }

    @Test
    fun parse_whitespaceId_fails() {
        assertRejected(withValidFirst(entry("  ", "Không tên", "khac", "3")))
    }

    // --- defaultShelfDays ---

    @Test
    fun parse_missingDefaultShelfDays_fails() {
        assertRejected(withValidFirst("""{"id":"ca-hoi","nameVi":"Cá hồi","group":"ca"}"""))
    }

    @Test
    fun parse_nullDefaultShelfDays_fails() {
        assertRejected(withValidFirst("""{"id":"ca-hoi","nameVi":"Cá hồi","group":"ca","defaultShelfDays":null}"""))
    }

    @Test
    fun parse_shelfDays0_fails_andNamesTheId() {
        assertRejected(withValidFirst(entry("ca-hoi", "Cá hồi", "ca", "0")), mustMention = "ca-hoi")
    }

    @Test
    fun parse_negativeShelfDays_fails_andNamesTheId() {
        assertRejected(withValidFirst(entry("ca-hoi", "Cá hồi", "ca", "-2")), mustMention = "ca-hoi")
    }

    @Test
    fun parse_shelfDays366_fails_andNamesTheId() {
        assertRejected(withValidFirst(entry("muoi", "Muối", "khac", "366")), mustMention = "muoi")
    }

    // --- nameVi ---

    @Test
    fun parse_emptyNameVi_fails_andNamesTheId() {
        assertRejected(withValidFirst(entry("ca-hoi", "", "ca", "2")), mustMention = "ca-hoi")
    }

    @Test
    fun parse_whitespaceNameVi_fails_andNamesTheId() {
        assertRejected(withValidFirst(entry("ca-hoi", "   ", "ca", "2")), mustMention = "ca-hoi")
    }

    // --- group ---

    @Test
    fun parse_unknownGroup_fails_andNamesTheGroup() {
        // id và tên không chứa chuỗi "rau" để thông báo lỗi chỉ đạt khi thật sự nêu group sai.
        assertRejected(withValidFirst(entry("cai-ngot", "Cải ngọt", "rau", "2")), mustMention = "rau")
    }

    @Test
    fun parse_groupWithWrongCase_fails() {
        assertRejected(withValidFirst(entry("thit-bo", "Thịt bò", "Thit", "2")), mustMention = "Thit")
    }

    @Test
    fun parse_emptyGroup_fails() {
        assertRejected(withValidFirst(entry("thit-bo", "Thịt bò", "", "2")))
    }

    // --- JSON hỏng ---

    @Test
    fun parse_truncatedJson_fails() {
        assertRejected("""[{"id":"ca-hoi","nameVi":"Cá hồi""")
    }

    @Test
    fun parse_rootIsObjectNotArray_fails() {
        assertRejected(entry("ca-hoi", "Cá hồi", "ca", "2"))
    }

    @Test
    fun parse_emptyString_fails() {
        assertRejected("")
    }

    // --- bundle thật shelf_table.json ---

    private val maxDaysByGroup = mapOf(
        "thit" to 3,
        "ca" to 3,
        "rau-la" to 7,
        "rau-cu" to 30,
        "trai-cay" to 21,
        "sua-trung" to 30,
        "khac" to 30,
    )

    private val bundle: List<Ingredient> by lazy { ShelfTable.parse(readShelfTableJson()) }

    @Test
    fun bundle_hasBetween50And70Items() {
        assertTrue(bundle.size in 50..70, "Bảng hạn có ${bundle.size} món, cần 50..70")
    }

    @Test
    fun bundle_idsAreUnique() {
        val dupes = bundle.groupBy { it.id }.filterValues { it.size > 1 }.keys
        assertEquals(emptySet(), dupes)
    }

    @Test
    fun bundle_namesAreUnique() {
        val dupes = bundle.groupBy { it.nameVi.trim().lowercase() }.filterValues { it.size > 1 }.keys
        assertEquals(emptySet(), dupes)
    }

    @Test
    fun bundle_idsAreLowercaseAsciiKebabCase() {
        val pattern = Regex("^[a-z0-9]+(-[a-z0-9]+)*$")
        val bad = bundle.map { it.id }.filterNot { pattern.matches(it) }
        assertEquals(emptyList(), bad)
    }

    @Test
    fun bundle_everyGroupIsRepresented() {
        assertEquals(ShelfTable.GROUPS, bundle.map { it.group }.toSet())
    }

    @Test
    fun bundle_capsCoverEveryGroup() {
        assertEquals(ShelfTable.GROUPS, maxDaysByGroup.keys)
    }

    @Test
    fun bundle_shelfDaysWithinGroupCap() {
        val tooLong = bundle.filter { it.defaultShelfDays > maxDaysByGroup.getValue(it.group) }
            .map { "${it.id}=${it.defaultShelfDays}" }
        assertEquals(emptyList(), tooLong)
    }

    @Test
    fun bundle_freshMeatAndFishAtMost3Days() {
        val tooLong = bundle.filter { it.group == "thit" || it.group == "ca" }.filter { it.defaultShelfDays > 3 }
        assertEquals(emptyList(), tooLong)
    }

    @Test
    fun bundle_leafyVegetablesAtMost7Days() {
        val tooLong = bundle.filter { it.group == "rau-la" }.filter { it.defaultShelfDays > 7 }
        assertEquals(emptyList(), tooLong)
    }

    @Test
    fun bundle_noUserOverrides() {
        assertTrue(bundle.all { it.userShelfDays == null })
    }

    @Test
    fun bundle_everyKindIsRepresented() {
        assertEquals(IngredientKind.entries.toSet(), bundle.map { it.kind }.toSet())
    }

    @Test
    fun bundle_meatAndSeafoodHaveFreezerShelfDaysGreaterThanChill() {
        val meatAndFish = bundle.filter { it.group == "thit" || it.group == "ca" }
        assertTrue(meatAndFish.isNotEmpty())
        assertTrue(
            meatAndFish.all { it.freezerShelfDays != null && it.freezerShelfDays > it.defaultShelfDays },
            "Thịt/cá phải có freezerShelfDays lớn hơn defaultShelfDays",
        )
    }

    @Test
    fun bundle_nonFreezableCategoriesOmitFreezerShelfDays() {
        val nonFreezable = bundle.filter {
            it.kind != IngredientKind.FRESH || it.group in setOf("rau-la", "trai-cay")
        }
        assertTrue(
            nonFreezable.all { it.freezerShelfDays == null },
            "Gia vị, đồ ăn liền, rau lá, trái cây không có freezerShelfDays",
        )
    }
}
