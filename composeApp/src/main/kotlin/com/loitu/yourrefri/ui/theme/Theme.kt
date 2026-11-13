package com.loitu.yourrefri.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.loitu.yourrefri.R

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
val TerracottaSoft = Color(0xFFF7E4DA) // Nền nhãn "nên dùng trước", đi với TerracottaText
val WarmBeigeSoft = Color(0xFFEFE4CB)  // Nền nhãn "khoảng còn vài ngày", đi với WarmBeigeText
val WarmBeigeText = Color(0xFF5E4B22)  // Chữ trên WarmBeigeSoft
val LineSoft = Color(0xFFE3E7DC)       // Viền thẻ nhẹ, thay màu viền viết cứng
val TerracottaStrong = Color(0xFFB4553A) // Nền nút chính cam đất, chữ trắng đạt AA

// Bảng màu tối: nền xanh than ấm, không đen tuyền.
val DarkBackground = Color(0xFF1B1F1B)
val DarkSurface = Color(0xFF252A25)
val DarkSurfaceVariant = Color(0xFF2B312B)
val DarkPrimary = Color(0xFF9DBB9C)
val DarkOnPrimary = Color(0xFF14200F)
val DarkPrimarySoft = Color(0xFF34402F)
val DarkText = Color(0xFFECEFE8)
val DarkMutedText = Color(0xFFAAB2A6)
val DarkWarnSoft = Color(0xFF3F2C24)
val DarkWarnText = Color(0xFFF2BBA5)
val DarkBeigeSoft = Color(0xFF3A3322)
val DarkBeigeText = Color(0xFFE8D8B9)
val DarkLine = Color(0xFF394039)
val DarkSnackbar = Color(0xFF3A423A)   // Thanh thông báo ngắn, sáng hơn nền một chút

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

internal val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimarySoft,
    onPrimaryContainer = DarkText,
    secondary = DarkBeigeSoft,
    onSecondary = DarkBeigeText,
    tertiary = DarkWarnText,
    onTertiary = DarkWarnSoft,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMutedText,
    outline = DarkLine,
    error = DarkWarnText,
    onError = DarkWarnSoft,
)

// Font nhúng sẵn trong app (res/font), dùng được khi không có mạng. Đủ dấu tiếng Việt.
val BeVietnamPro = FontFamily(
    Font(R.font.be_vietnam_pro_regular, FontWeight.Normal),
    Font(R.font.be_vietnam_pro_medium, FontWeight.Medium),
    Font(R.font.be_vietnam_pro_semibold, FontWeight.SemiBold),
    Font(R.font.be_vietnam_pro_bold, FontWeight.Bold),
)

val AppTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
)

@Composable
fun YourRefriTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppColors provides appColorsFor(darkTheme)) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
