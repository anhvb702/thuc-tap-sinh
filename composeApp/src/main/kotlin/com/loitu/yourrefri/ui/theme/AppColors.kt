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
)

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
)

fun appColorsFor(dark: Boolean): AppColors = if (dark) DarkAppColors else LightAppColors

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}
