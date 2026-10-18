package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val KaizenDarkNavyColorScheme = darkColorScheme(
    primary = TealAccent,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0F3D3E),
    onPrimaryContainer = Color(0xFF99F6E4),
    secondary = IndigoAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2C326E),
    onSecondaryContainer = Color(0xFFC7D2FE),
    tertiary = AmberAccent,
    background = DarkNavyBg,
    onBackground = TextPrimaryDark,
    surface = DarkCardBg,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceSubtle,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkCardBorder,
    outlineVariant = Color(0xFF1E2846)
)

val KaizenMatteDarkColorScheme = darkColorScheme(
    primary = TealAccent,
    onPrimary = Color.Black,
    secondary = IndigoAccent,
    onSecondary = Color.White,
    background = MatteDarkBg,
    onBackground = TextPrimaryDark,
    surface = MatteDarkCard,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF1D2234),
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF2B324D)
)

val KaizenPastelColorScheme = lightColorScheme(
    primary = IndigoAccent,
    onPrimary = Color.White,
    secondary = TealAccent,
    onSecondary = Color.White,
    background = PastelBg,
    onBackground = TextPrimaryLight,
    surface = PastelCardBg,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFEDE9FE),
    onSurfaceVariant = TextSecondaryLight,
    outline = PastelCardBorder
)

@Composable
fun KaizenTheme(
    themeMode: String = "dark_navy",
    accentHex: String = "#14B8A6",
    content: @Composable () -> Unit
) {
    val dynamicPrimary = try {
        Color(android.graphics.Color.parseColor(accentHex))
    } catch (_: Exception) {
        TealAccent
    }

    val baseScheme = when (themeMode) {
        "matte_pastel", "light" -> KaizenPastelColorScheme.copy(primary = dynamicPrimary)
        "matte_dark" -> KaizenMatteDarkColorScheme.copy(primary = dynamicPrimary)
        else -> KaizenDarkNavyColorScheme.copy(primary = dynamicPrimary)
    }

    MaterialTheme(
        colorScheme = baseScheme,
        typography = Typography,
        content = content
    )
}
