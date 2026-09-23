package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AnuDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = SpaceBlack,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = ElectricCyan,
    secondary = NeonViolet,
    onSecondary = SpaceBlack,
    secondaryContainer = SurfaceDark,
    onSecondaryContainer = PurpleGlow,
    tertiary = NeonGreen,
    onTertiary = SpaceBlack,
    background = SpaceBlack,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    error = CoralRed
)

@Composable
fun AnuTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AnuDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AnuTheme(content = content)
}
