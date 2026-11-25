package com.loitu.yourrefri.reminder

import androidx.compose.ui.graphics.toArgb
import com.loitu.yourrefri.ui.theme.LightAppColors
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
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

    @Test
    fun bigPictureUsesSpecifiedDimensionsAndHorizontalSafeZone() {
        assertEquals(1080, ReminderCardRenderer.WIDTH)
        assertEquals(450, ReminderCardRenderer.HEIGHT)
        assertEquals(56, ReminderCardRenderer.SAFE_ZONE_HORIZONTAL_PX)
        assertTrue(ReminderCardRenderer.CONTENT_SAFE_INSET_PX >= 150)
    }

    @Test
    fun urgentBigPictureUsesExtraCountFromBodyEvenWithDishTitle() {
        val content = buildReminderBigPictureContent(
            title = "Tối nay nấu Canh rau ngót nhé",
            body = "Mực tươi, Cá hồi và 2 món khác nên dùng trước (hạn ước tính).",
        )

        assertEquals("🍲 Canh rau ngót · Nên dùng sớm", content.contentTitle)
        assertEquals("+2 món nữa nên dùng sớm", content.contentText)
        assertEquals("Canh rau ngót", content.hero)
        assertEquals("Dễ nấu, ấm bụng cho bữa cơm nhà", content.subtitle)
        assertBigPictureCopy(content)
    }

    @Test
    fun urgentBigPictureReadsPlusCount() {
        val content = buildReminderBigPictureContent(
            title = "Tối nay nấu Canh rau ngót nhé",
            body = "+2 món nên dùng trước",
        )

        assertEquals("+2 món nữa nên dùng sớm", content.contentText)
        assertBigPictureCopy(content)
    }

    @Test
    fun urgentBigPictureWithoutExtraItemsInvitesOpeningSuggestion() {
        val content = buildReminderBigPictureContent(
            title = "Gợi ý từ tủ nhà mình",
            body = "Mực tươi nên dùng trước (hạn ước tính).",
        )

        assertEquals("🍲 Mực tươi · Nên dùng sớm", content.contentTitle)
        assertEquals("Mở xem gợi ý món ngon hôm nay", content.contentText)
        assertBigPictureCopy(content)
    }

    @Test
    fun nonUrgentBigPictureUsesTodaysSuggestion() {
        val content = buildReminderBigPictureContent(
            title = "Gợi ý bữa cơm: Canh rau ngót",
            body = "Món Canh rau ngót dễ nấu, ấm bụng cho bữa cơm nhà.",
        )

        assertEquals("🍲 Canh rau ngót · Gợi ý hôm nay", content.contentTitle)
        assertEquals("Mở xem gợi ý món ngon hôm nay", content.contentText)
        assertBigPictureCopy(content)
    }

    @Test
    fun bigPictureReturnsNullWhenRenderingRunsOutOfMemory() {
        assertNull(resolveBigPictureStyle { throw OutOfMemoryError() })
    }

    @Test
    fun bigPictureReturnsNullWhenRenderingFails() {
        assertNull(resolveBigPictureStyle { throw IllegalStateException() })
    }

    @Test
    fun bigPictureAttemptsRenderingAndAcceptsMissingBanner() {
        var renderCalls = 0
        val banner = resolveBigPictureStyle {
            renderCalls++
            null
        }

        assertNull(banner)
        assertEquals(1, renderCalls)
    }

    @Test
    fun accentColorArgb_matchesNotifyHeroStartToken() {
        assertEquals(LightAppColors.notifyHeroStart.toArgb(), ReminderCardRenderer.accentColorArgb())
    }

    @Test
    fun dishBleedLayout_bleedsAsymmetricallyPastRightEdge() {
        val layout = ReminderCardRenderer.computeDishBleedLayout(hasPicture = true)
        val rightEdge = layout.centerX + layout.diameter / 2f
        assertTrue(rightEdge > ReminderCardRenderer.WIDTH)
        assertEquals(1100f, rightEdge)
        assertEquals(225f, layout.centerY)
    }

    @Test
    fun dishBleedLayout_keepsClearGapBetweenTextAndDish() {
        val layout = ReminderCardRenderer.computeDishBleedLayout(hasPicture = true)
        val textRight = ReminderCardRenderer.CONTENT_SAFE_INSET_PX + layout.textWidth
        val dishLeft = layout.centerX - layout.diameter / 2f
        assertTrue(textRight < dishLeft)
        assertEquals(28f, dishLeft - textRight)
    }

    @Test
    fun dishBleedLayout_withoutPictureExpandsTextWidthToRightInset() {
        val layout = ReminderCardRenderer.computeDishBleedLayout(hasPicture = false)
        assertEquals(776, layout.textWidth)
    }

    @Test
    fun rendererConstants_keepPixelFullBleedGeometry() {
        assertEquals(1080, ReminderCardRenderer.WIDTH)
        assertEquals(450, ReminderCardRenderer.HEIGHT)
        assertEquals(340f, ReminderCardRenderer.DISH_DIAMETER_PX)
        assertEquals(20f, ReminderCardRenderer.DISH_RIGHT_BLEED_PX)
        assertEquals(152, ReminderCardRenderer.CONTENT_SAFE_INSET_PX)
    }

    @Test
    fun reminderNotifier_setsAccentColorAndAvoidsRemoteViews() {
        val path = "src/main/kotlin/com/loitu/yourrefri/reminder/ReminderNotifier.kt"
        val source = listOf(File(path), File("composeApp", path)).first { it.isFile }.readText()
        assertTrue(source.contains(".setColor(ReminderCardRenderer.accentColorArgb())"))
        assertFalse(source.contains("setCustomContentView"))
        assertFalse(source.contains("setCustomBigContentView"))
        assertFalse(source.contains("RemoteViews"))
    }

    private fun assertBigPictureCopy(content: ReminderBigPictureContent) {
        assertEquals("Xem món & nấu ngay", content.actionLabel)
        assertNoBannedWords(
            ReminderCardModel(
                content.contentTitle,
                content.contentText,
                "${content.hero} ${content.subtitle} ${content.actionLabel}",
            ),
        )
    }

    private fun assertNoBannedWords(model: ReminderCardModel) {
        val all = "${model.badge} ${model.hero} ${model.subtitle}"
        listOf("hết hạn", "lãng phí", "bỏ lỡ", "bạn quên", "streak", "tồn kho").forEach { banned -> // lint-copy: rule
            assertFalse(all.contains(banned, ignoreCase = true), "chứa từ cấm: $banned")
        }
    }
}
