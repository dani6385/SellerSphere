package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MinimalistDarkPrimary,
    secondary = MinimalistDarkSecondary,
    tertiary = MinimalistDarkTertiary,
    background = MinimalistDarkBackground,
    surface = MinimalistDarkSurface,
    surfaceVariant = MinimalistDarkSurfaceVariant,
    onPrimary = MinimalistDarkOnPrimary,
    onSecondary = MinimalistDarkBackground,
    onTertiary = Color.Black,
    onBackground = MinimalistDarkOnBackground,
    onSurface = MinimalistDarkOnSurface,
    onSurfaceVariant = MinimalistDarkSecondary,
    outline = MinimalistDarkOutline,
    outlineVariant = MinimalistDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = MinimalistLightPrimary,
    secondary = MinimalistLightSecondary,
    tertiary = MinimalistLightTertiary,
    background = MinimalistLightBackground,
    surface = MinimalistLightSurface,
    surfaceVariant = MinimalistLightSurfaceVariant,
    onPrimary = MinimalistLightOnPrimary,
    onSecondary = MinimalistLightSurface,
    onTertiary = Color.White,
    onBackground = MinimalistLightOnBackground,
    onSurface = MinimalistLightOnSurface,
    onSurfaceVariant = MinimalistLightSecondary,
    outline = MinimalistLightOutline,
    outlineVariant = MinimalistLightOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Always false to ensure our Clean Minimalism theme applies consistently
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
