package com.loitu.yourrefri.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainScaffoldTabStateTest {
    @Test
    fun tabStateKeysAreDistinctAndNonBlank() {
        val keys = NavTab.entries.map(::tabStateKey)

        assertTrue(keys.all { it.isNotBlank() })
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun globalOverlaysAreComputedWhenEitherOverlayIsVisible() {
        assertFalse(shouldComputeGlobalOverlays(false, false))
        assertTrue(shouldComputeGlobalOverlays(true, false))
        assertTrue(shouldComputeGlobalOverlays(false, true))
        assertTrue(shouldComputeGlobalOverlays(true, true))
    }

    @Test
    fun fallbackHomeViewModelIsRememberedBeforeAnimatedContent() {
        val source = scaffoldSource()
        val fallback = Regex(
            """val resolvedHomeViewModel\s*=\s*homeViewModel\s*\?:\s*remember\s*\{""" +
                """\s*(?:[\w.]+\.)?HomeViewModel\(\)\s*\}""",
        ).find(source)
        val animatedContentStart = source.indexOf("AnimatedContent(")

        assertTrue(fallback != null && fallback.range.first < animatedContentStart)
        val animatedContent = source.substring(animatedContentStart)
        assertFalse(animatedContent.contains("HomeViewModel()"))
        assertTrue(animatedContent.contains("homeViewModel = resolvedHomeViewModel"))
    }

    @Test
    fun eachTabSavesItsOwnContentState() {
        val source = scaffoldSource()
        val provider = Regex.escape("tabStateHolder.SaveableStateProvider(tabStateKey(tab))")

        assertTrue(Regex("""NavTab\.HOME\s*->\s*$provider\s*\{\s*HomeScreen\(""").containsMatchIn(source))
        assertTrue(Regex("""NavTab\.PANTRY\s*->\s*$provider\s*\{\s*PantryGridScreen\(""").containsMatchIn(source))
        assertTrue(Regex("""else\s*\{\s*$provider\s*\{\s*RecipeScreen\(""").containsMatchIn(source))
        assertTrue(Regex("""NavTab\.SETTINGS\s*->\s*$provider\s*\{\s*SettingsScreen\(""").containsMatchIn(source))
    }

    @Test
    fun globalWeeklyPlanIsBuiltOnlyInsideTheOverlayGuard() {
        val guardedPlan = Regex(
            """val globalWeeklyPlan\s*=\s*if\s*\(shouldComputeGlobalOverlays\(showWeeklyOverlay, showShoppingOverlay\)\)""" +
                """\s*\{\s*remember\([^)]*\)\s*\{\s*buildWeeklyMealPlan\([^)]*\)\s*\}\s*\}\s*else null""",
        )

        assertTrue(guardedPlan.containsMatchIn(scaffoldSource()))
    }

    private fun scaffoldSource(): String {
        val relative = "src/main/kotlin/com/loitu/yourrefri/ui/MainScaffold.kt"
        return listOf(File(relative), File("composeApp", relative))
            .first { it.isFile }.readText()
    }
}
