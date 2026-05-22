package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppTheme {
    LIGHT, DARK, SUNSET
}

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    secondary = LightSecondary,
    onSecondary = Color.White,
    tertiary = LightTertiary,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnBackground,
    outline = LightBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF0F172A),
    secondary = DarkSecondary,
    onSecondary = Color(0xFF0F172A),
    tertiary = DarkTertiary,
    onTertiary = Color(0xFF0F172A),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnBackground,
    outline = DarkBorder
)

private val SunsetColorScheme = darkColorScheme(
    primary = SunsetPrimary,
    onPrimary = Color(0xFF1E1010),
    secondary = SunsetSecondary,
    onSecondary = Color(0xFF1E1010),
    tertiary = SunsetTertiary,
    onTertiary = Color(0xFF1E1010),
    background = SunsetBackground,
    onBackground = SunsetOnBackground,
    surface = SunsetSurface,
    onSurface = SunsetOnSurface,
    surfaceVariant = SunsetSurfaceVariant,
    onSurfaceVariant = SunsetOnBackground,
    outline = SunsetBorder
)

@Composable
fun MyApplicationTheme(
    appTheme: AppTheme = AppTheme.LIGHT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.SUNSET -> SunsetColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
