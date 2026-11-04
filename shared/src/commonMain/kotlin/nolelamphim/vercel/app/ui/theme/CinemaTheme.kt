package nolelamphim.vercel.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val CinemaColors = darkColorScheme(
    primary = Color(0xFFF97316),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF7C2D12),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFB3B3B3),
    onSecondary = Color(0xFF202024),
    background = Color(0xFF1B1B1F),
    onBackground = Color.White,
    surface = Color(0xFF26262B),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2E2E34),
    onSurfaceVariant = Color(0xFFB3B3B3),
    outline = Color(0x1AFFFFFF),
    outlineVariant = Color(0x29FFFFFF),
    error = Color(0xFFEF4444),
)

private val CinemaTypography = Typography(
    headlineLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 36.sp,
        lineHeight = 43.sp,
        fontWeight = FontWeight.Bold,
    ),
    headlineSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 26.sp,
        lineHeight = 33.sp,
        fontWeight = FontWeight.Bold,
    ),
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 20.sp,
        lineHeight = 27.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 15.sp,
        lineHeight = 23.sp,
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    labelSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 12.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.Medium,
    ),
)

@Composable
fun CinemaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CinemaColors,
        typography = CinemaTypography,
        content = content,
    )
}
