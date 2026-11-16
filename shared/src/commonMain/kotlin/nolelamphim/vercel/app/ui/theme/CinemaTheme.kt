package nolelamphim.vercel.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import nolelamphim.vercel.app.getPlatform

// Cinefy & Pro Cinema Palette
object CinemaColors {
    val Background = Color(0xFF0D0D11)
    val Surface = Color(0xFF16161D)
    val SurfaceElevated = Color(0xFF21212B)
    val SurfaceCard = Color(0xFF1B1B24)
    val Primary = Color(0xFFFF812B) // Cinefy vibrant orange
    val PrimaryVariant = Color(0xFFFFB16D)
    val PrimaryContainer = Color(0xFF251910)
    val Gold = Color(0xFFFBBF24)
    val BlueBadge = Color(0xFF38BDF8)
    val GreenBadge = Color(0xFF34D399)
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0x1FFFFFFF)
    val BorderFocus = Color(0xFFFF812B)
    val GlassBorder = Color(0x2EFFFFFF)
    val GlassBackground = Color(0xD912121A)
    val NeonCyan = Color(0xFF06B6D4)
    val NeonEmerald = Color(0xFF10B981)
    val NeonViolet = Color(0xFF8B5CF6)
    val NeonRed = Color(0xFFEF4444)

    // Cinefy TV Big Screen Tokens (cinefy-tv.html)
    val TvBackground = Color(0xFF08090C)
    val TvOrange = Color(0xFFFF812B)
    val TvOrangeHighlight = Color(0xFFFFB16D)
    val TvOrangeGlow = Color(0x66FF812B)
    val TvEyebrowBg = Color(0xC9251910)
    val TvEyebrowBorder = Color(0x5CFF9B5C)
    val TvEyebrowText = Color(0xFFFFC18F)
    val TvWhite = Color(0xFFF7F5F2)
    val TvMuted = Color(0xFFA4A2A1)
    val TvGlass = Color(0x13FFFFFF) // rgba(255,255,255,0.075)
    val TvLine = Color(0x21FFFFFF)  // rgba(255,255,255,0.13)
    val TvPosterBg = Color(0xFF17171B)
    val TvSurface = Color(0xFF101014)
    val TvAmbientOrange = Color(0x26B45B29)
    val TvAmbientPurple = Color(0x1954406B)

    val TvOrangeGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFB16D), Color(0xFFFF812B)),
    )
    val TvRankGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFBD84), Color(0xFFFF812B)),
    )

    val PrimaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFB16D), Color(0xFFFF812B)),
    )
    val AccentGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF812B), Color(0xFFEC4899)),
    )
    val GlassGradient = Brush.verticalGradient(
        colors = listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.04f)),
    )
    val PosterOverlayGradient = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color(0x80000000), Color(0xFA0D0D11)),
    )
    val HeroOverlayGradient = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color(0x330D0D11),
            Color(0xCC0D0D11),
            Color(0xFF0D0D11),
        ),
    )
    val HeroSideGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xE60D0D11),
            Color(0x990D0D11),
            Color.Transparent,
        ),
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = CinemaColors.Primary,
    onPrimary = Color.White,
    primaryContainer = CinemaColors.PrimaryContainer,
    onPrimaryContainer = Color.White,
    secondary = CinemaColors.TextSecondary,
    onSecondary = CinemaColors.Surface,
    background = CinemaColors.Background,
    onBackground = CinemaColors.TextPrimary,
    surface = CinemaColors.Surface,
    onSurface = CinemaColors.TextPrimary,
    surfaceVariant = CinemaColors.SurfaceElevated,
    onSurfaceVariant = CinemaColors.TextSecondary,
    outline = CinemaColors.BorderSubtle,
    outlineVariant = Color(0x33FFFFFF),
    error = Color(0xFFEF4444),
)

// Cinefy TV Big Screen Cinema Dark Color Scheme (cinefy-tv.html)
private val TvDarkColorScheme = darkColorScheme(
    primary = CinemaColors.TvOrange,
    onPrimary = Color(0xFF211108),
    primaryContainer = CinemaColors.TvEyebrowBg,
    onPrimaryContainer = CinemaColors.TvEyebrowText,
    secondary = CinemaColors.TvOrangeHighlight,
    onSecondary = Color(0xFF211108),
    background = CinemaColors.TvBackground,
    onBackground = CinemaColors.TvWhite,
    surface = CinemaColors.TvSurface,
    onSurface = CinemaColors.TvWhite,
    surfaceVariant = CinemaColors.TvPosterBg,
    onSurfaceVariant = CinemaColors.TvMuted,
    outline = CinemaColors.TvLine,
    outlineVariant = CinemaColors.TvGlass,
    error = Color(0xFFEF4444),
)

private val CinemaTypography = Typography(
    headlineLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 32.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp,
    ),
    headlineSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 13.sp,
        lineHeight = 19.sp,
    ),
    bodySmall = androidx.compose.ui.text.TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
    ),
    labelSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.SemiBold,
    ),
)

// Cinefy TV 10-Foot Distance Typography
private val TvTypography = Typography(
    headlineLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 38.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
    ),
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
    ),
    headlineSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 17.sp,
        lineHeight = 23.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 16.sp,
        lineHeight = 23.sp,
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = androidx.compose.ui.text.TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 13.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.Medium,
    ),
    labelSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 11.sp,
        lineHeight = 15.sp,
        fontWeight = FontWeight.SemiBold,
    ),
)

@Composable
fun CinemaTheme(content: @Composable () -> Unit) {
    val isTv = LocalIsTv.current || getPlatform().isTv
    MaterialTheme(
        colorScheme = if (isTv) TvDarkColorScheme else DarkColorScheme,
        typography = if (isTv) TvTypography else CinemaTypography,
        content = content,
    )
}

