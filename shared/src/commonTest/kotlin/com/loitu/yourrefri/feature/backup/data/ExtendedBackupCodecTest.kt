package com.loitu.yourrefri.feature.backup.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ExtendedBackupCodecTest {
    private val legacyV2Head =
        """{"format":"yourrefri-backup","version":2,"pantry":[],"shelfOverrides":[],"quantities":[],"cookLogs":[]"""

    private fun baseSnapshot(
        version: Int = BackupCodec.VERSION,
        compartments: List<CompartmentBackup> = emptyList(),
        customRecipes: List<CustomRecipeBackup> = emptyList(),
        leftovers: List<LeftoverBackup> = emptyList(),
        extraShoppingItems: List<ExtraShoppingBackup> = emptyList(),
    ) = PantryBackupSnapshot(
        format = BackupCodec.FORMAT,
        version = version,
        pantry = emptyList(),
        shelfOverrides = emptyList(),
        quantities = emptyList(),
        cookLogs = emptyList(),
        compartments = compartments,
        customRecipes = customRecipes,
        leftovers = leftovers,
        extraShoppingItems = extraShoppingItems,
    )

    @Test
    fun oldVersionTwoWithoutNewFieldsDecodesToEmptyLists() {
        val decoded = BackupCodec.decode("$legacyV2Head}")
        assertEquals(emptyList(), decoded.compartments)
        assertEquals(emptyList(), decoded.customRecipes)
        assertEquals(emptyList(), decoded.leftovers)
        assertEquals(emptyList(), decoded.extraShoppingItems)
    }

    @Test
    fun roundTripsCompartmentsCustomRecipesLeftoversAndExtraShopping() {
        val original = baseSnapshot(
            compartments = listOf(
                CompartmentBackup("beef", "FREEZE"),
                CompartmentBackup("egg", "CHILL"),
            ),
            customRecipes = listOf(
                CustomRecipeBackup(
                    recipeId = "custom-1",
                    title = "Canh chua",
                    steps = listOf("Nấu nước dùng", "Cho cá vào"),
                    required = listOf(listOf("fish"), listOf("tomato", "pineapple")),
                    optional = listOf(listOf("herb")),
                    minutes = 20,
                ),
            ),
            leftovers = listOf(
                LeftoverBackup("leftover-1", "Thịt kho", "CHILL", 20400L, 2),
                LeftoverBackup("leftover-2", "Canh gà", "FREEZE", 20401L, 14),
            ),
            extraShoppingItems = listOf(
                ExtraShoppingBackup("milk", 20400L),
                ExtraShoppingBackup("bread", 20402L),
            ),
        )

        val encoded = BackupCodec.encode(original)
        assertTrue(encoded.contains("\"compartments\""))
        assertTrue(encoded.contains("\"customRecipes\""))
        assertTrue(encoded.contains("\"leftovers\""))
        assertTrue(encoded.contains("\"extraShoppingItems\""))
        assertEquals(original, BackupCodec.decode(encoded))
    }

    @Test
    fun versionOneRejectsNewFields() {
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.encode(baseSnapshot(version = 1, compartments = listOf(CompartmentBackup("egg", "CHILL"))))
        }
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.encode(
                baseSnapshot(
                    version = 1,
                    customRecipes = listOf(CustomRecipeBackup("custom-1", "Món", required = listOf(listOf("egg")))),
                ),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.encode(baseSnapshot(version = 1, leftovers = listOf(LeftoverBackup("l-1", "Cơm", "CHILL", 20400L, 2))))
        }
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.encode(baseSnapshot(version = 1, extraShoppingItems = listOf(ExtraShoppingBackup("egg", 20400L))))
        }
    }

    @Test
    fun validationRejectsInvalidValuesAndDuplicates() {
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.encode(baseSnapshot(compartments = listOf(CompartmentBackup("egg", "FRIDGE"))))
        }
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.encode(
                baseSnapshot(
                    compartments = listOf(CompartmentBackup("egg", "CHILL"), CompartmentBackup("egg", "FREEZE")),
                ),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.encode(
                baseSnapshot(customRecipes = listOf(CustomRecipeBackup("custom-1", " ", required = listOf(listOf("egg"))))),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.encode(
                baseSnapshot(customRecipes = listOf(CustomRecipeBackup("custom-1", "Món", required = emptyList()))),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.encode(
                baseSnapshot(leftovers = listOf(LeftoverBackup("l-1", "Cơm", "CHILL", 20400L, -1))),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.encode(
                baseSnapshot(extraShoppingItems = listOf(ExtraShoppingBackup(" ", 20400L))),
            )
        }
    }

    private fun custom(id: String = "custom-1", steps: List<String> = emptyList()) =
        CustomRecipeBackup(id, "Món", steps = steps, required = listOf(listOf("egg")))

    @Test
    fun rejectsCustomRecipeWithMoreThanEightSteps() {
        val nine = List(9) { "Bước $it" }
        assertFailsWith<IllegalArgumentException> { BackupCodec.encode(baseSnapshot(customRecipes = listOf(custom(steps = nine)))) }
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.decode(rawWithRecipe("custom-1", nine.joinToString(",") { "\"$it\"" }))
        }
        BackupCodec.encode(baseSnapshot(customRecipes = listOf(custom(steps = nine.take(8)))))
    }

    @Test
    fun rejectsCustomRecipeWithBlankStep() {
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.encode(baseSnapshot(customRecipes = listOf(custom(steps = listOf("Nấu", "  ")))))
        }
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.decode(rawWithRecipe("custom-1", "\"\""))
        }
    }

    @Test
    fun rejectsCustomRecipeIdWithoutPrefix() {
        assertFailsWith<IllegalArgumentException> { BackupCodec.encode(baseSnapshot(customRecipes = listOf(custom(id = "mon-1")))) }
        assertFailsWith<IllegalArgumentException> { BackupCodec.decode(rawWithRecipe("mon-1", "")) }
    }

    @Test
    fun rejectsEpochDaysOutsideIntRange() {
        val tooBig = Int.MAX_VALUE.toLong() + 1
        val tooSmall = Int.MIN_VALUE.toLong() - 1
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.encode(baseSnapshot(leftovers = listOf(LeftoverBackup("l-1", "Cơm", "CHILL", tooBig, 2))))
        }
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.encode(baseSnapshot(leftovers = listOf(LeftoverBackup("l-1", "Cơm", "CHILL", tooSmall, 2))))
        }
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.encode(baseSnapshot(extraShoppingItems = listOf(ExtraShoppingBackup("egg", tooBig))))
        }
        assertFailsWith<IllegalArgumentException> {
            BackupCodec.encode(baseSnapshot(extraShoppingItems = listOf(ExtraShoppingBackup("egg", tooSmall))))
        }
        val raw = "$legacyV2Head,\"extraShoppingItems\":[{\"ingredientId\":\"egg\",\"addedAtEpochDays\":4294967296}]}"
        assertFailsWith<IllegalArgumentException> { BackupCodec.decode(raw) }
    }

    @Test
    fun oldBackupWithValidCustomRecipeAndDatesStillDecodes() {
        val raw = "$legacyV2Head,\"customRecipes\":[{\"recipeId\":\"custom-9\",\"title\":\"Món\"," +
            "\"steps\":[\"Nấu\"],\"required\":[[\"egg\"]]}]," +
            "\"leftovers\":[{\"id\":\"l\",\"title\":\"Cơm\",\"compartment\":\"CHILL\",\"markedAtEpochDays\":20400,\"shelfDays\":2}]}"
        val decoded = BackupCodec.decode(raw)
        assertEquals("custom-9", decoded.customRecipes.single().recipeId)
        assertEquals(20400L, decoded.leftovers.single().markedAtEpochDays)
    }

    private fun rawWithRecipe(id: String, stepsJson: String) =
        "$legacyV2Head,\"customRecipes\":[{\"recipeId\":\"$id\",\"title\":\"Món\"," +
            "\"steps\":[$stepsJson],\"required\":[[\"egg\"]]}]}"
}
