package com.loitu.yourrefri.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Màu theo vai trò, đổi theo chế độ sáng/tối. Màn hình dùng `AppTheme.colors.xxx` thay cho hằng số màu cố định.
 *
 * Bảng chuyển từ hằng số cũ: WarmIvory→background, SurfaceWhite / Color.White làm nền thẻ→surface,
 * SoftGreenLight→surfaceSoft, SoftGreen→primarySoft, SageGreenStrong→primary (chữ Color.White trên nó→onPrimary),
 * SageGreen→brand, SageGreenText→accentText, DeepCharcoal→text, MutedGray→muted, LineSoft→line,
 * Terracotta→warn, TerracottaSoft→warnSoft, TerracottaText→warnText, WarmBeigeSoft→beigeSoft, WarmBeigeText→beigeText.
 */
@Immutable
class AppColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceSoft: Color,
    val primarySoft: Color,
    val primary: Color,
    val onPrimary: Color,
    val brand: Color,
    val text: Color,
    val muted: Color,
    val accentText: Color,
    val line: Color,
    val warn: Color,
    val warnSoft: Color,
    val warnText: Color,
    val beigeSoft: Color,
    val beigeText: Color,
    val snackbar: Color,
    val onSnackbar: Color,
    val snackbarAction: Color,
    // Màu cam đất làm điểm nhấn chính theo ảnh thiết kế: tab đang chọn, nút chính, nút giọng nói.
    val cta: Color,
    val onCta: Color,
    val ctaSoft: Color,
    val ctaText: Color,
    // Cạnh dưới của nút nổi: tối hơn màu nền nút, để nút trông như có độ dày khi nhấn.
    val primaryEdge: Color,
    val ctaEdge: Color,
    val badgeRed: Color,
    val navContainer: Color,
    val navUnselected: Color,
)

private val LightPrimaryEdge = Color(0xFF3B4F3C)
private val LightCtaEdge = Color(0xFF8A3D27)
private val DarkPrimaryEdge = Color(0xFF5E7A5D)
private val DarkCtaEdge = Color(0xFF9A6A58)
private val LightBadgeRed = Color(0xFFD93829)
private val DarkBadgeRed = Color(0xFFFF6B5E)
private val LightNavContainer = Color(0xFF2B302B)
private val DarkNavContainer = Color(0xFF242924)
private val NavUnselectedLabel = Color(0xFFC5CCC5)

internal val LightAppColors = AppColors(
    isDark = false,
    background = WarmIvory,
    surface = SurfaceWhite,
    surfaceSoft = SoftGreenLight,
    primarySoft = SoftGreen,
    primary = SageGreenStrong,
    onPrimary = Color.White,
    brand = SageGreen,
    text = DeepCharcoal,
    muted = MutedGray,
    accentText = SageGreenText,
    line = LineSoft,
    warn = Terracotta,
    warnSoft = TerracottaSoft,
    warnText = TerracottaText,
    beigeSoft = WarmBeigeSoft,
    beigeText = WarmBeigeText,
    snackbar = DeepCharcoal,
    onSnackbar = WarmIvory,
    snackbarAction = DarkPrimary,
    cta = TerracottaStrong,
    onCta = Color.White,
    ctaSoft = TerracottaSoft,
    ctaText = TerracottaText,
    primaryEdge = LightPrimaryEdge,
    ctaEdge = LightCtaEdge,
    badgeRed = LightBadgeRed,
    navContainer = LightNavContainer,
    navUnselected = NavUnselectedLabel,
)

internal val DarkAppColors = AppColors(
    isDark = true,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceSoft = DarkSurfaceVariant,
    primarySoft = DarkPrimarySoft,
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    brand = DarkPrimary,
    text = DarkText,
    muted = DarkMutedText,
    accentText = DarkPrimary,
    line = DarkLine,
    warn = Terracotta,
    warnSoft = DarkWarnSoft,
    warnText = DarkWarnText,
    beigeSoft = DarkBeigeSoft,
    beigeText = DarkBeigeText,
    snackbar = DarkSnackbar,
    onSnackbar = DarkText,
    snackbarAction = DarkPrimary,
    cta = DarkWarnText,
    onCta = DarkWarnSoft,
    ctaSoft = DarkWarnSoft,
    ctaText = DarkWarnText,
    primaryEdge = DarkPrimaryEdge,
    ctaEdge = DarkCtaEdge,
    badgeRed = DarkBadgeRed,
    navContainer = DarkNavContainer,
    navUnselected = NavUnselectedLabel,
)

fun appColorsFor(dark: Boolean): AppColors = if (dark) DarkAppColors else LightAppColors

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}
