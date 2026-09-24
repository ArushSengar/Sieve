package com.sieve.filter.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AppleGreen,
    secondary = AppleBlue,
    tertiary = ApplePurple,
    background = AppleBackground,
    surface = AppleCard,
    surfaceVariant = AppleCardSecondary,
    onBackground = AppleTextPrimary,
    onSurface = AppleTextPrimary,
    onSurfaceVariant = AppleTextSecondary,
    outline = AppleSeparator,
    outlineVariant = AppleHairline
)

// AMOLED True Black palette for OLED battery saving (turns off pixels)
private val AmoledDarkColorScheme = darkColorScheme(
    primary = AppleGreen,
    secondary = AppleBlue,
    tertiary = ApplePurple,
    background = AppleBackground,
    surface = AppleCard,
    surfaceVariant = AppleCardSecondary,
    onBackground = AppleTextPrimary,
    onSurface = AppleTextPrimary,
    onSurfaceVariant = AppleTextSecondary,
    outline = AppleSeparator,
    outlineVariant = AppleHairline
)

private val LightColorScheme = lightColorScheme(
    primary = AppleGreen,
    secondary = AppleBlue,
    tertiary = ApplePurple,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = Color(0xFFC6C6C8),
    outlineVariant = Color(0xFFE5E5EA)
)

@Composable
fun SieveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isAmoled: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme && isAmoled -> AmoledDarkColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
