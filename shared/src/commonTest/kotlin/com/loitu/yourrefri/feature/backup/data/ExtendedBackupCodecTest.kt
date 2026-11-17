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
}
