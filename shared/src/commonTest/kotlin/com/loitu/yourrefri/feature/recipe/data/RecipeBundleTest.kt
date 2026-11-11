package com.loitu.yourrefri.feature.recipe.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RecipeBundleTest {
    private val row = """{"id":"dish","title":"Món","steps":["Nấu"],"required":[{"anyOf":["egg"]}]}"""
    private fun parseRow(value: String = row) = RecipeBundle.parse("[$value]")

    @Test
    fun validBundle_preservesAllFieldsAndOrder() {
        val first = """{"id":"z","title":"Món Z","steps":["Hai","Một"],"required":[{"anyOf":["pork","egg","egg"]},{"anyOf":["rice"]}],"optional":[{"anyOf":["milk"]}]}"""
        val second = row.replace("dish", "a")
        assertEquals(
            listOf(
                Recipe("z", "Món Z", listOf("Hai", "Một"), listOf(Slot(listOf("pork", "egg", "egg")), Slot(listOf("rice"))), listOf(Slot(listOf("milk")))),
                Recipe("a", "Món", listOf("Nấu"), listOf(Slot(listOf("egg")))),
            ),
            RecipeBundle.parse("[$first,$second]"),
        )
    }

    @Test
    fun optionalOmitted_defaultsEmpty() {
        assertEquals(emptyList(), parseRow().single().optional)
    }

    @Test
    fun emptyArray_returnsEmpty() {
        assertEquals(emptyList(), RecipeBundle.parse("[]"))
    }

    @Test
    fun duplicateRecipeId_rejectsWithId() {
        val error = assertFailsWith<IllegalArgumentException> { RecipeBundle.parse("[$row,$row]") }
        assertTrue(error.message.orEmpty().contains("dish"))
    }

    @Test
    fun malformedAndWrongRoot_reject() {
        for (json in listOf("", "[", row, "[{id:1}]", "[$row,]")) {
            assertFailsWith<IllegalArgumentException> { RecipeBundle.parse(json) }
        }
    }

    @Test
    fun missingNullAndWrongTypes_reject() {
        for (json in listOf(
            row.replace("\"title\":\"Món\",", ""),
            row.replace("\"title\":\"Món\"", "\"title\":null"),
            row.replace("\"steps\":[\"Nấu\"]", "\"steps\":{}"),
        )) assertFailsWith<IllegalArgumentException> { parseRow(json) }
    }

    @Test
    fun quantificationAndUnknownFields_rejectAtEveryLevel() {
        for (json in listOf(
            row.replace("\"id\":", "\"quantity\":2,\"id\":"),
            row.replace("\"anyOf\":", "\"amount\":2,\"anyOf\":"),
            row.replace("\"id\":", "\"unit\":\"g\",\"id\":"),
            row.replace("\"id\":", "\"extra\":true,\"id\":"),
        )) assertFailsWith<IllegalArgumentException> { parseRow(json) }
    }

    @Test
    fun invalidSlot_rejects() {
        for (json in listOf(
            row.replace("[\"egg\"]", "[]"),
            row.replace("\"egg\"", "\"  \""),
            row.replace("{\"anyOf\":[\"egg\"]}", "{}"),
        )) assertFailsWith<IllegalArgumentException> { parseRow(json) }
    }

    @Test
    fun invalidRecipeFields_reject() {
        for (json in listOf(
            row.replace("\"dish\"", "\"  \""),
            row.replace("\"Món\"", "\"  \""),
            row.replace("[\"Nấu\"]", "[]"),
            row.replace("\"Nấu\"", "\"  \""),
            row.replace("[{\"anyOf\":[\"egg\"]}]", "[]"),
        )) assertFailsWith<IllegalArgumentException> { parseRow(json) }
    }

    @Test
    fun eightStepsAccepted_nineRejected() {
        fun withSteps(count: Int) = row.replace("[\"Nấu\"]", (1..count).joinToString(prefix = "[", postfix = "]") { "\"Bước $it\"" })
        assertEquals(8, parseRow(withSteps(8)).single().steps.size)
        assertFailsWith<IllegalArgumentException> { parseRow(withSteps(9)) }
    }
}
