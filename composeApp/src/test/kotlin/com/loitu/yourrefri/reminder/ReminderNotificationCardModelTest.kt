package com.loitu.yourrefri.reminder

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReminderNotificationCardModelTest {
    @Test
    fun urgentGenericReminderExtractsPunchyHeroAndSingleLineSubtitle() {
        val model = buildReminderCardModel(
            title = "Gợi ý từ tủ nhà mình",
            body = "Mực tươi, Cá hồi và 10 món khác nên dùng trước (hạn ước tính). Mở xem gợi ý món nhé.",
        )
        assertTrue(model.badge.contains("Nên dùng sớm", ignoreCase = true), "badge=${model.badge}")
        assertTrue(model.hero.contains("Mực tươi") && model.hero.length <= 28, "hero=${model.hero}")
        assertTrue(model.subtitle.isNotBlank() && model.subtitle.length <= 48, "subtitle=${model.subtitle}")
        assertTrue(model.subtitle.contains("10 món"), "subtitle phải giữ số món: ${model.subtitle}")
        assertFalse(model.hero == model.subtitle, "hero và subtitle không được lặp nhau")
        assertNoBannedWords(model)
    }

    @Test
    fun dishSuggestionReminderExtractsCleanDishHero() {
        val model = buildReminderCardModel(
            title = "Tối nay nấu Canh rau ngót nhé",
            body = "Món Canh rau ngót dễ nấu, ấm bụng cho bữa cơm nhà.",
        )
        assertTrue(model.badge.contains("Gợi ý", ignoreCase = true), "badge=${model.badge}")
        assertTrue(model.hero.contains("Canh rau ngót") && model.hero.length <= 28, "hero=${model.hero}")
        assertTrue(model.subtitle.isNotBlank() && model.subtitle.length <= 48, "subtitle=${model.subtitle}")
        assertNoBannedWords(model)
    }

    @Test
    fun blankInputsFallBackToWarmSafeDefaults() {
        val model = buildReminderCardModel(title = "", body = "")
        assertTrue(model.badge.isNotBlank())
        assertTrue(model.hero.isNotBlank())
        assertTrue(model.subtitle.isNotBlank())
        assertNoBannedWords(model)
    }

    private fun assertNoBannedWords(model: ReminderCardModel) {
        val all = "${model.badge} ${model.hero} ${model.subtitle}"
        listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên", "streak", "tồn kho").forEach { banned -> // lint-copy: rule
            assertFalse(all.contains(banned, ignoreCase = true), "chứa từ cấm: $banned")
        }
    }
}
