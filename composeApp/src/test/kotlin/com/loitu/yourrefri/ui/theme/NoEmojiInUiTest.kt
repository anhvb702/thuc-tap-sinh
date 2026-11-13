package com.loitu.yourrefri.ui.theme

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * DESIGN.md: icon giao diện là icon nét, không dùng emoji. Test quét các file màn hình và báo lỗi nếu còn emoji.
 * Dòng có chú thích `guard:allow` được bỏ qua (chỗ cắt emoji khỏi chuỗi dùng chung đang bị test khác khóa).
 */
class NoEmojiInUiTest {
    private val base = "com/loitu/yourrefri"
    private val guarded = listOf(
        "feature/home/ui/HomeScreen.kt",
        "feature/pantry/ui/PantryGridScreen.kt",
        "feature/recipe/ui/RecipeScreen.kt",
        "feature/recipe/ui/RecipeDetailScreen.kt",
        "feature/recipe/ui/WeeklyMealPlanContent.kt",
        "feature/recipe/ui/HandsFreeCookScreen.kt",
        "feature/recipe/ui/RecipeTabs.kt",
        "feature/recipe/ui/RecipeHeroPager.kt",
        "feature/recipe/ui/RecipeDailyCards.kt",
        "feature/pantry/ui/VoiceInput.kt",
        "feature/settings/ui/SettingsScreen.kt",
        "ui/MainScaffold.kt",
    )

    private fun isEmoji(cp: Int): Boolean =
        cp in 0x1F300..0x1FAFF || cp in 0x2600..0x27BF || cp in 0x23E9..0x23FF || cp == 0x2B50 || cp == 0xFE0F

    private fun sourceFile(relative: String): File =
        listOf(
            File("src/main/kotlin/$base/$relative"),
            File("composeApp/src/main/kotlin/$base/$relative"),
        ).firstOrNull { it.exists() } ?: error("Không thấy file nguồn: $relative")

    @Test
    fun guardedScreensContainNoEmoji() {
        val violations = mutableListOf<String>()
        guarded.forEach { relative ->
            sourceFile(relative).readLines().forEachIndexed { index, line ->
                if ("guard:allow" in line) return@forEachIndexed
                if (line.codePoints().anyMatch { isEmoji(it) }) violations += "$relative:${index + 1} ${line.trim().take(90)}"
            }
        }
        assertTrue(violations.isEmpty(), "Còn emoji trong giao diện:\n" + violations.joinToString("\n"))
    }
}
