package com.loitu.yourrefri.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Warm Minimalism Signature Palette (DESIGN.md)
val WarmIvory = Color(0xFFF7F5EF)     // Nền chính
val SageGreen = Color(0xFF789477)     // Màu chủ đạo / Primary
val SageGreenText = Color(0xFF536C51) // Chữ xanh đạt tương phản trên nền sáng
val SageGreenStrong = Color(0xFF506950) // Nền hành động có chữ trắng
val SoftGreen = Color(0xFFDDE8D9)     // Nền thẻ phụ / Chip
val SoftGreenLight = Color(0xFFF0F5EF)// Nền thẻ đang có nhẹ nhàng
val WarmBeige = Color(0xFFE8D8B9)     // Điểm nhấn ấm
val Terracotta = Color(0xFFD98C70)    // Cảnh báo nhẹ (đồ sắp dùng)
val TerracottaText = Color(0xFF8C4A35) // Chữ cảnh báo đạt tương phản trên nền sáng
val DeepCharcoal = Color(0xFF343B34)  // Màu chữ chính
val MutedGray = Color(0xFF596057)     // Chữ phụ / ngày tháng đạt tương phản AA
val SurfaceWhite = Color(0xFFFFFFFF)  // Thẻ trắng

private val LightColorScheme = lightColorScheme(
    primary = SageGreenStrong,
    onPrimary = Color.White,
    primaryContainer = SoftGreen,
    onPrimaryContainer = DeepCharcoal,
    secondary = WarmBeige,
    onSecondary = DeepCharcoal,
    tertiary = TerracottaText,
    onTertiary = Color.White,
    background = WarmIvory,
    onBackground = DeepCharcoal,
    surface = SurfaceWhite,
    onSurface = DeepCharcoal,
    surfaceVariant = SoftGreenLight,
    onSurfaceVariant = MutedGray,
    error = TerracottaText,
    onError = Color.White,
)

val AppTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        color = DeepCharcoal,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        color = DeepCharcoal,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = DeepCharcoal,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = DeepCharcoal,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = DeepCharcoal,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = MutedGray,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = SageGreenText,
    ),
)

@Composable
fun YourRefriTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content,
    )
}
