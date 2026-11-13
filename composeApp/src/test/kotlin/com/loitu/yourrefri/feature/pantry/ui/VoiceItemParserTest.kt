package com.loitu.yourrefri.feature.pantry.ui

import com.loitu.yourrefri.feature.pantry.data.Ingredient
import kotlin.test.Test
import kotlin.test.assertEquals

class VoiceItemParserTest {
    private val ingredients = listOf(
        Ingredient("ca-chua", "Cà chua", "rau-cu", 5),
        Ingredient("hanh-la", "Hành lá", "rau-la", 5),
        Ingredient("thit-bo", "Thịt bò", "thit", 2),
        Ingredient("thit-heo", "Thịt heo", "thit", 2),
        Ingredient("ca-basa", "Cá basa", "ca", 2),
        Ingredient("trung-ga", "Trứng gà", "sua-trung", 21),
        Ingredient("nam-rom", "Nấm rơm", "khac", 3),
        Ingredient("sua-tuoi", "Sữa tươi", "sua-trung", 7),
        Ingredient("sua", "Sữa", "sua-trung", 7),
        Ingredient("rau-muong", "Rau muống", "rau-la", 3),
    )

    private fun parse(text: String) = parseVoiceItems(text, ingredients)

    @Test
    fun emptyOrBlankGivesNothing() {
        assertEquals(emptyList(), parse(""))
        assertEquals(emptyList(), parse("   "))
    }

    @Test
    fun singleItemWithDiacritics() {
        assertEquals(listOf("ca-chua"), parse("cà chua"))
    }

    @Test
    fun singleItemWithoutDiacriticsAndUppercase() {
        assertEquals(listOf("ca-chua"), parse("CA CHUA"))
    }

    @Test
    fun splitsOnCommaAndVa() {
        assertEquals(listOf("ca-chua", "hanh-la", "trung-ga"), parse("cà chua, hành lá và trứng gà"))
    }

    @Test
    fun splitsOnVoiAndCung() {
        assertEquals(listOf("thit-bo", "ca-basa"), parse("thịt bò với cá basa"))
        assertEquals(listOf("thit-bo", "sua-tuoi"), parse("thịt bò cùng sữa tươi"))
        assertEquals(listOf("thit-bo", "sua-tuoi"), parse("thit bo cung sua tuoi"))
    }

    @Test
    fun dropsSpokenQuantityAndUnit() {
        assertEquals(listOf("ca-chua"), parse("hai quả cà chua"))
        assertEquals(listOf("trung-ga"), parse("10 quả trứng gà"))
        assertEquals(listOf("thit-bo", "hanh-la"), parse("một cân thịt bò và 2 bó hành lá"))
    }

    @Test
    fun numeralNamNotConfusedWithMushroom() {
        assertEquals(listOf("nam-rom"), parse("nấm rơm"))
        assertEquals(listOf("trung-ga"), parse("năm quả trứng gà"))
    }

    @Test
    fun noDuplicateIds() {
        assertEquals(listOf("ca-chua"), parse("cà chua, hai quả cà chua và cà chua"))
    }

    @Test
    fun ambiguousOrUnknownIsSkipped() {
        assertEquals(emptyList(), parse("thịt"))
        assertEquals(emptyList(), parse("xin chào bạn khỏe không"))
        assertEquals(listOf("ca-chua"), parse("bánh pizza và cà chua"))
    }

    @Test
    fun leadingFillerWordsAreIgnored() {
        assertEquals(listOf("ca-chua", "hanh-la"), parse("tôi mua cà chua và hành lá"))
    }

    @Test
    fun itemsWithoutSeparatorsAreFoundByName() {
        assertEquals(listOf("ca-chua", "trung-ga", "hanh-la"), parse("cà chua trứng gà hành lá"))
    }

    @Test
    fun heardTextIsTrimmedAndSingleSpaced() {
        assertEquals("cà chua và hành lá", voiceHeardText("  cà   chua và  hành lá "))
    }

    @Test
    fun separatorMattersForPartialNames() {
        assertEquals(listOf("rau-muong", "trung-ga"), parse("muống với trứng gà"))
        assertEquals(listOf("rau-muong", "trung-ga"), parse("muống và trứng gà"))
    }

    @Test
    fun quantityWordIsDroppedBeforePartialName() {
        assertEquals(listOf("rau-muong"), parse("hai quả muống"))
    }

    @Test
    fun longestNameWinsWhenScanningWithoutSeparators() {
        assertEquals(listOf("sua-tuoi", "trung-ga"), parse("mua sữa tươi trứng gà"))
    }
}
