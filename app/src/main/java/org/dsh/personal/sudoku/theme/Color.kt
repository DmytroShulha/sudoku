package org.dsh.personal.sudoku.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Speculative One UI 8.0/8.5 Palette
 * Focus: Fluidity, depth, and extreme clarity.
 */

@Suppress("MagicNumber")
val SamsungOneUI8Blue = Color(0xFF1B6CFF) // More modern, slightly deeper but vibrant blue
@Suppress("MagicNumber")
val SamsungOneUI8BlueContainer = Color(0xFFD6E4FF)

@Suppress("MagicNumber")
val SamsungOneUI8Gray100 = Color(0xFFF9F9FB) // Ultra light background
@Suppress("MagicNumber")
val SamsungOneUI8Gray200 = Color(0xFFF2F2F7) // Standard background
@Suppress("MagicNumber")
val SamsungOneUI8Gray300 = Color(0xFFE5E5EA) // Secondary surface
@Suppress("MagicNumber")
val SamsungOneUI8Gray900 = Color(0xFF1C1C1E) // High contrast text/dark surface
@Suppress("MagicNumber")
val SamsungOneUI8Gray950 = Color(0xFF0A0A0A) // Deeper black for "OLED+" look

@Suppress("MagicNumber")
val SamsungOneUI8Red = Color(0xFFFF3B30) // More vibrant system red

// Light Theme Specifics
val LightBackground = SamsungOneUI8Gray200
val LightSurface = Color.White
val LightOnSurface = SamsungOneUI8Gray900

// Dark Theme Specifics
val DarkBackground = Color.Black
val DarkSurface = SamsungOneUI8Gray950
val DarkOnSurface = Color.White

@Suppress("MagicNumber")
val LightAppColorScheme = lightColorScheme(
    primary = SamsungOneUI8Blue,
    onPrimary = Color.White,
    primaryContainer = SamsungOneUI8BlueContainer,
    onPrimaryContainer = Color(0xFF002B70),
    secondary = SamsungOneUI8Gray900,
    onSecondary = Color.White,
    secondaryContainer = SamsungOneUI8Gray300,
    onSecondaryContainer = SamsungOneUI8Gray900,
    tertiary = Color(0xFF5856D6), // Indigo accent
    onTertiary = Color.White,
    error = SamsungOneUI8Red,
    onError = Color.White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = SamsungOneUI8Gray300,
    onSurfaceVariant = Color(0xFF48484A),
    outline = Color(0xFFC7C7CC),
    outlineVariant = Color(0xFFD1D1D6)
)

@Suppress("MagicNumber")
val DarkAppColorScheme = darkColorScheme(
    primary = SamsungOneUI8Blue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0045B5),
    onPrimaryContainer = SamsungOneUI8BlueContainer,
    secondary = Color.White,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF2C2C2E),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF5E5CE6),
    onTertiary = Color.White,
    error = Color(0xFFFF453A),
    onError = Color.White,
    background = DarkBackground,
    onBackground = Color(0xFFF2F2F7),
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFFC7C7CC),
    outline = Color(0xFF3A3A3C),
    outlineVariant = Color(0xFF242426)
)
