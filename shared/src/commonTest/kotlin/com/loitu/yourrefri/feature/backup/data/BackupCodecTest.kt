package com.loitu.yourrefri.feature.backup.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BackupCodecTest {
    private val validJson = """
        {
          "format":"yourrefri-backup",
          "version":1,
          "pantry":[
            {"ingredientId":"egg","have":false,"markedAt":"2026-11-01"},
            {"ingredientId":"rice","have":true,"markedAt":"2026-11-03"}
          ],
          "shelfOverrides":[{"ingredientId":"egg","shelfDays":0}],
          "quantities":[{"ingredientId":"rice","quantity":3}],
          "units":[{"ingredientId":"rice","unit":"kg"},{"ingredientId":"egg","unit":"quả"}],
          "cookLogs":[
            {"id":7,"recipeId":"soup","cookedOn":"2026-11-03","photoUri":"content://photos/7","savedIngredientIds":["rice","egg"]},
            {"id":6,"recipeId":"stew","cookedOn":"2026-11-03","photoUri":null,"savedIngredientIds":[]}
          ]
        }
    """.trimIndent()

    private val expected = PantryBackupSnapshot(
        format = "yourrefri-backup",
        version = 1,
        pantry = listOf(
            PantryBackupItem("egg", false, "2026-11-01"),
            PantryBackupItem("rice", true, "2026-11-03"),
        ),
        shelfOverrides = listOf(ShelfOverrideBackup("egg", 0)),
        quantities = listOf(QuantityOverrideBackup("rice", 3)),
        units = listOf(UnitOverrideBackup("rice", "kg"), UnitOverrideBackup("egg", "quả")),
        cookLogs = listOf(
            CookLogBackup(7, "soup", "2026-11-03", "content://photos/7", listOf("rice", "egg")),
            CookLogBackup(6, "stew", "2026-11-03", null, emptyList()),
        ),
    )

    @Test
    fun encode_includesEveryPersistentField() {
        val encoded = BackupCodec.encode(expected)

        assertEquals(expected, BackupCodec.decode(encoded))
        assertTrue(encoded.contains("\"format\":\"yourrefri-backup\""))
        assertTrue(encoded.contains("\"version\":1"))
        assertTrue(encoded.contains("\"have\":false"))
        assertTrue(encoded.contains("\"shelfDays\":0"))
        assertTrue(encoded.contains("\"quantity\":3"))
        assertTrue(encoded.contains("\"unit\":\"kg\""))
        assertTrue(encoded.contains("\"id\":7"))
        assertTrue(encoded.contains("\"photoUri\":\"content://photos/7\""))
        assertTrue(encoded.contains("\"savedIngredientIds\":[\"rice\",\"egg\"]"))
    }

    @Test
    fun decode_roundTripsAndPreservesOrdering() {
        val decoded = BackupCodec.decode(validJson)

        assertEquals(expected, decoded)
        assertEquals(listOf(7L, 6L), decoded.cookLogs.map { it.id })
        assertEquals(listOf("rice", "egg"), decoded.cookLogs.first().savedIngredientIds)
        assertEquals(listOf("rice", "egg"), decoded.units.map { it.ingredientId })
        assertEquals(listOf("kg", "quả"), decoded.units.map { it.unit })
    }

    @Test
    fun decode_oldVersionOneBackupWithoutUnits_defaultsToEmpty() {
        val oldJson = validJson.replace(
            "\"units\":[{\"ingredientId\":\"rice\",\"unit\":\"kg\"},{\"ingredientId\":\"egg\",\"unit\":\"quả\"}],",
            "",
        )

        assertEquals(emptyList(), BackupCodec.decode(oldJson).units)
    }

    @Test
    fun decode_emptySnapshot_returnsEmptyTables() {
        val json = """{"format":"yourrefri-backup","version":1,"pantry":[],"shelfOverrides":[],"quantities":[],"cookLogs":[]}"""

        assertEquals(
            PantryBackupSnapshot("yourrefri-backup", 1, emptyList(), emptyList(), emptyList(), emptyList()),
            BackupCodec.decode(json),
        )
    }

    @Test
    fun versionTwo_roundTripsWeeklyMealsAndPreservesOrder() {
        val snapshot = expected.copy(
            version = 2,
            weeklyMeals = listOf(
                WeeklyMealBackup("2026-12-31", "LUNCH", "omelette", 2),
                WeeklyMealBackup("2026-12-31", "DINNER", "soup", 1),
                WeeklyMealBackup("2027-01-01", "LUNCH", "rice", 4),
            ),
        )

        val encoded = BackupCodec.encode(snapshot)

        assertTrue(encoded.contains("\"version\":2"))
        assertEquals(snapshot, BackupCodec.decode(encoded))
    }

    @Test
    fun decode_versionOneWithoutWeeklyField_defaultsToEmptySchedule() {
        val decoded = BackupCodec.decode(validJson)

        assertEquals(1, decoded.version)
        assertEquals(emptyList(), decoded.weeklyMeals)
    }

    @Test
    fun encode_rejectsDuplicateWeeklyDayAndMeal() {
        val duplicate = expected.copy(
            version = 2,
            weeklyMeals = listOf(
                WeeklyMealBackup("2026-12-31", "LUNCH", "omelette", 2),
                WeeklyMealBackup("2026-12-31", "LUNCH", "soup", 1),
            ),
        )

        assertFailsWith<IllegalArgumentException> { BackupCodec.encode(duplicate) }
    }

    @Test
    fun decode_rejectsUnsupportedVersionOrFormat() {
        for (json in listOf(
            validJson.replace("\"version\":1", "\"version\":3"),
            validJson.replace("yourrefri-backup", "another-format"),
        )) assertFailsWith<IllegalArgumentException> { BackupCodec.decode(json) }
    }

    @Test
    fun decode_rejectsMalformedJsonUnknownFieldsAndInvalidDate() {
        for (json in listOf(
            "{",
            validJson.replace("\"version\":1", "\"version\":1,\"extra\":true"),
            validJson.replace("2026-11-01", "2026-02-30"),
        )) assertFailsWith<IllegalArgumentException> { BackupCodec.decode(json) }
    }

    @Test
    fun decode_rejectsDuplicateIdsAndInvalidValues() {
        val duplicatePantryId = validJson.replace(
            "{\"ingredientId\":\"rice\",\"have\":true,\"markedAt\":\"2026-11-03\"}",
            "{\"ingredientId\":\"egg\",\"have\":true,\"markedAt\":\"2026-11-03\"}",
        )
        val invalidRows = listOf(
            validJson.replace("\"ingredientId\":\"egg\"", "\"ingredientId\":\"\""),
            validJson.replace("\"shelfDays\":0", "\"shelfDays\":-1"),
            validJson.replace("\"quantity\":3", "\"quantity\":1"),
            validJson.replace("\"id\":7", "\"id\":0"),
        )

        assertFailsWith<IllegalArgumentException> { BackupCodec.decode(duplicatePantryId) }
        invalidRows.forEach { json ->
            assertFailsWith<IllegalArgumentException> { BackupCodec.decode(json) }
        }
    }

    @Test
    fun decode_rejectsSavedIdsUnsupportedByAdapter() {
        for (savedIds in listOf("[\"\"]", "[\"egg,fish\"]")) {
            val json = validJson.replace("[\"rice\",\"egg\"]", savedIds)
            assertFailsWith<IllegalArgumentException> { BackupCodec.decode(json) }
        }
    }

    @Test
    fun decode_rejectsUnsupportedUnitOverride() {
        val invalid = validJson.replace("\"unit\":\"quả\"", "\"unit\":\"lon\"")
        assertFailsWith<IllegalArgumentException> { BackupCodec.decode(invalid) }
    }
}
