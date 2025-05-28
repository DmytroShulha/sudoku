package org.dsh.personal.sudoku.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val PrimaryBlue = Color(0xFF3D5AFE)
val PrimaryBlueDark = Color(0xFF0031CA)
val PrimaryBlueLight = Color(0xFF7A89FF)

// Accent Colors (Remain as defined for custom static theme)
val AccentOrange = Color(0xFFFFAB40)
val AccentOrangeDark = Color(0xFFC77C02)
val AccentOrangeLight = Color(0xFFFFDD72)

// Base Error Colors (Remain as defined for custom static theme)
val AppErrorBase = Color(0xFFB00020) // For light theme error
val AppOnErrorBase = Color.White      // For light theme onError

// Light Theme Specific Base Colors
val LightBackgroundBase = Color(0xFFF5F5F5)
val LightOnBackgroundBase = Color.Black
val LightSurfaceBase = Color.White
val LightOnSurfaceBase = Color.Black

// Dark Theme Specific Base Colors
val DarkBackgroundBase = Color(0xFF121212)
val DarkOnBackgroundBase = Color.White // Often a slightly off-white like #E0E0E0 is better
val DarkSurfaceBase = Color(0xFF1E1E1E)
val DarkOnSurfaceBase = Color.White    // Often a slightly off-white

// Static Light Color Scheme (Fallback)
val LightAppColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = PrimaryBlueDark, // Text color on primaryContainer
    secondary = AccentOrange,
    onSecondary = Color.Black,
    secondaryContainer = AccentOrangeLight,
    onSecondaryContainer = AccentOrangeDark, // Text color on secondaryContainer
    tertiary = AccentOrange, // Can be same as secondary or a new accent
    onTertiary = Color.Black,
    tertiaryContainer = AccentOrangeLight,
    onTertiaryContainer = AccentOrangeDark,
    error = AppErrorBase,
    onError = AppOnErrorBase,
    errorContainer = Color(0xFFF9DEDC), // Light error container
    onErrorContainer = Color(0xFF410E0B), // Dark text on error container
    background = LightBackgroundBase,
    onBackground = LightOnBackgroundBase,
    surface = LightSurfaceBase,
    onSurface = LightOnSurfaceBase,
    surfaceVariant = Color(0xFFE7E0EC), // Material 3 default light surfaceVariant
    onSurfaceVariant = Color(0xFF49454F), // Material 3 default light onSurfaceVariant
    outline = Color(0xFF79747E), // Material 3 default light outline
    inverseOnSurface = DarkOnSurfaceBase, // e.g., Color(0xFFF1F0F4)
    inverseSurface = DarkBackgroundBase,  // e.g., Color(0xFF303030)
    inversePrimary = PrimaryBlueDark, // A primary color variant for inverse surfaces
    surfaceTint = PrimaryBlue, // Typically same as primary
    outlineVariant = Color(0xFFCAC4D0), // Material 3 default light outlineVariant
    scrim = Color.Black
)

// Static Dark Color Scheme (Fallback)
val DarkAppColorScheme = darkColorScheme(
    primary = PrimaryBlueLight, // Lighter primary for dark theme
    onPrimary = PrimaryBlueDark, // Darker text on light primary
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = PrimaryBlueLight,
    secondary = AccentOrangeLight,
    onSecondary = AccentOrangeDark,
    secondaryContainer = AccentOrangeDark,
    onSecondaryContainer = AccentOrangeLight,
    tertiary = AccentOrangeLight,
    onTertiary = AccentOrangeDark,
    tertiaryContainer = AccentOrangeDark,
    onTertiaryContainer = AccentOrangeLight,
    error = Color(0xFFFFB4AB), // Material 3 default dark error
    onError = Color(0xFF690005), // Material 3 default dark onError
    errorContainer = Color(0xFF93000A), // Dark error container
    onErrorContainer = Color(0xFFFFDAD6), // Light text on dark error container
    background = DarkBackgroundBase,
    onBackground = DarkOnBackgroundBase, // Consider Color(0xFFE6E1E5)
    surface = DarkSurfaceBase,
    onSurface = DarkOnSurfaceBase,       // Consider Color(0xFFE6E1E5)
    surfaceVariant = Color(0xFF49454F), // Material 3 default dark surfaceVariant
    onSurfaceVariant = Color(0xFFCAC4D0), // Material 3 default dark onSurfaceVariant
    outline = Color(0xFF938F99), // Material 3 default dark outline
    inverseOnSurface = LightOnSurfaceBase, // e.g., Color(0xFF1C1B1F)
    inverseSurface = LightBackgroundBase,  // e.g., Color(0xFFE6E1E5)
    inversePrimary = PrimaryBlue, // A primary color variant for inverse surfaces
    surfaceTint = PrimaryBlueLight, // Typically same as primary (for dark theme)
    outlineVariant = Color(0xFF49454F), // Material 3 default dark outlineVariant
    scrim = Color.Black
)

// Sudoku Specific Colors (These are custom and used directly, not part of MaterialTheme colors)
// Light Theme Sudoku Colors
val SudokuGridLineColorLight = Color(0xFFBDBDBD)
val SudokuCellTextColorLight = Color(0xFF212121)
val SudokuSelectedCellColorLight = PrimaryBlueLight
val SudokuHighlightedCellColorLight = Color(0xFFFFF9C4)
val SudokuConflictingCellColorLight = AppErrorBase // Use the base error color
val SudokuFixedNumberColorLight = Color.Black
val SudokuUserNumberColorLight = PrimaryBlue

// Dark Theme Sudoku Colors
val SudokuGridLineColorDark = Color(0xFF424242)
val SudokuCellTextColorDark = Color(0xFFE0E0E0)
val SudokuSelectedCellColorDark = PrimaryBlueDark
val SudokuHighlightedCellColorDark = Color(0xFF4A463A) // Darker yellow
val SudokuConflictingCellColorDark = AccentOrange // Using accent for less harsh error in dark
val SudokuFixedNumberColorDark = Color.White
val SudokuUserNumberColorDark = PrimaryBlueLight