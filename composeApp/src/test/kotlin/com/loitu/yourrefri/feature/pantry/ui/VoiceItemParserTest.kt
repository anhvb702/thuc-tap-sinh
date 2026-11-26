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
        Ingredient("dau-phu", "Đậu phụ", "khac", 2),
        Ingredient("dua-leo", "Dưa leo", "rau-cu", 5),
        Ingredient("ba-chi", "Thịt ba chỉ", "thit", 2),
        Ingredient("thit-heo-xay", "Thịt heo xay", "thit", 1),
        Ingredient("cha-lua", "Chả lụa", "thit", 3),
        Ingredient("bong-cai", "Bông cải xanh", "rau-cu", 5),
        Ingredient("dau-que", "Đậu que", "rau-cu", 5),
        Ingredient("nuoc-tuong", "Nước tương", "gia-vi", 30),
        Ingredient("bot-ngot", "Bột ngọt", "gia-vi", 30),
        Ingredient("hanh-tim", "Hành tím", "gia-vi", 21),
    )

    private fun parse(text: String) = parseVoiceItems(text, ingredients)

    @Test
    fun rmsNormalizesAndClampsToUnitRange() {
        assertEquals(0f, normalizeVoiceRms(-2f))
        assertEquals(0f, normalizeVoiceRms(-10f))
        assertEquals(0.5f, normalizeVoiceRms(4f))
        assertEquals(1f, normalizeVoiceRms(10f))
        assertEquals(1f, normalizeVoiceRms(20f))
    }

    @Test
    fun nonFiniteRmsNormalizesToSilence() {
        assertEquals(0f, normalizeVoiceRms(Float.NaN))
        assertEquals(0f, normalizeVoiceRms(Float.POSITIVE_INFINITY))
        assertEquals(0f, normalizeVoiceRms(Float.NEGATIVE_INFINITY))
    }

    @Test
    fun loudVoiceUsesFullSymmetricWave() {
        assertEquals(
            listOf(0.45f, 0.78f, 1f, 0.78f, 0.45f),
            voiceWaveBarHeights(1f, animatorsEnabled = true),
        )
    }

    @Test
    fun silenceKeepsBarsVisible() {
        assertEquals(
            listOf(0.1125f, 0.195f, 0.25f, 0.195f, 0.1125f),
            voiceWaveBarHeights(0f, animatorsEnabled = true),
        )
    }

    @Test
    fun waveformClampsLevelsOutsideUnitRange() {
        assertEquals(voiceWaveBarHeights(0f, true), voiceWaveBarHeights(-1f, true))
        assertEquals(voiceWaveBarHeights(1f, true), voiceWaveBarHeights(2f, true))
    }

    @Test
    fun reducedMotionKeepsBarsStaticAtAnyLevel() {
        val staticHeights = List(5) { 0.45f }
        assertEquals(staticHeights, voiceWaveBarHeights(0f, animatorsEnabled = false))
        assertEquals(staticHeights, voiceWaveBarHeights(1f, animatorsEnabled = false))
    }

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

    @Test
    fun northSouthSynonymsMatchInSentence() {
        assertEquals(listOf("dau-phu", "thit-heo"), parse("đậu hũ và thịt lợn"))
    }

    @Test
    fun northSouthSynonymsMatchInCommaList() {
        assertEquals(
            listOf("dua-leo", "ba-chi", "bot-ngot", "nuoc-tuong"),
            parse("dưa chuột, ba rọi, mì chính, xì dầu"),
        )
    }

    @Test
    fun northSouthSynonymsKeepQuantityStripping() {
        assertEquals(listOf("ba-chi"), parse("mua ba rọi"))
        assertEquals(listOf("cha-lua"), parse("hai cái giò lụa"))
    }

    @Test
    fun minceAndSouthernMeatNamesMapToGroundPork() {
        assertEquals(listOf("thit-heo-xay"), parse("thịt bằm"))
        assertEquals(listOf("thit-heo-xay"), parse("lợn xay"))
        assertEquals(listOf("thit-heo-xay"), parse("thịt heo xay"))
    }

    @Test
    fun southernVegetableNamesMap() {
        assertEquals(listOf("bong-cai", "dau-que"), parse("súp lơ xanh và đậu cô ve"))
        assertEquals(listOf("hanh-tim"), parse("củ hành tím"))
    }

    @Test
    fun regionalSynonymsReplaceWholeWordsOnly() {
        assertEquals("bot ngot dau phu", applyRegionalSynonyms("mi chinh tau hu"))
        assertEquals("thit heo xay", applyRegionalSynonyms("thit heo xay"))
        assertEquals("ca loc", applyRegionalSynonyms("ca qua"))
        assertEquals("ca loc", applyRegionalSynonyms("ca chuoi"))
        assertEquals("dau que", applyRegionalSynonyms("dau co ve"))
        assertEquals("bong cai xanh", applyRegionalSynonyms("sup lo xanh"))
        assertEquals("ca quay", applyRegionalSynonyms("ca quay"))
    }
}
