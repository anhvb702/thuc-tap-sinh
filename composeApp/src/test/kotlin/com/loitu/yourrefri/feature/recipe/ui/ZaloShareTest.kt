package com.loitu.yourrefri.feature.recipe.ui

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ZaloShareTest {
    private fun source(path: String): String {
        val root = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
            .first { File(it, "composeApp/src/main/kotlin/com/loitu/yourrefri/$path").isFile }
        return File(root, "composeApp/src/main/kotlin/com/loitu/yourrefri/$path").readText()
    }

    @Test fun intentsTargetZaloAndChooserPreservesText() {
        val helper = source("feature/recipe/ui/ZaloShare.kt")
        assertTrue(helper.contains("const val ZALO_PACKAGE = \"com.zing.zalo\""))
        assertTrue(helper.contains("Intent.ACTION_SEND"))
        assertTrue(helper.contains("type = \"text/plain\""))
        assertTrue(helper.contains("putExtra(Intent.EXTRA_TEXT, text)"))
        assertTrue(helper.contains("setPackage(ZALO_PACKAGE)"))
        assertTrue(helper.contains("Intent.createChooser"))
        assertTrue(helper.contains("chooserTitle: String = \"Gửi danh sách đi chợ\""))
        assertTrue(helper.contains("ActivityNotFoundException"))
        assertTrue(helper.contains("catch (_: SecurityException)"))
    }

    @Test fun shoppingButtonsCopyAndShareTheSameChecklist() {
        val daily = source("feature/recipe/ui/DailyShopping.kt")
        val weekly = source("feature/recipe/ui/WeeklyMealPlanContent.kt")
        listOf(daily, weekly).forEach { screen ->
            assertTrue(screen.contains("clipboard.setText(AnnotatedString(text))"))
            assertTrue(screen.contains("shareToZaloOrChooser(context, text)"))
            assertTrue(screen.contains("Gửi qua Zalo"))
        }
    }
}
