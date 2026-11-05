package com.loitu.yourrefri.feature.recipe.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class RecipeModelsTest {

    private fun nSteps(n: Int): List<String> = (1..n).map { "Bước $it" }

    private val fishSlot = Slot(anyOf = listOf("ca-basa", "ca-hoi"))
    private val tomatoSlot = Slot(anyOf = listOf("ca-chua"))

    private fun recipe(
        id: String = "canh-chua-ca",
        title: String = "Canh chua cá",
        steps: List<String> = nSteps(3),
        required: List<Slot> = listOf(fishSlot, tomatoSlot),
    ) = Recipe(id = id, title = title, steps = steps, required = required)

    // --- Slot ---

    @Test
    fun slot_singleId_isValid() {
        assertEquals(listOf("trung-ga"), Slot(anyOf = listOf("trung-ga")).anyOf)
    }

    @Test
    fun slot_multipleIds_keepsGivenOrder() {
        assertEquals(listOf("ca-hoi", "ca-basa"), Slot(anyOf = listOf("ca-hoi", "ca-basa")).anyOf)
    }

    @Test
    fun slot_emptyAnyOf_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> { Slot(anyOf = emptyList()) }
    }

    @Test
    fun slot_emptyStringId_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> { Slot(anyOf = listOf("ca-hoi", "")) }
    }

    @Test
    fun slot_whitespaceId_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> { Slot(anyOf = listOf("ca-hoi", "   ")) }
    }

    @Test
    fun slot_equalByValue() {
        val a = Slot(anyOf = listOf("ca-basa", "ca-hoi"))
        val b = Slot(anyOf = listOf("ca-basa", "ca-hoi"))
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, Slot(anyOf = listOf("ca-hoi", "ca-basa")))
    }

    // --- Recipe: mặc định và giữ đủ trường ---

    @Test
    fun recipe_optionalDefaultsToEmptyList() {
        assertEquals(emptyList(), recipe().optional)
    }

    @Test
    fun recipe_keepsAllGivenFields() {
        val optional = listOf(Slot(anyOf = listOf("hanh-la")))
        val r = Recipe(
            id = "canh-chua-ca",
            title = "Canh chua cá",
            steps = listOf("Sơ chế cá", "Nấu nước dùng", "Cho cá vào"),
            required = listOf(fishSlot, tomatoSlot),
            optional = optional,
        )
        assertEquals("canh-chua-ca", r.id)
        assertEquals("Canh chua cá", r.title)
        assertEquals(listOf("Sơ chế cá", "Nấu nước dùng", "Cho cá vào"), r.steps)
        assertEquals(listOf(fishSlot, tomatoSlot), r.required)
        assertEquals(optional, r.optional)
    }

    // --- Recipe: số bước 1..8 ---

    @Test
    fun recipe_oneStep_isValid() {
        assertEquals(1, recipe(steps = nSteps(1)).steps.size)
    }

    @Test
    fun recipe_eightSteps_isValid() {
        assertEquals(8, recipe(steps = nSteps(8)).steps.size)
    }

    @Test
    fun recipe_emptySteps_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> { recipe(steps = emptyList()) }
    }

    @Test
    fun recipe_nineSteps_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> { recipe(steps = nSteps(9)) }
    }

    @Test
    fun recipe_blankStep_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> { recipe(steps = listOf("Sơ chế cá", " ", "Nấu")) }
    }

    // --- Recipe: id, title, required ---

    @Test
    fun recipe_emptyId_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> { recipe(id = "") }
    }

    @Test
    fun recipe_whitespaceId_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> { recipe(id = "   ") }
    }

    @Test
    fun recipe_emptyTitle_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> { recipe(title = "") }
    }

    @Test
    fun recipe_whitespaceTitle_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> { recipe(title = "  ") }
    }

    @Test
    fun recipe_emptyRequired_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> { recipe(required = emptyList()) }
    }

    // Kiểm tra phải nằm trong init: copy() cũng đi qua constructor nên không lách được.
    @Test
    fun recipe_copyWithNineSteps_throwsIllegalArgument() {
        val valid = recipe()
        assertFailsWith<IllegalArgumentException> { valid.copy(steps = nSteps(9)) }
    }

    // --- Recipe: bằng nhau theo giá trị ---

    @Test
    fun recipe_equalByValue() {
        val a = recipe()
        val b = recipe()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun recipe_differentStepOrder_notEqual() {
        assertNotEquals(
            recipe(steps = listOf("Một", "Hai")),
            recipe(steps = listOf("Hai", "Một")),
        )
    }

    @Test
    fun recipe_differentOptional_notEqual() {
        val withOptional = recipe().copy(optional = listOf(Slot(anyOf = listOf("hanh-la"))))
        assertNotEquals(recipe(), withOptional)
    }
}
