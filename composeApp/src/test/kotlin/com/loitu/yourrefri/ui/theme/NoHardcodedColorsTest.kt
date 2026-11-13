package com.loitu.yourrefri.ui.theme

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Chế độ tối chỉ đúng khi màn hình lấy màu từ `AppTheme.colors`. Test này quét mã nguồn các màn hình
 * và báo lỗi nếu còn màu viết cứng hoặc import hằng số màu của bảng sáng.
 * Dòng có chú thích `guard:allow` được bỏ qua (dùng cho hằng số đang bị test khác khóa).
 */
class NoHardcodedColorsTest {
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
    private val lightTokens = listOf(
        "WarmIvory", "SurfaceWhite", "SoftGreenLight", "SoftGreen", "SageGreenStrong", "SageGreenText",
        "SageGreen", "DeepCharcoal", "MutedGray", "LineSoft", "TerracottaSoft", "TerracottaText",
        "Terracotta", "WarmBeigeSoft", "WarmBeigeText", "WarmBeige",
    )
    private val tokenImport = Regex("""^import com\.loitu\.yourrefri\.ui\.theme\.(${lightTokens.joinToString("|")})$""")

    private fun sourceFile(relative: String): File {
        val candidates = listOf(
            File("src/main/kotlin/$base/$relative"),
            File("composeApp/src/main/kotlin/$base/$relative"),
        )
        return candidates.firstOrNull { it.exists() } ?: error("Không thấy file nguồn: $relative")
    }

    @Test
    fun guardedScreensUseThemeColorsOnly() {
        val violations = mutableListOf<String>()
        guarded.forEach { relative ->
            sourceFile(relative).readLines().forEachIndexed { index, raw ->
                val line = raw.trim()
                if ("guard:allow" in line || line.startsWith("//") || line.startsWith("*")) return@forEachIndexed
                val where = "$relative:${index + 1}"
                if ("Color(0x" in line) violations += "$where Color(0x...)"
                if (Regex("""\bColor\.White\b""").containsMatchIn(line)) violations += "$where Color.White"
                if (Regex("""\bColor\.Black\b""").containsMatchIn(line)) violations += "$where Color.Black"
                if (tokenImport.matches(line)) violations += "$where $line"
            }
        }
        assertTrue(violations.isEmpty(), "Còn màu viết cứng:\n" + violations.joinToString("\n"))
    }
}
